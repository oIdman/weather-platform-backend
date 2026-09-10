package org.jeecg.modules.workflow.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.task.Attachment;
import org.flowable.task.api.DelegationState;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.jeecg.modules.workflow.api.constant.WorkflowProcessConstants;
import org.jeecg.modules.workflow.api.dto.WorkflowApprovalDetailRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowCancelRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowDeployRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowPage;
import org.jeecg.modules.workflow.api.dto.WorkflowProcessInstancePageRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowStartRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskActionRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskDelegateRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskReturnRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskSignCreateRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskSignDeleteRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskTransferRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskPageRequest;
import org.jeecg.modules.workflow.api.vo.WorkflowApprovalDetailVO;
import org.jeecg.modules.workflow.api.vo.WorkflowActivityNodeVO;
import org.jeecg.modules.workflow.api.vo.WorkflowBpmnViewVO;
import org.jeecg.modules.workflow.api.vo.WorkflowDefinitionVO;
import org.jeecg.modules.workflow.api.vo.WorkflowInstanceVO;
import org.jeecg.modules.workflow.api.vo.WorkflowModelVO;
import org.jeecg.modules.workflow.api.vo.WorkflowPrintDataVO;
import org.jeecg.modules.workflow.api.vo.WorkflowTaskVO;
import org.jeecg.modules.workflow.api.vo.WorkflowUserSimpleVO;
import org.jeecg.modules.workflow.flowable.WorkflowBpmnNavigator;
import org.jeecg.modules.workflow.flowable.WorkflowBpmnValidator;
import org.jeecg.modules.workflow.flowable.WorkflowCandidateResolver;
import org.jeecg.modules.workflow.service.WorkflowEngineService;
import org.jeecg.modules.workflow.service.WorkflowModelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class WorkflowEngineServiceImpl implements WorkflowEngineService {

    private static final String APPROVED_VARIABLE = "approved";
    private static final String START_USER_SELECT_ASSIGNEES = "PROCESS_START_USER_SELECT_ASSIGNEES";
    private static final String APPROVE_USER_SELECT_ASSIGNEES = "PROCESS_APPROVE_USER_SELECT_ASSIGNEES";
    private static final String TASK_RESULT_STATUS = WorkflowProcessConstants.TASK_VARIABLE_STATUS;
    private static final String TASK_REASON = WorkflowProcessConstants.TASK_VARIABLE_REASON;
    private static final String TASK_SIGN_PIC_URL = "TASK_SIGN_PIC_URL";
    private static final String TASK_ATTACHMENT_TYPE = "1";
    private static final String SIGN_TYPE_BEFORE = "before";
    private static final String SIGN_TYPE_AFTER = "after";
    private static final String LEGACY_SIGN_PARENT_CATEGORY_PREFIX = "workflow-sign:";
    private static final String SIGN_CHILD_CATEGORY_PREFIX = "workflow-sign-child:";
    private static final Set<String> RESERVED_VARIABLES = Set.of(
            APPROVED_VARIABLE,
            WorkflowProcessConstants.VARIABLE_STATUS,
            WorkflowProcessConstants.VARIABLE_REASON,
            TASK_RESULT_STATUS,
            TASK_REASON,
            TASK_SIGN_PIC_URL,
            START_USER_SELECT_ASSIGNEES,
            APPROVE_USER_SELECT_ASSIGNEES
    );

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final WorkflowIdentityAdapter identityAdapter;
    private final WorkflowBpmnValidator bpmnValidator;
    private final ISysBaseAPI sysBaseApi;
    private final WorkflowModelService modelService;
    private final WorkflowCandidateResolver candidateResolver;
    private final ObjectMapper objectMapper;
    private final WorkflowBpmnNavigator bpmnNavigator;

    public WorkflowEngineServiceImpl(RepositoryService repositoryService,
                                     RuntimeService runtimeService,
                                     TaskService taskService,
                                     HistoryService historyService,
                                     WorkflowIdentityAdapter identityAdapter,
                                     WorkflowBpmnValidator bpmnValidator,
                                     ISysBaseAPI sysBaseApi,
                                     WorkflowModelService modelService,
                                     WorkflowCandidateResolver candidateResolver,
                                     ObjectMapper objectMapper,
                                     WorkflowBpmnNavigator bpmnNavigator) {
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
        this.taskService = taskService;
        this.historyService = historyService;
        this.identityAdapter = identityAdapter;
        this.bpmnValidator = bpmnValidator;
        this.sysBaseApi = sysBaseApi;
        this.modelService = modelService;
        this.candidateResolver = candidateResolver;
        this.objectMapper = objectMapper;
        this.bpmnNavigator = bpmnNavigator;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowDefinitionVO deploy(WorkflowDeployRequest request) {
        BpmnModel model = bpmnValidator.validate(request.getBpmnXml());
        String resourceName = normalizeResourceName(request.getResourceName());
        Deployment deployment = repositoryService.createDeployment()
                .name(request.getName())
                .tenantId(identityAdapter.currentTenantId())
                .addString(resourceName, request.getBpmnXml())
                .deploy();
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        if (definition == null) {
            throw new JeecgBootException("流程部署后未找到流程定义");
        }
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            repositoryService.setProcessDefinitionCategory(definition.getId(), request.getCategory());
            definition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(definition.getId()).singleResult();
        }
        if (!Objects.equals(model.getMainProcess().getId(), definition.getKey())) {
            throw new JeecgBootException("BPMN 流程 Key 与部署结果不一致");
        }
        return toDefinition(definition, deployment);
    }

    @Override
    public WorkflowPage<WorkflowDefinitionVO> definitionPage(int pageNo, int pageSize, String name) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .processDefinitionTenantId(identityAdapter.currentTenantId())
                .latestVersion();
        if (name != null && !name.isBlank()) {
            query.processDefinitionNameLike("%" + name.trim() + "%");
        }
        long total = query.count();
        List<WorkflowDefinitionVO> list = query.orderByProcessDefinitionName().asc()
                .listPage(offset(pageNo, pageSize), pageSize).stream()
                .map(definition -> toDefinition(definition, null))
                .toList();
        return new WorkflowPage<>(list, total);
    }

    @Override
    public List<WorkflowDefinitionVO> definitionList(int suspensionState, boolean startableOnly) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .processDefinitionTenantId(identityAdapter.currentTenantId())
                .latestVersion();
        if (suspensionState == 1) {
            query.active();
        } else if (suspensionState == 2) {
            query.suspended();
        } else {
            throw new JeecgBootException("流程定义状态必须为 1（激活）或 2（挂起）");
        }
        return query.orderByProcessDefinitionName().asc().list().stream()
                .filter(definition -> !startableOnly || canCurrentUserStart(definition))
                .map(definition -> toDefinition(definition, null))
                .toList();
    }

    @Override
    public List<WorkflowDefinitionVO> simpleDefinitionList() {
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionTenantId(identityAdapter.currentTenantId())
                .latestVersion()
                .active()
                .orderByProcessDefinitionName().asc()
                .list().stream()
                .map(definition -> {
                    WorkflowDefinitionVO vo = new WorkflowDefinitionVO();
                    vo.setId(definition.getId());
                    vo.setName(definition.getName());
                    vo.setKey(definition.getKey());
                    return vo;
                })
                .toList();
    }

    @Override
    public WorkflowDefinitionVO getDefinition(String id, String key) {
        ProcessDefinition definition;
        if (id != null && !id.isBlank()) {
            definition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(id)
                    .processDefinitionTenantId(identityAdapter.currentTenantId())
                    .singleResult();
        } else if (key != null && !key.isBlank()) {
            definition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(key)
                    .processDefinitionTenantId(identityAdapter.currentTenantId())
                    .latestVersion()
                    .active()
                    .singleResult();
        } else {
            throw new JeecgBootException("流程定义编号和流程标识不能同时为空");
        }
        if (definition == null) {
            return null;
        }
        WorkflowDefinitionVO vo = toDefinition(definition, null);
        vo.setBpmnXml(readBpmnXml(definition));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String start(WorkflowStartRequest request) {
        String tenantId = identityAdapter.currentTenantId();
        ProcessDefinition definition;
        if (request.getProcessDefinitionId() != null && !request.getProcessDefinitionId().isBlank()) {
            definition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(request.getProcessDefinitionId().trim())
                    .active()
                    .singleResult();
            if (definition != null && !Objects.equals(tenantId, definition.getTenantId())) {
                definition = null;
            }
        } else if (request.getProcessDefinitionKey() != null && !request.getProcessDefinitionKey().isBlank()) {
            definition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(request.getProcessDefinitionKey().trim())
                    .processDefinitionTenantId(tenantId)
                    .latestVersion()
                    .active()
                    .singleResult();
        } else {
            throw new JeecgBootException("流程定义编号不能为空");
        }
        if (definition == null) {
            throw new JeecgBootException("流程定义不存在、未发布或已挂起");
        }
        if (!canCurrentUserStart(definition)) {
            throw new JeecgBootException("当前用户无权发起该流程");
        }
        // 流程编号和业务标识统一使用同一个 UUID，避免审批中心出现两个无法关联的编号。
        String processInstanceId = UUID.randomUUID().toString();
        Map<String, Object> variables = sanitizedVariables(request.getVariables());
        variables.put(WorkflowProcessConstants.VARIABLE_STATUS, WorkflowProcessConstants.STATUS_RUNNING);
        Map<String, List<String>> startAssignees = normalizeAssignees(request.getStartUserSelectAssignees());
        if (!startAssignees.isEmpty()) {
            variables.put(START_USER_SELECT_ASSIGNEES, startAssignees);
        }
        variables.put("_workflowTenantId", tenantId);
        variables.put("_workflowStartUserId", identityAdapter.currentUserId());
        variables.put(WorkflowProcessConstants.PROCESS_INSTANCE_SKIP_EXPRESSION_ENABLED, true);
        initializeMultiInstanceVariables(repositoryService.getBpmnModel(definition.getId()), definition.getId(),
                variables, identityAdapter.currentUserId());
        Authentication.setAuthenticatedUserId(identityAdapter.currentUserId());
        try {
            org.flowable.engine.runtime.ProcessInstanceBuilder builder = runtimeService.createProcessInstanceBuilder()
                    .processDefinitionId(definition.getId())
                    .predefineProcessInstanceId(processInstanceId)
                    .businessKey(processInstanceId)
                    .variables(variables);
            builder.name(request.getName() == null || request.getName().isBlank()
                    ? definition.getName() : request.getName().trim());
            ProcessInstance instance = builder.start();
            return instance.getId();
        } finally {
            Authentication.setAuthenticatedUserId(null);
        }
    }

    @Override
    public WorkflowPage<WorkflowInstanceVO> myProcessPage(WorkflowProcessInstancePageRequest request) {
        return processPage(request, identityAdapter.currentUserId());
    }

    @Override
    public WorkflowPage<WorkflowInstanceVO> managerProcessPage(WorkflowProcessInstancePageRequest request) {
        return processPage(request, normalize(request.getStartUserId()));
    }

    @Override
    public WorkflowInstanceVO getProcessInstance(String id) {
        assertCanView(id);
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(id)
                .processInstanceTenantId(identityAdapter.currentTenantId())
                .includeProcessVariables()
                .singleResult();
        WorkflowInstanceVO vo = toInstance(instance);
        Map<String, Object> variables = new LinkedHashMap<>();
        historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(id)
                .list()
                .forEach(variable -> variables.put(variable.getVariableName(), variable.getValue()));
        variables.keySet().removeIf(this::isSystemVariable);
        vo.setFormVariables(variables);
        vo.setTasks(taskList(id));
        return vo;
    }

    @Override
    public WorkflowBpmnViewVO getBpmnModelView(String processInstanceId) {
        assertCanView(processInstanceId);
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .processInstanceTenantId(identityAdapter.currentTenantId())
                .singleResult();
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(instance.getProcessDefinitionId()).singleResult();
        if (definition == null) {
            throw new JeecgBootException("流程定义不存在");
        }
        WorkflowBpmnViewVO vo = new WorkflowBpmnViewVO();
        try (InputStream input = repositoryService.getResourceAsStream(definition.getDeploymentId(), definition.getResourceName())) {
            vo.setBpmnXml(new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new JeecgBootException("读取 BPMN 模型失败", exception);
        }
        vo.setCurrentActivityIds(taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskTenantId(identityAdapter.currentTenantId())
                .active().list().stream().map(Task::getTaskDefinitionKey).distinct().toList());
        vo.setCompletedActivityIds(historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .taskTenantId(identityAdapter.currentTenantId())
                .finished().list().stream().map(HistoricTaskInstance::getTaskDefinitionKey).distinct().toList());
        return vo;
    }

    @Override
    public WorkflowApprovalDetailVO getApprovalDetail(WorkflowApprovalDetailRequest request) {
        if (request.getProcessInstanceId() == null || request.getProcessInstanceId().isBlank()) {
            return getApprovalPreview(request);
        }
        String processInstanceId = request.getProcessInstanceId().trim();
        WorkflowInstanceVO instance = getProcessInstance(processInstanceId);
        WorkflowApprovalDetailVO vo = new WorkflowApprovalDetailVO();
        vo.setStatus(instance.getResultStatus() == null
                ? instance.getEndTime() == null ? WorkflowProcessConstants.STATUS_RUNNING : WorkflowProcessConstants.STATUS_APPROVED
                : instance.getResultStatus());
        vo.setProcessInstance(instance);
        vo.setTasks(instance.getTasks());
        TaskQuery todoQuery = myTodoQuery().processInstanceId(processInstanceId);
        if (request.getTaskId() != null && !request.getTaskId().isBlank()) {
            todoQuery.taskId(request.getTaskId().trim());
        }
        Task todoTask = todoQuery.list().stream().findFirst().orElse(null);
        if (todoTask == null) {
            TaskQuery signOwnerQuery = taskService.createTaskQuery().active()
                    .taskTenantId(identityAdapter.currentTenantId())
                    .processInstanceId(processInstanceId)
                    .taskOwner(identityAdapter.currentUserId())
                    .includeTaskLocalVariables();
            if (request.getTaskId() != null && !request.getTaskId().isBlank()) {
                signOwnerQuery.taskId(request.getTaskId().trim());
            }
            todoTask = signOwnerQuery.list().stream().filter(task -> signType(task) != null).findFirst().orElse(null);
        }
        if (todoTask != null) {
            WorkflowTaskVO todoTaskVO = toTask(todoTask);
            todoTaskVO.setChildren(activeChildTasks(todoTask.getId()).stream().map(this::toTask).toList());
            vo.setTodoTask(todoTaskVO);
        }
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(instance.getProcessDefinitionId()).singleResult();
        vo.setProcessDefinition(definition == null ? null : toDefinition(definition, null));
        if (definition != null) {
            BpmnModel bpmnModel = repositoryService.getBpmnModel(definition.getId());
            Map<String, Object> variables = historicVariables(processInstanceId);
            variables.putAll(approvalVariables(request));
            vo.setActivityNodes(buildActivityNodes(bpmnModel, instance, variables));
            if (vo.getTodoTask() != null && vo.getTodoTask().getTaskDefinitionKey() != null) {
                applyOperationSettings(vo, vo.getTodoTask().getTaskDefinitionKey(), bpmnModel);
            }
        }
        vo.setBpmnModelView(getBpmnModelView(processInstanceId));
        return vo;
    }

    private WorkflowApprovalDetailVO getApprovalPreview(WorkflowApprovalDetailRequest request) {
        String definitionId = request.getProcessDefinitionId().trim();
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(definitionId)
                .processDefinitionTenantId(identityAdapter.currentTenantId())
                .active()
                .singleResult();
        if (definition == null) {
            throw new JeecgBootException("流程定义不存在、未发布或已挂起");
        }
        if (!canCurrentUserStart(definition)) {
            throw new JeecgBootException("当前用户无权发起该流程");
        }
        BpmnModel bpmnModel = repositoryService.getBpmnModel(definitionId);
        if (bpmnModel == null || bpmnModel.getMainProcess() == null) {
            throw new JeecgBootException("流程定义缺少 BPMN 模型");
        }
        StartEvent startEvent = bpmnModel.getMainProcess().getFlowElements().stream()
                .filter(StartEvent.class::isInstance)
                .map(StartEvent.class::cast)
                .findFirst()
                .orElseThrow(() -> new JeecgBootException("流程定义缺少开始节点"));
        Map<String, Object> variables = approvalVariables(request);
        variables.put("_workflowTenantId", identityAdapter.currentTenantId());
        variables.put("_workflowStartUserId", identityAdapter.currentUserId());

        WorkflowApprovalDetailVO vo = new WorkflowApprovalDetailVO();
        vo.setStatus(-1);
        vo.setTasks(Collections.emptyList());
        vo.setProcessDefinition(toDefinition(definition, null));
        List<WorkflowActivityNodeVO> activityNodes = new ArrayList<>();
        activityNodes.add(simpleActivityNode("StartUserNode", "发起人", 10, -1));
        activityNodes.addAll(bpmnNavigator.reachableUserTasks(startEvent, bpmnModel, variables).stream()
                .map(node -> toActivityNode(node, identityAdapter.currentUserId(), variables,
                        bpmnNavigator.skipExpressionMatches(node, variables)
                                ? WorkflowProcessConstants.TASK_STATUS_SKIPPED
                                : -1)).toList());
        activityNodes.add(simpleActivityNode("EndNode", "结束", 1, -1));
        vo.setActivityNodes(activityNodes);
        return vo;
    }

    private List<WorkflowActivityNodeVO> buildActivityNodes(BpmnModel bpmnModel, WorkflowInstanceVO instance,
                                                             Map<String, Object> variables) {
        if (bpmnModel == null || bpmnModel.getMainProcess() == null) {
            return Collections.emptyList();
        }
        StartEvent startEvent = bpmnModel.getMainProcess().getFlowElements().stream()
                .filter(StartEvent.class::isInstance).map(StartEvent.class::cast).findFirst().orElse(null);
        if (startEvent == null) {
            return Collections.emptyList();
        }
        Map<String, List<WorkflowTaskVO>> taskMap = new LinkedHashMap<>();
        for (WorkflowTaskVO task : instance.getTasks() == null ? Collections.<WorkflowTaskVO>emptyList() : instance.getTasks()) {
            if (task.getTaskDefinitionKey() != null) {
                taskMap.computeIfAbsent(task.getTaskDefinitionKey(), ignored -> new ArrayList<>()).add(task);
            }
        }

        List<WorkflowActivityNodeVO> result = new ArrayList<>();
        WorkflowActivityNodeVO startNode = simpleActivityNode("StartUserNode", "发起人", 10, 2);
        startNode.setStartTime(instance.getStartTime());
        startNode.setEndTime(instance.getStartTime());
        result.add(startNode);

        Set<String> mappedTaskKeys = new LinkedHashSet<>();
        List<UserTask> reachableUserTasks = bpmnNavigator.reachableUserTasks(startEvent, bpmnModel, variables);
        boolean rejectedEnd = containsRejectedTask(instance.getTasks());
        for (int index = 0; index < reachableUserTasks.size(); index++) {
            UserTask userTask = reachableUserTasks.get(index);
            List<WorkflowTaskVO> tasks = taskMap.getOrDefault(userTask.getId(), Collections.emptyList());
            int status = activityStatus(tasks);
            if (status == -1 && !rejectedEnd
                    && bpmnNavigator.skipExpressionMatches(userTask, variables)) {
                boolean laterTaskExists = reachableUserTasks.subList(index + 1, reachableUserTasks.size()).stream()
                        .anyMatch(later -> !taskMap.getOrDefault(later.getId(), Collections.emptyList()).isEmpty());
                if (laterTaskExists || instance.getEndTime() != null) {
                    status = WorkflowProcessConstants.TASK_STATUS_SKIPPED;
                }
            }
            WorkflowActivityNodeVO node = toActivityNode(userTask, instance.getStartUserId(), variables, status);
            applyActivityTasks(node, tasks);
            result.add(node);
            mappedTaskKeys.add(userTask.getId());
        }
        taskMap.forEach((taskKey, tasks) -> {
            if (mappedTaskKeys.contains(taskKey) || tasks.isEmpty()) {
                return;
            }
            WorkflowActivityNodeVO node = simpleActivityNode(taskKey, tasks.get(0).getName(), 11,
                    activityStatus(tasks));
            applyActivityTasks(node, tasks);
            result.add(node);
        });

        if (instance.getEndTime() != null && !containsRejectedTask(instance.getTasks())) {
            WorkflowActivityNodeVO endNode = simpleActivityNode("EndNode", "结束", 1,
                    "CANCELED".equals(instance.getStatus()) ? 4 : 2);
            endNode.setStartTime(instance.getEndTime());
            endNode.setEndTime(instance.getEndTime());
            result.add(endNode);
        }
        return result;
    }

    private WorkflowActivityNodeVO simpleActivityNode(String id, String name, int nodeType, int status) {
        WorkflowActivityNodeVO node = new WorkflowActivityNodeVO();
        node.setId(id);
        node.setName(name);
        node.setNodeType(nodeType);
        node.setStatus(status);
        node.setTasks(Collections.emptyList());
        node.setCandidateUsers(Collections.emptyList());
        return node;
    }

    private int activityStatus(List<WorkflowTaskVO> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return -1;
        }
        if (tasks.stream().anyMatch(task -> "TODO".equals(task.getStatus()))) {
            return 1;
        }
        return tasks.stream().map(WorkflowTaskVO::getResultStatus).filter(Objects::nonNull)
                .filter(status -> status != 2).findFirst().orElse(2);
    }

    private void applyActivityTasks(WorkflowActivityNodeVO node, List<WorkflowTaskVO> tasks) {
        node.setTasks(tasks);
        node.setStartTime(tasks.stream().map(WorkflowTaskVO::getCreateTime).filter(Objects::nonNull)
                .min(Date::compareTo).orElse(null));
        node.setEndTime(tasks.stream().map(WorkflowTaskVO::getEndTime).filter(Objects::nonNull)
                .max(Date::compareTo).orElse(null));
    }

    private boolean containsRejectedTask(List<WorkflowTaskVO> tasks) {
        return tasks != null && tasks.stream().anyMatch(task -> Objects.equals(task.getResultStatus(), 3));
    }

    private Map<String, Object> historicVariables(String processInstanceId) {
        Map<String, Object> variables = new LinkedHashMap<>();
        historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list()
                .forEach(variable -> variables.put(variable.getVariableName(), variable.getValue()));
        return variables;
    }

    @Override
    public List<WorkflowActivityNodeVO> getNextApprovalNodes(WorkflowApprovalDetailRequest request) {
        if (request.getTaskId() == null || request.getTaskId().isBlank()) {
            throw new JeecgBootException("流程任务编号不能为空");
        }
        Task task = requireMyTodo(request.getTaskId());
        if (request.getProcessInstanceId() != null && !request.getProcessInstanceId().isBlank()
                && !Objects.equals(request.getProcessInstanceId(), task.getProcessInstanceId())) {
            throw new JeecgBootException("任务不属于指定的流程实例");
        }
        // 加签子任务只负责收敛父任务，不直接推动 BPMN execution，也无需选择下一节点审批人。
        if (task.getParentTaskId() != null) {
            return Collections.emptyList();
        }
        ProcessInstance instance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .processInstanceTenantId(identityAdapter.currentTenantId())
                .singleResult();
        if (instance == null) {
            throw new JeecgBootException("运行中的流程实例不存在");
        }
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        if (bpmnModel == null) {
            return Collections.emptyList();
        }
        Map<String, Object> variables = new HashMap<>(runtimeService.getVariables(instance.getId()));
        variables.putAll(approvalVariables(request));
        FlowElement currentElement = bpmnModel.getFlowElement(task.getTaskDefinitionKey());
        if (currentElement == null) {
            throw new JeecgBootException("当前任务节点不存在于流程模型");
        }
        return bpmnNavigator.nextUserTasks(currentElement, bpmnModel, variables).stream()
                .map(node -> toActivityNode(node, instance.getStartUserId(), variables))
                .toList();
    }

    private Map<String, Object> approvalVariables(WorkflowApprovalDetailRequest request) {
        Map<String, Object> variables = new HashMap<>();
        if (request.getProcessVariables() != null) {
            variables.putAll(request.getProcessVariables());
        }
        if (request.getProcessVariablesStr() == null || request.getProcessVariablesStr().isBlank()) {
            return variables;
        }
        try {
            Map<String, Object> parsed = objectMapper.readValue(request.getProcessVariablesStr(), new TypeReference<>() {
            });
            if (parsed != null) {
                variables.putAll(parsed);
            }
            return variables;
        } catch (JsonProcessingException exception) {
            throw new JeecgBootException("流程变量不是有效的 JSON 对象", exception);
        }
    }

    @Override
    public WorkflowPrintDataVO getPrintData(String processInstanceId) {
        WorkflowPrintDataVO vo = new WorkflowPrintDataVO();
        vo.setPrintTemplateEnable(false);
        vo.setProcessInstance(getProcessInstance(processInstanceId));
        vo.setTasks(taskList(processInstanceId));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(WorkflowCancelRequest request, boolean manager) {
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(request.getId())
                .processInstanceTenantId(identityAdapter.currentTenantId())
                .singleResult();
        if (instance == null || instance.getEndTime() != null) {
            throw new JeecgBootException("运行中的流程实例不存在");
        }
        if (!manager && !identityAdapter.currentUserId().equals(instance.getStartUserId())) {
            throw new JeecgBootException("只能取消本人发起的流程");
        }
        String reason = normalize(request.getReason());
        if (reason == null) {
            reason = "取消流程";
        }
        List<Task> activeTasks = taskService.createTaskQuery()
                .processInstanceId(request.getId())
                .taskTenantId(identityAdapter.currentTenantId())
                .active()
                .list();
        for (Task activeTask : activeTasks) {
            taskService.setVariableLocal(activeTask.getId(), TASK_RESULT_STATUS,
                    WorkflowProcessConstants.STATUS_CANCELED);
            taskService.setVariableLocal(activeTask.getId(), TASK_REASON, reason);
        }
        taskService.addComment(null, request.getId(), "cancel", reason);
        runtimeService.setVariable(request.getId(), WorkflowProcessConstants.VARIABLE_STATUS,
                WorkflowProcessConstants.STATUS_CANCELED);
        runtimeService.setVariable(request.getId(), WorkflowProcessConstants.VARIABLE_REASON, reason);
        runtimeService.deleteProcessInstance(request.getId(), reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteHistoricProcessInstance(String processInstanceId) {
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .processInstanceTenantId(identityAdapter.currentTenantId())
                .singleResult();
        if (instance == null) {
            throw new JeecgBootException("流程实例不存在");
        }
        if (instance.getEndTime() == null) {
            throw new JeecgBootException("只有已结束或已取消的流程实例可以删除");
        }
        historyService.deleteHistoricProcessInstance(processInstanceId);
    }

    @Override
    public WorkflowPage<WorkflowTaskVO> todoPage(WorkflowTaskPageRequest request) {
        TaskQuery query = myTodoQuery();
        applyTaskFilters(query, request);
        long total = query.count();
        List<WorkflowTaskVO> list = query.orderByTaskCreateTime().desc()
                .listPage(offset(request.getPageNo(), request.getPageSize()), request.getPageSize()).stream()
                .map(this::toTask)
                .toList();
        return new WorkflowPage<>(list, total);
    }

    @Override
    public WorkflowPage<WorkflowTaskVO> donePage(WorkflowTaskPageRequest request) {
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(identityAdapter.currentUserId())
                .taskTenantId(identityAdapter.currentTenantId())
                .includeTaskLocalVariables()
                .finished();
        applyHistoricTaskFilters(query, request);
        long total = query.count();
        List<WorkflowTaskVO> list = query.orderByHistoricTaskInstanceEndTime().desc()
                .listPage(offset(request.getPageNo(), request.getPageSize()), request.getPageSize()).stream()
                .map(this::toTask)
                .toList();
        return new WorkflowPage<>(list, total);
    }

    @Override
    public WorkflowPage<WorkflowTaskVO> managerTaskPage(WorkflowTaskPageRequest request) {
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskTenantId(identityAdapter.currentTenantId())
                .includeTaskLocalVariables();
        applyHistoricTaskFilters(query, request);
        long total = query.count();
        List<WorkflowTaskVO> list = query.orderByHistoricTaskInstanceStartTime().desc()
                .listPage(offset(request.getPageNo(), request.getPageSize()), request.getPageSize()).stream()
                .map(this::toTask).toList();
        return new WorkflowPage<>(list, total);
    }

    @Override
    public List<WorkflowTaskVO> taskList(String processInstanceId) {
        assertCanView(processInstanceId);
        return historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .taskTenantId(identityAdapter.currentTenantId())
                .includeTaskLocalVariables()
                .orderByHistoricTaskInstanceStartTime().asc()
                .list().stream().map(this::toTask).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(WorkflowTaskActionRequest request) {
        completeTask(request, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(WorkflowTaskActionRequest request) {
        completeTask(request, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeAutomatically(String taskId, boolean approved, String reason) {
        Task task = taskService.createTaskQuery().taskId(taskId).active().singleResult();
        if (task == null) {
            return;
        }
        WorkflowTaskActionRequest request = new WorkflowTaskActionRequest();
        request.setId(taskId);
        request.setReason(reason);
        completeTask(task, request, approved, false);
    }

    @Override
    public List<WorkflowTaskVO> returnableTaskList(String taskId) {
        Task task = requireMyTodo(taskId);
        Map<String, WorkflowTaskVO> candidates = new LinkedHashMap<>();
        historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .taskTenantId(identityAdapter.currentTenantId())
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .list()
                .forEach(historyTask -> {
                    if (!Objects.equals(historyTask.getTaskDefinitionKey(), task.getTaskDefinitionKey())) {
                        WorkflowTaskVO candidate = new WorkflowTaskVO();
                        candidate.setName(historyTask.getName());
                        candidate.setTaskDefinitionKey(historyTask.getTaskDefinitionKey());
                        candidates.putIfAbsent(historyTask.getTaskDefinitionKey(), candidate);
                    }
                });
        return List.copyOf(candidates.values());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnTask(WorkflowTaskReturnRequest request) {
        Task task = requireMyTodo(request.getId());
        if (task.getParentTaskId() != null) {
            throw new JeecgBootException("加签子任务不能执行退回");
        }
        boolean returnable = returnableTaskList(task.getId()).stream()
                .anyMatch(candidate -> Objects.equals(candidate.getTaskDefinitionKey(), request.getTargetTaskDefinitionKey()));
        if (!returnable) {
            throw new JeecgBootException("目标节点不是当前任务可退回的历史节点");
        }
        moveActiveTasksToTarget(task, request.getTargetTaskDefinitionKey(), request.getReason().trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delegateTask(WorkflowTaskDelegateRequest request) {
        Task task = requireMyTodo(request.getId());
        validateUser(request.getDelegateUserId());
        String currentUserId = identityAdapter.currentUserId();
        if (task.getAssignee() == null) {
            taskService.claim(task.getId(), currentUserId);
        }
        if (task.getOwner() == null) {
            taskService.setOwner(task.getId(), currentUserId);
        }
        taskService.addComment(task.getId(), task.getProcessInstanceId(), "delegate", request.getReason().trim());
        taskService.delegateTask(task.getId(), request.getDelegateUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferTask(WorkflowTaskTransferRequest request) {
        Task task = requireMyTodo(request.getId());
        validateUser(request.getAssigneeUserId());
        String currentUserId = identityAdapter.currentUserId();
        if (task.getOwner() == null) {
            taskService.setOwner(task.getId(), task.getAssignee() == null ? currentUserId : task.getAssignee());
        }
        taskService.addComment(task.getId(), task.getProcessInstanceId(), "transfer", request.getReason().trim());
        taskService.setAssignee(task.getId(), request.getAssigneeUserId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSignTask(WorkflowTaskSignCreateRequest request) {
        Task parent = requireMyTodoOrSignOwner(request.getId());
        String currentSignType = signType(parent);
        if (currentSignType != null && !Objects.equals(currentSignType, request.getType())) {
            throw new JeecgBootException("当前任务已存在" + signTypeName(currentSignType) + "，不能同时使用" + signTypeName(request.getType()));
        }
        if (taskResultStatus(parent) == WorkflowProcessConstants.TASK_STATUS_APPROVING) {
            throw new JeecgBootException("后加签任务已进入审批通过中，不能继续加签");
        }
        request.getUserIds().forEach(this::validateUser);
        String currentUserId = identityAdapter.currentUserId();
        if (parent.getAssignee() == null) {
            if (!Objects.equals(parent.getOwner(), currentUserId)) {
                taskService.claim(parent.getId(), currentUserId);
                parent = taskService.createTaskQuery().taskId(parent.getId()).includeTaskLocalVariables().singleResult();
            }
        }
        Set<String> occupiedUserIds = taskService.createTaskQuery()
                .processInstanceId(parent.getProcessInstanceId())
                .taskDefinitionKey(parent.getTaskDefinitionKey())
                .active().list().stream()
                .flatMap(task -> java.util.stream.Stream.of(task.getAssignee(), task.getOwner()))
                .filter(Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        Set<String> duplicateUserIds = new LinkedHashSet<>(request.getUserIds());
        duplicateUserIds.retainAll(occupiedUserIds);
        if (!duplicateUserIds.isEmpty()) {
            throw new JeecgBootException("加签用户不能与当前节点已有审批人重复：" + String.join(",", duplicateUserIds));
        }

        TaskEntityImpl parentEntity = (TaskEntityImpl) parent;
        if (SIGN_TYPE_BEFORE.equals(request.getType())) {
            if (parentEntity.getOwner() == null) {
                parentEntity.setOwner(parentEntity.getAssignee());
            }
            parentEntity.setAssignee(null);
        }
        parentEntity.setScopeType(request.getType());
        taskService.saveTask(parentEntity);
        if (SIGN_TYPE_BEFORE.equals(request.getType())) {
            updateTaskStatus(parentEntity.getId(), WorkflowProcessConstants.TASK_STATUS_WAIT);
        }

        for (String userId : request.getUserIds()) {
            createSignChildTask(parentEntity, userId, request.getReason().trim());
        }
        taskService.addComment(parentEntity.getId(), parentEntity.getProcessInstanceId(), "addSign", request.getReason().trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSignTask(WorkflowTaskSignDeleteRequest request) {
        Task child = taskService.createTaskQuery().taskId(request.getId())
                .taskTenantId(identityAdapter.currentTenantId()).includeTaskLocalVariables().singleResult();
        if (child == null || child.getParentTaskId() == null) {
            throw new JeecgBootException("加签任务不存在");
        }
        Task parent = taskService.createTaskQuery().taskId(child.getParentTaskId())
                .taskTenantId(identityAdapter.currentTenantId()).includeTaskLocalVariables().singleResult();
        if (parent == null || signType(parent) == null) {
            throw new JeecgBootException("加签任务不存在");
        }
        if (parent == null || (!Objects.equals(parent.getOwner(), identityAdapter.currentUserId())
                && !Objects.equals(parent.getAssignee(), identityAdapter.currentUserId()))) {
            throw new JeecgBootException("无权减签该任务");
        }
        List<Task> tasksToDelete = allActiveChildTasks(child.getId());
        tasksToDelete.add(child);
        String reason = request.getReason().trim();
        for (Task task : tasksToDelete) {
            updateTaskStatusAndReason(task.getId(), WorkflowProcessConstants.STATUS_CANCELED, reason);
        }
        taskService.addComment(parent.getId(), parent.getProcessInstanceId(), "deleteSign", request.getReason().trim());
        for (Task task : tasksToDelete) {
            taskService.deleteTask(task.getId(), reason);
        }
        restoreSignParentIfReady(parent.getId());
    }

    @Override
    public List<WorkflowTaskVO> childTaskList(String parentTaskId) {
        Task parent = taskService.createTaskQuery().taskId(parentTaskId)
                .taskTenantId(identityAdapter.currentTenantId()).includeTaskLocalVariables().singleResult();
        if (parent == null) {
            throw new JeecgBootException("父任务不存在");
        }
        String currentUserId = identityAdapter.currentUserId();
        if (!Objects.equals(parent.getOwner(), currentUserId) && !Objects.equals(parent.getAssignee(), currentUserId)) {
            throw new JeecgBootException("无权查看该加签任务");
        }
        return activeChildTasks(parentTaskId).stream().map(this::toTask).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawTask(String taskId) {
        HistoricTaskInstance historic = historyService.createHistoricTaskInstanceQuery()
                .taskId(taskId)
                .taskAssignee(identityAdapter.currentUserId())
                .taskTenantId(identityAdapter.currentTenantId())
                .finished()
                .singleResult();
        if (historic == null || historic.getTaskDefinitionKey() == null) {
            throw new JeecgBootException("已办任务不存在或无权撤回");
        }
        HistoricTaskInstance latest = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(historic.getProcessInstanceId())
                .taskTenantId(identityAdapter.currentTenantId())
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .listPage(0, 1).stream().findFirst().orElse(null);
        if (latest == null || !Objects.equals(latest.getId(), historic.getId())) {
            throw new JeecgBootException("只有流程中最后完成的任务可以撤回");
        }
        List<Task> activeTasks = taskService.createTaskQuery()
                .processInstanceId(historic.getProcessInstanceId())
                .taskTenantId(identityAdapter.currentTenantId())
                .active()
                .includeTaskLocalVariables()
                .list();
        if (activeTasks.isEmpty()) {
            throw new JeecgBootException("流程已经结束，无法撤回");
        }
        BpmnModel bpmnModel = repositoryService.getBpmnModel(historic.getProcessDefinitionId());
        FlowElement targetElement = bpmnModel == null ? null
                : bpmnModel.getFlowElement(historic.getTaskDefinitionKey());
        Set<String> activeTaskKeys = activeTasks.stream()
                .map(Task::getTaskDefinitionKey).filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        Set<String> reachableKeys = new LinkedHashSet<>();
        if (targetElement != null) {
            bpmnNavigator.structurallyReachableUserTasks(targetElement, bpmnModel, activeTaskKeys).stream()
                    .map(UserTask::getId).filter(Objects::nonNull).forEach(reachableKeys::add);
        }
        List<String> activityIds = activeTasks.stream()
                .map(Task::getTaskDefinitionKey)
                .filter(Objects::nonNull)
                .filter(reachableKeys::contains)
                .distinct()
                .toList();
        if (activityIds.isEmpty()) {
            activityIds = activeTasks.stream().map(Task::getTaskDefinitionKey)
                    .filter(Objects::nonNull).distinct().toList();
        }
        for (Task activeTask : activeTasks) {
            if (activityIds.contains(activeTask.getTaskDefinitionKey())) {
                taskService.setVariableLocal(activeTask.getId(), TASK_RESULT_STATUS,
                        WorkflowProcessConstants.STATUS_CANCELED);
                taskService.setVariableLocal(activeTask.getId(), TASK_REASON, "前一节点撤回");
            }
        }
        taskService.addComment(null, historic.getProcessInstanceId(), "withdraw", "审批人撤回");
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(historic.getProcessInstanceId())
                .moveActivityIdsToSingleActivityId(activityIds, historic.getTaskDefinitionKey())
                .changeState();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void triggerCallback(String processInstanceId, String taskDefineKey) {
        if (processInstanceId == null || processInstanceId.isBlank()
                || taskDefineKey == null || taskDefineKey.isBlank()) {
            throw new JeecgBootException("回调参数不能为空");
        }
        Execution execution = runtimeService.createExecutionQuery()
                .processInstanceId(processInstanceId)
                .activityId(taskDefineKey)
                .singleResult();
        if (execution == null) {
            throw new JeecgBootException("流程回调等待节点不存在或已被触发");
        }
        runtimeService.trigger(execution.getId());
    }

    private void completeTask(WorkflowTaskActionRequest request, boolean approved) {
        Task task = requireMyTodo(request.getId());
        completeTask(task, request, approved, true);
    }

    private void completeTask(Task task, WorkflowTaskActionRequest request, boolean approved,
                              boolean claimUnassignedTask) {
        String userId = claimUnassignedTask ? identityAdapter.currentUserId() : null;
        String reason = request.getReason() == null ? "" : request.getReason().trim();
        if (taskResultStatus(task) == WorkflowProcessConstants.TASK_STATUS_APPROVING) {
            throw new JeecgBootException("后加签任务正在审批中，请等待全部加签任务完成");
        }
        if (claimUnassignedTask) {
            validateNodeRules(task, request, approved);
        }
        if (approved && SIGN_TYPE_AFTER.equals(signType(task)) && !activeChildTasks(task.getId()).isEmpty()) {
            persistTaskResult(task, WorkflowProcessConstants.TASK_STATUS_APPROVING, reason,
                    request.getSignPicUrl(), request.getAttachments());
            taskService.addComment(task.getId(), task.getProcessInstanceId(), "approve", reason);
            runtimeService.setVariables(task.getProcessInstanceId(), completionVariables(task, request, true));
            activateAfterSignChildren(task.getId());
            return;
        }
        persistTaskResult(task, approved, reason, request.getSignPicUrl(), request.getAttachments());
        taskService.addComment(task.getId(), task.getProcessInstanceId(), approved ? "approve" : "reject", reason);
        if (!approved) {
            if (task.getParentTaskId() == null && rejectByConfiguredHandler(task, reason)) {
                return;
            }
            markParentTaskRejected(task, reason);
            runtimeService.setVariable(task.getProcessInstanceId(), WorkflowProcessConstants.VARIABLE_STATUS,
                    WorkflowProcessConstants.STATUS_REJECTED);
            runtimeService.setVariable(task.getProcessInstanceId(), WorkflowProcessConstants.VARIABLE_REASON, reason);
            moveProcessToEnd(task);
            return;
        }
        if (task.getParentTaskId() != null) {
            taskService.complete(task.getId());
            restoreSignParentIfReady(task.getParentTaskId());
            return;
        }
        if (!activeChildTasks(task.getId()).isEmpty()) {
            throw new JeecgBootException("请先完成当前任务的全部加签任务");
        }
        if (claimUnassignedTask && task.getAssignee() == null) {
            taskService.claim(task.getId(), userId);
        }
        Map<String, Object> variables = completionVariables(task, request, approved);
        if (task.getDelegationState() == DelegationState.PENDING) {
            taskService.resolveTask(task.getId(), variables);
        } else {
            taskService.complete(task.getId(), variables);
        }
    }

    private Map<String, Object> completionVariables(Task task, WorkflowTaskActionRequest request, boolean approved) {
        // 审批页面会回传已加载的流程变量，其中可能包含引擎维护的系统变量；
        // 这些变量不能作为表单变量再次校验，但也不应阻断正常审批。
        Map<String, Object> variables = sanitizedApprovalVariables(request.getVariables());
        filterNodeEditableVariables(task, variables);
        Map<String, List<String>> nextAssignees = normalizeAssignees(request.getNextAssignees());
        if (!nextAssignees.isEmpty()) {
            Map<String, List<String>> merged = assigneeMap(
                    runtimeService.getVariable(task.getProcessInstanceId(), APPROVE_USER_SELECT_ASSIGNEES));
            merged.putAll(nextAssignees);
            variables.put(APPROVE_USER_SELECT_ASSIGNEES, merged);
        }
        Map<String, Object> evaluationVariables = new LinkedHashMap<>(runtimeService.getVariables(task.getProcessInstanceId()));
        evaluationVariables.putAll(variables);
        initializeMultiInstanceVariables(repositoryService.getBpmnModel(task.getProcessDefinitionId()),
                task.getProcessDefinitionId(), evaluationVariables,
                processStartUserId(task.getProcessInstanceId()), variables);
        variables.put(APPROVED_VARIABLE, approved);
        String returnFlag = WorkflowProcessConstants.returnTaskFlagVariable(task.getTaskDefinitionKey());
        if (runtimeService.hasVariable(task.getProcessInstanceId(), returnFlag)) {
            runtimeService.removeVariable(task.getProcessInstanceId(), returnFlag);
        }
        return variables;
    }

    private void filterNodeEditableVariables(Task task, Map<String, Object> variables) {
        if (variables.isEmpty()) {
            return;
        }
        BpmnModel model = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        FlowElement element = model == null ? null : model.getFlowElement(task.getTaskDefinitionKey());
        if (!(element instanceof UserTask userTask)) {
            return;
        }
        String permissionText = extensionValue(userTask,
                WorkflowProcessConstants.EXTENSION_FORM_FIELD_PERMISSION);
        if (permissionText == null || permissionText.isBlank()) {
            return;
        }
        Map<String, String> permissions;
        try {
            permissions = objectMapper.readValue(permissionText, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new JeecgBootException("字段权限配置格式不正确", exception);
        }
        variables.entrySet().removeIf(entry -> !"WRITE".equals(permissions.get(entry.getKey())));
    }

    /**
     * Materializes candidate IDs for simple-designer multi-instance tasks before Flowable
     * evaluates the collection expression. The overload writing into {@code target} is used
     * when a running task advances to a later multi-instance node.
     */
    private void initializeMultiInstanceVariables(BpmnModel model, String processDefinitionId,
                                                  Map<String, Object> variables, String startUserId) {
        initializeMultiInstanceVariables(model, processDefinitionId, variables, startUserId, variables);
    }

    private void initializeMultiInstanceVariables(BpmnModel model, String processDefinitionId,
                                                  Map<String, Object> variables, String startUserId,
                                                  Map<String, Object> target) {
        if (model == null || model.getMainProcess() == null) {
            return;
        }
        for (FlowElement element : model.getMainProcess().getFlowElements()) {
            if (!(element instanceof UserTask userTask) || userTask.getLoopCharacteristics() == null) {
                continue;
            }
            Integer strategy = candidateStrategy(userTask);
            Set<String> ids = candidateUserIds(userTask, strategy, startUserId, variables,
                    processDefinitionId);
            if (ids.isEmpty() && strategy == null && hasLiteralAssignee(userTask)) {
                ids = Set.of(userTask.getAssignee().trim());
            }
            List<String> assignees = new ArrayList<>(ids);
            if (assignees.isEmpty()) {
                // 与芋道自定义多实例行为一致：保留一个空办理人实例，交由任务创建监听器
                // 执行“自动通过/自动拒绝”，避免 Flowable 对空集合直接跳过整个节点。
                assignees.add(null);
            }
            target.put(WorkflowProcessConstants.multiInstanceAssigneesVariable(userTask.getId()), assignees);
        }
    }

    private boolean hasLiteralAssignee(UserTask task) {
        return task.getAssignee() != null && !task.getAssignee().isBlank()
                && !task.getAssignee().contains("${") && !task.getAssignee().contains("#{");
    }

    private String processStartUserId(String processInstanceId) {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        return processInstance == null ? null : processInstance.getStartUserId();
    }

    private void markParentTaskRejected(Task task, String reason) {
        String parentTaskId = task.getParentTaskId();
        while (parentTaskId != null) {
            Task parentTask = taskService.createTaskQuery().taskId(parentTaskId).singleResult();
            if (parentTask == null) {
                return;
            }
            taskService.setVariableLocal(parentTask.getId(), TASK_RESULT_STATUS,
                    WorkflowProcessConstants.STATUS_REJECTED);
            taskService.setVariableLocal(parentTask.getId(), TASK_REASON, reason);
            parentTaskId = parentTask.getParentTaskId();
        }
    }

    private void moveProcessToEnd(Task task) {
        BpmnModel model = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        if (model == null || model.getMainProcess() == null) {
            throw new JeecgBootException("流程定义缺少 BPMN 模型");
        }
        List<EndEvent> endEvents = model.getMainProcess().getFlowElements().stream()
                .filter(EndEvent.class::isInstance)
                .map(EndEvent.class::cast)
                .toList();
        if (endEvents.size() != 1) {
            throw new JeecgBootException("流程必须且只能包含一个结束节点");
        }
        List<Task> activeTasks = taskService.createTaskQuery()
                .processInstanceId(task.getProcessInstanceId())
                .taskTenantId(task.getTenantId())
                .active()
                .includeTaskLocalVariables()
                .list();
        for (Task activeTask : activeTasks) {
            if (!Objects.equals(activeTask.getId(), task.getId())) {
                Object status = activeTask.getTaskLocalVariables() == null ? null
                        : activeTask.getTaskLocalVariables().get(TASK_RESULT_STATUS);
                boolean finalStatus = status instanceof Number number
                        && (number.intValue() == WorkflowProcessConstants.STATUS_APPROVED
                        || number.intValue() == WorkflowProcessConstants.STATUS_REJECTED
                        || number.intValue() == WorkflowProcessConstants.STATUS_CANCELED
                        || number.intValue() == WorkflowProcessConstants.TASK_STATUS_RETURNED
                        || number.intValue() == WorkflowProcessConstants.TASK_STATUS_APPROVING);
                if (!finalStatus) {
                    taskService.setVariableLocal(activeTask.getId(), TASK_RESULT_STATUS,
                            WorkflowProcessConstants.STATUS_CANCELED);
                    taskService.setVariableLocal(activeTask.getId(), TASK_REASON,
                            "流程被拒绝或取消，系统取消任务");
                }
            }
        }
        List<String> activeActivityIds = activeTasks.stream()
                .map(Task::getTaskDefinitionKey)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (activeActivityIds.isEmpty()) {
            throw new JeecgBootException("流程没有可结束的活动任务");
        }
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(task.getProcessInstanceId())
                .moveActivityIdsToSingleActivityId(activeActivityIds, endEvents.get(0).getId())
                .changeState();
    }

    /**
     * 按节点“拒绝后处理”配置执行驳回。只有配置为“驳回到指定任务节点”时返回 true，
     * 其他情况返回 false，由调用方继续按“拒绝并终止流程”处理。
     */
    private boolean rejectByConfiguredHandler(Task task, String reason) {
        BpmnModel model = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        FlowElement element = model == null ? null : model.getFlowElement(task.getTaskDefinitionKey());
        if (!(element instanceof UserTask userTask)) {
            return false;
        }
        int handlerType = extensionInteger(userTask, WorkflowProcessConstants.EXTENSION_REJECT_HANDLER_TYPE,
                WorkflowProcessConstants.REJECT_HANDLER_FINISH_PROCESS);
        if (handlerType != WorkflowProcessConstants.REJECT_HANDLER_RETURN_USER_TASK) {
            return false;
        }
        String targetTaskKey = extensionValue(userTask, WorkflowProcessConstants.EXTENSION_REJECT_RETURN_TASK_ID);
        if (targetTaskKey == null || targetTaskKey.isBlank()) {
            throw new JeecgBootException("当前节点配置了驳回处理，但缺少目标退回节点");
        }
        if (Objects.equals(targetTaskKey, task.getTaskDefinitionKey())) {
            throw new JeecgBootException("驳回目标节点不能是当前审批节点");
        }
        FlowElement targetElement = model.getFlowElement(targetTaskKey);
        if (!(targetElement instanceof UserTask)) {
            throw new JeecgBootException("驳回目标节点不存在或不是审批节点：" + targetTaskKey);
        }
        moveActiveTasksToTarget(task, targetTaskKey, reason);
        return true;
    }

    private void moveActiveTasksToTarget(Task task, String targetTaskKey, String reason) {
        BpmnModel model = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        if (model == null || !(model.getFlowElement(targetTaskKey) instanceof UserTask targetElement)) {
            throw new JeecgBootException("退回目标节点不存在或不是审批节点：" + targetTaskKey);
        }
        List<Task> activeTasks = taskService.createTaskQuery()
                .processInstanceId(task.getProcessInstanceId())
                .taskTenantId(task.getTenantId())
                .active()
                .includeTaskLocalVariables()
                .list();
        Set<String> activeTaskKeys = activeTasks.stream()
                .map(Task::getTaskDefinitionKey)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        Set<String> reachableKeys = new LinkedHashSet<>();
        reachableKeys.add(targetTaskKey);
        bpmnNavigator.structurallyReachableUserTasks(targetElement, model, activeTaskKeys).stream()
                .map(UserTask::getId).filter(Objects::nonNull).forEach(reachableKeys::add);
        List<String> returnTaskKeys = activeTasks.stream()
                .map(Task::getTaskDefinitionKey)
                .filter(Objects::nonNull)
                .filter(reachableKeys::contains)
                .distinct()
                .toList();
        if (returnTaskKeys.isEmpty() || !returnTaskKeys.contains(task.getTaskDefinitionKey())) {
            throw new JeecgBootException("当前任务不在退回目标节点的可达路径上");
        }

        runtimeService.setVariable(task.getProcessInstanceId(),
                WorkflowProcessConstants.returnTaskFlagVariable(targetTaskKey), true);
        taskService.addComment(task.getId(), task.getProcessInstanceId(), "return", reason);
        taskService.setVariableLocal(task.getId(), TASK_RESULT_STATUS,
                WorkflowProcessConstants.TASK_STATUS_RETURNED);
        taskService.setVariableLocal(task.getId(), TASK_REASON, reason);
        for (Task other : activeTasks) {
            if (!Objects.equals(other.getId(), task.getId())
                    && returnTaskKeys.contains(other.getTaskDefinitionKey())) {
                taskService.setVariableLocal(other.getId(), TASK_RESULT_STATUS,
                        WorkflowProcessConstants.STATUS_CANCELED);
                taskService.setVariableLocal(other.getId(), TASK_REASON, reason);
            }
        }
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(task.getProcessInstanceId())
                .moveActivityIdsToSingleActivityId(returnTaskKeys, targetTaskKey)
                .changeState();
    }

    private String extensionValue(FlowElement element, String name) {
        if (element == null || element.getExtensionElements() == null) {
            return null;
        }
        List<ExtensionElement> values = element.getExtensionElements().get(name);
        if (values == null || values.isEmpty() || values.get(0).getElementText() == null) {
            return null;
        }
        return values.get(0).getElementText().trim();
    }

    private int extensionInteger(FlowElement element, String name, int defaultValue) {
        String value = extensionValue(element, name);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new JeecgBootException("流程节点扩展配置格式不正确：" + name, exception);
        }
    }

    private void applyOperationSettings(WorkflowApprovalDetailVO vo, String taskDefinitionKey,
                                        BpmnModel bpmnModel) {
        FlowElement element = bpmnModel == null ? null : bpmnModel.getFlowElement(taskDefinitionKey);
        if (!(element instanceof UserTask userTask)) {
            return;
        }
        String buttonsText = extensionValue(userTask, WorkflowProcessConstants.EXTENSION_ENABLED_BUTTONS);
        List<Integer> buttons = new ArrayList<>();
        if (buttonsText == null || buttonsText.isBlank()) {
            buttons.addAll(List.of(1, 2, 3, 4, 5, 6, 7));
        } else {
            for (String value : buttonsText.split(",")) {
                try {
                    buttons.add(Integer.parseInt(value.trim()));
                } catch (NumberFormatException ignored) {
                    // 忽略单个非法按钮编号，避免详情页因旧模型配置异常而无法打开。
                }
            }
        }
        vo.setEnabledButtons(buttons);
        String buttonSettingsText = extensionValue(userTask,
                WorkflowProcessConstants.EXTENSION_BUTTONS_SETTING);
        if (buttonSettingsText != null && !buttonSettingsText.isBlank()) {
            try {
                Map<Integer, String> displayNames = new LinkedHashMap<>();
                List<Map<String, Object>> settings = objectMapper.readValue(buttonSettingsText,
                        new TypeReference<>() {
                        });
                for (Map<String, Object> setting : settings) {
                    Object idValue = setting.get("id");
                    Object nameValue = setting.get("displayName");
                    if (idValue == null || nameValue == null || nameValue.toString().isBlank()) {
                        continue;
                    }
                    try {
                        displayNames.put(Integer.parseInt(idValue.toString()), nameValue.toString().trim());
                    } catch (NumberFormatException ignored) {
                        // 跳过单个非法按钮编号，兼容旧模型中的手工 JSON。
                    }
                }
                vo.setButtonDisplayNames(displayNames);
            } catch (Exception exception) {
                throw new JeecgBootException("操作按钮配置格式不正确", exception);
            }
        }
        vo.setSignEnable("true".equalsIgnoreCase(
                extensionValue(userTask, WorkflowProcessConstants.EXTENSION_SIGN_ENABLE)));
        vo.setReasonRequire("true".equalsIgnoreCase(
                extensionValue(userTask, WorkflowProcessConstants.EXTENSION_REASON_REQUIRE)));
        String fieldPermissionText = extensionValue(userTask,
                WorkflowProcessConstants.EXTENSION_FORM_FIELD_PERMISSION);
        if (fieldPermissionText != null && !fieldPermissionText.isBlank()) {
            try {
                vo.setFormFieldsPermission(objectMapper.readValue(fieldPermissionText, new TypeReference<>() {
                }));
            } catch (Exception exception) {
                throw new JeecgBootException("字段权限配置格式不正确", exception);
            }
        }
    }

    private void validateNodeRules(Task task, WorkflowTaskActionRequest request, boolean approved) {
        BpmnModel model = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        FlowElement element = model == null ? null : model.getFlowElement(task.getTaskDefinitionKey());
        if (!(element instanceof UserTask userTask)) {
            return;
        }
        boolean reasonRequire = "true".equalsIgnoreCase(
                extensionValue(userTask, WorkflowProcessConstants.EXTENSION_REASON_REQUIRE));
        if (reasonRequire && (request.getReason() == null || request.getReason().isBlank())) {
            throw new JeecgBootException("该节点必须填写审批意见");
        }
        boolean signEnable = "true".equalsIgnoreCase(
                extensionValue(userTask, WorkflowProcessConstants.EXTENSION_SIGN_ENABLE));
        if (approved && signEnable
                && (request.getSignPicUrl() == null || request.getSignPicUrl().isBlank())) {
            throw new JeecgBootException("该节点必须上传审批签名");
        }
    }

    private void persistTaskResult(Task task, boolean approved, String reason,
                                   String signPicUrl, List<String> attachments) {
        persistTaskResult(task, approved ? WorkflowProcessConstants.STATUS_APPROVED
                : WorkflowProcessConstants.STATUS_REJECTED, reason, signPicUrl, attachments);
    }

    private void persistTaskResult(Task task, int status, String reason,
                                   String signPicUrl, List<String> attachments) {
        taskService.setVariableLocal(task.getId(), TASK_RESULT_STATUS, status);
        taskService.setVariableLocal(task.getId(), TASK_REASON, reason);
        if (signPicUrl != null && !signPicUrl.isBlank()) {
            taskService.setVariableLocal(task.getId(), TASK_SIGN_PIC_URL,
                    normalizeEvidenceUrl(signPicUrl, "签名图片"));
        }
        for (String attachment : normalizeAttachmentUrls(attachments)) {
            taskService.createAttachment(TASK_ATTACHMENT_TYPE, task.getId(), task.getProcessInstanceId(),
                    attachmentName(attachment), null, attachment);
        }
    }

    private List<String> normalizeAttachmentUrls(List<String> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return Collections.emptyList();
        }
        return attachments.stream().filter(Objects::nonNull).map(String::trim)
                .filter(item -> !item.isBlank()).distinct()
                .map(item -> normalizeEvidenceUrl(item, "附件")).toList();
    }

    private String normalizeEvidenceUrl(String value, String label) {
        String url = value.trim();
        if (url.length() > 2048 || url.contains("\r") || url.contains("\n")) {
            throw new JeecgBootException(label + "地址不合法");
        }
        String lower = url.toLowerCase(java.util.Locale.ROOT);
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            return url;
        }
        boolean unsafeRelativePath = url.isBlank() || url.startsWith("//") || url.startsWith("\\")
                || url.contains(":") || url.contains("\\")
                || Arrays.stream(url.split("/")).anyMatch(".."::equals);
        if (unsafeRelativePath) {
            throw new JeecgBootException(label + "仅支持安全的站内路径或 HTTP/HTTPS 地址");
        }
        return url;
    }

    private String attachmentName(String url) {
        String path = url;
        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            path = path.substring(0, queryIndex);
        }
        int slashIndex = path.lastIndexOf('/');
        String name = slashIndex >= 0 ? path.substring(slashIndex + 1) : path;
        return name.isBlank() ? "attachment" : name;
    }

    private Task requireMyTodo(String taskId) {
        Task task = myTodoQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new JeecgBootException("待办任务不存在或当前用户无权处理");
        }
        return task;
    }

    private Task requireMyTodoOrSignOwner(String taskId) {
        Task task = myTodoQuery().taskId(taskId).singleResult();
        if (task != null) {
            return task;
        }
        task = taskService.createTaskQuery().taskId(taskId)
                .taskTenantId(identityAdapter.currentTenantId()).active().includeTaskLocalVariables().singleResult();
        if (task == null || signType(task) == null
                || !Objects.equals(task.getOwner(), identityAdapter.currentUserId())) {
            throw new JeecgBootException("待办任务不存在或当前用户无权处理");
        }
        return task;
    }

    private void validateUser(String userId) {
        if (userId == null || userId.isBlank() || sysBaseApi.getUserById(userId) == null) {
            throw new JeecgBootException("用户不存在：" + userId);
        }
    }

    private WorkflowActivityNodeVO toActivityNode(UserTask task, String startUserId,
                                                   Map<String, Object> variables) {
        return toActivityNode(task, startUserId, variables, 1);
    }

    private WorkflowActivityNodeVO toActivityNode(UserTask task, String startUserId,
                                                   Map<String, Object> variables, int status) {
        WorkflowActivityNodeVO vo = new WorkflowActivityNodeVO();
        vo.setId(task.getId());
        vo.setName(task.getName());
        vo.setNodeType(11);
        vo.setStatus(status);
        vo.setTasks(Collections.emptyList());
        Integer strategy = candidateStrategy(task);
        vo.setCandidateStrategy(strategy);
        vo.setCandidateUsers(candidateUserIds(task, strategy, startUserId, variables).stream()
                .map(sysBaseApi::getUserById)
                .filter(Objects::nonNull)
                .map(this::toSimpleUser)
                .toList());
        return vo;
    }

    private Integer candidateStrategy(UserTask task) {
        Integer strategy = candidateResolver.candidateStrategy(task);
        if (strategy != null) {
            return strategy;
        }
        if (task.getAssignee() != null && task.getAssignee().contains("_workflowStartUserId")) {
            return 36;
        }
        if (task.getCandidateUsers() != null && !task.getCandidateUsers().isEmpty()) {
            return 30;
        }
        if (task.getCandidateGroups() != null && !task.getCandidateGroups().isEmpty()) {
            return 10;
        }
        return null;
    }

    private Set<String> candidateUserIds(UserTask task, Integer strategy, String startUserId,
                                         Map<String, Object> variables) {
        return candidateResolver.resolve(task, strategy, startUserId, variables);
    }

    private Set<String> candidateUserIds(UserTask task, Integer strategy, String startUserId,
                                         Map<String, Object> variables, String processDefinitionId) {
        return candidateResolver.resolve(task, strategy, startUserId, variables, processDefinitionId);
    }

    private WorkflowUserSimpleVO toSimpleUser(LoginUser user) {
        WorkflowUserSimpleVO vo = new WorkflowUserSimpleVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getRealname());
        vo.setAvatar(user.getAvatar());
        vo.setStatus(user.getStatus());
        return vo;
    }

    private Map<String, List<String>> normalizeAssignees(Map<String, List<String>> source) {
        if (source == null || source.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Map<String, List<String>> result = new LinkedHashMap<>();
        source.forEach((activityId, userIds) -> {
            if (activityId == null || activityId.isBlank() || userIds == null || userIds.isEmpty()) {
                throw new JeecgBootException("自选审批节点和审批人不能为空");
            }
            List<String> normalized = userIds.stream().filter(Objects::nonNull)
                    .map(String::trim).filter(id -> !id.isBlank()).distinct().toList();
            if (normalized.isEmpty()) {
                throw new JeecgBootException("自选审批人不能为空");
            }
            normalized.forEach(this::validateUser);
            result.put(activityId.trim(), normalized);
        });
        return result;
    }

    private Map<String, List<String>> assigneeMap(Object value) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        if (!(value instanceof Map<?, ?> source)) {
            return result;
        }
        source.forEach((key, rawIds) -> {
            if (key == null || !(rawIds instanceof Collection<?> values)) {
                return;
            }
            List<String> ids = values.stream().filter(Objects::nonNull)
                    .map(String::valueOf).filter(id -> !id.isBlank()).toList();
            result.put(String.valueOf(key), ids);
        });
        return result;
    }

    private void createSignChildTask(TaskEntityImpl parent, String userId, String reason) {
        TaskEntityImpl child = (TaskEntityImpl) taskService.newTask(UUID.randomUUID().toString());
        child.setName(parent.getName() + "（加签）");
        child.setDescription(reason);
        child.setCategory(SIGN_CHILD_CATEGORY_PREFIX + parent.getScopeType());
        child.setParentTaskId(parent.getId());
        child.setProcessDefinitionId(parent.getProcessDefinitionId());
        child.setProcessInstanceId(parent.getProcessInstanceId());
        child.setTaskDefinitionKey(parent.getTaskDefinitionKey());
        child.setTaskDefinitionId(parent.getTaskDefinitionId());
        child.setPriority(parent.getPriority());
        child.setCreateTime(new Date());
        child.setTenantId(parent.getTenantId());
        if (SIGN_TYPE_BEFORE.equals(parent.getScopeType())) {
            child.setAssignee(userId);
        } else {
            child.setOwner(userId);
        }
        taskService.saveTask(child);
        updateTaskStatus(child.getId(), SIGN_TYPE_BEFORE.equals(parent.getScopeType())
                ? WorkflowProcessConstants.TASK_STATUS_RUNNING
                : WorkflowProcessConstants.TASK_STATUS_WAIT);
    }

    private void activateAfterSignChildren(String parentTaskId) {
        for (Task child : activeChildTasks(parentTaskId)) {
            if (child.getAssignee() == null && child.getOwner() != null) {
                taskService.setAssignee(child.getId(), child.getOwner());
            }
            updateTaskStatus(child.getId(), WorkflowProcessConstants.TASK_STATUS_RUNNING);
        }
    }

    private void updateTaskStatus(String taskId, int status) {
        taskService.setVariableLocal(taskId, TASK_RESULT_STATUS, status);
    }

    private void updateTaskStatusAndReason(String taskId, int status, String reason) {
        updateTaskStatus(taskId, status);
        taskService.setVariableLocal(taskId, TASK_REASON, reason);
    }

    private int taskResultStatus(Task task) {
        Object status = task.getTaskLocalVariables() == null ? null
                : task.getTaskLocalVariables().get(TASK_RESULT_STATUS);
        return status instanceof Number number ? number.intValue() : WorkflowProcessConstants.TASK_STATUS_RUNNING;
    }

    private String signType(Task task) {
        if (SIGN_TYPE_BEFORE.equals(task.getScopeType()) || SIGN_TYPE_AFTER.equals(task.getScopeType())) {
            return task.getScopeType();
        }
        String category = task.getCategory();
        if (category != null && category.startsWith(LEGACY_SIGN_PARENT_CATEGORY_PREFIX)) {
            String legacyType = category.substring(LEGACY_SIGN_PARENT_CATEGORY_PREFIX.length());
            return SIGN_TYPE_BEFORE.equals(legacyType) || SIGN_TYPE_AFTER.equals(legacyType) ? legacyType : null;
        }
        return null;
    }

    private String signTypeName(String type) {
        return SIGN_TYPE_BEFORE.equals(type) ? "前加签" : "后加签";
    }

    private void clearSignType(Task task) {
        TaskEntityImpl entity = (TaskEntityImpl) task;
        entity.setScopeType(null);
        if (entity.getCategory() != null && entity.getCategory().startsWith(LEGACY_SIGN_PARENT_CATEGORY_PREFIX)) {
            entity.setCategory(null);
        }
        taskService.saveTask(entity);
    }

    private List<Task> activeChildTasks(String parentTaskId) {
        return taskService.createTaskQuery().taskTenantId(identityAdapter.currentTenantId())
                .active().includeTaskLocalVariables().list().stream()
                .filter(task -> Objects.equals(parentTaskId, task.getParentTaskId())).toList();
    }

    private List<Task> allActiveChildTasks(String parentTaskId) {
        List<Task> result = new ArrayList<>();
        for (Task child : activeChildTasks(parentTaskId)) {
            result.addAll(allActiveChildTasks(child.getId()));
            result.add(child);
        }
        return result;
    }

    private void restoreSignParentIfReady(String parentTaskId) {
        if (parentTaskId == null || parentTaskId.isBlank()) {
            return;
        }
        if (!activeChildTasks(parentTaskId).isEmpty()) {
            return;
        }
        Task parent = taskService.createTaskQuery().taskId(parentTaskId)
                .taskTenantId(identityAdapter.currentTenantId()).includeTaskLocalVariables().singleResult();
        if (parent == null) {
            return;
        }
        String type = signType(parent);
        if (type == null) {
            return;
        }
        String ancestorTaskId = parent.getParentTaskId();
        if (SIGN_TYPE_BEFORE.equals(type)) {
            clearSignType(parent);
            if (parent.getAssignee() == null && parent.getOwner() != null) {
                taskService.setAssignee(parent.getId(), parent.getOwner());
            }
            updateTaskStatus(parent.getId(), WorkflowProcessConstants.TASK_STATUS_RUNNING);
            return;
        }
        int status = taskResultStatus(parent);
        clearSignType(parent);
        if (status != WorkflowProcessConstants.TASK_STATUS_APPROVING) {
            return;
        }
        updateTaskStatus(parent.getId(), WorkflowProcessConstants.STATUS_APPROVED);
        taskService.complete(parent.getId());
        restoreSignParentIfReady(ancestorTaskId);
    }

    private TaskQuery myTodoQuery() {
        String userId = identityAdapter.currentUserId();
        TaskQuery query = taskService.createTaskQuery()
                .active()
                .taskTenantId(identityAdapter.currentTenantId())
                .includeTaskLocalVariables()
                .or()
                .taskAssignee(userId)
                .taskCandidateUser(userId);
        List<String> roles = identityAdapter.currentRoleCodes();
        if (!roles.isEmpty()) {
            query.taskCandidateGroupIn(roles);
        }
        return query.endOr();
    }

    private WorkflowPage<WorkflowInstanceVO> processPage(WorkflowProcessInstancePageRequest request,
                                                          String startUserId) {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
                .includeProcessVariables()
                .processInstanceTenantId(identityAdapter.currentTenantId());
        if (startUserId != null) {
            query.startedBy(startUserId);
        }
        if (hasText(request.getName())) {
            query.processInstanceNameLike("%" + request.getName().trim() + "%");
        }
        if (hasText(request.getProcessDefinitionKey())) {
            query.processDefinitionKey(request.getProcessDefinitionKey().trim());
        }
        if (hasText(request.getCategory())) {
            query.processDefinitionCategory(request.getCategory().trim());
        }
        if (request.getStatus() != null) {
            query.variableValueEquals(WorkflowProcessConstants.VARIABLE_STATUS, request.getStatus());
        }
        applyProcessTimeRange(query, request.getCreateTime(), false);
        applyProcessTimeRange(query, request.getEndTime(), true);
        parseFormFieldFilters(request.getFormFieldsParams()).forEach(query::variableValueEquals);
        long total = query.count();
        List<WorkflowInstanceVO> list = query.orderByProcessInstanceStartTime().desc()
                .listPage(offset(request.getPageNo(), request.getPageSize()), request.getPageSize()).stream()
                .map(this::toInstance)
                .toList();
        return new WorkflowPage<>(list, total);
    }

    private void applyProcessTimeRange(HistoricProcessInstanceQuery query, LocalDateTime[] range,
                                       boolean finished) {
        if (range == null || range.length == 0) {
            return;
        }
        Date start = toDate(range[0]);
        Date end = toDate(range[1]);
        if (finished) {
            query.finishedAfter(start).finishedBefore(end);
        } else {
            query.startedAfter(start).startedBefore(end);
        }
    }

    private void applyTaskFilters(TaskQuery query, WorkflowTaskPageRequest request) {
        if (hasText(request.getName())) {
            query.taskNameLike("%" + request.getName().trim() + "%");
        }
        if (hasText(request.getCategory())) {
            query.taskCategory(request.getCategory().trim());
        }
        if (hasText(request.getProcessDefinitionKey())) {
            query.processDefinitionKey(request.getProcessDefinitionKey().trim());
        }
        if (request.getStatus() != null) {
            query.processVariableValueEquals(WorkflowProcessConstants.VARIABLE_STATUS, request.getStatus());
        }
        if (request.getCreateTime() != null && request.getCreateTime().length != 0) {
            query.taskCreatedAfter(toDate(request.getCreateTime()[0]));
            query.taskCreatedBefore(toDate(request.getCreateTime()[1]));
        }
    }

    private void applyHistoricTaskFilters(HistoricTaskInstanceQuery query, WorkflowTaskPageRequest request) {
        if (hasText(request.getName())) {
            query.taskNameLike("%" + request.getName().trim() + "%");
        }
        if (hasText(request.getCategory())) {
            query.taskCategory(request.getCategory().trim());
        }
        if (hasText(request.getProcessDefinitionKey())) {
            query.processDefinitionKey(request.getProcessDefinitionKey().trim());
        }
        if (request.getStatus() != null) {
            query.taskVariableValueEquals(TASK_RESULT_STATUS, request.getStatus());
        }
        if (request.getCreateTime() != null && request.getCreateTime().length != 0) {
            query.taskCreatedAfter(toDate(request.getCreateTime()[0]));
            query.taskCreatedBefore(toDate(request.getCreateTime()[1]));
        }
    }

    private Map<String, Object> parseFormFieldFilters(String value) {
        if (!hasText(value)) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Object> source = objectMapper.readValue(value, new TypeReference<>() { });
            if (source.size() > 50) {
                throw new JeecgBootException("动态表单查询字段不能超过 50 个");
            }
            Map<String, Object> filters = new LinkedHashMap<>();
            source.forEach((key, filterValue) -> {
                if (!hasText(key) || key.length() > 128 || RESERVED_VARIABLES.contains(key)
                        || key.startsWith("_workflow")) {
                    throw new JeecgBootException("动态表单查询包含非法或系统保留字段");
                }
                if (filterValue != null && !String.valueOf(filterValue).isBlank()) {
                    filters.put(key, filterValue);
                }
            });
            return filters;
        } catch (JsonProcessingException exception) {
            throw new JeecgBootException("动态表单查询条件不是有效的 JSON 对象", exception);
        }
    }

    private Date toDate(LocalDateTime value) {
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    private WorkflowDefinitionVO toDefinition(ProcessDefinition definition, Deployment deployment) {
        WorkflowDefinitionVO vo = new WorkflowDefinitionVO();
        vo.setId(definition.getId());
        vo.setKey(definition.getKey());
        vo.setName(definition.getName());
        vo.setVersion(definition.getVersion());
        vo.setCategory(definition.getCategory());
        vo.setDeploymentId(definition.getDeploymentId());
        vo.setResourceName(definition.getResourceName());
        vo.setSuspended(definition.isSuspended());
        vo.setSuspensionState(definition.isSuspended() ? 2 : 1);
        Deployment resolved = deployment == null
                ? repositoryService.createDeploymentQuery().deploymentId(definition.getDeploymentId()).singleResult()
                : deployment;
        if (resolved != null) {
            vo.setDeploymentTime(resolved.getDeploymentTime());
        }
        WorkflowModelVO model = modelService.getByDeploymentId(definition.getDeploymentId());
        if (model != null) {
            vo.setCategoryName(model.getCategoryName());
            vo.setModelType(model.getType());
            vo.setModelId(model.getId());
            vo.setFormType(model.getFormType());
            vo.setFormId(model.getFormId());
            vo.setFormName(model.getFormName());
            vo.setFormCustomCreatePath(model.getFormCustomCreatePath());
            vo.setFormCustomViewPath(model.getFormCustomViewPath());
            vo.setIcon(model.getIcon());
            vo.setDescription(model.getDescription());
            vo.setVisible(model.getVisible());
            vo.setSort(model.getSort());
            if (model.getSimpleModel() != null) {
                try {
                    vo.setSimpleModel(objectMapper.writeValueAsString(model.getSimpleModel()));
                } catch (JsonProcessingException exception) {
                    throw new JeecgBootException("序列化简单流程模型失败", exception);
                }
            }
            if (model.getProcessDefinition() != null) {
                vo.setFormConf(model.getProcessDefinition().getFormConf());
                vo.setFormFields(model.getProcessDefinition().getFormFields());
            }
        }
        return vo;
    }

    private boolean canCurrentUserStart(ProcessDefinition definition) {
        WorkflowModelVO model = modelService.getByDeploymentId(definition.getDeploymentId());
        if (model == null) {
            return true;
        }
        if (Boolean.FALSE.equals(model.getVisible())) {
            return false;
        }
        if (model.getStartUserIds() != null && !model.getStartUserIds().isEmpty()) {
            return model.getStartUserIds().contains(identityAdapter.currentUserId());
        }
        if (model.getStartDeptIds() != null && !model.getStartDeptIds().isEmpty()) {
            return identityAdapter.currentDepartmentIds().stream().anyMatch(model.getStartDeptIds()::contains);
        }
        return true;
    }

    private String readBpmnXml(ProcessDefinition definition) {
        try (InputStream input = repositoryService.getResourceAsStream(
                definition.getDeploymentId(), definition.getResourceName())) {
            return new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new JeecgBootException("读取 BPMN 模型失败", exception);
        }
    }

    private WorkflowInstanceVO toInstance(HistoricProcessInstance instance) {
        WorkflowInstanceVO vo = new WorkflowInstanceVO();
        vo.setId(instance.getId());
        vo.setName(instance.getName());
        vo.setBusinessKey(instance.getBusinessKey());
        vo.setProcessDefinitionId(instance.getProcessDefinitionId());
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(instance.getProcessDefinitionId()).singleResult();
        vo.setProcessDefinitionName(definition == null ? null : definition.getName());
        vo.setCategory(definition == null ? null : definition.getCategory());
        WorkflowModelVO model = definition == null ? null : modelService.getByDeploymentId(definition.getDeploymentId());
        vo.setCategoryName(model == null ? null : model.getCategoryName());
        vo.setStartUserId(instance.getStartUserId());
        vo.setStartUserName(resolveUserName(instance.getStartUserId()));
        vo.setStartTime(instance.getStartTime());
        vo.setEndTime(instance.getEndTime());
        Map<String, Object> processVariables = instance.getProcessVariables();
        Integer resultStatus = processStatus(processVariables == null ? null
                : processVariables.get(WorkflowProcessConstants.VARIABLE_STATUS));
        vo.setResultStatus(resultStatus);
        if (processVariables != null && processVariables.get(WorkflowProcessConstants.VARIABLE_REASON) != null) {
            vo.setReason(String.valueOf(processVariables.get(WorkflowProcessConstants.VARIABLE_REASON)));
        }
        vo.setStatus(resolveProcessStatus(instance, resultStatus));
        vo.setCurrentTasks(taskService.createTaskQuery().processInstanceId(instance.getId()).active().list()
                .stream().map(Task::getName).toList());
        return vo;
    }

    private Integer processStatus(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.valueOf(String.valueOf(value));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private WorkflowTaskVO toTask(Task task) {
        WorkflowTaskVO vo = baseTask(task.getId(), task.getName(), task.getTaskDefinitionKey(),
                task.getProcessInstanceId(), task.getProcessDefinitionId(), task.getAssignee());
        vo.setCreateTime(task.getCreateTime());
        vo.setOwner(task.getOwner());
        vo.setAssigneeName(resolveUserName(task.getAssignee()));
        vo.setOwnerName(resolveUserName(task.getOwner()));
        vo.setParentTaskId(task.getParentTaskId());
        applyTaskEvidence(vo, task.getId(), task.getTaskLocalVariables());
        if (Objects.equals(vo.getResultStatus(), WorkflowProcessConstants.TASK_STATUS_WAIT)) {
            vo.setStatus("WAIT");
        } else if (Objects.equals(vo.getResultStatus(), WorkflowProcessConstants.TASK_STATUS_APPROVING)) {
            vo.setStatus("APPROVING");
        } else {
            vo.setStatus("TODO");
        }
        return vo;
    }

    private WorkflowTaskVO toTask(HistoricTaskInstance task) {
        WorkflowTaskVO vo = baseTask(task.getId(), task.getName(), task.getTaskDefinitionKey(),
                task.getProcessInstanceId(), task.getProcessDefinitionId(), task.getAssignee());
        vo.setCreateTime(task.getCreateTime());
        vo.setOwner(task.getOwner());
        vo.setAssigneeName(resolveUserName(task.getAssignee()));
        vo.setOwnerName(resolveUserName(task.getOwner()));
        vo.setParentTaskId(task.getParentTaskId());
        vo.setEndTime(task.getEndTime());
        vo.setDurationInMillis(task.getDurationInMillis());
        applyTaskEvidence(vo, task.getId(), task.getTaskLocalVariables());
        vo.setStatus(resolveTaskStatus(task, vo.getResultStatus()));
        if ((vo.getReason() == null || vo.getReason().isBlank()) && task.getDeleteReason() != null) {
            vo.setReason(task.getDeleteReason());
        }
        return vo;
    }

    private String resolveTaskStatus(HistoricTaskInstance task, Integer resultStatus) {
        if (task.getEndTime() == null) {
            return "TODO";
        }
        if (Objects.equals(resultStatus, WorkflowProcessConstants.STATUS_REJECTED)) {
            return "REJECTED";
        }
        if (Objects.equals(resultStatus, WorkflowProcessConstants.STATUS_CANCELED)) {
            return "CANCELED";
        }
        if (Objects.equals(resultStatus, WorkflowProcessConstants.TASK_STATUS_RETURNED)) {
            return "RETURNED";
        }
        if (Objects.equals(resultStatus, WorkflowProcessConstants.TASK_STATUS_APPROVING)) {
            return "APPROVING";
        }
        return task.getDeleteReason() == null ? "DONE" : "CANCELED";
    }

    private void applyTaskEvidence(WorkflowTaskVO vo, String taskId, Map<String, Object> localVariables) {
        Map<String, Object> variables = localVariables == null ? Collections.emptyMap() : localVariables;
        Object resultStatus = variables.get(TASK_RESULT_STATUS);
        if (resultStatus instanceof Number number) {
            vo.setResultStatus(number.intValue());
        }
        Object reason = variables.get(TASK_REASON);
        if (reason != null) {
            vo.setReason(String.valueOf(reason));
        }
        Object signPicUrl = variables.get(TASK_SIGN_PIC_URL);
        if (signPicUrl != null) {
            vo.setSignPicUrl(String.valueOf(signPicUrl));
        }
        vo.setAttachments(taskService.getTaskAttachments(taskId).stream()
                .filter(attachment -> Objects.equals(TASK_ATTACHMENT_TYPE, attachment.getType()))
                .map(Attachment::getUrl).filter(Objects::nonNull).toList());
    }

    private WorkflowTaskVO baseTask(String id, String name, String taskDefinitionKey,
                                    String processInstanceId, String processDefinitionId, String assignee) {
        WorkflowTaskVO vo = new WorkflowTaskVO();
        vo.setId(id);
        vo.setName(name);
        vo.setTaskDefinitionKey(taskDefinitionKey);
        vo.setProcessInstanceId(processInstanceId);
        vo.setProcessDefinitionId(processDefinitionId);
        vo.setAssignee(assignee);
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .includeProcessVariables()
                .singleResult();
        if (instance != null) {
            vo.setProcessInstanceName(instance.getName());
            Map<String, Object> variables = instance.getProcessVariables();
            Integer status = processStatus(variables == null ? null
                    : variables.get(WorkflowProcessConstants.VARIABLE_STATUS));
            vo.setProcessInstanceStatus(resolveProcessStatus(instance, status));
            vo.setBusinessKey(instance.getBusinessKey());
            vo.setStartUserId(instance.getStartUserId());
            vo.setStartUserName(resolveUserName(instance.getStartUserId()));
            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(instance.getProcessDefinitionId()).singleResult();
            if (definition != null) {
                vo.setCategory(definition.getCategory());
                WorkflowModelVO model = modelService.getByDeploymentId(definition.getDeploymentId());
                vo.setCategoryName(model == null ? null : model.getCategoryName());
            }
        }
        return vo;
    }

    private String resolveUserName(String userId) {
        if (!hasText(userId)) {
            return null;
        }
        LoginUser user = sysBaseApi.getUserById(userId);
        if (user == null) {
            return null;
        }
        return hasText(user.getRealname()) ? user.getRealname() : user.getUsername();
    }

    private String resolveProcessStatus(HistoricProcessInstance instance, Integer resultStatus) {
        if (resultStatus != null && resultStatus == WorkflowProcessConstants.STATUS_REJECTED) {
            return "REJECTED";
        }
        if (resultStatus != null && resultStatus == WorkflowProcessConstants.STATUS_CANCELED) {
            return "CANCELED";
        }
        if (instance.getEndTime() == null) {
            return "RUNNING";
        }
        return instance.getDeleteReason() == null ? "COMPLETED" : "CANCELED";
    }

    private void assertCanView(String processInstanceId) {
        String userId = identityAdapter.currentUserId();
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .processInstanceTenantId(identityAdapter.currentTenantId()).singleResult();
        if (instance == null) {
            throw new JeecgBootException("流程实例不存在");
        }
        if (userId.equals(instance.getStartUserId())) {
            return;
        }
        if (SecurityUtils.getSubject().isPermitted("bpm:process-instance:manager-query")) {
            return;
        }
        long involved = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .taskTenantId(identityAdapter.currentTenantId())
                .taskAssignee(userId).count();
        long currentCandidate = myTodoQuery().processInstanceId(processInstanceId).count();
        long currentOwner = taskService.createTaskQuery().active()
                .processInstanceId(processInstanceId)
                .taskTenantId(identityAdapter.currentTenantId())
                .taskOwner(userId).count();
        if (involved == 0 && currentCandidate == 0 && currentOwner == 0) {
            throw new JeecgBootException("无权查看该流程实例");
        }
    }

    private int offset(int pageNo, int pageSize) {
        int safePageNo = Math.max(pageNo, 1);
        return (safePageNo - 1) * Math.max(pageSize, 1);
    }

    private String normalizeResourceName(String resourceName) {
        String trimmed = resourceName.trim();
        if (trimmed.contains("..") || trimmed.contains("/") || trimmed.contains("\\")) {
            throw new JeecgBootException("资源文件名不能包含路径");
        }
        return trimmed.endsWith(".bpmn20.xml") ? trimmed : trimmed + ".bpmn20.xml";
    }

    private Map<String, Object> sanitizedVariables(Map<String, Object> source) {
        if (source == null) {
            return new HashMap<>();
        }
        if (source.size() > 100) {
            throw new JeecgBootException("单次流程变量不能超过 100 个");
        }
        Map<String, Object> variables = new HashMap<>();
        source.forEach((key, value) -> {
            if (key == null || key.isBlank() || key.startsWith("_workflow") || RESERVED_VARIABLES.contains(key)) {
                throw new JeecgBootException("流程变量名为空或使用了系统保留前缀");
            }
            variables.put(key, value);
        });
        return variables;
    }

    private Map<String, Object> sanitizedApprovalVariables(Map<String, Object> source) {
        if (source == null) {
            return new HashMap<>();
        }
        Map<String, Object> variables = new HashMap<>();
        source.forEach((key, value) -> {
            if (key == null || key.isBlank() || isSystemVariable(key)) {
                return;
            }
            variables.put(key, value);
        });
        if (variables.size() > 100) {
            throw new JeecgBootException("单次流程变量不能超过 100 个");
        }
        return variables;
    }

    private boolean isSystemVariable(String key) {
        return key != null && (key.startsWith("_workflow") || RESERVED_VARIABLES.contains(key));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return hasText(value) ? value.trim() : null;
    }
}
