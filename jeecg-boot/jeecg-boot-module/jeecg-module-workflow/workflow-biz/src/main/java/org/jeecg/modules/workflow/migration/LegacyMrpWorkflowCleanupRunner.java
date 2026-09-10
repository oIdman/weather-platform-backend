package org.jeecg.modules.workflow.migration;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Removes only deployments produced by the retired project-module auto-deployment runner.
 * Deployments containing any non-MRP process or with running instances are never touched.
 */
@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class LegacyMrpWorkflowCleanupRunner implements ApplicationRunner {

    private static final List<String> LEGACY_KEYS = List.of(
            "mrpHelloWorld", "objectionReview", "midcheckReview", "changeApproval", "acceptanceReview");

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;

    public LegacyMrpWorkflowCleanupRunner(RepositoryService repositoryService, RuntimeService runtimeService) {
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (String key : LEGACY_KEYS) {
            cleanup(key);
        }
    }

    private void cleanup(String key) {
        List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key).list();
        for (ProcessDefinition definition : definitions) {
            Deployment deployment = repositoryService.createDeploymentQuery()
                    .deploymentId(definition.getDeploymentId()).singleResult();
            if (!isRetiredMrpDeployment(deployment)) {
                continue;
            }
            long running = runtimeService.createProcessInstanceQuery()
                    .deploymentId(deployment.getId()).count();
            if (running > 0) {
                log.error("[workflow-migration] 旧流程存在运行实例，跳过删除：key={}, deploymentId={}, running={}",
                        key, deployment.getId(), running);
                continue;
            }
            repositoryService.deleteDeployment(deployment.getId(), true);
            log.info("[workflow-migration] 已删除旧 MRP 流程部署：key={}, deploymentId={}", key, deployment.getId());
        }
    }

    private boolean isRetiredMrpDeployment(Deployment deployment) {
        if (deployment == null || !"SpringBootAutoDeployment".equals(deployment.getName())) {
            return false;
        }
        List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId()).list();
        return !definitions.isEmpty()
                && definitions.stream().allMatch(definition -> LEGACY_KEYS.contains(definition.getKey()));
    }
}
