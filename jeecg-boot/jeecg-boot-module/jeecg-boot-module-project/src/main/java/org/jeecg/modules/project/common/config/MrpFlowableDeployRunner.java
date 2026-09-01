package org.jeecg.modules.project.common.config;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @Description: Flowable 流程幂等部署（hello-world 验证 + 立项异议核查等业务流程）
 * @Author: meteo-project
 * @Date: 2026-08-21
 * @Version: V1.0
 */
@Component
@Slf4j
public class MrpFlowableDeployRunner implements CommandLineRunner {

    private static final Map<String, String> PROCESSES = new LinkedHashMap<>();

    static {
        PROCESSES.put("mrpHelloWorld", "processes/mrp-hello-world.bpmn20.xml");
        PROCESSES.put("objectionReview", "processes/objection-review.bpmn20.xml");
        PROCESSES.put("midcheckReview", "processes/midcheck-review.bpmn20.xml");
        PROCESSES.put("changeApproval", "processes/change-approval.bpmn20.xml");
        PROCESSES.put("acceptanceReview", "processes/acceptance-review.bpmn20.xml");
    }

    @Autowired
    private RepositoryService repositoryService;

    @Override
    public void run(String... args) {
        for (Map.Entry<String, String> entry : PROCESSES.entrySet()) {
            String key = entry.getKey();
            String resource = entry.getValue();
            long exists = repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).count();
            if (exists > 0) {
                log.info("[mrp-flowable] 流程已部署，跳过（key={}）", key);
                continue;
            }
            Deployment deployment = repositoryService.createDeployment()
                    .name("MRP " + key)
                    .addClasspathResource(resource)
                    .deploy();
            log.info("[mrp-flowable] 流程部署成功，deploymentId={}，key={}", deployment.getId(), key);
        }
    }
}

