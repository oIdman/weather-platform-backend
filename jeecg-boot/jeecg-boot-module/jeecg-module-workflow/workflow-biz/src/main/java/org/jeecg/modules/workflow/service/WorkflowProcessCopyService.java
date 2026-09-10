package org.jeecg.modules.workflow.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.jeecg.modules.workflow.api.dto.WorkflowPage;
import org.jeecg.modules.workflow.api.dto.WorkflowTaskCopyRequest;
import org.jeecg.modules.workflow.api.vo.WorkflowCategoryVO;
import org.jeecg.modules.workflow.api.vo.WorkflowProcessCopyVO;
import org.jeecg.modules.workflow.entity.WorkflowProcessInstanceCopy;
import org.jeecg.modules.workflow.mapper.WorkflowProcessInstanceCopyMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class WorkflowProcessCopyService extends WorkflowMetadataServiceSupport {
    private final WorkflowProcessInstanceCopyMapper mapper;
    private final WorkflowIdentityAdapter identityAdapter;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;
    private final WorkflowCategoryService categoryService;
    private final ISysBaseAPI sysBaseApi;

    public WorkflowProcessCopyService(WorkflowProcessInstanceCopyMapper mapper,
                                      WorkflowIdentityAdapter identityAdapter,
                                      TaskService taskService,
                                      HistoryService historyService,
                                      RepositoryService repositoryService,
                                      WorkflowCategoryService categoryService,
                                      ISysBaseAPI sysBaseApi) {
        super(identityAdapter);
        this.mapper = mapper;
        this.identityAdapter = identityAdapter;
        this.taskService = taskService;
        this.historyService = historyService;
        this.repositoryService = repositoryService;
        this.categoryService = categoryService;
        this.sysBaseApi = sysBaseApi;
    }

    @Transactional(rollbackFor = Exception.class)
    public void copy(WorkflowTaskCopyRequest request) {
        Task task = requireOperableTask(request.getId());
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .processInstanceTenantId(identityAdapter.currentTenantId())
                .singleResult();
        if (instance == null) {
            throw new JeecgBootException("流程实例不存在或无权访问");
        }
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(task.getProcessDefinitionId()).singleResult();
        if (definition == null) {
            throw new JeecgBootException("流程定义不存在");
        }
        for (String userId : request.getCopyUserIds().stream().filter(StringUtils::hasText).distinct().toList()) {
            if (sysBaseApi.getUserById(userId) == null) {
                throw new JeecgBootException("抄送用户不存在");
            }
            WorkflowProcessInstanceCopy entity = new WorkflowProcessInstanceCopy();
            entity.setUserId(userId);
            entity.setStartUserId(instance.getStartUserId());
            entity.setProcessInstanceId(instance.getId());
            entity.setProcessInstanceName(instance.getName() == null ? definition.getName() : instance.getName());
            entity.setProcessDefinitionId(task.getProcessDefinitionId());
            entity.setCategory(definition.getCategory());
            entity.setActivityId(task.getTaskDefinitionKey());
            entity.setActivityName(task.getName());
            entity.setTaskId(task.getId());
            entity.setReason(request.getReason());
            prepareCreate(entity);
            mapper.insert(entity);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void copyFromNode(Collection<String> copyUserIds, String processInstanceId, String processDefinitionId,
                             String activityId, String activityName, String tenantId, String reason) {
        if (copyUserIds == null || copyUserIds.isEmpty()) {
            return;
        }
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .processInstanceTenantId(tenantId)
                .singleResult();
        if (instance == null) {
            throw new JeecgBootException("流程实例不存在或无权访问");
        }
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId).singleResult();
        if (definition == null) {
            throw new JeecgBootException("流程定义不存在");
        }
        long numericTenantId;
        try {
            numericTenantId = Long.parseLong(tenantId);
        } catch (NumberFormatException exception) {
            throw new JeecgBootException("流程租户编号不是有效数字");
        }
        Date now = new Date();
        for (String userId : copyUserIds.stream().filter(StringUtils::hasText).distinct().toList()) {
            long existing = mapper.selectCount(new LambdaQueryWrapper<WorkflowProcessInstanceCopy>()
                    .eq(WorkflowProcessInstanceCopy::getTenantId, numericTenantId)
                    .eq(WorkflowProcessInstanceCopy::getProcessInstanceId, processInstanceId)
                    .eq(WorkflowProcessInstanceCopy::getUserId, userId)
                    .eq(WorkflowProcessInstanceCopy::getActivityId, activityId));
            if (existing > 0) {
                continue;
            }
            WorkflowProcessInstanceCopy entity = new WorkflowProcessInstanceCopy();
            entity.setUserId(userId);
            entity.setStartUserId(instance.getStartUserId());
            entity.setProcessInstanceId(instance.getId());
            entity.setProcessInstanceName(instance.getName() == null ? definition.getName() : instance.getName());
            entity.setProcessDefinitionId(definition.getId());
            entity.setCategory(definition.getCategory());
            entity.setActivityId(activityId);
            entity.setActivityName(activityName);
            entity.setReason(reason);
            entity.setTenantId(numericTenantId);
            entity.setCreator("workflow");
            entity.setCreateTime(now);
            entity.setUpdater("workflow");
            entity.setUpdateTime(now);
            entity.setDeleted(0);
            mapper.insert(entity);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteByProcessInstanceId(String processInstanceId) {
        Long tenantId = currentTenantId();
        mapper.delete(new LambdaQueryWrapper<WorkflowProcessInstanceCopy>()
                .eq(WorkflowProcessInstanceCopy::getTenantId, tenantId)
                .eq(WorkflowProcessInstanceCopy::getProcessInstanceId, processInstanceId));
    }

    public WorkflowPage<WorkflowProcessCopyVO> page(int pageNo, int pageSize, String processInstanceName) {
        Page<WorkflowProcessInstanceCopy> page = mapper.selectPage(new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<WorkflowProcessInstanceCopy>()
                        .eq(WorkflowProcessInstanceCopy::getTenantId, currentTenantId())
                        .eq(WorkflowProcessInstanceCopy::getUserId, identityAdapter.currentUserId())
                        .like(StringUtils.hasText(processInstanceName), WorkflowProcessInstanceCopy::getProcessInstanceName,
                                processInstanceName)
                        .orderByDesc(WorkflowProcessInstanceCopy::getCreateTime));
        return new WorkflowPage<>(page.getRecords().stream().map(this::toVO).toList(), page.getTotal());
    }

    private Task requireOperableTask(String taskId) {
        String tenantId = identityAdapter.currentTenantId();
        String userId = identityAdapter.currentUserId();
        Task task = taskService.createTaskQuery().taskId(taskId).taskTenantId(tenantId).singleResult();
        if (task == null) {
            throw new JeecgBootException("待办任务不存在");
        }
        if (Objects.equals(task.getAssignee(), userId)) {
            return task;
        }
        boolean candidate = task.getAssignee() == null
                && (taskService.createTaskQuery().taskId(taskId).taskCandidateUser(userId).count() > 0
                || (!identityAdapter.currentRoleCodes().isEmpty()
                && taskService.createTaskQuery().taskId(taskId)
                .taskCandidateGroupIn(identityAdapter.currentRoleCodes()).count() > 0));
        if (!candidate) {
            throw new JeecgBootException("当前用户无权操作该任务");
        }
        return task;
    }

    private WorkflowProcessCopyVO toVO(WorkflowProcessInstanceCopy entity) {
        WorkflowProcessCopyVO vo = new WorkflowProcessCopyVO();
        vo.setId(entity.getId());
        vo.setUserId(entity.getUserId());
        vo.setUserName(userName(entity.getUserId()));
        vo.setStartUserId(entity.getStartUserId());
        vo.setStartUserName(userName(entity.getStartUserId()));
        vo.setProcessInstanceId(entity.getProcessInstanceId());
        vo.setProcessInstanceName(entity.getProcessInstanceName());
        vo.setProcessDefinitionId(entity.getProcessDefinitionId());
        vo.setCategory(entity.getCategory());
        Map<String, String> categoryNames = categoryService.simpleList().stream()
                .collect(Collectors.toMap(WorkflowCategoryVO::getCode, WorkflowCategoryVO::getName,
                        (left, right) -> left));
        vo.setCategoryName(categoryNames.get(entity.getCategory()));
        vo.setActivityId(entity.getActivityId());
        vo.setActivityName(entity.getActivityName());
        vo.setTaskId(entity.getTaskId());
        vo.setReason(entity.getReason());
        vo.setCreateTime(entity.getCreateTime());
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(entity.getProcessInstanceId()).singleResult();
        if (instance != null) {
            vo.setProcessInstanceStartTime(instance.getStartTime());
        }
        return vo;
    }

    private String userName(String userId) {
        LoginUser user = StringUtils.hasText(userId) ? sysBaseApi.getUserById(userId) : null;
        return user == null ? userId : user.getRealname();
    }
}
