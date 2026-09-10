-- Add the RuoYi/Vben workflow model entry. The old direct-deployment page remains as a hidden compatibility route.

UPDATE `sys_permission`
SET `redirect` = '/bpm/manager/model'
WHERE `id` = '1909040000000000020';

UPDATE `sys_permission`
SET `hidden` = 1,
    `sort_no` = 99
WHERE `id` = '1909040000000000002';

INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `url`, `component`, `is_route`, `component_name`, `redirect`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `icon`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `create_by`, `create_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('1909040000000000035', '1909040000000000020', '流程模型', '/bpm/manager/model', 'bpm/model/index', 1, 'BpmModel', NULL, 1, NULL, '1', 1, 0, 'ant-design:deployment-unit-outlined', 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`), `name` = VALUES(`name`), `url` = VALUES(`url`),
`component` = VALUES(`component`), `component_name` = VALUES(`component_name`),
`sort_no` = VALUES(`sort_no`), `is_leaf` = VALUES(`is_leaf`), `hidden` = 0,
`del_flag` = 0, `status` = '1';

INSERT INTO `sys_permission`
(`id`, `parent_id`, `name`, `is_route`, `menu_type`, `perms`, `perms_type`, `sort_no`, `always_show`, `is_leaf`, `keep_alive`, `hidden`, `hide_tab`, `create_by`, `create_time`, `del_flag`, `rule_flag`, `status`, `internal_or_external`)
VALUES
('1909040000000000060', '1909040000000000035', '模型查询', 0, 2, 'bpm:model:query', '1', 1, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000061', '1909040000000000035', '模型创建', 0, 2, 'bpm:model:create', '1', 2, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000062', '1909040000000000035', '模型更新', 0, 2, 'bpm:model:update', '1', 3, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000063', '1909040000000000035', '模型删除', 0, 2, 'bpm:model:delete', '1', 4, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000064', '1909040000000000035', '模型发布', 0, 2, 'bpm:model:deploy', '1', 5, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000065', '1909040000000000035', '模型导入', 0, 2, 'bpm:model:import', '1', 6, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000066', '1909040000000000035', '模型导出', 0, 2, 'bpm:model:export', '1', 7, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0),
('1909040000000000067', '1909040000000000035', '模型清理', 0, 2, 'bpm:model:clean', '1', 8, 0, 1, 0, 0, 0, 'admin', NOW(), 0, 0, '1', 0)
ON DUPLICATE KEY UPDATE
`parent_id` = VALUES(`parent_id`), `name` = VALUES(`name`), `perms` = VALUES(`perms`),
`sort_no` = VALUES(`sort_no`), `del_flag` = 0, `status` = '1';

INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(full_workflow_role.`role_id`, ':', target_permission.`id`)),
       full_workflow_role.`role_id`,
       target_permission.`id`,
       NULL,
       NOW(),
       '127.0.0.1'
FROM (
    SELECT `role_id`
    FROM `sys_role_permission`
    WHERE `permission_id` IN (
        '1909040000000000002', '1909040000000000003', '1909040000000000004',
        '1909040000000000005', '1909040000000000006'
    )
    GROUP BY `role_id`
    HAVING COUNT(DISTINCT `permission_id`) = 5
) full_workflow_role
JOIN `sys_permission` target_permission
  ON target_permission.`id` IN (
      '1909040000000000035', '1909040000000000060', '1909040000000000061',
      '1909040000000000062', '1909040000000000063', '1909040000000000064',
      '1909040000000000065', '1909040000000000066', '1909040000000000067'
  )
WHERE NOT EXISTS (
    SELECT 1
    FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = full_workflow_role.`role_id`
      AND current_grant.`permission_id` = target_permission.`id`
);
