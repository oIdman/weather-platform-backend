# 开发指南

## 1. 前置条件

推荐环境：

| 工具 | 推荐值 | 说明 |
| --- | --- | --- |
| JDK | 17 | 当前 `pom.xml` 编译基线 |
| Maven | 3.9.x | 官方建议 3.9.8 |
| Node.js | 20.19+ | Vite 8 的稳妥基线；不要优先选已 EOL 的 Node 18 |
| pnpm | 9+ | 前端包管理器 |
| MySQL | 8.0 | 仓库初始化脚本兼容 5.7+，新环境优先 8.0 |
| Redis | 5+ | 认证、缓存等基础能力 |
| Python | 3.x，可选 | 运行环境检查脚本 |
| Docker Compose | 可选 | 一键启动或基础设施 |

验证环境：

```bash
java -version
mvn -version
node -version
pnpm -version
python check_jeecgenv.py
```

AI/RAG 功能还可能需要 PostgreSQL/pgvector、MongoDB 和模型 API Key；这些不是启动基础后台的必要条件。

## 2. 本地单体开发

单体模式是普通功能开发的首选路径。

### 2.1 初始化基础设施

1. 创建 MySQL 数据库并导入 `jeecg-boot/db/jeecgboot-mysql-5.7.sql`。
2. 启动 Redis。
3. 在本地专用配置中设置数据库和 Redis 连接，不要提交真实密码。

主要配置文件：

```text
jeecg-boot/jeecg-module-system/jeecg-system-start/src/main/resources/
├─ application.yml
├─ application-dev.yml
└─ jeecg/jeecg_config.properties
```

### 2.2 构建与启动后端

```bash
# 安装开发配置下的后端模块
mvn -f jeecg-boot/pom.xml -Pdev clean install -DskipTests

# 命令行启动单体服务
mvn -f jeecg-boot/jeecg-module-system/jeecg-system-start/pom.xml -Pdev spring-boot:run
```

也可以在 IDE 中运行：

```text
org.jeecg.JeecgSystemApplication
```

默认后端地址为 `http://localhost:8080/jeecg-boot`。

### 2.3 启动前端

```bash
pnpm --dir jeecgboot-vue3 install
pnpm --dir jeecgboot-vue3 dev
```

默认开发端口为 3100。确认 `jeecgboot-vue3/.env.development` 中代理和 `VITE_GLOB_DOMAIN_URL` 指向当前后端；单体通常是 `http://localhost:8080/jeecg-boot`。

## 3. Docker 单体启动

仓库提供 Windows/Linux 快速脚本：

```text
start-docker-compose.bat
start-docker-compose.sh
```

手工流程：

```bash
mvn -f jeecg-boot/pom.xml -Pdocker clean install -DskipTests
pnpm --dir jeecgboot-vue3 install
pnpm --dir jeecgboot-vue3 build:docker
docker compose -f docker-compose.yml up -d
```

`docker-compose.yml` 会启动 MySQL（主机端口 13306）、Redis、后端（8080）、前端 Nginx（80）和 pgvector。示例凭据只适用于隔离的本地环境，部署前必须替换。

## 4. 微服务开发

只有任务明确需要微服务时才使用此路径。

需要同时启用环境 Profile 和 `SpringCloud` Profile，并准备 MySQL、Redis、Nacos 配置。核心启动顺序：

1. `com.alibaba.nacos.JeecgNacosApplication`
2. `org.jeecg.JeecgSystemCloudApplication`（以及需要的业务服务）
3. `org.jeecg.JeecgGatewayApplication`
4. 前端指向 `http://localhost:9999`，不要附加 `/jeecg-boot`

可使用：

```text
start-docker-compose-cloud.bat
start-docker-compose-cloud.sh
docker-compose-cloud.yml
```

微服务常用端口：Nacos 8848/18080、Gateway 9999、System 7001、Demo 7002、XXL-Job 9080。实际值以当前配置文件和 Compose 为准。

## 5. 日常开发命令

### 后端

```bash
# 全部单体测试
mvn -f jeecg-boot/pom.xml -Pdev test

# 构建单体启动模块及依赖
mvn -f jeecg-boot/pom.xml -Pdev -pl jeecg-module-system/jeecg-system-start -am package

# 指定测试
mvn -f jeecg-boot/pom.xml -Pdev -Dtest=ClassNameTest test
```

### 前端

```bash
pnpm --dir jeecgboot-vue3 dev
pnpm --dir jeecgboot-vue3 build
pnpm --dir jeecgboot-vue3 build:report
pnpm --dir jeecgboot-vue3 exec eslint <changed-files>
pnpm --dir jeecgboot-vue3 exec stylelint "src/**/*.{vue,css,less,scss}"
pnpm --dir jeecgboot-vue3 exec jest
```

注意：`package.json` 没有统一的 `lint` 或 `test` script。`batch:prettier` 会批量改写 `src`，仅在明确需要时使用；常规修改应只格式化变更文件。

## 6. 新增业务功能

推荐后端结构：

```text
org.jeecg.modules.<domain>/
├─ controller/
├─ entity/
├─ mapper/ 与 mapper/xml/
├─ service/ 与 service/impl/
└─ vo/ 或 dto/
```

推荐前端结构：

```text
src/views/<domain>/
├─ XxxList.vue
├─ XxxForm.vue
├─ Xxx.api.ts
└─ Xxx.data.ts
```

接口必须同时考虑功能权限和数据权限。权限标识使用稳定的 `模块:资源:动作` 形式，并在后端注解、菜单按钮配置和前端校验中保持一致。

## 7. 代码生成器

官方建议在以下文件配置生成目标：

```text
jeecg-boot/jeecg-module-system/jeecg-system-start/
src/main/resources/jeecg/jeecg_config.properties
```

关键项：

- `project_path`：目标后端模块；
- `ui_project_path`：前端项目；
- `bussi_package`：业务包名。

从 3.8.3 起可直接把前端代码生成到项目并生成菜单迁移脚本，但仍需人工审查：

- 生成路径是否正确且未覆盖手工代码；
- DTO/实体是否存在越权批量赋值；
- 权限标识、租户字段和数据权限是否完整；
- SQL 类型、索引、唯一约束、非空约束是否符合业务；
- 前端 API、类型、校验和 i18n 是否正确；
- 是否补充单元/集成测试。

不要提交个人机器的绝对 `project_path` 或数据库凭据。

## 8. 数据库变更

- 初始化脚本用于新环境，不应作为日常升级手段。
- 新变更放入 Flyway 增量目录，版本唯一、顺序明确、可重复审查。
- 不修改已被其他环境执行的迁移。
- 大表 DDL、数据回填和删除操作需要说明锁表、耗时、备份和回滚策略。
- 切换非 MySQL 数据库前阅读官方“切换其他数据库”文档；默认 Flyway 迁移主要面向 MySQL。

## 9. 调试清单

- 验证码 404：检查前端代理、`VITE_GLOB_DOMAIN_URL` 和后端 context path。
- 数据库连接失败：检查 profile、主机名、端口、库名和 MySQL 大小写配置。
- 登录后权限未更新：权限配置变更后退出并重新登录。
- 微服务无路由：确认 Nacos 配置已导入、服务健康注册、Gateway 最后启动。
- 代码生成位置错误：检查三个生成配置项，清理生成结果后重试，不要覆盖手工文件。

## 10. 官方参考

- [开发环境搭建](https://help.jeecg.com/java/setup/tools.html)
- [环境检查](https://help.jeecg.com/java/setup/checkenv/)
- [IDEA 启动项目](https://help.jeecg.com/java/setup/idea/startup/)
- [前端启动](https://help.jeecg.com/setup/startup/)
- [Docker 启动](https://help.jeecg.com/java/docker/quick/)
- [代码生成配置](https://help.jeecg.com/java/codegen/config/)
- [切换其他数据库](https://help.jeecg.com/java/setup/switchdb/)

