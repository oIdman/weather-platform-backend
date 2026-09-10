package org.jeecg.modules.workflow.flowable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.workflow.api.constant.WorkflowProcessConstants;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 简易设计器“触发器节点”的运行时处理。
 *
 * <p>类型 1：同步发送 HTTP 请求；类型 10/11：更新/删除流程表单变量。
 * HTTP 回调（类型 2）依赖附加 ReceiveTask 与回调接口，暂由调用方在后续迭代接入。</p>
 */
@Component("workflowTriggerTaskDelegate")
public class WorkflowTriggerNodeDelegate implements JavaDelegate {

    private static final List<String> PROTECTED_VARIABLE_PREFIXES = List.of(
            "PROCESS_", "TASK_", "_workflow"
    );

    private final RestTemplate restTemplate;
    private final RuntimeService runtimeService;
    private final ObjectMapper objectMapper;

    public WorkflowTriggerNodeDelegate(RestTemplate restTemplate, RuntimeService runtimeService,
                                       ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.runtimeService = runtimeService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void execute(DelegateExecution execution) {
        FlowElement flowElement = execution.getCurrentFlowElement();
        int type = extensionInteger(flowElement, WorkflowProcessConstants.EXTENSION_TRIGGER_TYPE);
        String paramText = extensionValue(flowElement, WorkflowProcessConstants.EXTENSION_TRIGGER_PARAM);
        switch (type) {
            case 1 -> executeHttpRequest(execution, paramText);
            case 2 -> executeHttpRequest(execution, paramText);
            case 10 -> updateFormVariables(execution.getProcessInstanceId(), paramText);
            case 11 -> deleteFormVariables(execution.getProcessInstanceId(), paramText);
            default -> throw new JeecgBootException("不支持的触发器类型：" + type);
        }
    }

    private void executeHttpRequest(DelegateExecution execution, String paramText) {
        if (paramText == null || paramText.isBlank()) {
            return;
        }
        JsonNode root = readParam(paramText);
        JsonNode setting = root.path("httpRequestSetting");
        String url = setting.path("url").asText(null);
        if (url == null || url.isBlank()) {
            return;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("processInstanceId", execution.getProcessInstanceId());
        String callbackTaskKey = setting.path("callbackTaskDefineKey").asText(null);
        if (callbackTaskKey != null && !callbackTaskKey.isBlank()) {
            body.add("taskDefineKey", callbackTaskKey);
        }
        Map<String, Object> variables = runtimeService.getVariables(execution.getProcessInstanceId());
        applyRequestParams(setting.path("header"), headers, variables);
        applyRequestParams(setting.path("body"), body, variables);
        try {
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        } catch (RestClientException exception) {
            throw new JeecgBootException("HTTP 触发器调用失败：" + safeUrl(url), exception);
        }
    }

    private void applyRequestParams(JsonNode params, HttpHeaders headers, Map<String, Object> variables) {
        if (params == null || params.isMissingNode() || !params.isArray()) {
            return;
        }
        for (JsonNode param : params) {
            String key = param.path("key").asText(null);
            if (key == null || key.isBlank()) {
                continue;
            }
            String value = resolveRequestParam(param, variables);
            if (value != null) {
                headers.add(key, value);
            }
        }
    }

    private void applyRequestParams(JsonNode params, MultiValueMap<String, String> body,
                                    Map<String, Object> variables) {
        if (params == null || params.isMissingNode() || !params.isArray()) {
            return;
        }
        for (JsonNode param : params) {
            String key = param.path("key").asText(null);
            if (key == null || key.isBlank()) {
                continue;
            }
            String value = resolveRequestParam(param, variables);
            if (value != null) {
                body.add(key, value);
            }
        }
    }

    private String resolveRequestParam(JsonNode param, Map<String, Object> variables) {
        int type = param.path("type").asInt(1);
        String value = param.path("value").asText(null);
        if (type == 2 || (value != null && value.startsWith("${"))) {
            Object variable = variables.get(value == null ? "" : value.replace("${", "").replace("}", ""));
            return variable == null ? null : String.valueOf(variable);
        }
        return value;
    }

    private void updateFormVariables(String processInstanceId, String paramText) {
        if (paramText == null || paramText.isBlank()) {
            return;
        }
        JsonNode root = readParam(paramText);
        Map<String, Object> updates = new LinkedHashMap<>();
        collectUpdateVariables(root.path("formSettings"), updates);
        collectUpdateVariables(root.path("updateFormFields"), updates);
        if (!updates.isEmpty()) {
            runtimeService.setVariables(processInstanceId, updates);
        }
    }

    private void collectUpdateVariables(JsonNode node, Map<String, Object> updates) {
        if (node == null || node.isMissingNode()) {
            return;
        }
        if (node.isArray()) {
            node.forEach(item -> collectUpdateVariables(item, updates));
            return;
        }
        if (node.isObject()) {
            if (node.has("updateFormFields") && node.get("updateFormFields").isObject()) {
                node.get("updateFormFields").fields().forEachRemaining(entry -> {
                    if (isFormVariable(entry.getKey())) {
                        updates.put(entry.getKey(), value(entry.getValue()));
                    }
                });
            } else {
                node.fields().forEachRemaining(entry -> {
                    if (isFormVariable(entry.getKey())) {
                        updates.put(entry.getKey(), value(entry.getValue()));
                    }
                });
            }
        }
    }

    private void deleteFormVariables(String processInstanceId, String paramText) {
        if (paramText == null || paramText.isBlank()) {
            return;
        }
        JsonNode root = readParam(paramText);
        List<String> deleteFields = new ArrayList<>();
        collectDeleteFields(root.path("formSettings"), deleteFields);
        collectDeleteFields(root.path("deleteFields"), deleteFields);
        if (!deleteFields.isEmpty()) {
            runtimeService.removeVariables(processInstanceId, deleteFields);
        }
    }

    private void collectDeleteFields(JsonNode node, List<String> fields) {
        if (node == null || node.isMissingNode()) {
            return;
        }
        if (node.isArray()) {
            node.forEach(item -> {
                if (item.isTextual()) {
                    String field = item.asText();
                    if (isFormVariable(field)) {
                        fields.add(field);
                    }
                } else {
                    collectDeleteFields(item, fields);
                }
            });
            return;
        }
        if (node.isObject()) {
            if (node.has("deleteFields") && node.get("deleteFields").isArray()) {
                collectDeleteFields(node.get("deleteFields"), fields);
            } else {
                node.fields().forEachRemaining(entry -> {
                    if (isFormVariable(entry.getKey())) {
                        fields.add(entry.getKey());
                    }
                });
            }
        }
    }

    private boolean isFormVariable(String name) {
        return name != null && !name.isBlank() && PROTECTED_VARIABLE_PREFIXES.stream().noneMatch(name::startsWith);
    }

    private JsonNode readParam(String paramText) {
        try {
            return objectMapper.readTree(paramText);
        } catch (Exception exception) {
            throw new JeecgBootException("触发器参数格式不正确", exception);
        }
    }

    private Object value(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        if (node.isNumber()) {
            return node.isIntegralNumber() ? node.asLong() : node.asDouble();
        }
        return node.asText();
    }

    private String safeUrl(String url) {
        if (url == null) {
            return "";
        }
        int query = url.indexOf('?');
        return query > 0 ? url.substring(0, query) : url;
    }

    private String extensionValue(FlowElement element, String name) {
        if (element == null || element.getExtensionElements() == null) {
            return null;
        }
        java.util.List<ExtensionElement> values = element.getExtensionElements().get(name);
        if (values == null || values.isEmpty() || values.get(0).getElementText() == null) {
            return null;
        }
        return values.get(0).getElementText().trim();
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
}
