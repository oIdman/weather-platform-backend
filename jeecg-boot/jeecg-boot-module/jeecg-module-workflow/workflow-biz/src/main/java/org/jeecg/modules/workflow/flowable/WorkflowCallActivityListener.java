package org.jeecg.modules.workflow.flowable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.bpmn.model.CallActivity;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.jeecg.modules.workflow.api.constant.WorkflowProcessConstants;
import org.jeecg.modules.workflow.api.vo.WorkflowModelVO;
import org.jeecg.modules.workflow.service.WorkflowModelService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Sets the authenticated user used by a called process when a simple-designer
 * child-process node specifies a start-user rule.
 */
@Component("workflowCallActivityListener")
public class WorkflowCallActivityListener implements ExecutionListener {

    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;
    private final WorkflowModelService modelService;
    private final WorkflowIdentityAdapter identityAdapter;
    private final ObjectMapper objectMapper;

    private Expression listenerConfig;

    public WorkflowCallActivityListener(RuntimeService runtimeService,
                                        RepositoryService repositoryService,
                                        WorkflowModelService modelService,
                                        WorkflowIdentityAdapter identityAdapter,
                                        ObjectMapper objectMapper) {
        this.runtimeService = runtimeService;
        this.repositoryService = repositoryService;
        this.modelService = modelService;
        this.identityAdapter = identityAdapter;
        this.objectMapper = objectMapper;
    }

    /** Flowable injects the JSON field extension through this JavaBean setter. */
    public void setListenerConfig(Expression listenerConfig) {
        this.listenerConfig = listenerConfig;
    }

    @Override
    public void notify(DelegateExecution execution) {
        Map<String, Object> setting = readSetting(execution);
        String startUserId = rootStartUserId(execution);
        int type = integer(setting.get("type"), 1);
        if (type == 2) {
            Object formValue = rootVariables(execution).get(text(setting.get("formField")));
            startUserId = firstUserId(formValue);
            if (!StringUtils.hasText(startUserId)) {
                startUserId = fallbackUserId(execution, integer(setting.get("emptyType"), 1));
            }
        }
        if (StringUtils.hasText(startUserId)) {
            Authentication.setAuthenticatedUserId(startUserId);
        }
    }

    private Map<String, Object> readSetting(DelegateExecution execution) {
        if (listenerConfig == null) {
            return Collections.emptyMap();
        }
        String text = listenerConfig.getExpressionText();
        if (!StringUtils.hasText(text)) {
            Object value = listenerConfig.getValue(execution);
            text = value == null ? null : String.valueOf(value);
        }
        if (!StringUtils.hasText(text)) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(text, new TypeReference<Map<String, Object>>() { });
        } catch (Exception exception) {
            throw new JeecgBootException("子流程发起人配置格式不正确", exception);
        }
    }

    private String rootStartUserId(DelegateExecution execution) {
        ProcessInstance root = rootInstance(execution);
        if (root != null && StringUtils.hasText(root.getStartUserId())) {
            return root.getStartUserId();
        }
        Object variable = rootVariables(execution).get("_workflowStartUserId");
        return firstUserId(variable);
    }

    private String fallbackUserId(DelegateExecution execution, int emptyType) {
        if (emptyType == 2) {
            String childManager = firstManagerUserId(childDefinition(execution));
            if (StringUtils.hasText(childManager)) {
                return childManager;
            }
        } else if (emptyType == 3) {
            ProcessInstance root = rootInstance(execution);
            if (root != null) {
                String mainManager = firstManagerUserId(repositoryService.createProcessDefinitionQuery()
                        .processDefinitionId(root.getProcessDefinitionId()).singleResult());
                if (StringUtils.hasText(mainManager)) {
                    return mainManager;
                }
            }
        }
        return rootStartUserId(execution);
    }

    private String firstManagerUserId(ProcessDefinition definition) {
        if (definition == null) {
            return null;
        }
        WorkflowModelVO model = modelService.getByDeploymentId(definition.getDeploymentId());
        List<String> managers = model == null ? null : model.getManagerUserIds();
        return managers == null ? null : managers.stream().filter(StringUtils::hasText).findFirst().orElse(null);
    }

    private ProcessDefinition childDefinition(DelegateExecution execution) {
        if (!(execution.getCurrentFlowElement() instanceof CallActivity activity)
                || !StringUtils.hasText(activity.getCalledElement())) {
            return null;
        }
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(activity.getCalledElement())
                .processDefinitionTenantId(normalizeTenant(execution.getTenantId()))
                .latestVersion().active().singleResult();
    }

    private ProcessInstance rootInstance(DelegateExecution execution) {
        String rootId = execution.getRootProcessInstanceId();
        if (!StringUtils.hasText(rootId)) {
            rootId = execution.getProcessInstanceId();
        }
        return StringUtils.hasText(rootId)
                ? runtimeService.createProcessInstanceQuery().processInstanceId(rootId).singleResult()
                : null;
    }

    private Map<String, Object> rootVariables(DelegateExecution execution) {
        ProcessInstance root = rootInstance(execution);
        return root == null || root.getProcessVariables() == null
                ? Collections.emptyMap() : root.getProcessVariables();
    }

    private String firstUserId(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Iterable<?> values) {
            for (Object item : values) {
                String id = firstUserId(item);
                if (StringUtils.hasText(id)) {
                    return id;
                }
            }
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            Object id = map.containsKey("id") ? map.get("id") : map.get("value");
            return firstUserId(id);
        }
        String text = String.valueOf(value).trim();
        if (text.startsWith("[") && text.endsWith("]")) {
            try {
                return firstUserId(objectMapper.readValue(text, new TypeReference<List<Object>>() { }));
            } catch (Exception ignored) {
                // Fall through to the scalar value for backwards-compatible form data.
            }
        }
        int comma = text.indexOf(',');
        return comma < 0 ? text : text.substring(0, comma).trim();
    }

    private int integer(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException exception) {
            throw new JeecgBootException("子流程发起人配置数值不正确");
        }
    }

    private String text(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private String normalizeTenant(String tenantId) {
        return StringUtils.hasText(tenantId) ? tenantId.trim() : identityAdapter.currentTenantId();
    }
}
