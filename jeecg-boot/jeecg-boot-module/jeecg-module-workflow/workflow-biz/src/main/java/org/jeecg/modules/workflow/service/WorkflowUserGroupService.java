package org.jeecg.modules.workflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.jeecg.modules.workflow.api.dto.WorkflowPage;
import org.jeecg.modules.workflow.api.dto.WorkflowUserGroupSaveRequest;
import org.jeecg.modules.workflow.api.vo.WorkflowUserGroupVO;
import org.jeecg.modules.workflow.entity.WorkflowUserGroup;
import org.jeecg.modules.workflow.mapper.WorkflowUserGroupMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class WorkflowUserGroupService extends WorkflowMetadataServiceSupport {
    private final WorkflowUserGroupMapper mapper;

    public WorkflowUserGroupService(WorkflowIdentityAdapter identityAdapter, WorkflowUserGroupMapper mapper) {
        super(identityAdapter);
        this.mapper = mapper;
    }

    @Transactional
    public Long create(WorkflowUserGroupSaveRequest request) {
        WorkflowUserGroup entity = fromRequest(request);
        prepareCreate(entity);
        mapper.insert(entity);
        return entity.getId();
    }

    @Transactional
    public void update(WorkflowUserGroupSaveRequest request) {
        if (request.getId() == null) {
            throw new JeecgBootException("流程用户组编号不能为空");
        }
        getEntity(request.getId());
        WorkflowUserGroup entity = fromRequest(request);
        prepareUpdate(entity);
        assertChanged(mapper.update(entity, tenantById(request.getId())), "流程用户组不存在或无权修改");
    }

    @Transactional
    public void delete(Long id) {
        assertChanged(mapper.delete(tenantById(id)), "流程用户组不存在或无权删除");
    }

    public WorkflowUserGroupVO get(Long id) {
        return toVO(getEntity(id));
    }

    public WorkflowPage<WorkflowUserGroupVO> page(int pageNo, int pageSize, String name, Integer status) {
        Page<WorkflowUserGroup> page = mapper.selectPage(new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<WorkflowUserGroup>()
                        .eq(WorkflowUserGroup::getTenantId, currentTenantId())
                        .like(StringUtils.hasText(name), WorkflowUserGroup::getName, name)
                        .eq(status != null, WorkflowUserGroup::getStatus, status)
                        .orderByDesc(WorkflowUserGroup::getId));
        return new WorkflowPage<>(page.getRecords().stream().map(this::toVO).toList(), page.getTotal());
    }

    public List<WorkflowUserGroupVO> simpleList() {
        return mapper.selectList(new LambdaQueryWrapper<WorkflowUserGroup>()
                        .eq(WorkflowUserGroup::getTenantId, currentTenantId())
                        .eq(WorkflowUserGroup::getStatus, 0)
                        .orderByDesc(WorkflowUserGroup::getId))
                .stream().map(this::toVO).toList();
    }

    private WorkflowUserGroup getEntity(Long id) {
        WorkflowUserGroup entity = mapper.selectOne(tenantById(id));
        if (entity == null) {
            throw new JeecgBootException("流程用户组不存在或无权访问");
        }
        return entity;
    }

    private LambdaQueryWrapper<WorkflowUserGroup> tenantById(Long id) {
        return new LambdaQueryWrapper<WorkflowUserGroup>()
                .eq(WorkflowUserGroup::getId, id)
                .eq(WorkflowUserGroup::getTenantId, currentTenantId());
    }

    private WorkflowUserGroup fromRequest(WorkflowUserGroupSaveRequest request) {
        WorkflowUserGroup entity = new WorkflowUserGroup();
        entity.setId(request.getId());
        entity.setName(request.getName().trim());
        entity.setDescription(request.getDescription());
        entity.setUserIds(new ArrayList<>(request.getUserIds()));
        entity.setStatus(request.getStatus());
        return entity;
    }

    private WorkflowUserGroupVO toVO(WorkflowUserGroup entity) {
        WorkflowUserGroupVO vo = new WorkflowUserGroupVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDescription(entity.getDescription());
        vo.setUserIds(entity.getUserIds());
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
