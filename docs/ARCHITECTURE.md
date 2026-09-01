# 架构说明

## 1. 架构摘要

本仓库是 JeecgBoot 3.9.5 的前后端分离代码库，提供两种后端运行形态：

- 单体：`jeecg-system-start` 聚合系统管理和业务模块，适合本地开发、中小规模部署和大多数功能迭代。
- 微服务：Nacos 负责注册/配置，Gateway 作为统一入口，System、Demo 等服务独立启动，适合确有隔离部署和服务治理需求的场景。

前端 `jeecgboot-vue3` 是独立 Vue SPA，通过 HTTP/WebSocket 访问单体上下文 `/jeecg-boot` 或微服务网关。动态菜单、按钮权限和数据权限由后端配置驱动。

```text
浏览器 / Vue 3 SPA (:3100 开发，:80 容器)
                |
                | /jeecgboot 开发代理
                v
      单体应用 :8080/jeecg-boot
                |
       MySQL + Redis + 可选对象存储/AI设施

或：

浏览器 -> Gateway :9999 -> System :7001 / Demo :7002 / 其他服务
              |              |
              +---- Nacos :8848（注册与配置）
```

## 2. 技术基线

下表以当前仓库构建文件为准，不以官网可能更新的宣传页替代：

| 层次 | 当前基线 |
| --- | --- |
| 平台 | JeecgBoot 3.9.5 |
| Java | 17（父 POM 注释还声明支持 21/24/25） |
| 后端 | Spring Boot 4.1.0、Spring Cloud 2025.1.2、Spring Cloud Alibaba 2025.1.0.0 |
| 持久层 | MyBatis-Plus 3.5.16、Druid、dynamic-datasource |
| 认证授权 | Apache Shiro 3.0、JWT 4.5、Redis |
| 前端 | Vue 3.5、TypeScript 5.9、Vite 8、Ant Design Vue 4、Pinia 3 |
| 数据库 | 默认 MySQL 脚本；支持 PostgreSQL、Oracle、SQL Server、MariaDB、达梦、人大金仓等适配 |
| 部署 | Maven、pnpm、Docker/Docker Compose、Nginx |

官网部分页面仍写 Vite 6 或较宽松的 Node 要求；本仓库已经使用 Vite 8，因此开发环境统一建议 Node 20.19+ 和 pnpm 9+。

## 3. 仓库与模块边界

### 后端

```text
jeecg-boot/
├─ pom.xml                         父 POM、版本和 Maven Profiles
├─ jeecg-boot-base-core/           公共配置、权限、注解、过滤器和基础能力
├─ jeecg-module-system/
│  ├─ jeecg-system-api/            System 对外契约；区分 local/cloud 实现
│  ├─ jeecg-system-biz/            用户、角色、菜单、字典等系统业务
│  └─ jeecg-system-start/          单体启动与环境配置
├─ jeecg-boot-module/              业务、Demo、AI/低代码扩展模块
├─ jeecg-server-cloud/
│  ├─ jeecg-cloud-nacos/           注册/配置中心
│  ├─ jeecg-cloud-gateway/         API 网关
│  ├─ jeecg-system-cloud-start/    System 微服务启动
│  ├─ jeecg-demo-cloud-start/      Demo 微服务启动
│  └─ jeecg-visual/                监控、任务、限流等可选组件
└─ db/                             初始化数据库脚本
```

边界规则：

- `base-core` 只放跨模块稳定基础能力，不放具体业务。
- `*-api` 定义跨模块/跨服务契约，`*-biz` 实现业务；启动模块只装配配置和依赖。
- 单体与微服务通过 local API/Feign API 适配，不应在业务层散落运行形态判断。
- 新业务优先成为 `jeecg-boot-module` 下独立模块，确需独立部署时再增加 cloud start。
- 当前可见的 `weather` 目录没有非构建产物源码，是预留结构而非已实现业务模块。

### 前端

```text
jeecgboot-vue3/
├─ build/                 Vite 插件和构建脚本
├─ public/                不经编译的静态资源
├─ src/
│  ├─ api/                后端 API 定义
│  ├─ components/         公共与 Jeecg 组件
│  ├─ layouts/            页面布局
│  ├─ locales/            国际化资源
│  ├─ router/             静态路由与路由基础设施
│  ├─ settings/           主题、组件、加密和站点设置
│  ├─ store/              Pinia 状态
│  ├─ utils/http/axios/   统一 HTTP、签名和错误处理
│  └─ views/              页面与动态扩展模块
├─ types/                 全局 TypeScript 类型
└─ .env*                  各模式公开运行配置
```

前端路由与菜单以服务端返回为主。隐藏按钮或路由不能代替服务端权限检查。

## 4. 典型请求链路

1. 用户登录，后端校验账号/验证码等认证因素并签发 JWT，相关会话状态由 Redis 支撑。
2. 前端统一 Axios 层携带 Token、租户等上下文，并执行项目已有请求签名流程。
3. Shiro 过滤器校验身份；Controller 上的 `@RequiresPermissions` 校验功能权限。
4. 标注 `@PermissionData` 的查询按菜单/请求配置注入行级规则。
5. Controller 将已校验请求交给 Service；Service 通过 Mapper/MyBatis-Plus 访问数据库。
6. 后端以 `Result<T>` 返回 `{ code, result, message, success }`，前端统一响应层处理错误和登录状态。

权限存在三层：

- 页面/按钮可见性：用户体验层；
- 请求权限：服务端操作授权；
- 数据权限/租户/对象归属：行级和对象级授权。

三者必须同时考虑，不能用前端隐藏代替服务端授权。

## 5. 数据与迁移

- 初始 MySQL 数据位于 `jeecg-boot/db/jeecgboot-mysql-5.7.sql`。
- 增量迁移位于 `jeecg-module-system/jeecg-system-start/src/main/resources/flyway/sql/mysql/`。
- 已发布迁移视为不可变；变更数据库结构时新增唯一版本脚本。
- 官方说明：切换非 MySQL 数据库时，需要评估并通常关闭默认 MySQL Flyway 流程，再使用对应转换脚本/适配配置。
- 支持多数据源不代表任意 SQL 安全；动态数据源、Online 表单和自定义 SQL 都必须受权限和输入边界约束。

## 6. 配置模型

后端主要配置位于：

- `jeecg-module-system/jeecg-system-start/src/main/resources/application.yml`
- `application-dev.yml`、`application-test.yml`、`application-prod.yml`、`application-docker.yml`
- `resources/jeecg/jeecg_config.properties`：代码生成器等 Jeecg 配置
- 微服务配置样例：`jeecg-server-cloud/jeecg-cloud-nacos/docs/DEFAULT_GROUP/`

前端使用 `.env` 与 `.env.<mode>`。`VITE_GLOB_*` 会写入构建后的 `_app.config.js`，可在部署后调整，但属于公开客户端配置，不能承载秘密。

## 7. 扩展决策

新增能力时按以下顺序选择：

1. 简单数据管理：评估 Online 表单/代码生成器。
2. 需要源码定制的 CRUD：用代码生成器生成骨架，人工合并到业务模块。
3. 复杂领域逻辑：手工实现清晰的业务模块和测试。
4. 只有明确需要独立伸缩、故障隔离或独立发布时，才增加微服务。

代码生成配置的关键路径是 `jeecg-system-start/src/main/resources/jeecg/jeecg_config.properties`，其中 `project_path`、`ui_project_path`、`bussi_package` 必须指向当前工作区，且本地绝对路径不应提交为团队默认值。

## 8. 官方参考

- [项目介绍](https://help.jeecg.com/java/)
- [项目目录结构](https://help.jeecg.com/java/projectDirectoryStructure/)
- [前端目录结构](https://help.jeecg.com/ui/setup/projectCatalog/)
- [代码生成配置](https://help.jeecg.com/java/codegen/config/)
- [Shiro 请求权限](https://help.jeecg.com/java/system/auth/request/)
- [数据权限用法](https://help.jeecg.com/java/system/dataauth/use/)
- [微服务启动](https://help.jeecg.com/java/springcloud/switchcloud/monomer-springboot4/)

