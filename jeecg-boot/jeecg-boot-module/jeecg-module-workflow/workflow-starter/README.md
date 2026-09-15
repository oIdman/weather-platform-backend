# Jeecg 工作流独立启动器

`jeecg-workflow-starter` 是工作流模块的独立 Spring Boot 启动入口，不依赖 `jeecg-system-start` 的启动类。它复用 `workflow-api`、`workflow-biz` 和 Flowable 数据表，默认监听 `8890`，避免与现有单体 `8880` 冲突。

## 启动

```powershell
mvn -f jeecg-boot/pom.xml -Pdev -pl jeecg-boot-module/jeecg-module-workflow/workflow-starter -am spring-boot:run
```

启动前请通过环境变量或外部配置覆盖 `spring.datasource.*`，不要使用示例密码。启动器默认提供一个可配置的身份适配器和 `ISysBaseAPI` 兼容 Bean，支持最小闭环所需的用户、角色、部门、岗位、部门负责人和父部门映射；生产部署仍建议提供自己的 `WorkflowIdentityAdapter`、`WorkflowCandidateIdentityAdapter` 和用户目录实现。

最小候选人配置示例：

```yaml
jeecg:
  workflow:
    standalone:
      user-id: workflow-system
      username: workflow-system
      tenant-id: '0'
      role-users:
        project-leader: [u001]
      department-users:
        research: [u001, u002]
      position-users:
        reviewer: [u002]
      user-group-users:
        '1001': [u001, u002]
      department-leaders:
        research: u001
      department-leader-users:
        research: [u001, u003]
      parent-departments:
        research: company
      user-names:
        u001: 张三
        u002: 李四
```

上述映射会同时用于候选人策略、连续部门负责人以及流程评论/抄送中的用户名称解析。

独立服务与 Jeecg 单体共享工作流 API 契约，但身份、租户和组织查询通过适配器隔离，因此不会把 `system-biz` 强耦合到工作流核心。
