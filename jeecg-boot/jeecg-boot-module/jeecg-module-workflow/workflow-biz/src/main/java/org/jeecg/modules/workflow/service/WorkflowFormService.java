package org.jeecg.modules.workflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.jeecg.modules.workflow.api.dto.WorkflowFormSaveRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowPage;
import org.jeecg.modules.workflow.api.vo.WorkflowFormVO;
import org.jeecg.modules.workflow.entity.WorkflowForm;
import org.jeecg.modules.workflow.mapper.WorkflowFormMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class WorkflowFormService extends WorkflowMetadataServiceSupport {
    private final WorkflowFormMapper mapper;

    public WorkflowFormService(WorkflowIdentityAdapter identityAdapter, WorkflowFormMapper mapper) {
        super(identityAdapter);
        this.mapper = mapper;
    }

    @Transactional
    public Long create(WorkflowFormSaveRequest request) {
        WorkflowForm entity = fromRequest(request);
        prepareCreate(entity);
        mapper.insert(entity);
        return entity.getId();
    }

    @Transactional
    public void update(WorkflowFormSaveRequest request) {
        if (request.getId() == null) {
            throw new JeecgBootException("流程表单编号不能为空");
        }
        getEntity(request.getId());
        WorkflowForm entity = fromRequest(request);
        prepareUpdate(entity);
        assertChanged(mapper.update(entity, tenantById(request.getId())), "流程表单不存在或无权修改");
    }

    @Transactional
    public void delete(Long id) {
        assertChanged(mapper.delete(tenantById(id)), "流程表单不存在或无权删除");
    }

    public WorkflowFormVO get(Long id) {
        return toVO(getEntity(id));
    }

    public WorkflowPage<WorkflowFormVO> page(int pageNo, int pageSize, String name, Integer status) {
        Page<WorkflowForm> page = mapper.selectPage(new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<WorkflowForm>()
                        .eq(WorkflowForm::getTenantId, currentTenantId())
                        .like(StringUtils.hasText(name), WorkflowForm::getName, name)
                        .eq(status != null, WorkflowForm::getStatus, status)
                        .orderByDesc(WorkflowForm::getId));
        return new WorkflowPage<>(page.getRecords().stream().map(this::toVO).toList(), page.getTotal());
    }

    public List<WorkflowFormVO> simpleList() {
        return mapper.selectList(new LambdaQueryWrapper<WorkflowForm>()
                        .eq(WorkflowForm::getTenantId, currentTenantId())
                        .eq(WorkflowForm::getStatus, 0)
                        .orderByDesc(WorkflowForm::getId))
                .stream().map(this::toVO).toList();
    }

    private WorkflowForm getEntity(Long id) {
        WorkflowForm entity = mapper.selectOne(tenantById(id));
        if (entity == null) {
            throw new JeecgBootException("流程表单不存在或无权访问");
        }
        return entity;
    }

    private LambdaQueryWrapper<WorkflowForm> tenantById(Long id) {
        return new LambdaQueryWrapper<WorkflowForm>()
                .eq(WorkflowForm::getId, id)
                .eq(WorkflowForm::getTenantId, currentTenantId());
    }

    private WorkflowForm fromRequest(WorkflowFormSaveRequest request) {
        WorkflowForm entity = new WorkflowForm();
        entity.setId(request.getId());
        entity.setName(request.getName().trim());
        entity.setStatus(request.getStatus());
        entity.setConf(request.getConf());
        entity.setFields(new ArrayList<>(request.getFields()));
        entity.setRemark(request.getRemark());
        return entity;
    }

    private WorkflowFormVO toVO(WorkflowForm entity) {
        WorkflowFormVO vo = new WorkflowFormVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setStatus(entity.getStatus());
        vo.setConf(entity.getConf());
        vo.setFields(entity.getFields());
        vo.setRemark(entity.getRemark());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
