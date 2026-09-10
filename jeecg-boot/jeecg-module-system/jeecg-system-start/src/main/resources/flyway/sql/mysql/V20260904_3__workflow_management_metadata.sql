-- Align the workflow management menu with the RuoYi/Yudao BPM hierarchy.
-- This migration only exposes pages whose Jeecg adapters are implemented.

ALTER TABLE `bpm_category` MODIFY `status` tinyint NOT NULL DEFAULT 0;
ALTER TABLE `bpm_form` MODIFY `status` tinyint NOT NULL DEFAULT 0;
ALTER TABLE `bpm_process_expression` MODIFY `status` tinyint NOT NULL DEFAULT 0;
ALTER TABLE `bpm_process_listener` MODIFY `status` tinyint NOT NULL DEFAULT 0;
ALTER TABLE `bpm_user_group` MODIFY `status` tinyint NOT NULL DEFAULT 0;

UPDATE `sys_permission`
SET `name` = '工作流程',
    `url` = '/bpm',
    `redirect` = '/bpm/task/todo',
    `component` = 'layouts/default/index',
    `always_show` = 0,
    `is_leaf` = 0,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000001';

INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `create_by`, `create_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('1909040000000000020', '1909040000000000001', '流程管理', '/bpm/manager', 'layouts/RouteView', 1, 'bpm-manager', '/bpm/manager/definition', 0, NULL, '1', 10, 0, 'ant-design:control-outlined', 0, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000021', '1909040000000000001', '审批中心', '/bpm/task', 'layouts/RouteView', 1, 'bpm-task-center', '/bpm/task/create', 0, NULL, '1', 20, 0, 'ant-design:audit-outlined', 0, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000030', '1909040000000000020', '流程表单', '/bpm/manager/form', 'bpm/form/index', 1, 'BpmForm', NULL, 1, NULL, '1', 2, 0, 'ant-design:form-outlined', 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000031', '1909040000000000020', '流程分类', '/bpm/manager/category', 'bpm/category/index', 1, 'BpmCategory', NULL, 1, NULL, '1', 3, 0, 'ant-design:group-outlined', 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000032', '1909040000000000020', '用户分组', '/bpm/manager/user-group', 'bpm/group/index', 1, 'BpmUserGroup', NULL, 1, NULL, '1', 4, 0, 'ant-design:team-outlined', 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000033', '1909040000000000020', '流程监听器', '/bpm/manager/process-listener', 'bpm/processListener/index', 1, 'BpmProcessListener', NULL, 1, NULL, '1', 5, 0, 'ant-design:customer-service-outlined', 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000034', '1909040000000000020', '流程表达式', '/bpm/manager/process-expression', 'bpm/processExpression/index', 1, 'BpmProcessExpression', NULL, 1, NULL, '1', 6, 0, 'ant-design:function-outlined', 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`), `name` = VALUES(`name`), `url` = VALUES(`url`),
`component` = VALUES(`component`), `component_name` = VALUES(`component_name`),
`sort_no` = VALUES(`sort_no`), `is_leaf` = VALUES(`is_leaf`), `del_flag` = 0, `status` = '1';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000020',
    `name` = '流程定义',
    `url` = '/bpm/manager/definition',
    `component` = 'workflow/definition/index',
    `sort_no` = 1,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000002';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000020',
    `name` = '流程实例',
    `url` = '/bpm/manager/process-instance',
    `component` = 'workflow/process/manager',
    `sort_no` = 10,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000006';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000021',
    `name` = '发起流程',
    `url` = '/bpm/task/create',
    `component` = 'workflow/process/my',
    `sort_no` = 0,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000005';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000021',
    `name` = '待办任务',
    `url` = '/bpm/task/todo',
    `component` = 'workflow/task/todo',
    `sort_no` = 10,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000003';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000021',
    `name` = '已办任务',
    `url` = '/bpm/task/done',
    `component` = 'workflow/task/done',
    `sort_no` = 20,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000004';

INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `is_route`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `create_by`, `create_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('1909040000000000040', '1909040000000000030', '表单查询', 0, 2, 'bpm:form:query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000041', '1909040000000000030', '表单创建', 0, 2, 'bpm:form:create', '1', 2, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000042', '1909040000000000030', '表单更新', 0, 2, 'bpm:form:update', '1', 3, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000043', '1909040000000000030', '表单删除', 0, 2, 'bpm:form:delete', '1', 4, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000044', '1909040000000000031', '分类查询', 0, 2, 'bpm:category:query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000045', '1909040000000000031', '分类创建', 0, 2, 'bpm:category:create', '1', 2, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000046', '1909040000000000031', '分类更新', 0, 2, 'bpm:category:update', '1', 3, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000047', '1909040000000000031', '分类删除', 0, 2, 'bpm:category:delete', '1', 4, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000048', '1909040000000000032', '用户组查询', 0, 2, 'bpm:user-group:query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000049', '1909040000000000032', '用户组创建', 0, 2, 'bpm:user-group:create', '1', 2, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000050', '1909040000000000032', '用户组更新', 0, 2, 'bpm:user-group:update', '1', 3, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000051', '1909040000000000032', '用户组删除', 0, 2, 'bpm:user-group:delete', '1', 4, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000052', '1909040000000000033', '监听器查询', 0, 2, 'bpm:process-listener:query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000053', '1909040000000000033', '监听器创建', 0, 2, 'bpm:process-listener:create', '1', 2, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000054', '1909040000000000033', '监听器更新', 0, 2, 'bpm:process-listener:update', '1', 3, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000055', '1909040000000000033', '监听器删除', 0, 2, 'bpm:process-listener:delete', '1', 4, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000056', '1909040000000000034', '表达式查询', 0, 2, 'bpm:process-expression:query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000057', '1909040000000000034', '表达式创建', 0, 2, 'bpm:process-expression:create', '1', 2, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000058', '1909040000000000034', '表达式更新', 0, 2, 'bpm:process-expression:update', '1', 3, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000059', '1909040000000000034', '表达式删除', 0, 2, 'bpm:process-expression:delete', '1', 4, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`), `name` = VALUES(`name`), `perms` = VALUES(`perms`),
`sort_no` = VALUES(`sort_no`), `del_flag` = 0, `status` = '1';
