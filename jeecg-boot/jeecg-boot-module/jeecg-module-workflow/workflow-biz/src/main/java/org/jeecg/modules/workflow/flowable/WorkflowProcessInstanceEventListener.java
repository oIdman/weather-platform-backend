package org.jeecg.modules.workflow.flowable;

import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.runtime.ProcessInstance;
import org.jeecg.modules.workflow.api.constant.WorkflowProcessConstants;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/**
 * Completes the Yudao-compatible process status lifecycle for naturally completed instances.
 */
@Component
public class WorkflowProcessInstanceEventListener extends AbstractFlowableEngineEventListener {

    private static final Set<FlowableEngineEventType> EVENTS = Set.of(FlowableEngineEventType.PROCESS_COMPLETED);

    private final ObjectProvider<RuntimeService> runtimeServiceProvider;

    public WorkflowProcessInstanceEventListener(ObjectProvider<RuntimeService> runtimeServiceProvider) {
        super(EVENTS);
        this.runtimeServiceProvider = runtimeServiceProvider;
    }

    @Override
    protected void processCompleted(FlowableEngineEntityEvent event) {
        ProcessInstance instance = (ProcessInstance) event.getEntity();
        Map<String, Object> variables = instance.getProcessVariables();
        Object status = variables == null ? null : variables.get(WorkflowProcessConstants.VARIABLE_STATUS);
        if (isRunning(status)) {
            runtimeServiceProvider.getObject().setVariable(instance.getId(), WorkflowProcessConstants.VARIABLE_STATUS,
                    WorkflowProcessConstants.STATUS_APPROVED);
        }
    }

    private boolean isRunning(Object status) {
        if (status instanceof Number number) {
            return number.intValue() == WorkflowProcessConstants.STATUS_RUNNING;
        }
        return status != null && String.valueOf(WorkflowProcessConstants.STATUS_RUNNING).equals(String.valueOf(status));
    }
}
