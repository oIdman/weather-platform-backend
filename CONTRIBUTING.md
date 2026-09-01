# 贡献指南

感谢你参与本项目。本仓库是基于 JeecgBoot 3.9.5 的前后端一体化代码库，同时保留单体和 Spring Cloud 两种运行方式。提交代码前请先阅读 [项目目标](docs/PROJECT_GOAL.md)、[架构说明](docs/ARCHITECTURE.md)、[开发指南](docs/DEVELOPMENT.md) 和 [安全规范](docs/SECURITY.md)。自动化 Agent 还必须遵守 [AGENTS.md](AGENTS.md)。

## 开始之前

- 先确认需求、影响模块和验收条件；不要把无关重构、格式化或依赖升级混入同一变更。
- 检查工作区已有修改并保留他人的工作，不要使用 `git reset --hard`、`git checkout --` 等破坏性命令。
- 不提交密码、Token、API Key、真实连接串、生产域名或个人数据。使用本地环境文件、环境变量或部署平台的密钥管理能力。
- 当前仓库主要是 JeecgBoot 平台基线。`weather` 命名的预留目录目前不代表已经实现天气业务；新增业务前应先确认产品需求和模块边界。

## 开发环境

推荐使用以下版本：

- JDK 17（项目编译基线；官方还支持 JDK 21/24）
- Maven 3.9.x
- Node.js 20.19+（本地 `package.json` 仍声明兼容 Node 18，但 Vite 8 和官方文档均应优先使用 Node 20+）
- pnpm 9+
- MySQL 8.0、Redis
- 可选：Docker、Docker Compose、PostgreSQL/pgvector、MongoDB、RabbitMQ

可以在仓库根目录执行 `python check_jeecgenv.py` 检查环境。完整步骤见 [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md)。

## 建议工作流

1. 从最新目标分支创建短生命周期分支。
2. 先复现问题或写明验收条件，再进行最小范围修改。
3. 后端业务代码放在对应业务模块，不把业务逻辑堆入 `jeecg-boot-base-core` 或启动模块。
4. 前端页面、API、类型和权限标识成组修改；后端权限标识必须与菜单/按钮配置一致。
5. 涉及数据库结构时增加可审查的增量 SQL/Flyway 脚本，不修改已经发布的历史迁移。
6. 运行与改动匹配的测试、静态检查和构建。
7. 检查 `git diff`，确认没有密钥、生成物、无关格式化或调试代码。

## 代码规范

### Java 后端

- 使用 `org.jeecg.modules.<业务模块>` 包结构，保持 Controller、Service、Mapper、Entity/VO 分层。
- Controller 只负责参数校验、权限边界和响应组装；业务规则进入 Service；SQL 进入 Mapper/XML。
- 使用项目统一的 `Result<T>` 响应，不另造返回协议。
- 复用 MyBatis-Plus、`QueryGenerator` 和现有基础设施，但不得直接信任前端传入的排序字段、表名、SQL 片段或文件路径。
- 新接口应明确请求权限；行级隔离按需要使用 `@PermissionData`，并验证租户/组织边界。
- 命名清晰，常量代替魔法值；复杂逻辑写“为什么”，不要重复描述代码。
- 只格式化本次改动。修改采用 `update-begin`/`update-end` 追踪注释的既有模块时，延续相邻格式；没有需求号或作者信息时先向维护者确认，不要杜撰。

### Vue/TypeScript 前端

- 使用 Vue 3、TypeScript、Composition API 和项目现有组件/Hook；路径别名优先使用 `/@/`。
- API 定义放在 `src/api/` 或对应页面的 `*.api.ts`，数据模型放在类型文件或 `*.data.ts`，避免在组件中散落请求逻辑。
- 保持 2 空格缩进、单引号、150 字符行宽以及现有 Prettier/Stylelint 配置。
- 非首屏重型依赖使用动态导入，避免扩大初始包体。
- 用户可见文本应考虑现有 i18n 结构；权限按钮应复用后端权限标识。
- 不在客户端代码或 `VITE_*` 变量中放秘密；所有被打包的前端变量都应视为公开信息。

## 测试与验证

按改动范围选择命令，不要求为纯文档变更构建整个项目：

```bash
# 后端：单体开发配置
mvn -f jeecg-boot/pom.xml -Pdev test

# 后端：仅构建单体启动模块及其依赖
mvn -f jeecg-boot/pom.xml -Pdev -pl jeecg-module-system/jeecg-system-start -am package

# 前端
pnpm --dir jeecgboot-vue3 exec eslint <changed-files>
pnpm --dir jeecgboot-vue3 exec stylelint "src/**/*.{vue,css,less,scss}"
pnpm --dir jeecgboot-vue3 exec jest
pnpm --dir jeecgboot-vue3 build
```

仓库当前没有统一的前端 `lint`/`test` script，因此不要在文档或 CI 中假设 `pnpm lint`、`pnpm test` 存在。

## 提交与评审

前端配置了 Conventional Commits。推荐提交格式：

```text
feat(module): 简要说明
fix(module): 简要说明
docs: 简要说明
```

Pull Request 应说明：

- 背景、目标和不在范围内的内容；
- 关键设计与受影响模块；
- 数据库、配置、权限或 API 兼容性影响；
- 已执行的验证命令及结果；
- UI 变更截图或接口示例（如适用）；
- 风险、回滚方式和后续事项。

## 参考资料

- [JEECG Java 文档中心](https://help.jeecg.com/java/)
- [官方开发环境搭建](https://help.jeecg.com/java/setup/tools.html)
- [官方编码规范](https://help.jeecg.com/java/norm/code/)
- [官方代码生成配置](https://help.jeecg.com/java/codegen/config/)
