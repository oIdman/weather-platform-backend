package org.jeecg.modules.workflow.starter;

import org.flowable.engine.ProcessEngine;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.modules.workflow.adapter.WorkflowCandidateIdentityAdapter;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.jeecg.modules.workflow.service.WorkflowEngineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = WorkflowStandaloneApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:workflow_starter;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "flowable.database-schema-update=true",
                "flowable.async-executor-activate=false",
                "flowable.check-process-definitions=false"
        })
class WorkflowStandaloneApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private WorkflowEngineService workflowEngineService;

    @Autowired
    private WorkflowIdentityAdapter workflowIdentityAdapter;

    @Autowired
    private WorkflowCandidateIdentityAdapter workflowCandidateIdentityAdapter;

    @Autowired
    private ISysBaseAPI sysBaseApi;

    @Test
    void startsIndependentWorkflowContextWithDefaultAdapters() {
        assertThat(applicationContext).isNotNull();
        assertThat(processEngine).isNotNull();
        assertThat(workflowEngineService).isNotNull();
        assertThat(workflowIdentityAdapter).isInstanceOf(StandaloneWorkflowIdentityAdapter.class);
        assertThat(workflowCandidateIdentityAdapter).isSameAs(workflowIdentityAdapter);
        assertThat(sysBaseApi.getUserById("workflow-system"))
                .extracting("id", "username")
                .containsExactly("workflow-system", "workflow-system");
    }
}
