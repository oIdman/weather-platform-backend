package org.jeecg.modules.workflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.TaskListener;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.jeecg.modules.workflow.api.dto.WorkflowPage;
import org.jeecg.modules.workflow.api.dto.WorkflowProcessListenerSaveRequest;
import org.jeecg.modules.workflow.api.vo.WorkflowProcessListenerVO;
import org.jeecg.modules.workflow.entity.WorkflowProcessListener;
import org.jeecg.modules.workflow.mapper.WorkflowProcessListenerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class WorkflowProcessListenerService extends WorkflowMetadataServiceSupport {
    private final WorkflowProcessListenerMapper mapper;

    public WorkflowProcessListenerService(WorkflowIdentityAdapter identityAdapter, WorkflowProcessListenerMapper mapper) {
        super(identityAdapter);
        this.mapper = mapper;
    }

    @Transactional
    public Long create(WorkflowProcessListenerSaveRequest request) {
        WorkflowProcessListener entity = fromRequest(request);
        prepareCreate(entity);
        mapper.insert(entity);
        return entity.getId();
    }

    @Transactional
    public void update(WorkflowProcessListenerSaveRequest request) {
        if (request.getId() == null) {
            throw new JeecgBootException("流程监听器编号不能为空");
        }
        getEntity(request.getId());
        WorkflowProcessListener entity = fromRequest(request);
        prepareUpdate(entity);
        assertChanged(mapper.update(entity, tenantById(request.getId())), "流程监听器不存在或无权修改");
    }

    @Transactional
    public void delete(Long id) {
        assertChanged(mapper.delete(tenantById(id)), "流程监听器不存在或无权删除");
    }

    public WorkflowProcessListenerVO get(Long id) {
        return toVO(getEntity(id));
    }

    public WorkflowPage<WorkflowProcessListenerVO> page(int pageNo, int pageSize, String name, Integer status, String type) {
        Page<WorkflowProcessListener> page = mapper.selectPage(new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<WorkflowProcessListener>()
                        .eq(WorkflowProcessListener::getTenantId, currentTenantId())
                        .like(StringUtils.hasText(name), WorkflowProcessListener::getName, name)
                        .eq(status != null, WorkflowProcessListener::getStatus, status)
                        .eq(StringUtils.hasText(type), WorkflowProcessListener::getType, type)
                        .orderByDesc(WorkflowProcessListener::getId));
        return new WorkflowPage<>(page.getRecords().stream().map(this::toVO).toList(), page.getTotal());
    }

    private WorkflowProcessListener getEntity(Long id) {
        WorkflowProcessListener entity = mapper.selectOne(tenantById(id));
        if (entity == null) {
            throw new JeecgBootException("流程监听器不存在或无权访问");
        }
        return entity;
    }

    private LambdaQueryWrapper<WorkflowProcessListener> tenantById(Long id) {
        return new LambdaQueryWrapper<WorkflowProcessListener>()
                .eq(WorkflowProcessListener::getId, id)
                .eq(WorkflowProcessListener::getTenantId, currentTenantId());
    }

    private WorkflowProcessListener fromRequest(WorkflowProcessListenerSaveRequest request) {
        WorkflowProcessListener entity = new WorkflowProcessListener();
        entity.setId(request.getId());
        entity.setName(request.getName().trim());
        entity.setType(request.getType());
        entity.setStatus(request.getStatus());
        entity.setEvent(request.getEvent());
        entity.setValueType(request.getValueType());
        entity.setValue(request.getValue().trim());
        validateImplementation(entity);
        return entity;
    }

    /**
     * Validate templates before they are selected into a BPMN definition. Without this check a
     * typo is only reported much later, when Flowable tries to deploy or start a process.
     */
    private void validateImplementation(WorkflowProcessListener listener) {
        if (!("execution".equalsIgnoreCase(listener.getType()) || "task".equalsIgnoreCase(listener.getType()))) {
            throw new JeecgBootException("监听器类型只支持 execution 或 task");
        }
        boolean validEvent = "execution".equalsIgnoreCase(listener.getType())
                ? ("start".equalsIgnoreCase(listener.getEvent()) || "end".equalsIgnoreCase(listener.getEvent())
                || "take".equalsIgnoreCase(listener.getEvent()))
                : ("create".equalsIgnoreCase(listener.getEvent()) || "assignment".equalsIgnoreCase(listener.getEvent())
                || "complete".equalsIgnoreCase(listener.getEvent()) || "delete".equalsIgnoreCase(listener.getEvent())
                || "update".equalsIgnoreCase(listener.getEvent()) || "timeout".equalsIgnoreCase(listener.getEvent()));
        if (!validEvent) {
            throw new JeecgBootException("监听事件与监听器类型不匹配");
        }
        if ("class".equalsIgnoreCase(listener.getValueType())) {
            try {
                Class<?> implementation = Class.forName(listener.getValue());
                boolean valid = "execution".equalsIgnoreCase(listener.getType())
                        ? ExecutionListener.class.isAssignableFrom(implementation)
                        : TaskListener.class.isAssignableFrom(implementation);
                if (!valid) {
                    throw new JeecgBootException("执行监听器必须实现 " + ExecutionListener.class.getName()
                            + "，任务监听器必须实现 " + TaskListener.class.getName());
                }
            } catch (ClassNotFoundException exception) {
                throw new JeecgBootException("监听器类不存在：" + listener.getValue());
            }
            return;
        }
        String value = listener.getValue();
        if (!("expression".equalsIgnoreCase(listener.getValueType())
                || "delegateExpression".equalsIgnoreCase(listener.getValueType()))
                || !value.startsWith("${") || !value.endsWith("}")) {
            throw new JeecgBootException("监听器表达式必须使用 ${...} 格式");
        }
    }

    private WorkflowProcessListenerVO toVO(WorkflowProcessListener entity) {
        WorkflowProcessListenerVO vo = new WorkflowProcessListenerVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setType(entity.getType());
        vo.setStatus(entity.getStatus());
        vo.setEvent(entity.getEvent());
        vo.setValueType(entity.getValueType());
        vo.setValue(entity.getValue());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}
