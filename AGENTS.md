# AGENTS.md

本文件适用于整个仓库。子目录若存在更具体的 `AGENTS.md`，则在其作用域内补充或覆盖本文件。

## 任务目标

以最小、可验证的改动维护 JeecgBoot 3.9.5 项目。先理解需求和现有实现，再修改；不要推测未定义业务。当前仓库主要是平台基线，空的 `weather` 预留目录不代表存在可依赖的天气业务实现。

## 仓库地图

- `jeecg-boot/`：Maven 后端父工程。
  - `jeecg-boot-base-core/`：通用基础设施、配置、注解、权限和公共能力。非基础能力不要放入这里。
  - `jeecg-module-system/jeecg-system-biz/`：系统管理业务。
  - `jeecg-module-system/jeecg-system-start/`：单体启动模块，默认端口 8080。
  - `jeecg-boot-module/`：业务/低代码/AI 模块；新增业务优先放在独立子模块或既有业务模块。
  - `jeecg-server-cloud/`：Nacos、Gateway、微服务启动和可观测性组件；普通单体需求不要无故修改。
  - `db/`：初始化 SQL。增量升级脚本位于启动模块的 Flyway 目录。
- `jeecgboot-vue3/`：Vue 3 + TypeScript 前端，开发端口 3100。
- `docker-compose.yml`：单体编排；`docker-compose-cloud.yml`：微服务编排。
- `docs/`：架构、开发、安全和项目目标文档。

## 动手前必须做

1. 阅读与任务有关的文件、相邻测试、模块 `pom.xml`/`package.json` 和配置。
2. 执行 `git status --short`，保留已有修改；不要重置或覆盖他人工作。
3. 写出可验证的成功条件。修 Bug 时优先先复现或增加失败测试。
4. 如果官方文档与仓库不同，以仓库实际版本为准，并记录差异。

## 修改边界

- 只改需求直接涉及的行和文件；不做顺手重构、全文件格式化或无关依赖升级。
- 不编辑 `target/`、`node_modules/`、`dist/`、日志、IDE 文件等生成物。
- 不自动提交、推送、创建 PR、部署或修改外部系统。
- 不把单体专用实现直接耦合到 cloud API，反之亦然；共享契约放 API 模块，实现留在 biz/start 模块。
- 不直接修改已经发布的 Flyway 脚本；新增递增且唯一版本的迁移，并考虑回滚/兼容。
- 代码生成只用于骨架。生成后必须检查包名、路径、权限、字段校验、租户/数据权限、SQL 和前端类型。

## 后端规范

- Java 基线为 17；保持 Spring Boot 4.1、MyBatis-Plus 和项目现有依赖体系，未经要求不升级。
- 包结构遵循 `org.jeecg.modules.<module>`；保持 Controller → Service → Mapper 分层。
- Controller 保持薄：校验输入、声明权限、调用 Service、返回 `Result<T>`。事务和业务规则放 Service。
- Mapper 使用参数绑定；禁止拼接用户可控 SQL、表名、列名、排序表达式或数据权限片段。
- 新增或修改写操作必须检查认证、`@RequiresPermissions` 权限标识、对象归属、租户和组织边界。
- 需要行级数据隔离时使用项目 `@PermissionData` 机制，并为绕过场景增加测试。
- 实体/DTO/VO 边界清晰；不要直接用持久化实体承接不受信任的批量更新字段。
- 日志不得记录密码、Token、验证码、Cookie、API Key、完整个人信息或敏感请求体。
- 使用常量代替魔法值；复杂逻辑解释原因。新增代码格式化，老代码只格式化改动区域。
- 若所在模块使用 `update-begin`/`update-end` 变更追踪注释，严格延续相邻格式；缺少真实作者、日期或需求号时先询问，禁止编造。

## 前端规范

- 使用 Vue 3 Composition API、TypeScript、Pinia 和现有 Jeecg/Ant Design Vue 组件。
- 路径别名优先 `/@/`；API 请求放 `src/api/` 或页面 `*.api.ts`，类型/表格定义放对应类型或 `*.data.ts`。
- 遵守项目 Prettier：2 空格、单引号、`printWidth: 150`、ES5 trailing comma；不要格式化无关文件。
- 非首屏重型依赖使用动态 `import()`；避免把大组件加入启动路径。
- 后端返回遵循 `{ code, result, message, success }`；不要在页面重复实现全局 Axios、鉴权或错误处理。
- 菜单/按钮可见性不是安全边界；前端权限标识必须与后端校验一致。
- `VITE_*` 会进入客户端包，绝不能放秘密。渲染富文本、URL、文件名和服务端错误时保持转义/白名单策略。
- 用户可见文本按现有 i18n 结构维护；避免只硬编码一种语言，除非相邻模块明确如此。

## 安全硬规则

- 永不提交真实凭据。配置示例使用明显占位符；生产密钥通过环境或密钥管理系统注入。
- 不扩大 Shiro 白名单、CORS、文件类型、上传大小、网关匿名路由或数据源访问范围，除非任务明确且有安全验证。
- 文件上传必须校验大小、类型、扩展名、存储路径和访问权限；服务端生成文件名，防止路径穿越。
- 所有查询和写操作都要考虑租户、组织和对象级授权；仅验证“已登录”不够。
- 不在错误响应中暴露堆栈、SQL、内部路径或下游密钥。
- 发现疑似密钥或漏洞时停止传播，按 [docs/SECURITY.md](docs/SECURITY.md) 处理。

## 验证命令

从仓库根目录运行与变更范围匹配的最小命令：

```bash
python check_jeecgenv.py
mvn -f jeecg-boot/pom.xml -Pdev test
mvn -f jeecg-boot/pom.xml -Pdev -pl jeecg-module-system/jeecg-system-start -am package
pnpm --dir jeecgboot-vue3 exec eslint <changed-files>
pnpm --dir jeecgboot-vue3 exec stylelint "src/**/*.{vue,css,less,scss}"
pnpm --dir jeecgboot-vue3 exec jest
pnpm --dir jeecgboot-vue3 build
```

没有统一的前端 `lint` 或 `test` script，不要声称执行了不存在的命令。纯文档修改至少检查 Markdown 链接、路径、命令和 `git diff --check`。

## 完成时报告

- 改了什么以及为什么；
- 实际执行的验证命令和结果；
- 未执行的验证及原因；
- 配置、迁移、兼容性、安全和回滚风险；
- 需要维护者决定的后续事项。

更多背景见 [CODEX_GOAL.md](CODEX_GOAL.md)、[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)、[docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) 和 [docs/SECURITY.md](docs/SECURITY.md)。
