-- Complete the RuoYi/Yudao workflow menu hierarchy and add process copy storage.

CREATE TABLE IF NOT EXISTS `bpm_process_instance_copy` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
    `user_id` varchar(64) NOT NULL COMMENT '被抄送用户编号',
    `start_user_id` varchar(64) NOT NULL COMMENT '流程发起用户编号',
    `process_instance_id` varchar(64) NOT NULL COMMENT '流程实例编号',
    `process_instance_name` varchar(128) NOT NULL COMMENT '流程实例名称',
    `process_definition_id` varchar(128) NOT NULL COMMENT '流程定义编号',
    `category` varchar(64) DEFAULT NULL COMMENT '流程分类编码',
    `activity_id` varchar(128) DEFAULT NULL COMMENT '流程活动编号',
    `activity_name` varchar(128) DEFAULT NULL COMMENT '流程活动名称',
    `task_id` varchar(64) DEFAULT NULL COMMENT '任务编号',
    `reason` varchar(512) DEFAULT NULL COMMENT '抄送意见',
    `creator` varchar(64) DEFAULT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updater` varchar(64) DEFAULT NULL,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` tinyint NOT NULL DEFAULT 0,
    `tenant_id` bigint NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_bpm_copy_user_tenant` (`user_id`, `tenant_id`, `deleted`),
    KEY `idx_bpm_copy_instance` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BPM 流程实例抄送表';

UPDATE `sys_permission`
SET `redirect` = '/bpm/task/create'
WHERE `id` = '1909040000000000001';

UPDATE `sys_permission`
SET `redirect` = '/bpm/manager/model'
WHERE `id` = '1909040000000000020';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000020',
    `name` = '流程实例',
    `url` = '/bpm/manager/process-instance',
    `component` = 'bpm/processInstance/manager/index',
    `component_name` = 'BpmProcessInstanceManager',
    `perms` = NULL,
    `sort_no` = 10,
    `hidden` = 0,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000006';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000021',
    `name` = '发起流程',
    `url` = '/bpm/task/create',
    `component` = 'bpm/processInstance/create/index',
    `component_name` = 'BpmProcessInstanceCreate',
    `perms` = NULL,
    `sort_no` = 0,
    `keep_alive` = 0,
    `hidden` = 0,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000005';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000021',
    `name` = '待办任务',
    `url` = '/bpm/task/todo',
    `component` = 'bpm/task/todo/index',
    `component_name` = 'BpmTodoTask',
    `perms` = NULL,
    `sort_no` = 10,
    `hidden` = 0,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000003';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000021',
    `name` = '已办任务',
    `url` = '/bpm/task/done',
    `component` = 'bpm/task/done/index',
    `component_name` = 'BpmDoneTask',
    `perms` = NULL,
    `sort_no` = 20,
    `hidden` = 0,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000004';

INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `create_by`, `create_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('1909040000000000036', '1909040000000000021', '我的流程', '/bpm/task/my', 'bpm/processInstance/index', 1, 'BpmProcessInstanceMy', NULL, 1, NULL, '1', 1, 0, 'ant-design:book-outlined', 1, 1, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000037', '1909040000000000020', '流程任务', '/bpm/manager/process-task', 'bpm/task/manager/index', 1, 'BpmManagerTask', NULL, 1, NULL, '1', 11, 0, 'ant-design:tags-outlined', 1, 1, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000038', '1909040000000000021', '抄送我的', '/bpm/task/copy', 'bpm/task/copy/index', 1, 'BpmProcessInstanceCopy', NULL, 1, 'bpm:process-instance-cc:query', '1', 30, 0, 'ant-design:copy-outlined', 1, 1, 0, 0, 'admin', NOW(), 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`), `name` = VALUES(`name`), `url` = VALUES(`url`),
`component` = VALUES(`component`), `component_name` = VALUES(`component_name`),
`perms` = VALUES(`perms`), `sort_no` = VALUES(`sort_no`), `is_leaf` = VALUES(`is_leaf`),
`keep_alive` = VALUES(`keep_alive`), `hidden` = 0, `del_flag` = 0, `status` = '1';

-- Reuse legacy button records so existing role associations keep working.
UPDATE `sys_permission`
SET `parent_id` = '1909040000000000036',
    `name` = '流程实例的创建',
    `perms` = 'bpm:process-instance:create',
    `sort_no` = 2,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000013';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000036',
    `name` = '流程实例的取消',
    `perms` = 'bpm:process-instance:cancel',
    `sort_no` = 3,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000014';

UPDATE `sys_permission`
SET `parent_id` = '1909040000000000003',
    `name` = '流程任务的更新',
    `perms` = 'bpm:task:update',
    `sort_no` = 2,
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000012';

INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `is_route`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `create_by`, `create_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('1909040000000000068', '1909040000000000036', '流程实例的查询', 0, 2, 'bpm:process-instance:query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000069', '1909040000000000035', '流程定义查询', 0, 2, 'bpm:process-definition:query', '1', 10, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000070', '1909040000000000003', '流程任务的查询', 0, 2, 'bpm:task:query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000072', '1909040000000000006', '流程实例的查询（管理员）', 0, 2, 'bpm:process-instance:manager-query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000073', '1909040000000000037', '流程任务的查询（管理员）', 0, 2, 'bpm:task:manager-query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000074', '1909040000000000006', '流程实例的取消（管理员）', 0, 2, 'bpm:process-instance:cancel-by-admin', '1', 2, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`), `name` = VALUES(`name`), `perms` = VALUES(`perms`),
`sort_no` = VALUES(`sort_no`), `del_flag` = 0, `status` = '1';

-- The former combined "我的申请" role receives the two split approval pages and their actions.
INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(source_role.`role_id`, ':', target_permission.`id`)), source_role.`role_id`, target_permission.`id`, NULL, NOW(), '127.0.0.1'
FROM (SELECT DISTINCT `role_id` FROM `sys_role_permission` WHERE `permission_id` = '1909040000000000005') source_role
JOIN `sys_permission` target_permission
  ON target_permission.`id` IN ('1909040000000000036', '1909040000000000068', '1909040000000000013', '1909040000000000014', '1909040000000000069')
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = source_role.`role_id` AND current_grant.`permission_id` = target_permission.`id`
);

-- Todo and done users receive target-compatible task permissions and the copy inbox.
INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(source_role.`role_id`, ':', target_permission.`id`)), source_role.`role_id`, target_permission.`id`, NULL, NOW(), '127.0.0.1'
FROM (
    SELECT DISTINCT `role_id` FROM `sys_role_permission`
    WHERE `permission_id` IN ('1909040000000000003', '1909040000000000004', '1909040000000000005')
) source_role
JOIN `sys_permission` target_permission
  ON target_permission.`id` IN ('1909040000000000070', '1909040000000000012', '1909040000000000038')
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = source_role.`role_id` AND current_grant.`permission_id` = target_permission.`id`
);

-- Existing process administrators also receive the target manager pages and actions.
INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(source_role.`role_id`, ':', target_permission.`id`)), source_role.`role_id`, target_permission.`id`, NULL, NOW(), '127.0.0.1'
FROM (SELECT DISTINCT `role_id` FROM `sys_role_permission` WHERE `permission_id` = '1909040000000000006') source_role
JOIN `sys_permission` target_permission
  ON target_permission.`id` IN ('1909040000000000037', '1909040000000000072', '1909040000000000073', '1909040000000000074')
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = source_role.`role_id` AND current_grant.`permission_id` = target_permission.`id`
);

-- Keep parent folders visible for every role that owns one of the aligned workflow leaves.
INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(source_role.`role_id`, ':', parent_permission.`id`)), source_role.`role_id`, parent_permission.`id`, NULL, NOW(), '127.0.0.1'
FROM (
    SELECT DISTINCT `role_id` FROM `sys_role_permission`
    WHERE `permission_id` IN (
        '1909040000000000035', '1909040000000000030', '1909040000000000031', '1909040000000000032',
        '1909040000000000033', '1909040000000000034', '1909040000000000006', '1909040000000000037',
        '1909040000000000005', '1909040000000000036', '1909040000000000003', '1909040000000000004', '1909040000000000038'
    )
) source_role
JOIN `sys_permission` parent_permission
  ON parent_permission.`id` IN ('1909040000000000001', '1909040000000000020', '1909040000000000021')
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = source_role.`role_id` AND current_grant.`permission_id` = parent_permission.`id`
);
