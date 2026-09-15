-- Add the hidden data-report route opened from a published process model.

INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `create_by`, `create_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('1909100000000000001', '1909040000000000001', '数据报表', '/bpm/process-instance/report', 'bpm/processInstance/report/index', 1, 'BpmProcessInstanceReport', NULL, 1, NULL, '1', 98, 0, 'ant-design:bar-chart-outlined', 1, 0, 1, 0, 'admin', NOW(), 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`), `name` = VALUES(`name`), `url` = VALUES(`url`),
`component` = VALUES(`component`), `component_name` = VALUES(`component_name`),
`is_route` = 1, `menu_type` = 1, `is_leaf` = 1, `keep_alive` = 0,
`hidden` = 1, `del_flag` = 0, `status` = '1';

-- Only roles that already have process-instance manager access receive the hidden route.
INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(source_role.`role_id`, ':1909100000000000001')), source_role.`role_id`, '1909100000000000001', NULL, NOW(), '127.0.0.1'
FROM (
    SELECT DISTINCT `role_id`
    FROM `sys_role_permission`
    WHERE `permission_id` IN ('1909040000000000006', '1909040000000000072')
) source_role
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = source_role.`role_id`
      AND current_grant.`permission_id` = '1909100000000000001'
);
