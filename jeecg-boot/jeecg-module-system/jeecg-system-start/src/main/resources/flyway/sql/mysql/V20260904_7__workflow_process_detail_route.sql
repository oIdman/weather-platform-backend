-- Add the hidden process detail route shared by todo, done, copied and manager lists.

INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `create_by`, `create_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('1909040000000000039', '1909040000000000001', '流程详情', '/bpm/process-instance/detail', 'bpm/processInstance/detail/index', 1, 'BpmProcessInstanceDetail', NULL, 1, NULL, '1', 99, 0, NULL, 1, 0, 1, 0, 'admin', NOW(), 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`), `name` = VALUES(`name`), `url` = VALUES(`url`),
`component` = VALUES(`component`), `component_name` = VALUES(`component_name`),
`is_route` = 1, `menu_type` = 1, `is_leaf` = 1, `keep_alive` = 0,
`hidden` = 1, `del_flag` = 0, `status` = '1';

INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(source_role.`role_id`, ':1909040000000000039')), source_role.`role_id`, '1909040000000000039', NULL, NOW(), '127.0.0.1'
FROM (
    SELECT DISTINCT `role_id` FROM `sys_role_permission`
    WHERE `permission_id` IN (
        '1909040000000000003', '1909040000000000004', '1909040000000000005',
        '1909040000000000006', '1909040000000000036', '1909040000000000037',
        '1909040000000000038'
    )
) source_role
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = source_role.`role_id`
      AND current_grant.`permission_id` = '1909040000000000039'
);
