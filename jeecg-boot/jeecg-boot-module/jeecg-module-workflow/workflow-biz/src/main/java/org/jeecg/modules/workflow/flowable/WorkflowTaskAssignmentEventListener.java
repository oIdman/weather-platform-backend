package org.jeecg.modules.workflow.flowable;

import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.MultiInstanceLoopCharacteristics;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.delegate.event.FlowableActivityCancelledEvent;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.flowable.task.api.Task;
import org.jeecg.modules.workflow.adapter.WorkflowCandidateIdentityAdapter;
import org.jeecg.modules.workflow.api.constant.WorkflowProcessConstants;
import org.jeecg.modules.workflow.service.WorkflowEngineService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Executes the assignment fallback rules persisted by the simple and BPMN designers. */
@Slf4j
@Component
public class WorkflowTaskAssignmentEventListener extends AbstractFlowableEngineEventListener {

    private static final Set<FlowableEngineEventType> EVENTS = Set.of(
            FlowableEngineEventType.TASK_CREATED, FlowableEngineEventType.TASK_ASSIGNED,
            FlowableEngineEventType.ACTIVITY_CANCELLED);

    private final ObjectProvider<TaskService> taskServiceProvider;
    private final ObjectProvider<RuntimeService> runtimeServiceProvider;
    private final ObjectProvider<RepositoryService> repositoryServiceProvider;
    private final ObjectProvider<HistoryService> historyServiceProvider;
    private final ObjectProvider<WorkflowEngineService> workflowEngineServiceProvider;
    private final ObjectProvider<WorkflowCandidateResolver> candidateResolverProvider;
    private final WorkflowCandidateIdentityAdapter candidateIdentityAdapter;

    public WorkflowTaskAssignmentEventListener(ObjectProvider<TaskService> taskServiceProvider,
                                               ObjectProvider<RuntimeService> runtimeServiceProvider,
                                               ObjectProvider<RepositoryService> repositoryServiceProvider,
                                               ObjectProvider<HistoryService> historyServiceProvider,
                                               ObjectProvider<WorkflowEngineService> workflowEngineServiceProvider,
                                               ObjectProvider<WorkflowCandidateResolver> candidateResolverProvider,
                                               WorkflowCandidateIdentityAdapter candidateIdentityAdapter) {
        super(EVENTS);
        this.taskServiceProvider = taskServiceProvider;
        this.runtimeServiceProvider = runtimeServiceProvider;
        this.repositoryServiceProvider = repositoryServiceProvider;
        this.historyServiceProvider = historyServiceProvider;
        this.workflowEngineServiceProvider = workflowEngineServiceProvider;
        this.candidateResolverProvider = candidateResolverProvider;
        this.candidateIdentityAdapter = candidateIdentityAdapter;
    }

    @Override
    protected void taskCreated(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();
        FlowElement element = flowElement(task);
        int approveType = extensionInteger(element, WorkflowProcessConstants.EXTENSION_APPROVE_TYPE);
        if (approveType == 2 || approveType == 3) {
            afterTransaction(() -> handleAutomaticApproval(task.getId(), approveType));
        } else if (task.getAssignee() == null && task.getOwner() == null) {
            // 简易设计器会生成 assignee 表达式；原生 BPMN 设计器通常只保存
            // candidateStrategy/candidateParam 扩展属性。两种来源统一在任务创建后
            // 解析，避免 BPMN 页面发布后出现“节点配置了审批人但没有待办”。
            afterTransaction(() -> handleUnassignedTask(task.getId()));
        }
    }

    private void handleUnassignedTask(String taskId) {
        Task task = activeTask(taskId);
        if (task == null || task.isSuspended() || task.getAssignee() != null || task.getOwner() != null) {
            return;
        }
        assignFromCandidateStrategy(task);
        Task refreshed = activeTask(taskId);
        if (refreshed != null && refreshed.getAssignee() == null && refreshed.getOwner() == null) {
            handleEmptyAssignee(taskId);
        }
    }

    private void assignFromCandidateStrategy(Task task) {
        FlowElement element = flowElement(task);
        if (!(element instanceof UserTask userTask)) {
            return;
        }
        // 多实例任务的负责人来自 elementVariable。不要把完整候选集合随机压成
        // 一个负责人，否则会破坏会签/或签的每实例分配。
        MultiInstanceLoopCharacteristics loop = userTask.getLoopCharacteristics();
        if (loop != null) {
            String elementVariable = loop.getElementVariable();
            if (task.getExecutionId() != null && !task.getExecutionId().isBlank()
                    && elementVariable != null && !elementVariable.isBlank()) {
                Object value = runtimeServiceProvider.getObject().getVariable(task.getExecutionId(), elementVariable);
                String assignee = value == null ? null : String.valueOf(value).trim();
                if (assignee != null && !assignee.isBlank()) {
                    taskServiceProvider.getObject().setAssignee(task.getId(), assignee);
                }
            }
            return;
        }
        Integer strategy = candidateResolverProvider.getObject().candidateStrategy(userTask);
        if (strategy == null) {
            return;
        }
        ProcessInstance instance = runtimeServiceProvider.getObject().createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).singleResult();
        if (instance == null || instance.isSuspended()) {
            return;
        }
        String assignee = candidateResolverProvider.getObject().resolveOne(userTask, strategy,
                instance.getStartUserId(), runtimeServiceProvider.getObject().getVariables(instance.getId()),
                task.getTenantId(), task.getProcessDefinitionId());
        if (assignee != null && !assignee.isBlank()) {
            taskServiceProvider.getObject().setAssignee(task.getId(), assignee);
        }
    }

    private void handleAutomaticApproval(String taskId, int approveType) {
        Task task = activeTask(taskId);
        if (task == null || task.isSuspended()) {
            return;
        }
        boolean approved = approveType == 2;
        workflowEngineServiceProvider.getObject().completeAutomatically(taskId, approved,
                approved ? "节点配置为自动通过" : "节点配置为自动拒绝");
    }

    @Override
    protected void taskAssigned(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();
        if (task.getAssignee() != null && !task.isSuspended()) {
            afterTransaction(() -> handleStartUserAssignment(task.getId()));
        }
    }

    @Override
    protected void activityCancelled(FlowableActivityCancelledEvent event) {
        // 清理/删除挂起流程时，Flowable 可能在任务取消事件中仍携带旧的任务实体。
        // 此时只允许引擎删除，不再补写展示用的任务变量，避免事务被挂起任务拒绝。
        // 清理上下文覆盖运行实例已先被删除的竞态窗口。
        if (WorkflowProcessCleanupContext.isActive() || isSuspendedProcess(event.getProcessInstanceId())) {
            return;
        }
        List<HistoricActivityInstance> activities = historyServiceProvider.getObject()
                .createHistoricActivityInstanceQuery().executionId(event.getExecutionId()).list();
        for (HistoricActivityInstance activity : activities) {
            if (activity.getTaskId() != null) {
                markTaskCanceled(activity.getTaskId());
            }
        }
        if (event.getProcessInstanceId() == null || event.getActivityId() == null) {
            return;
        }
        // Flowable 的并行/多实例取消事件有时使用多实例根 executionId，
        // 此时历史活动查询找不到子任务。按流程实例和活动节点兜底查询仍在运行的任务，
        // 确保或签/会签因完成条件取消的剩余实例落为“已取消”，而不是无状态消失。
        taskServiceProvider.getObject().createTaskQuery()
                .processInstanceId(event.getProcessInstanceId())
                .taskDefinitionKey(event.getActivityId())
                .active()
                .list()
                .forEach(task -> markTaskCanceled(task.getId()));
    }

    private boolean isSuspendedProcess(String processInstanceId) {
        if (processInstanceId == null || processInstanceId.isBlank()) {
            return false;
        }
        ProcessInstanceQuery query = runtimeServiceProvider.getObject().createProcessInstanceQuery();
        if (query == null) {
            return false;
        }
        ProcessInstance instance;
        try {
            instance = query.processInstanceId(processInstanceId).singleResult();
        } catch (FlowableException exception) {
            // 清理与取消可能跨线程交错，查询过程中实例也可能刚被删除；
            // 这种竞态同样不能阻止主删除操作。
            log.debug("流程实例清理期间跳过取消任务变量补写，processInstanceId={}", processInstanceId);
            return true;
        }
        // 删除流程实例时，Flowable 可能先移除 runtime execution，再派发
        // ACTIVITY_CANCELLED。此时已经查不到实例，但取消事件里仍可能携带旧的
        // suspended Task；继续补写任务变量会直接抛出“Cannot add variables to a
        // suspended Task”。实例不可见时按清理窗口处理，任务状态由历史删除原因表达。
        return instance == null || instance.isSuspended();
    }

    private void markTaskCanceled(String taskId) {
        Task task = activeTask(taskId);
        // 删除/清理流程实例时，Flowable 可能先把任务置为 suspended 再派发
        // ACTIVITY_CANCELLED 事件。挂起任务不允许写任务变量，否则会阻断
        // 整个流程实例的删除事务。
        if (task == null || task.isSuspended()) {
            return;
        }
        TaskService taskService = taskServiceProvider.getObject();
        Object status = taskService.getVariableLocal(taskId, WorkflowProcessConstants.TASK_VARIABLE_STATUS);
        if (isFinalTaskStatus(status)) {
            return;
        }
        try {
            taskService.setVariableLocal(taskId, WorkflowProcessConstants.TASK_VARIABLE_STATUS,
                    WorkflowProcessConstants.STATUS_CANCELED);
            taskService.setVariableLocal(taskId, WorkflowProcessConstants.TASK_VARIABLE_REASON,
                    "因流程流转或多人审批结束，系统取消任务");
        } catch (FlowableException exception) {
            // 任务可能在查询后被并发挂起/删除；取消流程本身应保持幂等，
            // 不能因为补写展示用的任务变量而回滚主操作。
            if (task.isSuspended() || isSuspendedTaskError(exception)) {
                log.debug("跳过挂起任务的取消标记，taskId={}", taskId);
                return;
            }
            throw exception;
        }
    }

    private boolean isSuspendedTaskError(FlowableException exception) {
        String message = exception.getMessage();
        return message != null && message.toLowerCase(java.util.Locale.ROOT).contains("suspended task");
    }

    private boolean isFinalTaskStatus(Object status) {
        if (!(status instanceof Number number)) {
            return false;
        }
        int value = number.intValue();
        return value == WorkflowProcessConstants.STATUS_APPROVED
                || value == WorkflowProcessConstants.STATUS_REJECTED
                || value == WorkflowProcessConstants.STATUS_CANCELED
                || value == WorkflowProcessConstants.TASK_STATUS_RETURNED
                || value == WorkflowProcessConstants.TASK_STATUS_APPROVING;
    }

    private void handleEmptyAssignee(String taskId) {
        Task task = activeTask(taskId);
        if (task == null || task.isSuspended() || task.getAssignee() != null || task.getOwner() != null) {
            return;
        }
        FlowElement element = flowElement(task);
        int handlerType = extensionInteger(element,
                WorkflowProcessConstants.EXTENSION_ASSIGN_EMPTY_HANDLER_TYPE);
        if (handlerType == WorkflowProcessConstants.ASSIGN_EMPTY_APPROVE) {
            workflowEngineServiceProvider.getObject().completeAutomatically(taskId, true,
                    "审批人为空，自动通过");
        } else if (handlerType == WorkflowProcessConstants.ASSIGN_EMPTY_REJECT) {
            workflowEngineServiceProvider.getObject().completeAutomatically(taskId, false,
                    "审批人为空，自动不通过");
        } else if (handlerType == WorkflowProcessConstants.ASSIGN_EMPTY_USER
                || handlerType == WorkflowProcessConstants.ASSIGN_EMPTY_MANAGER) {
            if (!(element instanceof UserTask userTask)) {
                return;
            }
            ProcessInstance instance = runtimeServiceProvider.getObject().createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId()).singleResult();
            if (instance == null || instance.isSuspended()) {
                return;
            }
            Set<String> fallbackUsers = candidateResolverProvider.getObject()
                    .resolveEmptyAssigneeCandidates(userTask, task.getProcessDefinitionId(), task.getTenantId());
            TaskService taskService = taskServiceProvider.getObject();
            for (String userId : fallbackUsers) {
                if (userId != null && !userId.isBlank()) {
                    taskService.addCandidateUser(taskId, userId);
                }
            }
        }
    }

    private void handleStartUserAssignment(String taskId) {
        Task task = activeTask(taskId);
        if (task == null || task.isSuspended() || task.getAssignee() == null) {
            return;
        }
        ProcessInstance instance = runtimeServiceProvider.getObject().createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId()).singleResult();
        if (instance == null || !Objects.equals(task.getAssignee(), instance.getStartUserId())) {
            return;
        }
        Object returnFlag = runtimeServiceProvider.getObject().getVariable(task.getProcessInstanceId(),
                WorkflowProcessConstants.returnTaskFlagVariable(task.getTaskDefinitionKey()));
        if (Boolean.TRUE.equals(returnFlag)) {
            return;
        }
        FlowElement element = flowElement(task);
        // 子流程调用活动通过输入参数控制是否跳过“发起人”节点。变量不存在时
        // 保持父流程原有 assignStartUserHandlerType 配置，兼容旧流程 XML。
        Object skipStartUserNode = runtimeServiceProvider.getObject().getVariable(
                task.getProcessInstanceId(), WorkflowProcessConstants.VARIABLE_SKIP_START_USER_NODE);
        if (skipStartUserNode != null && !Boolean.parseBoolean(String.valueOf(skipStartUserNode))) {
            return;
        }
        int handlerType = extensionInteger(element,
                WorkflowProcessConstants.EXTENSION_ASSIGN_START_USER_HANDLER_TYPE);
        if (handlerType == WorkflowProcessConstants.ASSIGN_START_USER_SKIP) {
            workflowEngineServiceProvider.getObject().completeAutomatically(taskId, true,
                    "审批人与发起人相同，自动通过");
            return;
        }
        if (handlerType != WorkflowProcessConstants.ASSIGN_START_USER_DEPARTMENT_LEADER) {
            return;
        }
        String departmentId = candidateIdentityAdapter.primaryDepartmentIdByUserId(instance.getStartUserId());
        Collection<String> leaderUserIds = candidateIdentityAdapter.leaderUserIdsByDepartmentId(departmentId);
        String leaderUserId = leaderUserIds == null ? null : leaderUserIds.stream()
                .filter(Objects::nonNull).map(String::trim).filter(id -> !id.isBlank())
                .findFirst().orElse(null);
        if (leaderUserId == null) {
            workflowEngineServiceProvider.getObject().completeAutomatically(taskId, true,
                    "发起人部门负责人为空，自动通过");
        } else if (!Objects.equals(leaderUserId, instance.getStartUserId())) {
            TaskService taskService = taskServiceProvider.getObject();
            taskService.addComment(taskId, task.getProcessInstanceId(), "transfer",
                    "审批人与发起人相同，转交部门负责人");
            taskService.setAssignee(taskId, leaderUserId);
        }
    }

    private Task activeTask(String taskId) {
        return taskServiceProvider.getObject().createTaskQuery().taskId(taskId).active().singleResult();
    }

    private FlowElement flowElement(Task task) {
        BpmnModel model = repositoryServiceProvider.getObject().getBpmnModel(task.getProcessDefinitionId());
        return model == null ? null : model.getFlowElement(task.getTaskDefinitionKey());
    }

    private int extensionInteger(FlowElement element, String name) {
        if (element == null) {
            return 0;
        }
        List<ExtensionElement> values = element.getExtensionElements().get(name);
        if (values == null || values.isEmpty() || values.get(0).getElementText() == null) {
            return 0;
        }
        try {
            return Integer.parseInt(values.get(0).getElementText().trim());
        } catch (NumberFormatException exception) {
            log.warn("流程节点扩展配置格式不正确，taskDefinitionKey={}, name={}",
                    element.getId(), name);
            return 0;
        }
    }

    private void afterTransaction(Runnable action) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            runSafely(action);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_ROLLED_BACK) {
                    runSafely(action);
                }
            }
        });
    }

    private void runSafely(Runnable action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            log.error("执行工作流审批人分配规则失败", exception);
        }
    }
}
