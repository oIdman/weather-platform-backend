# 工作流数据库安装包

`workflow-install-mysql.sql` 是本项目当前工作流改动的合并安装包，包含：

- Flowable 8.0.0 的 common、engine、history 全部运行表（包括 `ACT_GE_PROPERTY`）；
- `bpm_category`、`bpm_form`、`bpm_process_definition_info`、`bpm_node_config`、表达式、监听器、用户组、抄送等业务表；
- 工作流菜单、按钮权限、角色权限继承、流程详情和报表路由。

## 导入顺序

1. 先创建并选中 JeecgBoot 目标数据库，再导入 `jeecg-boot/db/jeecgboot-mysql-5.7.sql`。
2. 在同一个数据库中执行 `workflow-install-mysql.sql`。脚本不包含 `USE`，不会强制使用某个数据库名。
3. 再启动 `jeecg-system-start`。当前单体配置已关闭 Flowable 运行时自动建表，因此必须先完成第 2 步。

PowerShell 示例（按实际账号、数据库名和脚本路径修改）：

```powershell
mysql -h 127.0.0.1 -P 3306 -u root -p jeecg_boot < .\docs\sql\workflow-install-mysql.sql
```

脚本包含官方 Flowable 外键和索引，建议只在目标数据库首次安装工作流时执行一次，并在执行前备份数据库。项目中的 Flyway 文件仍保留为增量迁移来源；如果使用本合并包手工安装，不要再把同一批 SQL 复制执行第二次。

## 对应的增量文件

合并包中的 Jeecg 工作流增量来自：

- `V20260904_1__workflow_foundation.sql`
- `V20260904_2__workflow_menu_children.sql`
- `V20260904_3__workflow_management_metadata.sql`
- `V20260904_4__workflow_role_permission_inheritance.sql`
- `V20260904_5__workflow_model_menu.sql`
- `V20260904_6__workflow_task_center_alignment.sql`
- `V20260904_7__workflow_process_detail_route.sql`
- `V20260910_1__workflow_process_instance_report_route.sql`

这些文件位于 `jeecg-boot/jeecg-module-system/jeecg-system-start/src/main/resources/flyway/sql/mysql/`，用于项目自身的 Flyway 增量管理；交付给其他用户时直接执行合并包即可。
