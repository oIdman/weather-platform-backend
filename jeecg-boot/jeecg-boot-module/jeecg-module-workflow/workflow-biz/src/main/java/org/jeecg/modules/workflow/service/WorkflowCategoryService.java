package org.jeecg.modules.workflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.jeecg.modules.workflow.api.dto.WorkflowCategorySaveRequest;
import org.jeecg.modules.workflow.api.dto.WorkflowPage;
import org.jeecg.modules.workflow.api.vo.WorkflowCategoryVO;
import org.jeecg.modules.workflow.entity.WorkflowCategory;
import org.jeecg.modules.workflow.mapper.WorkflowCategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class WorkflowCategoryService extends WorkflowMetadataServiceSupport {
    private final WorkflowCategoryMapper mapper;

    public WorkflowCategoryService(WorkflowIdentityAdapter identityAdapter, WorkflowCategoryMapper mapper) {
        super(identityAdapter);
        this.mapper = mapper;
    }

    @Transactional
    public Long create(WorkflowCategorySaveRequest request) {
        assertCodeUnique(request.getCode(), null);
        WorkflowCategory entity = fromRequest(request);
        prepareCreate(entity);
        mapper.insert(entity);
        return entity.getId();
    }

    @Transactional
    public void update(WorkflowCategorySaveRequest request) {
        if (request.getId() == null) {
            throw new JeecgBootException("流程分类编号不能为空");
        }
        getEntity(request.getId());
        assertCodeUnique(request.getCode(), request.getId());
        WorkflowCategory entity = fromRequest(request);
        prepareUpdate(entity);
        assertChanged(mapper.update(entity, tenantById(request.getId())), "流程分类不存在或无权修改");
    }

    @Transactional
    public void updateSortBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new JeecgBootException("流程分类编号列表不能为空");
        }
        for (int index = 0; index < ids.size(); index++) {
            WorkflowCategory entity = new WorkflowCategory();
            entity.setSort(index);
            prepareUpdate(entity);
            assertChanged(mapper.update(entity, tenantById(ids.get(index))), "流程分类不存在或无权修改");
        }
    }

    @Transactional
    public void delete(Long id) {
        assertChanged(mapper.delete(tenantById(id)), "流程分类不存在或无权删除");
    }

    public WorkflowCategoryVO get(Long id) {
        return toVO(getEntity(id));
    }

    public WorkflowPage<WorkflowCategoryVO> page(int pageNo, int pageSize, String name, Integer status) {
        LambdaQueryWrapper<WorkflowCategory> query = new LambdaQueryWrapper<WorkflowCategory>()
                .eq(WorkflowCategory::getTenantId, currentTenantId())
                .like(StringUtils.hasText(name), WorkflowCategory::getName, name)
                .eq(status != null, WorkflowCategory::getStatus, status)
                .orderByAsc(WorkflowCategory::getSort)
                .orderByDesc(WorkflowCategory::getId);
        Page<WorkflowCategory> page = mapper.selectPage(new Page<>(pageNo, pageSize), query);
        return new WorkflowPage<>(page.getRecords().stream().map(this::toVO).toList(), page.getTotal());
    }

    public List<WorkflowCategoryVO> simpleList() {
        return mapper.selectList(new LambdaQueryWrapper<WorkflowCategory>()
                        .eq(WorkflowCategory::getTenantId, currentTenantId())
                        .eq(WorkflowCategory::getStatus, 0)
                        .orderByAsc(WorkflowCategory::getSort))
                .stream().map(this::toVO).toList();
    }

    private WorkflowCategory getEntity(Long id) {
        WorkflowCategory entity = mapper.selectOne(tenantById(id));
        if (entity == null) {
            throw new JeecgBootException("流程分类不存在或无权访问");
        }
        return entity;
    }

    private LambdaQueryWrapper<WorkflowCategory> tenantById(Long id) {
        return new LambdaQueryWrapper<WorkflowCategory>()
                .eq(WorkflowCategory::getId, id)
                .eq(WorkflowCategory::getTenantId, currentTenantId());
    }

    private void assertCodeUnique(String code, Long excludedId) {
        Long count = mapper.selectCount(new LambdaQueryWrapper<WorkflowCategory>()
                .eq(WorkflowCategory::getTenantId, currentTenantId())
                .eq(WorkflowCategory::getCode, code)
                .ne(excludedId != null, WorkflowCategory::getId, excludedId));
        if (count > 0) {
            throw new JeecgBootException("流程分类标识已存在");
        }
    }

    private WorkflowCategory fromRequest(WorkflowCategorySaveRequest request) {
        WorkflowCategory entity = new WorkflowCategory();
        entity.setId(request.getId());
        entity.setName(request.getName().trim());
        entity.setCode(request.getCode().trim());
        entity.setDescription(request.getDescription());
        entity.setStatus(request.getStatus());
        entity.setSort(request.getSort());
        return entity;
    }

    private WorkflowCategoryVO toVO(WorkflowCategory entity) {
        WorkflowCategoryVO vo = new WorkflowCategoryVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setCode(entity.getCode());
        vo.setDescription(entity.getDescription());
        vo.setStatus(entity.getStatus());
        vo.setSort(entity.getSort());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
