package org.jeecg.modules.workflow.flowable;

import org.jeecg.common.exception.JeecgBootException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkflowBpmnValidatorTest {

    private final WorkflowBpmnValidator validator = new WorkflowBpmnValidator();

    @Test
    void acceptsSingleStartAndSingleEndProcess() {
        assertEquals("portableApproval", validator.validate(validProcess()).getMainProcess().getId());
    }

    @Test
    void rejectsMultipleEndEvents() {
        String xml = validProcess().replace(
                "<endEvent id=\"end\" name=\"结束\" />",
                "<endEvent id=\"end\" name=\"结束\" /><endEvent id=\"otherEnd\" name=\"另一个结束\" />");
        assertThrows(JeecgBootException.class, () -> validator.validate(xml));
    }

    @Test
    void rejectsExternalEntityDeclarations() {
        String xml = validProcess().replace("<definitions", "<!DOCTYPE definitions [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]><definitions");
        assertThrows(JeecgBootException.class, () -> validator.validate(xml));
    }

    private String validProcess() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                             xmlns:flowable="http://flowable.org/bpmn"
                             targetNamespace="http://jeecg.org/workflow">
                  <process id="portableApproval" name="可移植审批" isExecutable="true">
                    <startEvent id="start" name="开始" />
                    <userTask id="approve" name="审批" flowable:assignee="${assignee}" />
                    <endEvent id="end" name="结束" />
                    <sequenceFlow id="flow1" sourceRef="start" targetRef="approve" />
                    <sequenceFlow id="flow2" sourceRef="approve" targetRef="end" />
                  </process>
                </definitions>
                """;
    }
}
