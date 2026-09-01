package org.jeecg.modules.project.common.config;

import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.common.engine.api.io.InputStreamProvider;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MrpFlowableProcessTest {

    private static final Map<String, String> PROCESSES = Map.of(
            "processes/mrp-hello-world.bpmn20.xml", "mrpHelloWorld",
            "processes/objection-review.bpmn20.xml", "objectionReview",
            "processes/midcheck-review.bpmn20.xml", "midcheckReview",
            "processes/change-approval.bpmn20.xml", "changeApproval",
            "processes/acceptance-review.bpmn20.xml", "acceptanceReview"
    );

    @Test
    void migratedProcessesAreValidFlowableModels() throws Exception {
        BpmnXMLConverter converter = new BpmnXMLConverter();

        for (Map.Entry<String, String> process : PROCESSES.entrySet()) {
            InputStreamProvider provider = () -> resource(process.getKey());
            converter.validateModel(provider);
            BpmnModel model = converter.convertToBpmnModel(provider, true, false);

            assertNotNull(model.getMainProcess(), process.getKey());
            assertEquals(process.getValue(), model.getMainProcess().getId(), process.getKey());
        }
    }

    private InputStream resource(String path) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
        assertNotNull(inputStream, path);
        return inputStream;
    }
}
