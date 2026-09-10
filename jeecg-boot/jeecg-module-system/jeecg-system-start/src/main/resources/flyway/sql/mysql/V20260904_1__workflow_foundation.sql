-- Workflow foundation adapted from ruoyi-vue-pro/sql/mysql/bpm-2026-04-18.sql.
-- Only reusable metadata is migrated. Demo data and bpm_oa_leave are intentionally excluded.

CREATE TABLE IF NOT EXISTS `bpm_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `code` varchar(64) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `sort` int NOT NULL DEFAULT 0,
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `tenant_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bpm_category_tenant_code` (`tenant_id`, `code`),
  KEY `idx_bpm_category_tenant_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流分类';

CREATE TABLE IF NOT EXISTS `bpm_form` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `conf` longtext NOT NULL,
  `fields` longtext NOT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `tenant_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_bpm_form_tenant_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流表单配置';

CREATE TABLE IF NOT EXISTS `bpm_process_definition_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `process_definition_id` varchar(64) NOT NULL,
  `model_id` varchar(64) DEFAULT NULL,
  `model_type` tinyint NOT NULL DEFAULT 10,
  `category` varchar(64) DEFAULT NULL,
  `icon` varchar(512) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `form_type` tinyint NOT NULL DEFAULT 20,
  `form_id` bigint DEFAULT NULL,
  `form_conf` longtext,
  `form_fields` longtext,
  `form_custom_create_path` varchar(255) DEFAULT NULL,
  `form_custom_view_path` varchar(255) DEFAULT NULL,
  `simple_model` longtext,
  `sort` bigint NOT NULL DEFAULT 0,
  `visible` tinyint NOT NULL DEFAULT 1,
  `start_user_ids` longtext,
  `start_dept_ids` longtext,
  `manager_user_ids` longtext,
  `allow_cancel_running_process` tinyint NOT NULL DEFAULT 1,
  `allow_withdraw_task` tinyint NOT NULL DEFAULT 0,
  `process_id_rule` varchar(255) DEFAULT NULL,
  `auto_approval_type` tinyint NOT NULL DEFAULT 0,
  `title_setting` varchar(512) DEFAULT NULL,
  `summary_setting` varchar(1024) DEFAULT NULL,
  `print_template_setting` longtext,
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `tenant_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bpm_definition_info_definition` (`process_definition_id`),
  KEY `idx_bpm_definition_info_tenant_category` (`tenant_id`, `category`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流流程定义扩展';

CREATE TABLE IF NOT EXISTS `bpm_node_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `process_definition_id` varchar(64) NOT NULL,
  `node_key` varchar(128) NOT NULL,
  `node_name` varchar(255) DEFAULT NULL,
  `form_view_url` varchar(255) DEFAULT NULL,
  `form_edit_enabled` tinyint NOT NULL DEFAULT 0,
  `field_permission_json` longtext,
  `button_permission_json` longtext,
  `node_extension_json` longtext,
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `tenant_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bpm_node_definition_key` (`process_definition_id`, `node_key`),
  KEY `idx_bpm_node_tenant` (`tenant_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流节点扩展配置';

CREATE TABLE IF NOT EXISTS `bpm_process_expression` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `expression` varchar(1024) NOT NULL,
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `tenant_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_bpm_expression_tenant_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流表达式';

CREATE TABLE IF NOT EXISTS `bpm_process_listener` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `type` varchar(64) NOT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `event` varchar(64) NOT NULL,
  `value_type` varchar(64) NOT NULL,
  `value` varchar(1024) NOT NULL,
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `tenant_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_bpm_listener_tenant_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流监听器配置';

CREATE TABLE IF NOT EXISTS `bpm_user_group` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `user_ids` longtext,
  `status` tinyint NOT NULL DEFAULT 1,
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `tenant_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_bpm_user_group_tenant_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流用户组';

CREATE TABLE IF NOT EXISTS `bpm_process_instance_copy` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(36) NOT NULL,
  `start_user_id` varchar(36) NOT NULL,
  `process_instance_id` varchar(64) NOT NULL,
  `process_instance_name` varchar(255) NOT NULL,
  `process_definition_id` varchar(64) NOT NULL,
  `category` varchar(64) DEFAULT NULL,
  `activity_id` varchar(64) DEFAULT NULL,
  `activity_name` varchar(255) DEFAULT NULL,
  `task_id` varchar(64) DEFAULT NULL,
  `reason` varchar(512) DEFAULT NULL,
  `creator` varchar(64) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT NULL,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `tenant_id` int NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_bpm_copy_user` (`tenant_id`, `user_id`, `deleted`),
  KEY `idx_bpm_copy_process` (`process_instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流抄送记录';

-- Menus and permissions. IDs are stable so upgrades remain idempotent.
INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `create_by`, `create_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('1909040000000000001', NULL, '工作流', '/workflow', 'layouts/default/index', 1, 'workflow', '/workflow/task/todo', 0, NULL, '1', 30, 1, 'ant-design:deployment-unit-outlined', 0, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000002', '1909040000000000001', '流程定义', '/workflow/definition', 'workflow/definition/index', 1, 'workflow-definition', NULL, 1, 'workflow:definition:list', '1', 1, 0, NULL, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000003', '1909040000000000001', '我的待办', '/workflow/task/todo', 'workflow/task/todo', 1, 'workflow-task-todo', NULL, 1, 'workflow:task:list', '1', 2, 0, NULL, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000004', '1909040000000000001', '我的已办', '/workflow/task/done', 'workflow/task/done', 1, 'workflow-task-done', NULL, 1, 'workflow:task:list', '1', 3, 0, NULL, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000005', '1909040000000000001', '我的申请', '/workflow/process/my', 'workflow/process/my', 1, 'workflow-process-my', NULL, 1, 'workflow:process:list', '1', 4, 0, NULL, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000006', '1909040000000000001', '流程实例', '/workflow/process/manager', 'workflow/process/manager', 1, 'workflow-process-manager', NULL, 1, 'workflow:process:manager', '1', 5, 0, NULL, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000011', '1909040000000000002', '部署流程', NULL, NULL, 0, NULL, NULL, 2, 'workflow:definition:deploy', '1', 1, 0, NULL, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000012', '1909040000000000003', '处理任务', NULL, NULL, 0, NULL, NULL, 2, 'workflow:task:handle', '1', 1, 0, NULL, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000013', '1909040000000000005', '发起流程', NULL, NULL, 0, NULL, NULL, 2, 'workflow:process:start', '1', 1, 0, NULL, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000014', '1909040000000000005', '取消流程', NULL, NULL, 0, NULL, NULL, 2, 'workflow:process:cancel', '1', 2, 0, NULL, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`name` = VALUES(`name`), `url` = VALUES(`url`), `component` = VALUES(`component`),
`perms` = VALUES(`perms`), `del_flag` = 0, `status` = '1';
