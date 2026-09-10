package org.jeecg.modules.workflow.api.constant;

/**
 * Flowable process variables and status values shared with the Yudao BPM contract.
 */
public final class WorkflowProcessConstants {

    public static final String VARIABLE_STATUS = "PROCESS_STATUS";
    public static final String VARIABLE_REASON = "PROCESS_REASON";
    public static final String TASK_VARIABLE_STATUS = "TASK_STATUS";
    public static final String TASK_VARIABLE_REASON = "TASK_REASON";
    public static final String PROCESS_INSTANCE_SKIP_EXPRESSION_ENABLED = "_FLOWABLE_SKIP_EXPRESSION_ENABLED";

    public static final String EXTENSION_CANDIDATE_STRATEGY = "candidateStrategy";
    public static final String EXTENSION_CANDIDATE_PARAM = "candidateParam";
    public static final String EXTENSION_ASSIGN_EMPTY_HANDLER_TYPE = "assignEmptyHandlerType";
    public static final String EXTENSION_ASSIGN_EMPTY_USER_IDS = "assignEmptyUserIds";
    public static final String EXTENSION_ASSIGN_START_USER_HANDLER_TYPE = "assignStartUserHandlerType";
    public static final String EXTENSION_REJECT_HANDLER_TYPE = "rejectHandlerType";
    public static final String EXTENSION_REJECT_RETURN_TASK_ID = "rejectReturnTaskId";
    public static final String EXTENSION_TRIGGER_TYPE = "triggerType";
    public static final String EXTENSION_TRIGGER_PARAM = "triggerParam";
    public static final String EXTENSION_APPROVE_TYPE = "approveType";
    public static final String EXTENSION_ENABLED_BUTTONS = "enabledButtons";
    public static final String EXTENSION_BUTTONS_SETTING = "buttonsSetting";
    public static final String EXTENSION_SIGN_ENABLE = "signEnable";
    public static final String EXTENSION_REASON_REQUIRE = "reasonRequire";
    public static final String EXTENSION_FORM_FIELD_PERMISSION = "formFieldsPermission";
    public static final String EXTENSION_BOUNDARY_EVENT_TYPE = "boundaryEventType";
    public static final String EXTENSION_TIMEOUT_HANDLER_TYPE = "timeoutHandlerType";

    public static final int ASSIGN_EMPTY_APPROVE = 1;
    public static final int ASSIGN_EMPTY_REJECT = 2;
    public static final int ASSIGN_EMPTY_USER = 3;
    public static final int ASSIGN_EMPTY_MANAGER = 4;

    public static final int ASSIGN_START_USER_AUDIT = 1;
    public static final int ASSIGN_START_USER_SKIP = 2;
    public static final int ASSIGN_START_USER_DEPARTMENT_LEADER = 3;

    public static final int REJECT_HANDLER_FINISH_PROCESS = 1;
    public static final int REJECT_HANDLER_RETURN_USER_TASK = 2;

    /**
     * Runtime collection variables used by simplified-designer multi-instance user tasks.
     * Keeping the names in the API module makes the BPMN converter and runtime engine use the
     * same contract without coupling either side to a web DTO.
     */
    private static final String MULTI_INSTANCE_ASSIGNEES_PREFIX = "_workflowTaskAssignees_";
    private static final String MULTI_INSTANCE_ASSIGNEE_PREFIX = "_workflowTaskAssignee_";
    private static final String RETURN_TASK_FLAG_PREFIX = "_workflowReturnTask_";

    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_APPROVED = 2;
    public static final int STATUS_REJECTED = 3;
    public static final int STATUS_CANCELED = 4;
    public static final int TASK_STATUS_WAIT = 0;
    public static final int TASK_STATUS_RUNNING = 1;
    public static final int TASK_STATUS_RETURNED = 5;
    public static final int TASK_STATUS_APPROVING = 7;
    public static final int TASK_STATUS_SKIPPED = -2;
    public static final int BOUNDARY_EVENT_USER_TASK_TIMEOUT = 1;
    public static final int TIMEOUT_HANDLER_REMINDER = 1;
    public static final int TIMEOUT_HANDLER_APPROVE = 2;
    public static final int TIMEOUT_HANDLER_REJECT = 3;

    public static String multiInstanceAssigneesVariable(String activityId) {
        return MULTI_INSTANCE_ASSIGNEES_PREFIX + activityId;
    }

    public static String multiInstanceAssigneeVariable(String activityId) {
        return MULTI_INSTANCE_ASSIGNEE_PREFIX + activityId;
    }

    public static String returnTaskFlagVariable(String activityId) {
        return RETURN_TASK_FLAG_PREFIX + activityId;
    }

    private WorkflowProcessConstants() {
    }
}
