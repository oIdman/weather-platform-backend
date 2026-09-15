package org.jeecg.modules.workflow.flowable;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.bpmn.model.ExtensionElement;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowTriggerNodeDelegateTest {

    private static final String FLOWABLE_NAMESPACE = "http://flowable.org/bpmn";

    @Test
    void acceptsYudaoDirectHttpSettingAndMapsResponseVariables() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        DelegateExecution execution = mock(DelegateExecution.class);
        ServiceTask task = task(1, "{\"url\":\"http://localhost/callback\",\"response\":[{\"key\":\"result\",\"value\":\"status\"}]} ");
        when(execution.getCurrentFlowElement()).thenReturn(task);
        when(execution.getProcessInstanceId()).thenReturn("process-1");
        when(execution.getTenantId()).thenReturn("tenant-1");
        when(runtimeService.getVariables("process-1")).thenReturn(Map.of());
        when(restTemplate.exchange(eq("http://localhost/callback"), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"data\":{\"status\":\"ok\"}}"));

        new WorkflowTriggerNodeDelegate(restTemplate, runtimeService, new ObjectMapper()).execute(execution);

        verify(runtimeService).setVariables("process-1", Map.of("result", "ok"));
    }

    @Test
    void acceptsYudaoDirectFormSettingsArray() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        RuntimeService runtimeService = mock(RuntimeService.class);
        DelegateExecution execution = mock(DelegateExecution.class);
        ServiceTask task = task(10, "[{\"updateFormFields\":{\"title\":\"updated\"}}]");
        when(execution.getCurrentFlowElement()).thenReturn(task);
        when(execution.getProcessInstanceId()).thenReturn("process-1");
        when(runtimeService.getVariables("process-1")).thenReturn(Map.of("title", "old"));

        new WorkflowTriggerNodeDelegate(restTemplate, runtimeService, new ObjectMapper()).execute(execution);

        verify(runtimeService).setVariables("process-1", Map.of("title", "updated"));
    }

    private ServiceTask task(int triggerType, String parameter) {
        ServiceTask task = new ServiceTask();
        Map<String, List<ExtensionElement>> extensions = new LinkedHashMap<>();
        extensions.put("triggerType", List.of(extension("triggerType", String.valueOf(triggerType))));
        extensions.put("triggerParam", List.of(extension("triggerParam", parameter.trim())));
        task.setExtensionElements(extensions);
        return task;
    }

    private ExtensionElement extension(String name, String value) {
        ExtensionElement extension = new ExtensionElement();
        extension.setName(name);
        extension.setElementText(value);
        extension.setNamespace(FLOWABLE_NAMESPACE);
        return extension;
    }
}
