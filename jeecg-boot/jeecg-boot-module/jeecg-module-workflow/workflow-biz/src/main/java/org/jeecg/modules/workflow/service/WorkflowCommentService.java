package org.jeecg.modules.workflow.service;

import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.task.Comment;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.workflow.adapter.WorkflowIdentityAdapter;
import org.jeecg.modules.workflow.api.dto.WorkflowCommentCreateRequest;
import org.jeecg.modules.workflow.api.vo.WorkflowCommentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class WorkflowCommentService {
    private final TaskService taskService;
    private final HistoryService historyService;
    private final WorkflowIdentityAdapter identityAdapter;
    private final ISysBaseAPI sysBaseApi;

    public WorkflowCommentService(TaskService taskService,
                                  HistoryService historyService,
                                  WorkflowIdentityAdapter identityAdapter,
                                  ISysBaseAPI sysBaseApi) {
        this.taskService = taskService;
        this.historyService = historyService;
        this.identityAdapter = identityAdapter;
        this.sysBaseApi = sysBaseApi;
    }

    public List<WorkflowCommentVO> list(String processInstanceId) {
        assertCanView(processInstanceId);
        return taskService.getProcessInstanceComments(processInstanceId).stream()
                .sorted(Comparator.comparing(Comment::getTime))
                .map(this::toVO)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void create(WorkflowCommentCreateRequest request) {
        Task task = myTodoQuery().taskId(request.getTaskId()).singleResult();
        if (task == null) {
            throw new JeecgBootException("待办任务不存在或当前用户无权评论");
        }
        Authentication.setAuthenticatedUserId(identityAdapter.currentUserId());
        try {
            taskService.addComment(task.getId(), task.getProcessInstanceId(), "comment", request.getMessage().trim());
        } finally {
            Authentication.setAuthenticatedUserId(null);
        }
    }

    private WorkflowCommentVO toVO(Comment comment) {
        WorkflowCommentVO vo = new WorkflowCommentVO();
        vo.setId(comment.getId());
        vo.setTaskId(comment.getTaskId());
        vo.setProcessInstanceId(comment.getProcessInstanceId());
        vo.setType(comment.getType());
        vo.setMessage(comment.getFullMessage());
        vo.setCreateTime(comment.getTime());
        if (comment.getTaskId() != null) {
            HistoricTaskInstance task = historyService.createHistoricTaskInstanceQuery()
                    .taskId(comment.getTaskId()).singleResult();
            if (task != null) {
                WorkflowCommentVO.TaskInfo taskInfo = new WorkflowCommentVO.TaskInfo();
                taskInfo.setId(task.getId());
                taskInfo.setName(task.getName());
                taskInfo.setTaskDefinitionKey(task.getTaskDefinitionKey());
                vo.setTask(taskInfo);
            }
        }
        if (comment.getUserId() != null) {
            LoginUser user = sysBaseApi.getUserById(comment.getUserId());
            WorkflowCommentVO.UserInfo userInfo = new WorkflowCommentVO.UserInfo();
            userInfo.setId(comment.getUserId());
            userInfo.setNickname(user == null ? comment.getUserId() : user.getRealname());
            userInfo.setAvatar(user == null ? null : user.getAvatar());
            vo.setUser(userInfo);
        }
        return vo;
    }

    private TaskQuery myTodoQuery() {
        String userId = identityAdapter.currentUserId();
        TaskQuery query = taskService.createTaskQuery()
                .active()
                .taskTenantId(identityAdapter.currentTenantId())
                .or()
                .taskAssignee(userId)
                .taskCandidateUser(userId);
        List<String> roles = identityAdapter.currentRoleCodes();
        if (!roles.isEmpty()) {
            query.taskCandidateGroupIn(roles);
        }
        return query.endOr();
    }

    private void assertCanView(String processInstanceId) {
        HistoricProcessInstance instance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .processInstanceTenantId(identityAdapter.currentTenantId()).singleResult();
        if (instance == null) {
            throw new JeecgBootException("流程实例不存在");
        }
        String userId = identityAdapter.currentUserId();
        if (Objects.equals(userId, instance.getStartUserId())) {
            return;
        }
        long involved = historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId)
                .taskTenantId(identityAdapter.currentTenantId())
                .taskAssignee(userId).count();
        // 未认领的候选审批任务没有历史办理人，但候选人仍应能查看该流程评论。
        long currentTodo = myTodoQuery().processInstanceId(processInstanceId).count();
        long currentOwner = taskService.createTaskQuery().active()
                .processInstanceId(processInstanceId)
                .taskTenantId(identityAdapter.currentTenantId())
                .taskOwner(userId).count();
        if (involved == 0 && currentTodo == 0 && currentOwner == 0) {
            throw new JeecgBootException("无权查看该流程评论");
        }
    }
}
