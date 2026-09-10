package org.jeecg.modules.workflow.flowable;

import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowElement;
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
    private final WorkflowCandidateIdentityAdapter candidateIdentityAdapter;

    public WorkflowTaskAssignmentEventListener(ObjectProvider<TaskService> taskServiceProvider,
                                               ObjectProvider<RuntimeService> runtimeServiceProvider,
                                               ObjectProvider<RepositoryService> repositoryServiceProvider,
                                               ObjectProvider<HistoryService> historyServiceProvider,
                                               ObjectProvider<WorkflowEngineService> workflowEngineServiceProvider,
                                               WorkflowCandidateIdentityAdapter candidateIdentityAdapter) {
        super(EVENTS);
        this.taskServiceProvider = taskServiceProvider;
        this.runtimeServiceProvider = runtimeServiceProvider;
        this.repositoryServiceProvider = repositoryServiceProvider;
        this.historyServiceProvider = historyServiceProvider;
        this.workflowEngineServiceProvider = workflowEngineServiceProvider;
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
            afterTransaction(() -> handleEmptyAssignee(task.getId()));
        }
    }

    private void handleAutomaticApproval(String taskId, int approveType) {
        if (activeTask(taskId) == null) {
            return;
        }
        boolean approved = approveType == 2;
        workflowEngineServiceProvider.getObject().completeAutomatically(taskId, approved,
                approved ? "节点配置为自动通过" : "节点配置为自动拒绝");
    }

    @Override
    protected void taskAssigned(FlowableEngineEntityEvent event) {
        Task task = (Task) event.getEntity();
        if (task.getAssignee() != null) {
            afterTransaction(() -> handleStartUserAssignment(task.getId()));
        }
    }

    @Override
    protected void activityCancelled(FlowableActivityCancelledEvent event) {
        List<HistoricActivityInstance> activities = historyServiceProvider.getObject()
                .createHistoricActivityInstanceQuery().executionId(event.getExecutionId()).list();
        for (HistoricActivityInstance activity : activities) {
            if (activity.getTaskId() != null) {
                markTaskCanceled(activity.getTaskId());
            }
        }
    }

    private void markTaskCanceled(String taskId) {
        Task task = activeTask(taskId);
        if (task == null) {
            return;
        }
        TaskService taskService = taskServiceProvider.getObject();
        Object status = taskService.getVariableLocal(taskId, WorkflowProcessConstants.TASK_VARIABLE_STATUS);
        if (isFinalTaskStatus(status)) {
            return;
        }
        taskService.setVariableLocal(taskId, WorkflowProcessConstants.TASK_VARIABLE_STATUS,
                WorkflowProcessConstants.STATUS_CANCELED);
        taskService.setVariableLocal(taskId, WorkflowProcessConstants.TASK_VARIABLE_REASON,
                "因流程流转或多人审批结束，系统取消任务");
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
        if (task == null || task.getAssignee() != null || task.getOwner() != null) {
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
        }
    }

    private void handleStartUserAssignment(String taskId) {
        Task task = activeTask(taskId);
        if (task == null || task.getAssignee() == null) {
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
