package org.jeecg.modules.workflow.config;

import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registers workflow event listeners with the Flowable process engine. */
@Configuration(proxyBeanMethods = false)
public class WorkflowFlowableConfiguration {

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> workflowProcessEngineConfigurer(
            ObjectProvider<FlowableEventListener> listeners) {
        return configuration -> configuration.setEventListeners(listeners.orderedStream().toList());
    }
}
