package org.jeecg.modules.workflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.jeecg.modules.workflow.api.dto.WorkflowPage;
import org.jeecg.modules.workflow.api.dto.WorkflowProcessExpressionSaveRequest;
import org.jeecg.modules.workflow.api.vo.WorkflowProcessExpressionVO;
import org.jeecg.modules.workflow.entity.WorkflowProcessExpression;
import org.jeecg.modules.workflow.mapper.WorkflowProcessExpressionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class WorkflowProcessExpressionService extends WorkflowMetadataServiceSupport {
    private final WorkflowProcessExpressionMapper mapper;

    public WorkflowProcessExpressionService(WorkflowIdentityAdapter identityAdapter, WorkflowProcessExpressionMapper mapper) {
        super(identityAdapter);
        this.mapper = mapper;
    }

    @Transactional
    public Long create(WorkflowProcessExpressionSaveRequest request) {
        WorkflowProcessExpression entity = fromRequest(request);
        prepareCreate(entity);
        mapper.insert(entity);
        return entity.getId();
    }

    @Transactional
    public void update(WorkflowProcessExpressionSaveRequest request) {
        if (request.getId() == null) {
            throw new JeecgBootException("流程表达式编号不能为空");
        }
        getEntity(request.getId());
        WorkflowProcessExpression entity = fromRequest(request);
        prepareUpdate(entity);
        assertChanged(mapper.update(entity, tenantById(request.getId())), "流程表达式不存在或无权修改");
    }

    @Transactional
    public void delete(Long id) {
        assertChanged(mapper.delete(tenantById(id)), "流程表达式不存在或无权删除");
    }

    public WorkflowProcessExpressionVO get(Long id) {
        return toVO(getEntity(id));
    }

    public WorkflowPage<WorkflowProcessExpressionVO> page(int pageNo, int pageSize, String name, Integer status) {
        Page<WorkflowProcessExpression> page = mapper.selectPage(new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<WorkflowProcessExpression>()
                        .eq(WorkflowProcessExpression::getTenantId, currentTenantId())
                        .like(StringUtils.hasText(name), WorkflowProcessExpression::getName, name)
                        .eq(status != null, WorkflowProcessExpression::getStatus, status)
                        .orderByDesc(WorkflowProcessExpression::getId));
        return new WorkflowPage<>(page.getRecords().stream().map(this::toVO).toList(), page.getTotal());
    }

    private WorkflowProcessExpression getEntity(Long id) {
        WorkflowProcessExpression entity = mapper.selectOne(tenantById(id));
        if (entity == null) {
            throw new JeecgBootException("流程表达式不存在或无权访问");
        }
        return entity;
    }

    private LambdaQueryWrapper<WorkflowProcessExpression> tenantById(Long id) {
        return new LambdaQueryWrapper<WorkflowProcessExpression>()
                .eq(WorkflowProcessExpression::getId, id)
                .eq(WorkflowProcessExpression::getTenantId, currentTenantId());
    }

    private WorkflowProcessExpression fromRequest(WorkflowProcessExpressionSaveRequest request) {
        WorkflowProcessExpression entity = new WorkflowProcessExpression();
        entity.setId(request.getId());
        entity.setName(request.getName().trim());
        entity.setStatus(request.getStatus());
        entity.setExpression(request.getExpression().trim());
        return entity;
    }

    private WorkflowProcessExpressionVO toVO(WorkflowProcessExpression entity) {
        WorkflowProcessExpressionVO vo = new WorkflowProcessExpressionVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setStatus(entity.getStatus());
        vo.setExpression(entity.getExpression());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
