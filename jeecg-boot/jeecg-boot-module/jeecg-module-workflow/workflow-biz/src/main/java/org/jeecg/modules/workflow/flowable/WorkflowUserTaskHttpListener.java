package org.jeecg.modules.workflow.flowable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.jeecg.common.exception.JeecgBootException;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

/** Executes the POST callbacks configured on simplified-designer user-task events. */
@Component("workflowUserTaskHttpListener")
@Scope("prototype")
public class WorkflowUserTaskHttpListener implements TaskListener {

    private final RestTemplate restTemplate;
    private final RuntimeService runtimeService;
    private final ObjectMapper objectMapper;

    @Setter
    private Expression listenerConfig;

    public WorkflowUserTaskHttpListener(RestTemplate restTemplate, RuntimeService runtimeService,
                                        ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.runtimeService = runtimeService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void notify(DelegateTask task) {
        JsonNode config = readConfig();
        String path = config.path("path").asText("").trim();
        URI uri = validateUri(path);
        Map<String, Object> variables = runtimeService.getVariables(task.getProcessInstanceId());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        applyHeaders(config.path("header"), headers, variables);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("processInstanceId", task.getProcessInstanceId());
        body.add("taskDefinitionKey", task.getTaskDefinitionKey());
        body.add("taskId", task.getId());
        if (task.getAssignee() != null) {
            body.add("assignee", task.getAssignee());
        }
        applyBody(config.path("body"), body, variables);
        try {
            restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
        } catch (RestClientException exception) {
            throw new JeecgBootException("任务监听器调用失败：" + uri.getScheme() + "://" + uri.getAuthority() + uri.getPath(), exception);
        }
    }

    private JsonNode readConfig() {
        String text = listenerConfig == null ? null : listenerConfig.getExpressionText();
        if (text == null || text.isBlank()) {
            throw new JeecgBootException("任务监听器配置不能为空");
        }
        try {
            return objectMapper.readTree(text);
        } catch (Exception exception) {
            throw new JeecgBootException("任务监听器配置格式不正确", exception);
        }
    }

    private URI validateUri(String path) {
        try {
            URI uri = URI.create(path);
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                    || uri.getHost() == null || uri.getUserInfo() != null) {
                throw new IllegalArgumentException();
            }
            return uri;
        } catch (IllegalArgumentException exception) {
            throw new JeecgBootException("任务监听器仅支持合法的 HTTP/HTTPS 地址");
        }
    }

    private void applyHeaders(JsonNode params, HttpHeaders headers, Map<String, Object> variables) {
        if (!params.isArray()) {
            return;
        }
        params.forEach(param -> {
            String key = param.path("key").asText("").trim();
            String value = resolveValue(param, variables);
            if (!key.isBlank() && value != null && !key.contains("\r") && !key.contains("\n")
                    && !value.contains("\r") && !value.contains("\n")) {
                headers.add(key, value);
            }
        });
    }

    private void applyBody(JsonNode params, MultiValueMap<String, String> body, Map<String, Object> variables) {
        if (!params.isArray()) {
            return;
        }
        params.forEach(param -> {
            String key = param.path("key").asText("").trim();
            String value = resolveValue(param, variables);
            if (!key.isBlank() && value != null) {
                body.add(key, value);
            }
        });
    }

    private String resolveValue(JsonNode param, Map<String, Object> variables) {
        String configured = param.path("value").asText(null);
        if (param.path("type").asInt(1) != 2) {
            return configured;
        }
        Object value = variables.get(configured == null ? "" : configured.replace("${", "").replace("}", ""));
        return value == null ? null : String.valueOf(value);
    }
}
