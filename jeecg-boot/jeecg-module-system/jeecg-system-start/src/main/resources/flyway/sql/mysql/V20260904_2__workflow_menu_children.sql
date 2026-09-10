-- Fix Vue3 menu rendering: always_show=1 suppresses child menus in the
-- current JeecgBoot menu component. The workflow root must expose children.
UPDATE `sys_permission`
SET `always_show` = 0,
    `is_leaf` = 0,
    `redirect` = '/workflow/task/todo',
    `del_flag` = 0,
    `status` = '1'
WHERE `id` = '1909040000000000001';
