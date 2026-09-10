package org.jeecg.modules.workflow.flowable;

import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.EndEvent;
import org.flowable.bpmn.model.StartEvent;
import org.flowable.common.engine.api.io.InputStreamProvider;
import org.jeecg.common.exception.JeecgBootException;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Component
public class WorkflowBpmnValidator {

    public BpmnModel validate(String xml) {
        try {
            if (xml == null || xml.isBlank() || xml.length() > 2_000_000) {
                throw new JeecgBootException("BPMN XML 不能为空且不能超过 2MB");
            }
            String upperXml = xml.toUpperCase();
            if (upperXml.contains("<!DOCTYPE") || upperXml.contains("<!ENTITY")) {
                throw new JeecgBootException("BPMN XML 不允许包含外部实体声明");
            }
            BpmnXMLConverter converter = new BpmnXMLConverter();
            InputStreamProvider provider = () -> new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
            converter.validateModel(provider);
            BpmnModel model = converter.convertToBpmnModel(provider, true, false);
            if (model.getProcesses().size() != 1) {
                throw new JeecgBootException("每个部署文件必须且只能包含一个流程");
            }
            long startCount = model.getMainProcess().findFlowElementsOfType(StartEvent.class).size();
            long endCount = model.getMainProcess().findFlowElementsOfType(EndEvent.class).size();
            if (startCount != 1 || endCount != 1) {
                throw new JeecgBootException("流程必须且只能包含一个开始事件和一个结束事件");
            }
            return model;
        } catch (JeecgBootException e) {
            throw e;
        } catch (Exception e) {
            throw new JeecgBootException("BPMN 校验失败：" + e.getMessage());
        }
    }
}
