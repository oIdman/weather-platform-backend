# JeecgBoot Workflow Module

完整移植范围、每轮更新记录、验证结果与后续目标见 [工作流完整移植进度](../../../docs/WORKFLOW_INTEGRATION_PROGRESS.md)。

This module ports the reusable Flowable workflow boundary from `ruoyi-vue-pro` without importing the RuoYi framework.

## Modules

- `workflow-api`: portable request, response, and page contracts.
- `workflow-biz`: Flowable orchestration, REST controllers, and host adapters.
- `WorkflowIdentityAdapter`: the only identity boundary currently implemented by JeecgBoot. Replace this adapter when extracting the module into a standalone service.

The module does not depend on the meteorological project module. Business records are linked through `businessKey`; a process stores only routing variables, not the complete business form.

## REST compatibility

The first approval loop keeps the RuoYi/Yudao route family:

- `POST /bpm/process-definition/deploy`
- `GET /bpm/process-definition/page`
- `POST /bpm/process-instance/create`
- `GET /bpm/process-instance/my-page`
- `GET /bpm/process-instance/manager-page`
- `DELETE /bpm/process-instance/cancel-by-start-user`
- `GET /bpm/task/todo-page`
- `GET /bpm/task/done-page`
- `PUT /bpm/task/approve`
- `PUT /bpm/task/reject`

Each BPMN file must contain exactly one process, one start event, and one end event. Rejection completes the current task with `approved=false`; the BPMN model must define the corresponding conditional branch.

## Database boundary

Flowable owns the `ACT_*` tables. Flyway owns the reusable `bpm_*` metadata tables in `V20260904_1__workflow_foundation.sql`. Runtime schema auto-update and classpath auto-deployment are disabled.

The migration deliberately excludes the RuoYi demonstration records and `bpm_oa_leave`. JeecgBoot user identifiers are strings, so user references in the adapted schema are `varchar`/JSON instead of RuoYi's numeric IDs.

## Next extraction steps

For a standalone deployment, add a small Spring Boot launcher and provide remote implementations for identity, tenant, file, and notification adapters. The API module and Flowable orchestration can remain unchanged.
