package org.jeecg.modules.workflow.flowable;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.UserTask;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.common.engine.api.variable.VariableContainer;
import org.flowable.common.engine.impl.variable.MapDelegateVariableContainer;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.workflow.adapter.WorkflowCandidateIdentityAdapter;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.jeecg.modules.workflow.api.constant.WorkflowProcessConstants;
import org.jeecg.modules.workflow.entity.WorkflowUserGroup;
import org.jeecg.modules.workflow.mapper.WorkflowUserGroupMapper;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 与芋道 {@code BpmTaskCandidateInvoker} 对齐的候选人计算入口。
 *
 * <p>流程模型只保存策略编号与字符串参数；宿主用户、部门、岗位数据通过适配器读取。</p>
 */
@Component("workflowCandidateResolver")
public class WorkflowCandidateResolver {

    private static final String FLOWABLE_NAMESPACE = "http://flowable.org/bpmn";
    private static final String CANDIDATE_STRATEGY = WorkflowProcessConstants.EXTENSION_CANDIDATE_STRATEGY;
    private static final String CANDIDATE_PARAM = WorkflowProcessConstants.EXTENSION_CANDIDATE_PARAM;
    private static final String START_USER_ID = "_workflowStartUserId";
    private static final String START_USER_SELECT_ASSIGNEES = "PROCESS_START_USER_SELECT_ASSIGNEES";
    private static final String APPROVE_USER_SELECT_ASSIGNEES = "PROCESS_APPROVE_USER_SELECT_ASSIGNEES";

    private final WorkflowCandidateIdentityAdapter candidateIdentityAdapter;
    private final WorkflowIdentityAdapter identityAdapter;
    private final WorkflowUserGroupMapper workflowUserGroupMapper;
    private final ManagementService managementService;
    private final RepositoryService repositoryService;
    private final ObjectMapper objectMapper;

    public WorkflowCandidateResolver(WorkflowCandidateIdentityAdapter candidateIdentityAdapter,
                                     WorkflowIdentityAdapter identityAdapter,
                                     WorkflowUserGroupMapper workflowUserGroupMapper,
                                     ManagementService managementService,
                                     RepositoryService repositoryService,
                                     ObjectMapper objectMapper) {
        this.candidateIdentityAdapter = candidateIdentityAdapter;
        this.identityAdapter = identityAdapter;
        this.workflowUserGroupMapper = workflowUserGroupMapper;
        this.managementService = managementService;
        this.repositoryService = repositoryService;
        this.objectMapper = objectMapper;
    }

    /** Flowable 普通单实例任务使用：从全部候选人中随机选择一个负责人。 */
    public String resolveOne(DelegateExecution execution) {
        if (!(execution.getCurrentFlowElement() instanceof UserTask task)) {
            return null;
        }
        Integer strategy = candidateStrategy(task);
        String startUserId = text(execution.getVariable(START_USER_ID));
        Set<String> userIds = resolve(task, strategy, startUserId, execution.getVariables(),
                execution.getTenantId(), execution.getProcessDefinitionId(), execution);
        if (userIds.isEmpty()) {
            return null;
        }
        int index = ThreadLocalRandom.current().nextInt(userIds.size());
        return new ArrayList<>(userIds).get(index);
    }

    /** Flowable 多实例任务使用：每次进入节点都重新计算候选人，空集合保留一个空任务。 */
    public List<String> resolveAll(DelegateExecution execution) {
        if (!(execution.getCurrentFlowElement() instanceof UserTask task)) {
            return Collections.singletonList(null);
        }
        Integer strategy = candidateStrategy(task);
        String startUserId = text(execution.getVariable(START_USER_ID));
        List<String> userIds = new ArrayList<>(resolve(task, strategy, startUserId, execution.getVariables(),
                execution.getTenantId(), execution.getProcessDefinitionId(), execution));
        if (userIds.isEmpty()) {
            userIds.add(null);
        }
        return userIds;
    }

    /**
     * 抄送节点使用：按当前 ServiceTask 的候选人扩展配置计算抄送人。
     * 与审批任务不同，抄送没有空候选人自动处理策略；计算为空时直接跳过。
     */
    public Set<String> resolveCopyUsers(FlowElement element, DelegateExecution execution) {
        Integer strategy = candidateStrategy(element);
        if (strategy == null) {
            return Collections.emptySet();
        }
        String rawParam = extensionValue(element, CANDIDATE_PARAM);
        List<String> params = splitComma(rawParam);
        String startUserId = text(execution == null ? null : execution.getVariable(START_USER_ID));
        Map<String, Object> variables = execution == null ? Collections.emptyMap() : execution.getVariables();
        Collection<String> userIds;
        switch (strategy) {
            case 10 -> userIds = candidateIdentityAdapter.userIdsByRoleCodes(params);
            case 20 -> userIds = candidateIdentityAdapter.userIdsByDepartmentIds(params);
            case 21 -> userIds = departmentLeaderUserIds(params);
            case 22 -> userIds = candidateIdentityAdapter.userIdsByPositionIds(params);
            case 23 -> userIds = configuredContinuousDepartmentLeaderUserIds(rawParam);
            case 30 -> userIds = params;
            case 34 -> userIds = selectedUserIds(variables, APPROVE_USER_SELECT_ASSIGNEES, element.getId());
            case 35 -> userIds = selectedUserIds(variables, START_USER_SELECT_ASSIGNEES, element.getId());
            case 32, 33, 36 -> userIds = startUserId == null ? Collections.emptyList() : List.of(startUserId);
            case 37 -> userIds = startUserDepartmentLeaderUserIds(startUserId, rawParam, false);
            case 38 -> userIds = startUserDepartmentLeaderUserIds(startUserId, rawParam, true);
            case 40 -> userIds = userGroupUserIds(params, execution == null ? identityAdapter.currentTenantId()
                    : execution.getTenantId());
            case 50 -> userIds = normalizeIds(variable(variables, rawParam));
            case 51 -> userIds = formDepartmentLeaderUserIds(rawParam, variables);
            case 60 -> userIds = execution == null ? Collections.emptyList()
                    : normalizeIds(evaluateExpression(rawParam, execution));
            default -> throw new JeecgBootException("不支持的候选人策略：" + strategy);
        }
        return new LinkedHashSet<>(normalizeActiveUserIds(userIds));
    }

    /** 预测页面、多实例集合初始化等非运行命令场景使用。 */
    public Set<String> resolve(UserTask task, Integer strategy, String startUserId,
                               Map<String, Object> variables) {
        return resolve(task, strategy, startUserId, variables, null);
    }

    /** 运行前候选人初始化使用；流程定义编号用于解析“转交流程管理员”。 */
    public Set<String> resolve(UserTask task, Integer strategy, String startUserId,
                               Map<String, Object> variables, String processDefinitionId) {
        ProcessDefinition definition = processDefinitionId == null ? null
                : repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
        String tenantId = definition == null ? identityAdapter.currentTenantId() : definition.getTenantId();
        return resolve(task, strategy, startUserId, variables, tenantId, processDefinitionId, null);
    }

    private Set<String> resolve(UserTask task, Integer strategy, String startUserId,
                                Map<String, Object> variables, String tenantId, String processDefinitionId,
                                DelegateExecution execution) {
        if (strategy == null) {
            return Collections.emptySet();
        }
        String rawParam = extensionValue(task, CANDIDATE_PARAM);
        List<String> params = splitComma(rawParam);
        Collection<String> userIds;
        switch (strategy) {
            case 10 -> userIds = candidateIdentityAdapter.userIdsByRoleCodes(
                    params.isEmpty() ? task.getCandidateGroups() : params);
            case 20 -> userIds = candidateIdentityAdapter.userIdsByDepartmentIds(params);
            case 21 -> userIds = departmentLeaderUserIds(params);
            case 22 -> userIds = candidateIdentityAdapter.userIdsByPositionIds(params);
            case 23 -> userIds = configuredContinuousDepartmentLeaderUserIds(rawParam);
            case 30 -> userIds = params.isEmpty() ? task.getCandidateUsers() : params;
            case 34 -> userIds = selectedUserIds(variables, APPROVE_USER_SELECT_ASSIGNEES, task.getId());
            case 35 -> userIds = selectedUserIds(variables, START_USER_SELECT_ASSIGNEES, task.getId());
            case 32, 33, 36 -> userIds = startUserId == null ? Collections.emptyList() : List.of(startUserId);
            case 37 -> userIds = startUserDepartmentLeaderUserIds(startUserId, rawParam, false);
            case 38 -> userIds = startUserDepartmentLeaderUserIds(startUserId, rawParam, true);
            case 40 -> userIds = userGroupUserIds(params, tenantId);
            case 50 -> userIds = normalizeIds(variable(variables, rawParam));
            case 51 -> userIds = formDepartmentLeaderUserIds(rawParam, variables);
            case 60 -> userIds = normalizeIds(execution == null
                    ? evaluateExpression(rawParam, variables) : evaluateExpression(rawParam, execution));
            default -> throw new JeecgBootException("不支持的候选人策略：" + strategy);
        }
        Set<String> result = new LinkedHashSet<>(normalizeActiveUserIds(userIds));
        if (result.isEmpty()) {
            result.addAll(emptyFallbackUserIds(task, processDefinitionId, tenantId));
        }
        if (result.size() > 1
                && extensionInteger(task, WorkflowProcessConstants.EXTENSION_ASSIGN_START_USER_HANDLER_TYPE)
                == WorkflowProcessConstants.ASSIGN_START_USER_SKIP) {
            result.remove(startUserId);
        }
        return result;
    }

    public Integer candidateStrategy(FlowElement element) {
        String value = element.getAttributeValue(FLOWABLE_NAMESPACE, CANDIDATE_STRATEGY);
        if (value == null) {
            value = extensionValue(element, CANDIDATE_STRATEGY);
        }
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new JeecgBootException("候选人策略格式不正确：" + value);
        }
    }

    private Set<String> emptyFallbackUserIds(UserTask task, String processDefinitionId, String tenantId) {
        int handlerType = extensionInteger(task,
                WorkflowProcessConstants.EXTENSION_ASSIGN_EMPTY_HANDLER_TYPE);
        if (handlerType == WorkflowProcessConstants.ASSIGN_EMPTY_USER) {
            return normalizeIds(extensionValue(task,
                    WorkflowProcessConstants.EXTENSION_ASSIGN_EMPTY_USER_IDS));
        }
        if (handlerType == WorkflowProcessConstants.ASSIGN_EMPTY_MANAGER) {
            return managerUserIds(processDefinitionId, tenantId);
        }
        return Collections.emptySet();
    }

    private Set<String> managerUserIds(String processDefinitionId, String tenantId) {
        if (processDefinitionId == null || processDefinitionId.isBlank()) {
            return Collections.emptySet();
        }
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
        if (definition == null) {
            return Collections.emptySet();
        }
        Model model = repositoryService.createModelQuery().modelTenantId(normalizeTenantId(tenantId)).list().stream()
                .filter(item -> Objects.equals(item.getDeploymentId(), definition.getDeploymentId()))
                .findFirst().orElse(null);
        if (model == null || model.getMetaInfo() == null || model.getMetaInfo().isBlank()) {
            return Collections.emptySet();
        }
        try {
            Map<String, Object> meta = objectMapper.readValue(model.getMetaInfo(),
                    new TypeReference<Map<String, Object>>() { });
            return normalizeIds(meta.get("managerUserIds"));
        } catch (Exception exception) {
            throw new JeecgBootException("流程管理员配置损坏", exception);
        }
    }

    private Collection<String> configuredContinuousDepartmentLeaderUserIds(String rawParam) {
        ParameterWithLevel parameter = parameterWithLevel(rawParam, "连续多级部门负责人");
        return continuousDepartmentLeaderUserIds(splitComma(parameter.value()), parameter.level());
    }

    private Collection<String> startUserDepartmentLeaderUserIds(String startUserId, String rawLevel,
                                                                 boolean continuous) {
        if (startUserId == null || startUserId.isBlank()) {
            return Collections.emptyList();
        }
        int level = positiveLevel(rawLevel, continuous ? "发起人连续多级部门负责人" : "发起人部门负责人");
        String departmentId = candidateIdentityAdapter.primaryDepartmentIdByUserId(startUserId);
        if (departmentId == null || departmentId.isBlank()) {
            return Collections.emptyList();
        }
        return continuous ? continuousDepartmentLeaderUserIds(List.of(departmentId), level)
                : exactLevelDepartmentLeaderUserIds(departmentId, level);
    }

    private Collection<String> formDepartmentLeaderUserIds(String rawParam, Map<String, Object> variables) {
        ParameterWithLevel parameter = parameterWithLevel(rawParam, "表单内部门负责人");
        List<String> departmentIds = new ArrayList<>(normalizeIds(variable(variables, parameter.value())));
        return continuousDepartmentLeaderUserIds(departmentIds, parameter.level());
    }

    private Collection<String> exactLevelDepartmentLeaderUserIds(String departmentId, int level) {
        String currentDepartmentId = departmentId;
        for (int index = 1; index < level; index++) {
            Set<String> parentIds = normalizedDepartmentIds(candidateIdentityAdapter
                    .parentDepartmentIds(Set.of(currentDepartmentId)));
            if (parentIds.isEmpty()) {
                break;
            }
            currentDepartmentId = parentIds.iterator().next();
        }
        return candidateIdentityAdapter.leaderUserIdsByDepartmentId(currentDepartmentId);
    }

    private Collection<String> continuousDepartmentLeaderUserIds(List<String> departmentIds, int level) {
        Set<String> result = new LinkedHashSet<>();
        for (String departmentId : departmentIds) {
            if (departmentId == null || departmentId.isBlank()) {
                continue;
            }
            String currentDepartmentId = departmentId;
            for (int index = 0; index < level; index++) {
                result.addAll(candidateIdentityAdapter.leaderUserIdsByDepartmentId(currentDepartmentId));
                Set<String> parentIds = normalizedDepartmentIds(candidateIdentityAdapter
                        .parentDepartmentIds(Set.of(currentDepartmentId)));
                if (parentIds.isEmpty()) {
                    break;
                }
                currentDepartmentId = parentIds.iterator().next();
            }
        }
        return result;
    }

    private Collection<String> departmentLeaderUserIds(List<String> departmentIds) {
        Set<String> result = new LinkedHashSet<>();
        departmentIds.forEach(departmentId -> result.addAll(
                candidateIdentityAdapter.leaderUserIdsByDepartmentId(departmentId)));
        return result;
    }

    private Collection<String> userGroupUserIds(List<String> groupIds, String tenantId) {
        List<Long> ids = groupIds.stream().map(this::parseLong).filter(Objects::nonNull).toList();
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        return workflowUserGroupMapper.selectList(new LambdaQueryWrapper<WorkflowUserGroup>()
                        .select(WorkflowUserGroup::getUserIds)
                        .in(WorkflowUserGroup::getId, ids)
                        .eq(WorkflowUserGroup::getTenantId, normalizeTenantId(tenantId))
                        .eq(WorkflowUserGroup::getStatus, 0))
                .stream().map(WorkflowUserGroup::getUserIds).filter(Objects::nonNull)
                .flatMap(Collection::stream).toList();
    }

    private Collection<String> selectedUserIds(Map<String, Object> variables, String variableName,
                                               String activityId) {
        Object raw = variables == null ? null : variables.get(variableName);
        Map<String, Object> selections = toObjectMap(raw);
        return normalizeIds(selections.get(activityId));
    }

    private Object evaluateExpression(String expressionText, DelegateExecution execution) {
        if (expressionText == null || expressionText.isBlank()) {
            return null;
        }
        Expression expression = CommandContextUtil.getProcessEngineConfiguration()
                .getExpressionManager().createExpression(expressionText);
        return expression.getValue(execution);
    }

    private Object evaluateExpression(String expressionText, Map<String, Object> variables) {
        if (expressionText == null || expressionText.isBlank()) {
            return null;
        }
        try {
            VariableContainer container = new MapDelegateVariableContainer(
                    variables == null ? Collections.emptyMap() : variables, VariableContainer.empty());
            return managementService.executeCommand(context -> CommandContextUtil.getProcessEngineConfiguration()
                    .getExpressionManager().createExpression(expressionText).getValue(container));
        } catch (RuntimeException ignored) {
            // 未运行节点可能引用 execution 或尚未产生的变量，预测时返回空而不阻断页面。
            return null;
        }
    }

    private Set<String> normalizeActiveUserIds(Collection<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptySet();
        }
        return userIds.stream().filter(Objects::nonNull).map(String::trim)
                .filter(id -> !id.isBlank()).distinct()
                .filter(candidateIdentityAdapter::isActiveUser)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> normalizeIds(Object value) {
        Set<String> result = new LinkedHashSet<>();
        appendIds(result, value);
        return result;
    }

    private void appendIds(Set<String> result, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof Collection<?> collection) {
            collection.forEach(item -> appendIds(result, item));
            return;
        }
        if (value.getClass().isArray()) {
            for (int index = 0; index < Array.getLength(value); index++) {
                appendIds(result, Array.get(value, index));
            }
            return;
        }
        if (value instanceof Map<?, ?> map) {
            Object id = map.containsKey("id") ? map.get("id") : map.get("value");
            appendIds(result, id);
            return;
        }
        String text = String.valueOf(value).trim();
        if (text.startsWith("[") && text.endsWith("]")) {
            try {
                appendIds(result, objectMapper.readValue(text, new TypeReference<List<Object>>() { }));
                return;
            } catch (Exception ignored) {
                // 非 JSON 字符串继续按逗号分隔。
            }
        }
        splitComma(text).forEach(result::add);
    }

    private Map<String, Object> toObjectMap(Object value) {
        if (value instanceof Map<?, ?> source) {
            Map<String, Object> result = new LinkedHashMap<>();
            source.forEach((key, item) -> {
                if (key != null) {
                    result.put(String.valueOf(key), item);
                }
            });
            return result;
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return objectMapper.readValue(text, new TypeReference<Map<String, Object>>() { });
            } catch (Exception ignored) {
                return Collections.emptyMap();
            }
        }
        return Collections.emptyMap();
    }

    private Object variable(Map<String, Object> variables, String name) {
        return variables == null || name == null ? null : variables.get(name.trim());
    }

    private ParameterWithLevel parameterWithLevel(String rawParam, String label) {
        if (rawParam == null) {
            throw new JeecgBootException(label + "参数不能为空");
        }
        String[] parts = rawParam.split("\\|", -1);
        if (parts.length != 2 || parts[0].isBlank()) {
            throw new JeecgBootException(label + "参数格式应为“参数|层级”");
        }
        return new ParameterWithLevel(parts[0].trim(), positiveLevel(parts[1], label));
    }

    private int positiveLevel(String value, String label) {
        try {
            int level = Integer.parseInt(value == null ? "" : value.trim());
            if (level > 0) {
                return level;
            }
        } catch (NumberFormatException ignored) {
            // 统一抛出业务错误。
        }
        throw new JeecgBootException(label + "的部门层级必须大于 0");
    }

    private Set<String> normalizedDepartmentIds(Collection<String> values) {
        if (values == null) {
            return Collections.emptySet();
        }
        return values.stream().filter(Objects::nonNull).map(String::trim)
                .filter(value -> !value.isBlank() && !"0".equals(value))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private List<String> splitComma(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return java.util.Arrays.stream(value.split(",")).map(String::trim)
                .filter(item -> !item.isBlank()).toList();
    }

    private String extensionValue(FlowElement element, String name) {
        if (element.getExtensionElements() == null) {
            return null;
        }
        List<ExtensionElement> elements = element.getExtensionElements().get(name);
        return elements == null || elements.isEmpty() ? null : elements.get(0).getElementText();
    }

    private int extensionInteger(FlowElement element, String name) {
        String value = extensionValue(element, name);
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new JeecgBootException("流程节点扩展配置格式不正确：" + name, exception);
        }
    }

    private Long parseLong(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String normalizeTenantId(String tenantId) {
        return tenantId == null || tenantId.isBlank() ? "0" : tenantId;
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private record ParameterWithLevel(String value, int level) {
    }
}
