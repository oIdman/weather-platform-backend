-- Keep existing workflow roles usable after introducing the RuoYi/Vben-style menu hierarchy.
-- Parent menu grants are derived from the pages a role already owned.

INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(existing_role.`role_id`, ':1909040000000000001')),
       existing_role.`role_id`,
       '1909040000000000001',
       NULL,
       NOW(),
       '127.0.0.1'
FROM (
    SELECT DISTINCT `role_id`
    FROM `sys_role_permission`
    WHERE `permission_id` IN (
        '1909040000000000002', '1909040000000000003', '1909040000000000004',
        '1909040000000000005', '1909040000000000006'
    )
) existing_role
WHERE NOT EXISTS (
    SELECT 1
    FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = existing_role.`role_id`
      AND current_grant.`permission_id` = '1909040000000000001'
);

INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(existing_role.`role_id`, ':1909040000000000020')),
       existing_role.`role_id`,
       '1909040000000000020',
       NULL,
       NOW(),
       '127.0.0.1'
FROM (
    SELECT DISTINCT `role_id`
    FROM `sys_role_permission`
    WHERE `permission_id` IN ('1909040000000000002', '1909040000000000006')
) existing_role
WHERE NOT EXISTS (
    SELECT 1
    FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = existing_role.`role_id`
      AND current_grant.`permission_id` = '1909040000000000020'
);

INSERT INTO `sys_role_permission`
(`id`, `role_id`, `permission_id`, `data_rule_ids`, `operate_date`, `operate_ip`)
SELECT MD5(CONCAT(existing_role.`role_id`, ':1909040000000000021')),
       existing_role.`role_id`,
       '1909040000000000021',
       NULL,
       NOW(),
       '127.0.0.1'
FROM (
    SELECT DISTINCT `role_id`
    FROM `sys_role_permission`
    WHERE `permission_id` IN (
        '1909040000000000003', '1909040000000000004', '1909040000000000005'
    )
) existing_role
WHERE NOT EXISTS (
    SELECT 1
    FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = existing_role.`role_id`
      AND current_grant.`permission_id` = '1909040000000000021'
);

-- A role that owned all five legacy workflow pages is treated as a full workflow role.
-- It inherits the newly introduced management pages and their button permissions.
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
      '1909040000000000011', '1909040000000000012', '1909040000000000013',
      '1909040000000000014', '1909040000000000030', '1909040000000000031',
      '1909040000000000032', '1909040000000000033', '1909040000000000034',
      '1909040000000000040', '1909040000000000041', '1909040000000000042',
      '1909040000000000043', '1909040000000000044', '1909040000000000045',
      '1909040000000000046', '1909040000000000047', '1909040000000000048',
      '1909040000000000049', '1909040000000000050', '1909040000000000051',
      '1909040000000000052', '1909040000000000053', '1909040000000000054',
      '1909040000000000055', '1909040000000000056', '1909040000000000057',
      '1909040000000000058', '1909040000000000059'
  )
WHERE NOT EXISTS (
    SELECT 1
    FROM `sys_role_permission` current_grant
    WHERE current_grant.`role_id` = full_workflow_role.`role_id`
      AND current_grant.`permission_id` = target_permission.`id`
);
