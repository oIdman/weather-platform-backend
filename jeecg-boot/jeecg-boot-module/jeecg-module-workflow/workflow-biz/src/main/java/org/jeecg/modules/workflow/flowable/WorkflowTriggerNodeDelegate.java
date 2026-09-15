package org.jeecg.modules.workflow.flowable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.jeecg.common.constant.CommonConstant;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 简易设计器“触发器节点”的运行时处理。
 *
 * <p>类型 1：同步发送 HTTP 请求；类型 2：发送请求后等待附加 ReceiveTask
 * 的回调；类型 10/11：按条件更新/删除流程表单变量。</p>
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
            case 1 -> executeHttpRequest(execution, paramText, true);
            case 2 -> executeHttpRequest(execution, paramText, false);
            case 10 -> updateFormVariables(execution, paramText);
            case 11 -> deleteFormVariables(execution, paramText);
            default -> throw new JeecgBootException("不支持的触发器类型：" + type);
        }
    }

    private void executeHttpRequest(DelegateExecution execution, String paramText, boolean handleResponse) {
        if (paramText == null || paramText.isBlank()) {
            return;
        }
        JsonNode root = readParam(paramText);
        // The current converter stores a wrapper object, while Yudao's
        // published simple-model XML stores the HTTP setting directly. Accept
        // both shapes so imported definitions retain their runtime behavior.
        JsonNode setting = childOrSelf(root, "httpRequestSetting");
        String url = setting.path("url").asText(null);
        if (url == null || url.isBlank()) {
            return;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String tenantId = execution.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            Object variableTenantId = execution.getVariable("_workflowTenantId");
            tenantId = variableTenantId == null ? null : String.valueOf(variableTenantId);
        }
        if (tenantId != null && !tenantId.isBlank()) {
            headers.add(CommonConstant.TENANT_ID, tenantId);
        }
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
            var response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
            if (handleResponse) {
                applyResponseVariables(execution.getProcessInstanceId(), response, setting.path("response"));
            }
        } catch (RestClientException exception) {
            throw new JeecgBootException("HTTP 触发器调用失败：" + safeUrl(url), exception);
        }
    }

    /** Maps configured response fields back to process variables for synchronous HTTP triggers. */
    private void applyResponseVariables(String processInstanceId,
                                        org.springframework.http.ResponseEntity<String> response,
                                        JsonNode responseSettings) {
        if (response == null || !response.getStatusCode().is2xxSuccessful()
                || response.getBody() == null || !responseSettings.isArray()) {
            return;
        }
        JsonNode payload;
        try {
            payload = objectMapper.readTree(response.getBody());
        } catch (Exception ignored) {
            return;
        }
        if (payload == null || payload.isNull()) {
            return;
        }
        JsonNode source = payload.has("data") ? payload.get("data")
                : (payload.has("result") ? payload.get("result") : payload);
        Map<String, Object> updates = new LinkedHashMap<>();
        responseSettings.forEach(setting -> {
            String target = setting.path("key").asText(null);
            String sourcePath = setting.path("value").asText(null);
            if (!isFormVariable(target) || sourcePath == null || sourcePath.isBlank()) {
                return;
            }
            JsonNode value = source;
            for (String part : sourcePath.split("\\.")) {
                if (value == null || value.isMissingNode()) {
                    return;
                }
                value = value.get(part);
            }
            if (value != null && !value.isMissingNode() && !value.isNull()) {
                updates.put(target, value(value));
            }
        });
        if (!updates.isEmpty()) {
            runtimeService.setVariables(processInstanceId, updates);
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

    private void updateFormVariables(DelegateExecution execution, String paramText) {
        if (paramText == null || paramText.isBlank()) {
            return;
        }
        JsonNode root = readParam(paramText);
        Map<String, Object> variables = runtimeService.getVariables(execution.getProcessInstanceId());
        Map<String, Object> updates = new LinkedHashMap<>();
        for (JsonNode setting : formSettings(root)) {
            if (matchesCondition(setting, execution, variables)) {
                collectUpdateFields(setting.path("updateFormFields"), updates);
            }
        }
        if (!updates.isEmpty()) {
            runtimeService.setVariables(execution.getProcessInstanceId(), updates);
        }
    }

    private void collectUpdateFields(JsonNode node, Map<String, Object> updates) {
        if (node == null || node.isMissingNode()) {
            return;
        }
        if (node.isObject()) {
            node.fields().forEachRemaining(entry -> {
                if (isFormVariable(entry.getKey())) {
                    updates.put(entry.getKey(), value(entry.getValue()));
                }
            });
        }
    }

    private void deleteFormVariables(DelegateExecution execution, String paramText) {
        if (paramText == null || paramText.isBlank()) {
            return;
        }
        JsonNode root = readParam(paramText);
        Map<String, Object> variables = runtimeService.getVariables(execution.getProcessInstanceId());
        List<String> deleteFields = new ArrayList<>();
        for (JsonNode setting : formSettings(root)) {
            if (matchesCondition(setting, execution, variables)) {
                collectDeleteFields(setting.path("deleteFields"), deleteFields);
            }
        }
        if (!deleteFields.isEmpty()) {
            runtimeService.removeVariables(execution.getProcessInstanceId(), deleteFields.stream().distinct().toList());
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

    /** Evaluates the same expression/rule condition shape used by the target simple designer. */
    private boolean matchesCondition(JsonNode setting, DelegateExecution execution,
                                     Map<String, Object> variables) {
        if (setting == null || setting.isMissingNode()) {
            return true;
        }
        JsonNode conditionType = setting.get("conditionType");
        if (conditionType == null || conditionType.isNull()) {
            return true;
        }
        if (conditionType.asInt() == 1) {
            String expression = setting.path("conditionExpression").asText(null);
            if (expression == null || expression.isBlank()) {
                return false;
            }
            try {
                String normalized = expression.startsWith("${") || expression.startsWith("#{")
                        ? expression : "${" + expression + "}";
                Expression valueExpression = CommandContextUtil.getProcessEngineConfiguration()
                        .getExpressionManager().createExpression(normalized);
                return Boolean.TRUE.equals(valueExpression.getValue(execution));
            } catch (RuntimeException ignored) {
                return false;
            }
        }
        if (conditionType.asInt() != 2) {
            return false;
        }
        JsonNode groups = setting.path("conditionGroups");
        JsonNode conditions = groups.path("conditions");
        if (!conditions.isArray() || conditions.size() == 0) {
            return false;
        }
        boolean groupAnd = !groups.has("and") || groups.path("and").asBoolean(true);
        boolean result = groupAnd;
        for (JsonNode group : conditions) {
            boolean groupResult = evaluateRuleGroup(group.path("rules"), variables,
                    !group.has("and") || group.path("and").asBoolean(true));
            result = groupAnd ? result && groupResult : result || groupResult;
        }
        return result;
    }

    private boolean evaluateRuleGroup(JsonNode rules, Map<String, Object> variables, boolean and) {
        if (!rules.isArray() || rules.size() == 0) {
            return false;
        }
        boolean result = and;
        for (JsonNode rule : rules) {
            boolean matched = evaluateRule(rule, variables);
            result = and ? result && matched : result || matched;
        }
        return result;
    }

    private boolean evaluateRule(JsonNode rule, Map<String, Object> variables) {
        String leftName = rule.path("leftSide").asText(null);
        String operator = rule.path("opCode").asText("==");
        String rightText = rule.path("rightSide").asText(null);
        if (leftName == null || leftName.isBlank() || rightText == null) {
            return false;
        }
        Object left = variables.get(leftName);
        Object right = parseComparisonValue(rightText, left);
        return switch (operator.toLowerCase(java.util.Locale.ROOT)) {
            case "==" -> java.util.Objects.equals(normalizeComparisonValue(left), normalizeComparisonValue(right));
            case "!=" -> !java.util.Objects.equals(normalizeComparisonValue(left), normalizeComparisonValue(right));
            case ">", ">=", "<", "<=" -> compare(left, right, operator);
            case "contain" -> contains(left, right, false);
            case "!contain" -> contains(left, right, true);
            default -> false;
        };
    }

    private Object parseComparisonValue(String value, Object left) {
        String trimmed = value.trim();
        if (left instanceof Number) {
            try {
                return new java.math.BigDecimal(trimmed);
            } catch (NumberFormatException ignored) {
                return trimmed;
            }
        }
        if ("true".equalsIgnoreCase(trimmed) || "false".equalsIgnoreCase(trimmed)) {
            return Boolean.valueOf(trimmed);
        }
        return trimmed;
    }

    private Object normalizeComparisonValue(Object value) {
        if (value instanceof Number number) {
            return new java.math.BigDecimal(number.toString());
        }
        return value == null ? null : String.valueOf(value);
    }

    private boolean compare(Object left, Object right, String operator) {
        if (!(left instanceof Number) || !(right instanceof Number)) {
            return false;
        }
        int compared = new java.math.BigDecimal(left.toString()).compareTo(new java.math.BigDecimal(right.toString()));
        return switch (operator) {
            case ">" -> compared > 0;
            case ">=" -> compared >= 0;
            case "<" -> compared < 0;
            case "<=" -> compared <= 0;
            default -> false;
        };
    }

    private boolean contains(Object left, Object right, boolean negate) {
        boolean result;
        if (left instanceof java.util.Collection<?> collection) {
            result = collection.contains(right) || collection.stream().anyMatch(item ->
                    String.valueOf(item).equals(String.valueOf(right)));
        } else if (left != null && left.getClass().isArray()) {
            result = false;
            int length = java.lang.reflect.Array.getLength(left);
            for (int index = 0; index < length; index++) {
                if (String.valueOf(java.lang.reflect.Array.get(left, index)).equals(String.valueOf(right))) {
                    result = true;
                    break;
                }
            }
        } else {
            result = left != null && String.valueOf(left).contains(String.valueOf(right));
        }
        return negate ? !result : result;
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

    private JsonNode childOrSelf(JsonNode root, String childName) {
        if (root != null && root.isObject()) {
            JsonNode child = root.get(childName);
            if (child != null && !child.isMissingNode() && !child.isNull()) {
                return child;
            }
        }
        return root;
    }

    private List<JsonNode> formSettings(JsonNode root) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            return Collections.emptyList();
        }
        if (root.isArray()) {
            List<JsonNode> result = new ArrayList<>();
            root.forEach(result::add);
            return result;
        }
        JsonNode nested = root.path("formSettings");
        if (nested.isArray()) {
            List<JsonNode> result = new ArrayList<>();
            nested.forEach(result::add);
            return result;
        }
        return List.of(root);
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
