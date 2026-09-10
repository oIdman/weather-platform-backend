package org.jeecg.modules.workflow.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.common.engine.impl.db.SuspensionState;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.Model;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.jeecg.modules.workflow.api.dto.WorkflowModelSaveRequest;
import org.jeecg.modules.workflow.api.vo.WorkflowCategoryVO;
import org.jeecg.modules.workflow.api.vo.WorkflowFormVO;
import org.jeecg.modules.workflow.api.vo.WorkflowModelDefinitionVO;
import org.jeecg.modules.workflow.api.vo.WorkflowModelVO;
import org.jeecg.modules.workflow.flowable.WorkflowBpmnValidator;
import org.jeecg.modules.workflow.flowable.WorkflowSimpleModelConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class WorkflowModelService {
    private static final Pattern XML_NAME_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9._-]*");
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final WorkflowIdentityAdapter identityAdapter;
    private final WorkflowBpmnValidator bpmnValidator;
    private final WorkflowSimpleModelConverter simpleModelConverter;
    private final WorkflowCategoryService categoryService;
    private final WorkflowFormService formService;
    private final ObjectMapper objectMapper;

    public WorkflowModelService(RepositoryService repositoryService,
                                RuntimeService runtimeService,
                                HistoryService historyService,
                                WorkflowIdentityAdapter identityAdapter,
                                WorkflowBpmnValidator bpmnValidator,
                                WorkflowSimpleModelConverter simpleModelConverter,
                                WorkflowCategoryService categoryService,
                                WorkflowFormService formService,
                                ObjectMapper objectMapper) {
        this.repositoryService = repositoryService;
        this.runtimeService = runtimeService;
        this.historyService = historyService;
        this.identityAdapter = identityAdapter;
        this.bpmnValidator = bpmnValidator;
        this.simpleModelConverter = simpleModelConverter;
        this.categoryService = categoryService;
        this.formService = formService;
        this.objectMapper = objectMapper;
    }

    public List<WorkflowModelVO> list(String name) {
        List<Model> models = repositoryService.createModelQuery()
                .modelTenantId(identityAdapter.currentTenantId())
                .list();
        String keyword = StringUtils.hasText(name) ? name.trim().toLowerCase() : null;
        return models.stream()
                .filter(model -> keyword == null || model.getName().toLowerCase().contains(keyword))
                .map(model -> toVO(model, false))
                .sorted(Comparator.comparing(WorkflowModelVO::getSort,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(WorkflowModelVO::getCreateTime,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public WorkflowModelVO get(String id) {
        return toVO(requireModel(id), true);
    }

    public WorkflowModelVO getByDeploymentId(String deploymentId) {
        if (!StringUtils.hasText(deploymentId)) {
            return null;
        }
        Model model = repositoryService.createModelQuery()
                .modelTenantId(identityAdapter.currentTenantId())
                .list().stream()
                .filter(item -> Objects.equals(item.getDeploymentId(), deploymentId))
                .findFirst().orElse(null);
        return model == null ? null : toVO(model, true);
    }

    public WorkflowModelSaveRequest exportModel(String id) {
        WorkflowModelVO model = get(id);
        return objectMapper.convertValue(model, WorkflowModelSaveRequest.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public String create(WorkflowModelSaveRequest request) {
        validateKey(request.getKey());
        if (repositoryService.createModelQuery()
                .modelKey(request.getKey())
                .modelTenantId(identityAdapter.currentTenantId())
                .count() > 0) {
            throw new JeecgBootException("流程标识已存在");
        }
        applyCreateDefaults(request);
        Model model = repositoryService.newModel();
        model.setKey(request.getKey().trim());
        model.setName(request.getName().trim());
        model.setCategory(request.getCategory());
        model.setTenantId(identityAdapter.currentTenantId());
        saveModel(model, request);
        return model.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(WorkflowModelSaveRequest request) {
        if (!StringUtils.hasText(request.getId())) {
            throw new JeecgBootException("流程模型编号不能为空");
        }
        Model model = requireManager(request.getId());
        if (!Objects.equals(model.getKey(), request.getKey())) {
            throw new JeecgBootException("流程标识创建后不能修改");
        }
        request.setSort(request.getSort() == null ? readSort(model) : request.getSort());
        request.setManagerUserIds(normalizeManagers(request.getManagerUserIds()));
        model.setName(request.getName().trim());
        model.setCategory(request.getCategory());
        saveModel(model, request);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSortBatch(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new JeecgBootException("流程模型编号列表不能为空");
        }
        long sort = System.currentTimeMillis();
        for (String id : ids) {
            Model model = requireManager(id);
            Map<String, Object> meta = readMeta(model);
            meta.put("sort", sort++);
            model.setMetaInfo(writeJson(meta));
            repositoryService.saveModel(model);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateBpmn(String id, String bpmnXml) {
        requireManager(id);
        bpmnValidator.validate(bpmnXml);
        repositoryService.addModelEditorSource(id, bpmnXml.getBytes(StandardCharsets.UTF_8));
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSimple(String id, Map<String, Object> simpleModel) {
        Model model = requireManager(id);
        String bpmnXml = simpleModelConverter.convert(model.getKey(), model.getName(), simpleModel);
        bpmnValidator.validate(bpmnXml);
        repositoryService.addModelEditorSource(id, bpmnXml.getBytes(StandardCharsets.UTF_8));
        repositoryService.addModelEditorSourceExtra(id, writeJson(simpleModel).getBytes(StandardCharsets.UTF_8));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deploy(String id) {
        Model model = requireManager(id);
        byte[] bpmnBytes = repositoryService.getModelEditorSource(id);
        if (bpmnBytes == null || bpmnBytes.length == 0) {
            throw new JeecgBootException("请先完成 BPMN 流程设计再发布");
        }
        bpmnValidator.validate(new String(bpmnBytes, StandardCharsets.UTF_8));
        Deployment deployment = repositoryService.createDeployment()
                .name(model.getName())
                .tenantId(identityAdapter.currentTenantId())
                .addBytes(model.getKey() + ".bpmn20.xml", bpmnBytes)
                .deploy();
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();
        if (definition == null) {
            throw new JeecgBootException("流程模型发布后未生成流程定义");
        }
        if (StringUtils.hasText(model.getCategory())) {
            repositoryService.setProcessDefinitionCategory(definition.getId(), model.getCategory());
        }
        suspendDeployment(model.getDeploymentId());
        model.setDeploymentId(deployment.getId());
        repositoryService.saveModel(model);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateState(String id, Integer state) {
        Model model = requireManager(id);
        ProcessDefinition definition = latestDefinition(model);
        if (definition == null) {
            throw new JeecgBootException("流程模型尚未发布");
        }
        if (Objects.equals(state, SuspensionState.ACTIVE.getStateCode())) {
            repositoryService.activateProcessDefinitionById(definition.getId(), true, null);
        } else if (Objects.equals(state, SuspensionState.SUSPENDED.getStateCode())) {
            repositoryService.suspendProcessDefinitionById(definition.getId(), true, null);
        } else {
            throw new JeecgBootException("流程状态只支持激活或挂起");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        Model model = requireManager(id);
        suspendDeployment(model.getDeploymentId());
        repositoryService.deleteModel(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void clean(String id) {
        Model model = requireManager(id);
        String tenantId = identityAdapter.currentTenantId();
        List<ProcessInstance> running = runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(model.getKey())
                .processInstanceTenantId(tenantId)
                .list();
        for (ProcessInstance instance : running) {
            runtimeService.deleteProcessInstance(instance.getId(), "流程模型清理");
        }
        List<HistoricProcessInstance> history = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(model.getKey())
                .processInstanceTenantId(tenantId)
                .list();
        for (HistoricProcessInstance instance : history) {
            historyService.deleteHistoricProcessInstance(instance.getId());
        }
    }

    private void saveModel(Model model, WorkflowModelSaveRequest request) {
        model.setMetaInfo(writeJson(metaFromRequest(request)));
        repositoryService.saveModel(model);
        if (Objects.equals(request.getType(), 20)) {
            Map<String, Object> simpleModel = request.getSimpleModel() == null
                    ? readSimpleModel(model) : request.getSimpleModel();
            if (simpleModel == null || simpleModel.isEmpty()) {
                simpleModel = simpleModelConverter.defaultSimpleModel();
            }
            String bpmnXml = simpleModelConverter.convert(model.getKey(), model.getName(), simpleModel);
            bpmnValidator.validate(bpmnXml);
            repositoryService.addModelEditorSource(model.getId(), bpmnXml.getBytes(StandardCharsets.UTF_8));
            repositoryService.addModelEditorSourceExtra(model.getId(),
                    writeJson(simpleModel).getBytes(StandardCharsets.UTF_8));
        } else if (StringUtils.hasText(request.getBpmnXml())) {
            bpmnValidator.validate(request.getBpmnXml());
            repositoryService.addModelEditorSource(model.getId(),
                    request.getBpmnXml().getBytes(StandardCharsets.UTF_8));
        } else if (repositoryService.getModelEditorSource(model.getId()) == null && Objects.equals(request.getType(), 10)) {
            repositoryService.addModelEditorSource(model.getId(),
                    defaultBpmn(model.getKey(), model.getName()).getBytes(StandardCharsets.UTF_8));
        }
        if (!Objects.equals(request.getType(), 20) && request.getSimpleModel() != null) {
            repositoryService.addModelEditorSourceExtra(model.getId(),
                    writeJson(request.getSimpleModel()).getBytes(StandardCharsets.UTF_8));
        }
    }

    private WorkflowModelVO toVO(Model model, boolean includeSources) {
        WorkflowModelVO vo = new WorkflowModelVO();
        try {
            objectMapper.readerForUpdating(vo).readValue(model.getMetaInfo() == null ? "{}" : model.getMetaInfo());
        } catch (Exception exception) {
            throw new JeecgBootException("流程模型元数据损坏", exception);
        }
        vo.setId(model.getId());
        vo.setKey(model.getKey());
        vo.setName(model.getName());
        vo.setCategory(model.getCategory());
        vo.setCreateTime(model.getCreateTime());
        vo.setUpdateTime(model.getLastUpdateTime());
        enrichNames(vo);
        ProcessDefinition definition = latestDefinition(model);
        if (definition != null) {
            vo.setProcessDefinition(toDefinition(definition, vo));
            vo.setStatus(definition.isSuspended()
                    ? SuspensionState.SUSPENDED.getStateCode()
                    : SuspensionState.ACTIVE.getStateCode());
        }
        if (includeSources) {
            byte[] bpmn = repositoryService.getModelEditorSource(model.getId());
            vo.setBpmnXml(bpmn == null ? null : new String(bpmn, StandardCharsets.UTF_8));
            byte[] simple = repositoryService.getModelEditorSourceExtra(model.getId());
            if (simple != null && simple.length > 0) {
                try {
                    vo.setSimpleModel(objectMapper.readValue(simple, MAP_TYPE));
                } catch (Exception exception) {
                    throw new JeecgBootException("简易流程模型数据损坏", exception);
                }
            }
        }
        return vo;
    }

    private WorkflowModelDefinitionVO toDefinition(ProcessDefinition definition, WorkflowModelVO model) {
        WorkflowModelDefinitionVO vo = new WorkflowModelDefinitionVO();
        vo.setId(definition.getId());
        vo.setKey(definition.getKey());
        vo.setName(definition.getName());
        vo.setVersion(definition.getVersion());
        vo.setSuspensionState(definition.isSuspended()
                ? SuspensionState.SUSPENDED.getStateCode()
                : SuspensionState.ACTIVE.getStateCode());
        vo.setFormType(model.getFormType());
        vo.setFormId(model.getFormId());
        vo.setFormName(model.getFormName());
        vo.setFormCustomCreatePath(model.getFormCustomCreatePath());
        vo.setFormCustomViewPath(model.getFormCustomViewPath());
        Deployment deployment = repositoryService.createDeploymentQuery()
                .deploymentId(definition.getDeploymentId()).singleResult();
        if (deployment != null) {
            vo.setDeploymentTime(deployment.getDeploymentTime());
        }
        if (model.getFormId() != null) {
            formService.simpleList().stream()
                    .filter(form -> Objects.equals(form.getId(), model.getFormId()))
                    .findFirst()
                    .ifPresent(form -> {
                        vo.setFormConf(form.getConf());
                        vo.setFormFields(form.getFields());
                    });
        }
        return vo;
    }

    private void enrichNames(WorkflowModelVO vo) {
        Map<String, String> categoryNames = categoryService.simpleList().stream()
                .collect(Collectors.toMap(WorkflowCategoryVO::getCode, WorkflowCategoryVO::getName,
                        (left, right) -> left));
        vo.setCategoryName(categoryNames.get(vo.getCategory()));
        if (vo.getFormId() != null) {
            vo.setFormName(formService.simpleList().stream()
                    .filter(form -> Objects.equals(form.getId(), vo.getFormId()))
                    .map(WorkflowFormVO::getName)
                    .findFirst().orElse(null));
        }
    }

    private Model requireModel(String id) {
        Model model = repositoryService.createModelQuery()
                .modelId(id)
                .modelTenantId(identityAdapter.currentTenantId())
                .singleResult();
        if (model == null) {
            throw new JeecgBootException("流程模型不存在或无权访问");
        }
        return model;
    }

    private Model requireManager(String id) {
        Model model = requireModel(id);
        List<String> managers = stringList(readMeta(model).get("managerUserIds"));
        if (!managers.contains(identityAdapter.currentUserId())) {
            throw new JeecgBootException("只有流程管理员可以修改该模型");
        }
        return model;
    }

    private ProcessDefinition latestDefinition(Model model) {
        if (!StringUtils.hasText(model.getDeploymentId())) {
            return null;
        }
        return repositoryService.createProcessDefinitionQuery()
                .deploymentId(model.getDeploymentId())
                .singleResult();
    }

    private void suspendDeployment(String deploymentId) {
        if (!StringUtils.hasText(deploymentId)) {
            return;
        }
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId).singleResult();
        if (definition != null && !definition.isSuspended()) {
            repositoryService.suspendProcessDefinitionById(definition.getId(), true, null);
        }
    }

    private Map<String, Object> metaFromRequest(WorkflowModelSaveRequest request) {
        Map<String, Object> meta = new LinkedHashMap<>(objectMapper.convertValue(request, MAP_TYPE));
        meta.remove("id");
        meta.remove("key");
        meta.remove("name");
        meta.remove("category");
        meta.remove("bpmnXml");
        meta.remove("simpleModel");
        return meta;
    }

    private Map<String, Object> readMeta(Model model) {
        if (!StringUtils.hasText(model.getMetaInfo())) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(model.getMetaInfo(), MAP_TYPE);
        } catch (Exception exception) {
            throw new JeecgBootException("流程模型元数据损坏", exception);
        }
    }

    private Map<String, Object> readSimpleModel(Model model) {
        byte[] source = repositoryService.getModelEditorSourceExtra(model.getId());
        if (source == null || source.length == 0) {
            return null;
        }
        try {
            return objectMapper.readValue(source, MAP_TYPE);
        } catch (Exception exception) {
            throw new JeecgBootException("简易流程模型数据损坏", exception);
        }
    }

    private long readSort(Model model) {
        Object sort = readMeta(model).get("sort");
        return sort instanceof Number number ? number.longValue() : System.currentTimeMillis();
    }

    private void applyCreateDefaults(WorkflowModelSaveRequest request) {
        request.setType(request.getType() == null ? 10 : request.getType());
        request.setFormType(request.getFormType() == null ? 10 : request.getFormType());
        request.setVisible(request.getVisible() == null ? Boolean.TRUE : request.getVisible());
        request.setSort(request.getSort() == null ? System.currentTimeMillis() : request.getSort());
        request.setManagerUserIds(normalizeManagers(request.getManagerUserIds()));
    }

    private List<String> normalizeManagers(List<String> managers) {
        List<String> result = managers == null ? new ArrayList<>() : new ArrayList<>(managers);
        if (!result.contains(identityAdapter.currentUserId())) {
            result.add(identityAdapter.currentUserId());
        }
        return result;
    }

    private List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        return list.stream().filter(Objects::nonNull).map(String::valueOf).toList();
    }

    private void validateKey(String key) {
        if (!StringUtils.hasText(key) || !XML_NAME_PATTERN.matcher(key.trim()).matches()) {
            throw new JeecgBootException("流程标识必须以字母或下划线开头，只能包含字母、数字、点、横线和下划线");
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            throw new JeecgBootException("流程模型数据序列化失败", exception);
        }
    }

    private String defaultBpmn(String key, String name) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" "
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                + "xmlns:flowable=\"http://flowable.org/bpmn\" "
                + "targetNamespace=\"http://jeecg.org/workflow\">\n"
                + "  <process id=\"" + xmlEscape(key) + "\" name=\"" + xmlEscape(name) + "\" isExecutable=\"true\">\n"
                + "    <startEvent id=\"StartEvent_1\" name=\"开始\"/>\n"
                + "    <endEvent id=\"EndEvent_1\" name=\"结束\"/>\n"
                + "    <sequenceFlow id=\"Flow_1\" sourceRef=\"StartEvent_1\" targetRef=\"EndEvent_1\"/>\n"
                + "  </process>\n"
                + "</definitions>";
    }

    private String xmlEscape(String value) {
        return value.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
