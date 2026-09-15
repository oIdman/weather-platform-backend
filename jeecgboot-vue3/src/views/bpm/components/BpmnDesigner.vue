<template>
  <div class="bpmn-designer">
    <div class="designer-toolbar">
      <a-space>
        <a-button size="small" @click="zoom(-0.1)">缩小</a-button>
        <a-button size="small" @click="resetZoom">适应画布</a-button>
        <a-button size="small" @click="zoom(0.1)">放大</a-button>
        <a-button size="small" :disabled="!canUndo" @click="undo">撤销</a-button>
        <a-button size="small" :disabled="!canRedo" @click="redo">重做</a-button>
      </a-space>
      <span class="toolbar-tip">从左侧拖入节点，点击节点后在右侧配置</span>
    </div>
    <div class="designer-body">
      <div ref="canvasRef" class="designer-canvas"></div>
      <aside class="property-panel">
        <template v-if="selectedElement">
          <h3>节点属性</h3>
          <a-form layout="vertical" size="small">
            <a-form-item label="节点类型"><a-input :value="selectedTypeLabel" disabled /></a-form-item>
            <a-form-item label="节点编号"><a-input :value="selectedElement.id" disabled /></a-form-item>
            <a-form-item label="节点名称">
              <a-input v-model:value="properties.name" @change="updateProperty('name', properties.name)" />
            </a-form-item>
            <a-form-item label="元素文档">
              <a-textarea
                v-model:value="properties.documentation"
                :rows="2"
                allow-clear
                placeholder="填写该节点的说明，办理人员可据此了解节点用途"
                @change="updateDocumentation"
              />
            </a-form-item>
            <template v-if="isUserTask || isServiceTask || isScriptTask || isReceiveTask || isCallActivity || isSubProcess">
              <a-divider orientation="left">异步执行</a-divider>
              <a-space class="task-switches">
                <a-checkbox v-model:checked="properties.asyncBefore" @change="updateAsyncProperties">异步前</a-checkbox>
                <a-checkbox v-model:checked="properties.asyncAfter" @change="updateAsyncProperties">异步后</a-checkbox>
                <a-checkbox v-if="properties.asyncBefore || properties.asyncAfter" v-model:checked="properties.exclusive" @change="updateAsyncProperties">独占</a-checkbox>
              </a-space>
            </template>
            <template v-if="selectedType === 'bpmn:Process'">
              <a-divider orientation="left">流程监听器</a-divider>
              <a-form-item label="执行监听器模板">
                <a-select
                  v-model:value="properties.processListenerIds"
                  mode="multiple"
                  allow-clear
                  :options="processListenerOptions"
                  placeholder="选择已启用的执行监听器模板"
                  @change="updateProcessListeners"
                />
                <div class="listener-hint">模板会写入当前流程定义；任务监听器请在具体用户任务上配置。</div>
                <div class="listener-editor process-listener-editor">
                  <div v-for="(listener, index) in executionListenerRows" :key="`execution-process-${index}`" class="listener-row">
                    <a-select v-model:value="listener.eventName" :options="executionListenerEventOptions" style="width: 118px" @change="updateExecutionListenerRows" />
                    <a-select v-model:value="listener.valueType" :options="listenerValueTypeOptions" style="width: 128px" @change="updateExecutionListenerRows" />
                    <a-input v-model:value="listener.value" placeholder="类名、表达式或 Bean" @change="updateExecutionListenerRows" />
                    <a-button danger type="text" @click="removeListenerRow('execution', index)">删除</a-button>
                  </div>
                  <a-button type="dashed" block @click="addListenerRow('execution')">添加流程执行监听器</a-button>
                </div>
              </a-form-item>
            </template>
            <template v-if="isUserTask">
              <a-form-item label="指定审批人">
                <JSelectUser
                  v-model:value="properties.assignee"
                  row-key="id"
                  label-key="realname"
                  @change="updateProperty('flowable:assignee', normalizeValue(properties.assignee))"
                />
              </a-form-item>
              <a-form-item label="候选用户">
                <JSelectUser
                  v-model:value="properties.candidateUsers"
                  row-key="id"
                  label-key="realname"
                  :multiple="true"
                  @change="updateProperty('flowable:candidateUsers', normalizeValue(properties.candidateUsers))"
                />
              </a-form-item>
              <a-form-item label="候选角色编码">
                <a-input
                  v-model:value="properties.candidateGroups"
                  placeholder="多个角色用英文逗号分隔"
                  @change="updateProperty('flowable:candidateGroups', properties.candidateGroups)"
                />
              </a-form-item>
              <a-form-item label="表单标识">
                <a-input v-model:value="properties.formKey" @change="updateProperty('flowable:formKey', properties.formKey)" />
              </a-form-item>
              <a-divider orientation="left">审批配置</a-divider>
              <a-form-item label="候选人策略">
                <a-select v-model:value="properties.candidateStrategy" :options="candidateStrategies" @change="updateCandidateStrategy" />
              </a-form-item>
              <a-form-item v-if="properties.candidateStrategy === 30" label="指定用户" required>
                <JSelectUser :value="properties.candidateParam" row-key="id" label-key="realname" multiple="multiple" @update:value="setCandidateValue" />
              </a-form-item>
              <a-form-item v-else-if="properties.candidateStrategy === 10" label="指定角色" required>
                <JSelectRole :value="properties.candidateParam" row-key="roleCode" multiple="multiple" @update:value="setCandidateValue" />
              </a-form-item>
              <template v-else-if="[20, 21, 23].includes(properties.candidateStrategy)">
                <a-form-item label="指定部门" required>
                  <JSelectDept :value="candidateBaseParam" row-key="id" multiple="multiple" @update:value="setCandidateBaseParam" />
                </a-form-item>
                <a-form-item v-if="properties.candidateStrategy === 23" label="连续部门层级" required>
                  <a-input-number :value="candidateLevel" :min="1" :max="20" style="width: 100%" @change="setCandidateLevel" />
                </a-form-item>
              </template>
              <a-form-item v-else-if="properties.candidateStrategy === 22" label="指定岗位" required>
                <JSelectPosition :value="properties.candidateParam" row-key="id" multiple="multiple" @update:value="setCandidateValue" />
              </a-form-item>
              <a-form-item v-else-if="[37, 38].includes(properties.candidateStrategy)" label="部门层级" required>
                <a-input-number :value="candidateLevel" :min="1" :max="20" style="width: 100%" @change="setCandidateLevel" />
              </a-form-item>
              <a-form-item v-else-if="properties.candidateStrategy === 50" label="表单用户字段" required>
                <a-input v-model:value="properties.candidateParam" placeholder="例如 approverUserIds" @change="updateExtension('candidateParam', properties.candidateParam)" />
              </a-form-item>
              <template v-else-if="properties.candidateStrategy === 51">
                <a-form-item label="表单部门字段" required>
                  <a-input v-model:value="candidateBaseParam" placeholder="例如 departmentId" @change="setCandidateBaseParam(candidateBaseParam)" />
                </a-form-item>
                <a-form-item label="连续部门层级" required>
                  <a-input-number :value="candidateLevel" :min="1" :max="20" style="width: 100%" @change="setCandidateLevel" />
                </a-form-item>
              </template>
              <a-form-item v-else-if="properties.candidateStrategy === 60" label="流程表达式" required>
                <a-textarea v-model:value="properties.candidateParam" :rows="3" placeholder="例如 ${assigneeId}" @change="updateExtension('candidateParam', properties.candidateParam)" />
              </a-form-item>
              <a-form-item v-else-if="[34, 35, 36].includes(properties.candidateStrategy)" label="审批人">
                <a-input :value="candidateStrategyHint" disabled />
              </a-form-item>
              <a-form-item v-else-if="properties.candidateStrategy === 40" label="用户组编号" required>
                <a-input v-model:value="properties.candidateParam" placeholder="多个用户组编号用英文逗号分隔" @change="updateExtension('candidateParam', properties.candidateParam)" />
              </a-form-item>
              <a-form-item label="多人审批方式">
                <a-select v-model:value="properties.approveMethod" :options="approveMethods" @change="updateApproveMethod" />
              </a-form-item>
              <a-form-item v-if="properties.approveMethod === 2" label="会签通过比例">
                <a-input-number v-model:value="properties.approveRatio" :min="1" :max="100" addon-after="%" style="width: 100%" @change="updateApproveRatio" />
              </a-form-item>
              <div class="timeout-hint">会签、或签和依次审批会生成 Flowable 多实例集合，集合来自当前节点候选人策略。</div>
              <a-form-item label="审批人为空">
                <a-select v-model:value="properties.emptyHandlerType" :options="emptyHandlerTypes" @change="updateExtension('assignEmptyHandlerType', properties.emptyHandlerType)" />
              </a-form-item>
              <a-form-item label="拒绝后处理">
                <a-select v-model:value="properties.rejectHandlerType" :options="rejectHandlerTypes" @change="updateExtension('rejectHandlerType', properties.rejectHandlerType)" />
              </a-form-item>
              <a-form-item v-if="properties.rejectHandlerType === 2" label="退回节点编号">
                <a-input v-model:value="properties.rejectReturnTaskId" placeholder="填写目标用户任务 ID" @change="updateExtension('rejectReturnTaskId', properties.rejectReturnTaskId)" />
              </a-form-item>
              <a-form-item label="审批人与发起人相同">
                <a-select v-model:value="properties.assignStartUserHandlerType" :options="assignStartUserHandlerTypes" @change="updateExtension('assignStartUserHandlerType', properties.assignStartUserHandlerType)" />
              </a-form-item>
              <a-form-item label="跳过表达式">
                <a-textarea v-model:value="properties.skipExpression" :rows="2" placeholder="例如 ${amount > 1000}" @change="updateProperty('flowable:skipExpression', properties.skipExpression)" />
              </a-form-item>
              <a-form-item label="表单字段权限">
                <a-empty v-if="!fieldPermissionRows.length" description="请先在表单设计中选择流程表单" :image-style="{ height: '32px' }" />
                <div v-else class="field-permission-editor">
                  <div class="field-permission-head">
                    <span>字段名称</span>
                    <span><a @click="setAllFieldPermissions('READ')">只读</a><a @click="setAllFieldPermissions('WRITE')">可编辑</a><a @click="setAllFieldPermissions('NONE')">隐藏</a></span>
                  </div>
                  <div v-for="item in fieldPermissionRows" :key="item.field" class="field-permission-row">
                    <span class="field-permission-label">{{ item.label }}</span>
                    <a-radio-group v-model:value="item.permission" size="small" @change="updateFieldPermissions">
                      <a-radio value="READ">只读</a-radio>
                      <a-radio value="WRITE">可编辑</a-radio>
                      <a-radio value="NONE">隐藏</a-radio>
                    </a-radio-group>
                  </div>
                </div>
                <a-textarea v-model:value="properties.fieldsPermission" :rows="2" class="advanced-json" placeholder='高级 JSON，例如 {"amount":"WRITE"}' @change="updateFieldPermissionsFromJson" />
              </a-form-item>
              <a-form-item label="操作按钮">
                <a-checkbox-group
                  v-model:value="properties.enabledButtons"
                  :options="buttonOptions"
                  @change="updateEnabledButtons"
                />
                <div class="listener-hint">控制办理页显示的操作；后端仍会按同一配置校验权限。</div>
              </a-form-item>
              <a-form-item label="操作按钮高级 JSON">
                <a-textarea v-model:value="properties.buttonsSetting" :rows="3" placeholder='例如 [{"id":1,"displayName":"通过","enable":true}]' @change="updateExtension('buttonsSetting', properties.buttonsSetting)" />
              </a-form-item>
              <a-space class="task-switches">
                <a-checkbox v-model:checked="properties.signEnable" @change="updateExtension('signEnable', properties.signEnable ? 'true' : '')">需要签名</a-checkbox>
                <a-checkbox v-model:checked="properties.reasonRequire" @change="updateExtension('reasonRequire', properties.reasonRequire ? 'true' : '')">意见必填</a-checkbox>
              </a-space>
              <a-form-item label="任务监听器 JSON">
                <div class="listener-editor">
                  <div v-for="(listener, index) in taskListenerRows" :key="`task-${index}`" class="listener-row">
                    <a-select v-model:value="listener.eventName" :options="taskListenerEventOptions" style="width: 118px" @change="updateTaskListenerRows" />
                    <a-select v-model:value="listener.valueType" :options="listenerValueTypeOptions" style="width: 128px" @change="updateTaskListenerRows" />
                    <a-input v-model:value="listener.value" placeholder="类名、表达式或 Bean" @change="updateTaskListenerRows" />
                    <a-button danger type="text" @click="removeListenerRow('task', index)">删除</a-button>
                  </div>
                  <a-button type="dashed" block @click="addListenerRow('task')">添加任务监听器</a-button>
                </div>
                <a-textarea v-model:value="properties.taskListeners" :rows="2" class="advanced-json" placeholder='高级 JSON，例如 [{"eventName":"create","valueType":"delegateExpression","value":"yourTaskListenerBean"}]' @change="updateTaskListenerJson" />
              </a-form-item>
              <a-form-item label="执行监听器 JSON">
                <div class="listener-editor">
                  <div v-for="(listener, index) in executionListenerRows" :key="`execution-task-${index}`" class="listener-row">
                    <a-select v-model:value="listener.eventName" :options="executionListenerEventOptions" style="width: 118px" @change="updateExecutionListenerRows" />
                    <a-select v-model:value="listener.valueType" :options="listenerValueTypeOptions" style="width: 128px" @change="updateExecutionListenerRows" />
                    <a-input v-model:value="listener.value" placeholder="类名、表达式或 Bean" @change="updateExecutionListenerRows" />
                    <a-button danger type="text" @click="removeListenerRow('execution', index)">删除</a-button>
                  </div>
                  <a-button type="dashed" block @click="addListenerRow('execution')">添加执行监听器</a-button>
                </div>
                <a-textarea v-model:value="properties.executionListeners" :rows="2" class="advanced-json" placeholder='高级 JSON，例如 [{"eventName":"start","valueType":"delegateExpression","value":"yourExecutionListenerBean"}]' @change="updateExecutionListenerJson" />
              </a-form-item>
              <a-form-item label="任务监听器模板">
                <a-select
                  v-model:value="properties.taskListenerIds"
                  mode="multiple"
                  allow-clear
                  :options="taskListenerOptions"
                  placeholder="选择已启用的任务监听器模板"
                  @change="updateTaskListeners"
                />
              </a-form-item>
              <a-divider orientation="left">审批超时</a-divider>
              <a-form-item label="启用超时">
                <a-switch v-model:checked="properties.timeoutEnabled" @change="toggleTimeoutBoundary" />
              </a-form-item>
              <template v-if="properties.timeoutEnabled">
                <a-form-item label="超时处理">
                  <a-select v-model:value="properties.timeoutHandlerType" :options="timeoutHandlerTypes" @change="updateTimeoutHandler" />
                </a-form-item>
                <a-form-item label="超时时间（ISO-8601）">
                  <a-input v-model:value="properties.timeoutDuration" placeholder="例如 PT30M、P7D" @change="updateTimeoutDuration" />
                </a-form-item>
                <a-form-item v-if="properties.timeoutHandlerType === 1" label="提醒次数">
                  <a-input-number
                    v-model:value="properties.timeoutMaxRemindCount"
                    :min="1"
                    :max="99"
                    style="width: 100%"
                    @change="updateTimeoutMaxRemindCount"
                  />
                  <div class="timeout-hint">大于 1 次时生成重复定时器；每次间隔使用上面的超时时间。</div>
                </a-form-item>
                <div class="timeout-hint">已创建边界定时事件，请在画布上从该事件拖出连线配置后续动作。</div>
              </template>
            </template>
            <template v-if="isCallActivity">
              <a-divider orientation="left">调用活动</a-divider>
              <a-form-item label="被调用子流程">
                <a-select
                  v-model:value="properties.calledElement"
                  show-search
                  allow-clear
                  :options="childProcessOptions"
                  option-filter-prop="label"
                  placeholder="请选择或输入流程标识"
                  @change="updateCallActivity('calledElement')"
                />
              </a-form-item>
              <a-form-item label="调用标识类型">
                <a-select v-model:value="properties.calledElementType" :options="calledElementTypes" @change="updateCallActivity('calledElementType')" />
              </a-form-item>
              <a-form-item label="流程实例名称">
                <a-input v-model:value="properties.processInstanceName" allow-clear @change="updateCallActivity('processInstanceName')" />
              </a-form-item>
              <a-space class="task-switches">
                <a-checkbox v-model:checked="properties.inheritVariables" @change="updateCallActivity('inheritVariables')">继承流程变量</a-checkbox>
                <a-checkbox v-model:checked="properties.inheritBusinessKey" @change="updateCallActivity('inheritBusinessKey')">继承业务标识</a-checkbox>
              </a-space>
              <a-form-item v-if="!properties.inheritBusinessKey" label="业务标识表达式">
                <a-input v-model:value="properties.businessKey" allow-clear placeholder="例如 ${businessKey}" @change="updateCallActivity('businessKey')" />
              </a-form-item>
              <a-form-item label="输入参数 JSON">
                <a-textarea
                  v-model:value="properties.inVariablesJson"
                  :rows="3"
                  placeholder='例如 [{"source":"amount","target":"amount"}]'
                  @change="updateCallVariables('In', properties.inVariablesJson)"
                />
              </a-form-item>
              <a-form-item label="输出参数 JSON">
                <a-textarea
                  v-model:value="properties.outVariablesJson"
                  :rows="3"
                  placeholder='例如 [{"source":"result","target":"result"}]'
                  @change="updateCallVariables('Out', properties.outVariablesJson)"
                />
              </a-form-item>
            </template>
            <template v-if="isSubProcess">
              <a-divider orientation="left">扩展子流程</a-divider>
              <a-form-item label="子流程状态">
                <a-radio-group v-model:value="properties.subProcessExpanded" @change="updateSubProcess">
                  <a-radio :value="true">展开</a-radio>
                  <a-radio :value="false">折叠</a-radio>
                </a-radio-group>
              </a-form-item>
              <a-checkbox v-model:checked="properties.subProcessTriggeredByEvent" @change="updateSubProcess">
                由事件触发
              </a-checkbox>
              <div class="timeout-hint">展开子流程可在画布内部继续拖入节点；折叠子流程用于只保留一个活动占位。</div>
            </template>
            <template v-if="isScriptTask">
              <a-divider orientation="left">脚本任务</a-divider>
              <a-form-item label="脚本格式">
                <a-input v-model:value="properties.scriptFormat" allow-clear placeholder="例如 groovy、javascript" @change="updateScriptTask" />
              </a-form-item>
              <a-form-item label="脚本类型">
                <a-select v-model:value="properties.scriptType" :options="scriptTypes" @change="updateScriptTask" />
              </a-form-item>
              <a-form-item v-if="properties.scriptType === 'inline'" label="内联脚本">
                <a-textarea v-model:value="properties.script" :rows="5" allow-clear @change="updateScriptTask" />
              </a-form-item>
              <a-form-item v-else label="外部资源地址">
                <a-input v-model:value="properties.resource" allow-clear placeholder="例如 classpath:workflow/approve.groovy" @change="updateScriptTask" />
              </a-form-item>
              <a-form-item label="结果变量">
                <a-input v-model:value="properties.resultVariable" allow-clear placeholder="可选，保存脚本返回值" @change="updateScriptTask" />
              </a-form-item>
            </template>
            <template v-if="isReceiveTask">
              <a-divider orientation="left">接收任务</a-divider>
              <a-form-item label="消息名称">
                <a-input
                  v-model:value="properties.receiveMessageName"
                  allow-clear
                  placeholder="外部系统发送消息时使用此名称"
                  @change="updateReceiveTask"
                />
              </a-form-item>
              <div class="timeout-hint">运行中的流程可调用 /bpm/task/trigger-event，并传入 eventType=message、eventName=此消息名称。</div>
            </template>
            <template v-if="isGateway">
              <a-divider orientation="left">网关配置</a-divider>
              <a-form-item label="默认流转分支">
                <a-select
                  v-model:value="properties.defaultFlowId"
                  allow-clear
                  :options="gatewayFlowOptions"
                  placeholder="未设置默认分支"
                  @change="updateGatewayDefaultFlow"
                />
              </a-form-item>
              <div class="timeout-hint">条件分支请点击网关的连线配置表达式；默认分支在其它条件均不满足时执行。</div>
            </template>
            <template v-if="isServiceTask">
              <a-divider orientation="left">服务任务</a-divider>
              <a-form-item label="执行类型">
                <a-select v-model:value="properties.serviceExecuteType" :options="serviceExecuteTypes" @change="updateServiceTask" />
              </a-form-item>
              <a-form-item v-if="properties.serviceExecuteType === 'class'" label="Java 类">
                <a-input v-model:value="properties.serviceClass" allow-clear placeholder="实现 JavaDelegate 的类名" @change="updateServiceTask" />
              </a-form-item>
              <a-form-item v-else-if="properties.serviceExecuteType === 'expression'" label="表达式">
                <a-input v-model:value="properties.serviceExpression" allow-clear placeholder="例如 ${serviceBean.execute(execution)}" @change="updateServiceTask" />
              </a-form-item>
              <a-form-item v-else-if="properties.serviceExecuteType === 'delegateExpression'" label="代理表达式">
                <a-input v-model:value="properties.serviceDelegateExpression" allow-clear placeholder="例如 ${workflowService}" @change="updateServiceTask" />
              </a-form-item>
              <template v-else>
                <a-form-item label="请求方法">
                  <a-select v-model:value="properties.httpMethod" :options="httpMethods" @change="updateServiceTask" />
                </a-form-item>
                <a-form-item label="请求地址">
                  <a-input v-model:value="properties.httpUrl" allow-clear placeholder="支持 ${流程变量}" @change="updateServiceTask" />
                </a-form-item>
                <a-form-item label="请求头 JSON">
                  <a-textarea v-model:value="properties.httpHeaders" :rows="3" placeholder='例如 {"Content-Type":"application/json"}' @change="updateServiceTask" />
                </a-form-item>
                <a-form-item label="返回变量前缀">
                  <a-input v-model:value="properties.httpResultVariablePrefix" allow-clear @change="updateServiceTask" />
                </a-form-item>
                <a-space class="task-switches">
                  <a-checkbox v-model:checked="properties.httpDisallowRedirects" @change="updateServiceTask">禁止重定向</a-checkbox>
                  <a-checkbox v-model:checked="properties.httpSaveResponseParameters" @change="updateServiceTask">保存返回变量</a-checkbox>
                  <a-checkbox v-model:checked="properties.httpSaveResponseParametersTransient" @change="updateServiceTask">瞬时变量</a-checkbox>
                  <a-checkbox v-model:checked="properties.httpSaveResponseVariableAsJson" @change="updateServiceTask">保存为 JSON</a-checkbox>
                </a-space>
                <a-checkbox v-model:checked="properties.httpIgnoreException" @change="updateServiceTask">忽略 HTTP 异常</a-checkbox>
              </template>
            </template>
            <template v-if="isBoundaryEvent">
              <a-divider orientation="left">边界事件</a-divider>
              <a-form-item label="事件类型">
                <a-select v-model:value="properties.boundaryType" :options="boundaryEventTypes" @change="updateBoundaryDefinition" />
              </a-form-item>
              <a-form-item v-if="['error', 'message', 'signal'].includes(properties.boundaryType)" label="事件标识">
                <a-input v-model:value="properties.boundaryCode" allow-clear placeholder="错误码、消息名或信号名" @change="updateBoundaryDefinition" />
              </a-form-item>
              <a-form-item v-if="properties.boundaryType === 'conditional'" label="条件表达式">
                <a-textarea v-model:value="properties.boundaryExpression" :rows="2" placeholder="例如 ${amount > 1000}" @change="updateBoundaryDefinition" />
              </a-form-item>
              <a-form-item v-if="properties.boundaryType === 'timer'" label="定时器时长（ISO-8601）">
                <a-input v-model:value="properties.timeoutDuration" placeholder="例如 PT30M、P7D" @change="updateBoundaryDefinition" />
              </a-form-item>
              <a-checkbox v-model:checked="properties.boundaryCancelActivity" @change="updateBoundaryCancelActivity">
                触发后取消附着节点
              </a-checkbox>
              <div class="timeout-hint">请从边界事件拖出连线，配置触发后要执行的后续路径。</div>
            </template>
            <template v-if="isConfigurableEvent">
              <a-divider orientation="left">事件定义</a-divider>
              <a-form-item label="事件类型">
                <a-select v-model:value="properties.eventDefinitionType" :options="eventDefinitionTypes" allow-clear @change="updateEventDefinition" />
              </a-form-item>
              <a-form-item v-if="['message', 'signal'].includes(properties.eventDefinitionType)" label="事件名称">
                <a-input v-model:value="properties.eventDefinitionName" allow-clear placeholder="消息或信号名称" @change="updateEventDefinition" />
              </a-form-item>
              <a-form-item v-if="properties.eventDefinitionType === 'conditional'" label="条件表达式">
                <a-textarea v-model:value="properties.eventDefinitionExpression" :rows="2" allow-clear placeholder="例如 ${amount > 1000}" @change="updateEventDefinition" />
              </a-form-item>
              <a-form-item v-if="properties.eventDefinitionType === 'timer'" label="定时器时长（ISO-8601）">
                <a-input v-model:value="properties.eventDefinitionDuration" allow-clear placeholder="例如 PT30M、P1D" @change="updateEventDefinition" />
              </a-form-item>
              <div class="timeout-hint">消息/信号事件名称需与外部触发方保持一致；捕获事件可使用 /bpm/task/trigger-event 唤醒。</div>
            </template>
            <a-form-item v-if="isSequenceFlow" label="流转条件">
              <a-textarea
                v-model:value="properties.conditionExpression"
                :rows="3"
                placeholder="例如 ${amount > 1000}"
                @change="updateConditionExpression"
              />
            </a-form-item>
          </a-form>
        </template>
        <a-empty v-else description="请选择流程节点" :image-style="{ height: '56px' }" />
      </aside>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
  import JSelectDept from '/@/components/Form/src/jeecg/components/JSelectDept.vue';
  import JSelectPosition from '/@/components/Form/src/jeecg/components/JSelectPosition.vue';
  import JSelectRole from '/@/components/Form/src/jeecg/components/JSelectRole.vue';
  import JSelectUser from '/@/components/Form/src/jeecg/components/JSelectUser.vue';
  import { getModelList } from '/@/api/bpm/model';
  import { getProcessListenerPage, type BpmProcessListener } from '/@/api/bpm/processListener';
  import bpmnPalette, { defaultNodeName } from './BpmnPalette';
  import bpmnTranslation, { translateBpmnLabel } from './BpmnTranslation';
  import flowableDescriptor from './flowableDescriptor';

  type FormFieldOption = { label: string; value: string };
  const props = withDefaults(defineProps<{ modelValue?: string; formFields?: FormFieldOption[] }>(), {
    formFields: () => [],
  });
  const emit = defineEmits<{ (event: 'update:modelValue', value: string): void }>();

  const canvasRef = ref<HTMLElement>();
  const modeler = ref<any>();
  const selectedElement = ref<any>();
  const selectedType = computed(() => selectedElement.value?.businessObject?.$type || '-');
  const selectedTypeLabel = computed(() => ({
    'bpmn:Process': '流程',
    'bpmn:StartEvent': '开始事件',
    'bpmn:IntermediateCatchEvent': '中间捕获事件',
    'bpmn:IntermediateThrowEvent': '中间抛出事件',
    'bpmn:BoundaryEvent': '边界事件',
    'bpmn:EndEvent': '结束事件',
    'bpmn:UserTask': '用户任务',
    'bpmn:Task': '任务',
    'bpmn:ServiceTask': '服务任务',
    'bpmn:SendTask': '发送任务',
    'bpmn:ReceiveTask': '接收任务',
    'bpmn:ScriptTask': '脚本任务',
    'bpmn:CallActivity': '调用子流程',
    'bpmn:SubProcess': '子流程',
    'bpmn:ExclusiveGateway': '排他网关',
    'bpmn:ParallelGateway': '并行网关',
    'bpmn:InclusiveGateway': '包容网关',
    'bpmn:EventBasedGateway': '事件网关',
    'bpmn:SequenceFlow': '顺序流',
  } as Record<string, string>)[selectedType.value] || selectedType.value);
  const isUserTask = computed(() => selectedType.value === 'bpmn:UserTask');
  const isServiceTask = computed(() => ['bpmn:ServiceTask', 'bpmn:SendTask'].includes(selectedType.value));
  const isScriptTask = computed(() => selectedType.value === 'bpmn:ScriptTask');
  const isReceiveTask = computed(() => selectedType.value === 'bpmn:ReceiveTask');
  const isCallActivity = computed(() => selectedType.value === 'bpmn:CallActivity');
  const isSubProcess = computed(() => selectedType.value === 'bpmn:SubProcess');
  const isGateway = computed(() => selectedType.value?.endsWith('Gateway'));
  const isBoundaryEvent = computed(() => selectedType.value === 'bpmn:BoundaryEvent');
  const isConfigurableEvent = computed(() => ['bpmn:IntermediateCatchEvent', 'bpmn:IntermediateThrowEvent', 'bpmn:StartEvent', 'bpmn:EndEvent'].includes(selectedType.value));
  const isSequenceFlow = computed(() => selectedType.value === 'bpmn:SequenceFlow');
  const childProcessOptions = ref<{ label: string; value: string }[]>([]);
  const fieldPermissionRows = ref<{ field: string; label: string; permission: string }[]>([]);
  const gatewayFlowOptions = computed(() =>
    (selectedElement.value?.outgoing || []).map((flow: any) => ({
      label: flow.businessObject?.name ? `${flow.businessObject.name} (${flow.id})` : flow.id,
      value: flow.id,
    }))
  );
  const candidateStrategies = [
    { label: '指定用户', value: 30 },
    { label: '指定角色', value: 10 },
    { label: '部门成员', value: 20 },
    { label: '部门负责人', value: 21 },
    { label: '连续多级部门负责人', value: 23 },
    { label: '指定岗位', value: 22 },
    { label: '指定用户组', value: 40 },
    { label: '审批人自选', value: 34 },
    { label: '发起人自选', value: 35 },
    { label: '发起人本人', value: 36 },
    { label: '发起人部门负责人', value: 37 },
    { label: '发起人连续多级部门负责人', value: 38 },
    { label: '表单内用户字段', value: 50 },
    { label: '表单内部门负责人', value: 51 },
    { label: '流程表达式', value: 60 },
  ];
  const approveMethods = [
    { label: '依次审批', value: 4 },
    { label: '会签', value: 2 },
    { label: '或签', value: 3 },
    { label: '随机一人', value: 1 },
  ];
  const emptyHandlerTypes = [
    { label: '自动通过', value: 1 },
    { label: '自动拒绝', value: 2 },
    { label: '指定人员审批', value: 3 },
    { label: '转交流程管理员', value: 4 },
  ];
  const rejectHandlerTypes = [
    { label: '直接终止流程', value: 1 },
    { label: '驳回到指定节点', value: 2 },
  ];
  const assignStartUserHandlerTypes = [
    { label: '由发起人对自己审批', value: 1 },
    { label: '自动跳过', value: 2 },
    { label: '转交部门负责人', value: 3 },
  ];
  const timeoutHandlerTypes = [
    { label: '自动提醒', value: 1 },
    { label: '自动同意', value: 2 },
    { label: '自动拒绝', value: 3 },
  ];
  const boundaryEventTypes = [
    { label: '定时器', value: 'timer' },
    { label: '错误', value: 'error' },
    { label: '消息', value: 'message' },
    { label: '信号', value: 'signal' },
    { label: '条件', value: 'conditional' },
  ];
  const eventDefinitionTypes = [
    { label: '消息', value: 'message' },
    { label: '信号', value: 'signal' },
    { label: '定时器', value: 'timer' },
    { label: '条件', value: 'conditional' },
  ];
  const scriptTypes = [
    { label: '内联脚本', value: 'inline' },
    { label: '外部资源', value: 'external' },
  ];
  const serviceExecuteTypes = [
    { label: 'Java 类', value: 'class' },
    { label: '表达式', value: 'expression' },
    { label: '代理表达式', value: 'delegateExpression' },
    { label: 'HTTP 调用', value: 'http' },
  ];
  const calledElementTypes = [
    { label: '流程标识（Key）', value: 'key' },
    { label: '流程定义 ID', value: 'id' },
    { label: '消息', value: 'message' },
  ];
  const httpMethods = [
    { label: 'GET', value: 'GET' },
    { label: 'POST', value: 'POST' },
    { label: 'PUT', value: 'PUT' },
    { label: 'DELETE', value: 'DELETE' },
  ];
  const buttonOptions = [
    { label: '通过', value: 1 },
    { label: '拒绝', value: 2 },
    { label: '转办', value: 3 },
    { label: '委派', value: 4 },
    { label: '加签', value: 5 },
    { label: '退回', value: 6 },
    { label: '抄送', value: 7 },
    { label: '跳过', value: 8 },
  ];
  const canUndo = ref(false);
  const canRedo = ref(false);
  const properties = reactive({
    name: '',
    documentation: '',
    asyncBefore: false,
    asyncAfter: false,
    exclusive: false,
    assignee: '' as string | string[],
    candidateUsers: [] as string | string[],
    candidateGroups: '',
    formKey: '',
    delegateExpression: '',
    serviceExecuteType: 'delegateExpression',
    serviceClass: '',
    serviceExpression: '',
    serviceDelegateExpression: '',
    httpMethod: 'GET',
    httpUrl: '',
    httpHeaders: '{}',
    httpResultVariablePrefix: '',
    httpDisallowRedirects: false,
    httpIgnoreException: false,
    httpSaveResponseParameters: false,
    httpSaveResponseParametersTransient: false,
    httpSaveResponseVariableAsJson: false,
    conditionExpression: '',
    candidateStrategy: 30 as number,
    candidateParam: '',
    enabledButtons: [1, 2, 3, 4, 5, 6, 8] as number[],
    approveMethod: 4 as number,
    approveRatio: 100 as number,
    emptyHandlerType: 1 as number,
    rejectHandlerType: 1 as number,
    rejectReturnTaskId: '',
    assignStartUserHandlerType: 1 as number,
    skipExpression: '',
    fieldsPermission: '',
    buttonsSetting: '',
    taskListeners: '',
    executionListeners: '',
    processListenerIds: [] as number[],
    taskListenerIds: [] as number[],
    signEnable: false,
    reasonRequire: false,
    timeoutEnabled: false,
    timeoutHandlerType: 2 as number,
    timeoutDuration: 'P7D',
    timeoutMaxRemindCount: 1 as number,
    calledElement: '',
    calledElementType: 'key',
    processInstanceName: '',
    inheritVariables: false,
    inheritBusinessKey: false,
    businessKey: '',
    inVariablesJson: '[]',
    outVariablesJson: '[]',
    subProcessExpanded: true,
    subProcessTriggeredByEvent: false,
    defaultFlowId: '',
    scriptType: 'inline',
    scriptFormat: '',
    script: '',
    resource: '',
    resultVariable: '',
    receiveMessageName: '',
    receiveMessageId: '',
    boundaryType: 'timer',
    boundaryCode: '',
    boundaryExpression: '',
    boundaryCancelActivity: true,
    eventDefinitionType: '',
    eventDefinitionName: '',
    eventDefinitionExpression: '',
    eventDefinitionDuration: 'P1D',
  });
  const candidateBaseParam = computed({
    get: () => String(properties.candidateParam || '').split('|')[0],
    set: (value: string) => setCandidateBaseParam(value),
  });
  const candidateLevel = computed(() => {
    const parts = String(properties.candidateParam || '').split('|');
    return Math.max(1, Number(parts[1] || parts[0]) || 1);
  });
  const candidateStrategyHint = computed(() => {
    if (properties.candidateStrategy === 34) return '由上一审批人在通过时选择';
    if (properties.candidateStrategy === 35) return '由流程发起人在提交时选择';
    return '流程发起人本人';
  });
  const processListenerRecords = ref<BpmProcessListener[]>([]);
  const processListenerOptions = computed(() => processListenerRecords.value
    .filter((item) => item.type === 'execution')
    .map((item) => ({ label: `${item.name}（${item.event}）`, value: item.id! })));
  const taskListenerOptions = computed(() => processListenerRecords.value
    .filter((item) => item.type === 'task')
    .map((item) => ({ label: `${item.name}（${item.event}）`, value: item.id! })));
  const taskListenerEventOptions = [
    { label: '创建 create', value: 'create' },
    { label: '指派 assignment', value: 'assignment' },
    { label: '完成 complete', value: 'complete' },
    { label: '删除 delete', value: 'delete' },
    { label: '更新 update', value: 'update' },
  ];
  const executionListenerEventOptions = [
    { label: '开始 start', value: 'start' },
    { label: '结束 end', value: 'end' },
    { label: '连线 take', value: 'take' },
  ];
  const listenerValueTypeOptions = [
    { label: '代理表达式', value: 'delegateExpression' },
    { label: 'Java 类', value: 'class' },
    { label: '表达式', value: 'expression' },
  ];
  const taskListenerRows = ref<Array<{ eventName: string; valueType: string; value: string }>>([]);
  const executionListenerRows = ref<Array<{ eventName: string; valueType: string; value: string }>>([]);
  let lastXml = '';
  let changeTimer: ReturnType<typeof setTimeout> | undefined;

  function service<T = any>(name: string): T {
    return modeler.value?.get(name) as T;
  }

  async function importXml(xml?: string) {
    if (!modeler.value || !xml?.trim() || xml === lastXml) return;
    await modeler.value.importXML(xml);
    ensureDefaultNodeNames();
    lastXml = xml;
    resetZoom();
  }

  /** 为没有 name 的历史 BPMN 元素补充中文默认名称，避免画布和办理记录显示英文类型名或技术 ID。 */
  function ensureDefaultNodeNames() {
    const elementRegistry = service<any>('elementRegistry');
    const modeling = service<any>('modeling');
    if (!elementRegistry || !modeling) return;
    elementRegistry.getAll().forEach((element: any) => {
      const businessObject = element.businessObject;
      if (!businessObject) return;
      const currentName = businessObject.name?.trim() || '';
      // Only replace labels known to be bpmn-js defaults. A user-entered English
      // business name must remain unchanged.
      const translatedLegacyName = currentName ? translateBpmnLabel(currentName) : '';
      if (currentName && !translatedLegacyName) return;
      const name = translatedLegacyName || defaultNodeName(businessObject.$type);
      if (name) modeling.updateProperties(element, { name });
    });
  }

  async function emitXml() {
    if (!modeler.value) return;
    const result = await modeler.value.saveXML({ format: true });
    lastXml = result.xml || '';
    emit('update:modelValue', lastXml);
  }

  function scheduleEmit() {
    if (changeTimer) clearTimeout(changeTimer);
    changeTimer = setTimeout(emitXml, 180);
    const commandStack = service<any>('commandStack');
    canUndo.value = Boolean(commandStack?.canUndo());
    canRedo.value = Boolean(commandStack?.canRedo());
  }

  function loadProperties(element?: any) {
    selectedElement.value = element;
    const businessObject = element?.businessObject;
    properties.name = businessObject?.name || '';
    properties.documentation = businessObject?.documentation?.[0]?.text || '';
    properties.asyncBefore = Boolean(businessObject?.asyncBefore);
    properties.asyncAfter = Boolean(businessObject?.asyncAfter);
    properties.exclusive = Boolean(businessObject?.exclusive);
    properties.assignee = businessObject?.assignee || '';
    properties.candidateUsers = businessObject?.candidateUsers ? String(businessObject.candidateUsers).split(',').filter(Boolean) : [];
    properties.candidateGroups = businessObject?.candidateGroups || '';
    properties.formKey = businessObject?.formKey || '';
    properties.delegateExpression = businessObject?.delegateExpression || '';
    properties.serviceClass = businessObject?.class || '';
    properties.serviceExpression = businessObject?.expression || '';
    properties.serviceDelegateExpression = businessObject?.delegateExpression || '';
    properties.serviceExecuteType = businessObject?.$attrs?.['flowable:type'] === 'http'
      ? 'http'
      : properties.serviceClass
        ? 'class'
        : properties.serviceExpression
          ? 'expression'
          : 'delegateExpression';
    properties.httpMethod = extensionFieldValue(businessObject, 'requestMethod') || 'GET';
    properties.httpUrl = extensionFieldValue(businessObject, 'requestUrl');
    properties.httpHeaders = extensionFieldValue(businessObject, 'requestHeaders') || '{}';
    properties.httpResultVariablePrefix = extensionFieldValue(businessObject, 'resultVariablePrefix');
    properties.httpDisallowRedirects = extensionFieldValue(businessObject, 'disallowRedirects') === 'true';
    properties.httpIgnoreException = extensionFieldValue(businessObject, 'ignoreException') === 'true';
    properties.httpSaveResponseParameters = extensionFieldValue(businessObject, 'saveResponseParameters') === 'true';
    properties.httpSaveResponseParametersTransient = extensionFieldValue(businessObject, 'saveResponseParametersTransient') === 'true';
    properties.httpSaveResponseVariableAsJson = extensionFieldValue(businessObject, 'saveResponseVariableAsJson') === 'true';
    properties.conditionExpression = businessObject?.conditionExpression?.body || businessObject?.conditionExpression || '';
    properties.candidateStrategy = Number(extensionValue(businessObject, 'candidateStrategy')) || 30;
    properties.candidateParam = extensionValue(businessObject, 'candidateParam');
    const enabledButtons = extensionValue(businessObject, 'enabledButtons');
    properties.enabledButtons = enabledButtons
      ? enabledButtons.split(',').map((value) => Number(value.trim())).filter((value) => Number.isInteger(value))
      : [1, 2, 3, 4, 5, 6, 8];
    const approveMethodExtension = Number(extensionValue(businessObject, 'approveMethod'));
    const loopCharacteristics = businessObject?.loopCharacteristics;
    properties.approveMethod = approveMethodExtension || (loopCharacteristics ? (loopCharacteristics.isSequential ? 4 : 3) : 1);
    properties.approveRatio = Number(extensionValue(businessObject, 'approveRatio')) || 100;
    properties.emptyHandlerType = Number(extensionValue(businessObject, 'assignEmptyHandlerType')) || 1;
    properties.rejectHandlerType = Number(extensionValue(businessObject, 'rejectHandlerType')) || 1;
    properties.rejectReturnTaskId = extensionValue(businessObject, 'rejectReturnTaskId');
    properties.assignStartUserHandlerType = Number(extensionValue(businessObject, 'assignStartUserHandlerType')) || 1;
    properties.skipExpression = businessObject?.skipExpression || '';
    properties.fieldsPermission = extensionValue(businessObject, 'formFieldsPermission');
    syncFieldPermissionRows();
    properties.buttonsSetting = extensionValue(businessObject, 'buttonsSetting');
    properties.taskListeners = listenerSettingsJson(businessObject, 'TaskListener', extensionValue(businessObject, 'taskListeners'));
    properties.executionListeners = listenerSettingsJson(businessObject, 'ExecutionListener', extensionValue(businessObject, 'executionListeners'));
    syncListenerRows();
    properties.processListenerIds = businessObject?.$type === 'bpmn:Process' ? processListenerIds(businessObject) : [];
    properties.taskListenerIds = businessObject?.$type === 'bpmn:UserTask' ? listenerIds(businessObject, 'TaskListener') : [];
    properties.signEnable = extensionValue(businessObject, 'signEnable') === 'true';
    properties.reasonRequire = extensionValue(businessObject, 'reasonRequire') === 'true';
    properties.calledElement = businessObject?.calledElement || '';
    properties.calledElementType = businessObject?.calledElementType || 'key';
    properties.processInstanceName = businessObject?.processInstanceName || '';
    properties.inheritVariables = String(businessObject?.inheritVariables || '').toLowerCase() === 'true';
    properties.inheritBusinessKey = String(businessObject?.inheritBusinessKey || '').toLowerCase() === 'true';
    properties.businessKey = businessObject?.businessKey || '';
    properties.inVariablesJson = JSON.stringify(extensionVariables(businessObject, 'In'));
    properties.outVariablesJson = JSON.stringify(extensionVariables(businessObject, 'Out'));
    properties.subProcessExpanded = businessObject?.isExpanded !== false;
    properties.subProcessTriggeredByEvent = Boolean(businessObject?.triggeredByEvent);
    properties.defaultFlowId = businessObject?.default?.id || '';
    properties.scriptFormat = businessObject?.scriptFormat || '';
    properties.script = businessObject?.script || '';
    properties.resource = businessObject?.resource || '';
    properties.resultVariable = businessObject?.resultVariable || '';
    properties.scriptType = properties.script ? 'inline' : 'external';
    properties.receiveMessageName = businessObject?.messageRef?.name || '';
    properties.receiveMessageId = businessObject?.messageRef?.id || '';
    loadEventDefinitionProperties(businessObject);
    const timeoutBoundary = isTimeoutBoundaryFor(element);
    properties.timeoutHandlerType = Number(extensionValue(timeoutBoundary?.businessObject, 'timeoutHandlerType')) || 2;
    const timeoutDefinition = timeoutBoundary?.businessObject?.eventDefinitions?.[0];
    const cycleText = String(timeoutDefinition?.timeCycle || '');
    const repeatMatch = cycleText.match(/^R(\d+)\/(.+)$/);
    properties.timeoutDuration = timeoutDefinition?.timeDuration || repeatMatch?.[2] || 'P7D';
    properties.timeoutMaxRemindCount = repeatMatch ? Math.max(1, Number(repeatMatch[1])) : 1;
    properties.timeoutEnabled = Boolean(timeoutBoundary);
    loadBoundaryProperties(businessObject);
  }

  function loadBoundaryProperties(businessObject: any) {
    if (!businessObject || businessObject.$type !== 'bpmn:BoundaryEvent') return;
    const definition = businessObject.eventDefinitions?.[0];
    const type = String(definition?.$type || 'bpmn:TimerEventDefinition').replace('bpmn:', '').replace('EventDefinition', '').toLowerCase();
    properties.boundaryType = boundaryEventTypes.some((item) => item.value === type) ? type : 'timer';
    properties.boundaryCancelActivity = businessObject.cancelActivity !== false;
    properties.boundaryExpression = definition?.condition?.body || definition?.condition || '';
    properties.boundaryCode = definition?.errorRef?.errorCode || definition?.messageRef?.name || definition?.signalRef?.name || '';
  }

  function isTimeoutBoundaryFor(element: any, taskId?: string) {
    if (!element) return undefined;
    const businessObject = element.businessObject;
    const hostId = businessObject?.attachedToRef?.id || element.host?.id;
    if (element.type === 'bpmn:BoundaryEvent') {
      if (taskId && hostId !== taskId) return undefined;
      const hasTimer = businessObject?.eventDefinitions?.some((item: any) => item.$type === 'bpmn:TimerEventDefinition');
      return hasTimer ? element : undefined;
    }
    const registry = service<any>('elementRegistry');
    return registry?.filter((item: any) => item.type === 'bpmn:BoundaryEvent' && (item.businessObject?.attachedToRef?.id || item.host?.id) === (taskId || element.id))
      .find((item: any) => item.businessObject?.eventDefinitions?.some((definition: any) => definition.$type === 'bpmn:TimerEventDefinition'));
  }

  function extensionValue(businessObject: any, name: string) {
    const element = businessObject?.extensionElements?.values?.find((item: any) => item.$type === `flowable:${name}` || item.$type?.endsWith(`:${name}`));
    return element?.body || element?.value || '';
  }

  function extensionVariables(businessObject: any, type: 'In' | 'Out') {
    return (businessObject?.extensionElements?.values || [])
      .filter((item: any) => item.$type === `flowable:${type}` || item.$type?.endsWith(`:${type}`))
      .map((item: any) => ({ source: item.source || '', target: item.target || '' }));
  }

  function extensionFieldValue(businessObject: any, name: string) {
    const field = (businessObject?.extensionElements?.values || []).find(
      (item: any) => (item.$type === 'flowable:Field' || item.$type?.endsWith(':Field')) && item.name === name
    );
    return field?.string ?? field?.stringValue ?? field?.expression ?? '';
  }

  function listenerValue(listener: any) {
    return listener?.value || listener?.class || listener?.delegateExpression || listener?.expression || '';
  }

  function listenerSettingsJson(businessObject: any, listenerType: 'TaskListener' | 'ExecutionListener', fallback = '') {
    const listeners = (businessObject?.extensionElements?.values || [])
      .filter((item: any) => item.$type === `flowable:${listenerType}` || item.$type?.endsWith(`:${listenerType}`))
      .map((item: any) => {
        const value = listenerValue(item);
        const valueType = item.class ? 'class' : item.expression ? 'expression' : 'delegateExpression';
        return { eventName: item.event || '', valueType, value };
      });
    if (listeners.length > 0) return JSON.stringify(listeners);
    return fallback || '[]';
  }

  function parseListenerRows(value: string) {
    try {
      const parsed = JSON.parse(value || '[]');
      if (!Array.isArray(parsed)) return [];
      return parsed
        .map((item) => ({
          eventName: String(item?.eventName || item?.event || '').trim(),
          valueType: String(item?.valueType || item?.implementationType || 'delegateExpression'),
          value: String(item?.value || item?.implementation || item?.delegateExpression || '').trim(),
        }))
        .filter((item) => item.eventName && item.value);
    } catch {
      return [];
    }
  }

  function syncListenerRows() {
    taskListenerRows.value = parseListenerRows(properties.taskListeners);
    executionListenerRows.value = parseListenerRows(properties.executionListeners);
  }

  function listenerRowsJson(rows: Array<{ eventName: string; valueType: string; value: string }>) {
    return JSON.stringify(rows.filter((item) => item.eventName && item.value).map((item) => ({
      eventName: item.eventName,
      valueType: item.valueType || 'delegateExpression',
      value: item.value,
    })));
  }

  function updateTaskListenerRows() {
    properties.taskListeners = listenerRowsJson(taskListenerRows.value);
    updateTaskListenerJson(properties.taskListeners);
  }

  function updateExecutionListenerRows() {
    properties.executionListeners = listenerRowsJson(executionListenerRows.value);
    updateExecutionListenerJson(properties.executionListeners);
    if (selectedType.value === 'bpmn:Process' && selectedElement.value) {
      properties.processListenerIds = processListenerIds(selectedElement.value.businessObject);
    }
  }

  function addListenerRow(type: 'task' | 'execution') {
    const rows = type === 'task' ? taskListenerRows : executionListenerRows;
    rows.value.push({
      eventName: type === 'task' ? 'create' : 'start',
      valueType: 'delegateExpression',
      value: '',
    });
  }

  function removeListenerRow(type: 'task' | 'execution', index: number) {
    const rows = type === 'task' ? taskListenerRows : executionListenerRows;
    rows.value.splice(index, 1);
    type === 'task' ? updateTaskListenerRows() : updateExecutionListenerRows();
  }

  function processListenerIds(businessObject: any) {
    return listenerIds(businessObject, 'ExecutionListener');
  }

  function listenerIds(businessObject: any, listenerType: 'ExecutionListener' | 'TaskListener') {
    return (businessObject?.extensionElements?.values || [])
      .filter((item: any) => item.$type === `flowable:${listenerType}` || item.$type?.endsWith(`:${listenerType}`))
      .map((item: any) => processListenerRecords.value.find((template) =>
        template.type === (listenerType === 'TaskListener' ? 'task' : 'execution')
          && template.event === item.event && listenerValue(template) === listenerValue(item))?.id)
      .filter((id: number | undefined): id is number => id !== undefined);
  }

  function loadEventDefinitionProperties(businessObject: any) {
    const definition = businessObject?.eventDefinitions?.[0];
    const definitionType = String(definition?.$type || '')
      .replace('bpmn:', '')
      .replace('EventDefinition', '')
      .toLowerCase();
    properties.eventDefinitionType = eventDefinitionTypes.some((item) => item.value === definitionType) ? definitionType : '';
    properties.eventDefinitionName = definition?.messageRef?.name || definition?.signalRef?.name || '';
    properties.eventDefinitionExpression = definition?.condition?.body || definition?.condition || '';
    properties.eventDefinitionDuration = definition?.timeDuration || 'P1D';
  }

  function normalizeValue(value: string | string[]) {
    return Array.isArray(value) ? value.join(',') : value;
  }

  function updateProperty(name: string, value: unknown) {
    if (!selectedElement.value) return;
    service<any>('modeling').updateProperties(selectedElement.value, { [name]: value || undefined });
  }

  function updateDocumentation() {
    if (!selectedElement.value) return;
    const text = String(properties.documentation || '').trim();
    const documentation = text
      ? [service<any>('bpmnFactory').create('bpmn:Documentation', { text })]
      : [];
    service<any>('modeling').updateProperties(selectedElement.value, { documentation });
  }

  function normalizeFieldPermission(value: unknown) {
    const normalized = String(value || 'READ').toUpperCase();
    if (normalized === 'READONLY') return 'READ';
    if (normalized === 'EDITABLE') return 'WRITE';
    if (normalized === 'HIDE') return 'NONE';
    return ['READ', 'WRITE', 'NONE'].includes(normalized) ? normalized : 'READ';
  }

  function readFieldPermissionMap(value: string) {
    if (!value?.trim()) return {} as Record<string, string>;
    try {
      const parsed = JSON.parse(value);
      if (Array.isArray(parsed)) {
        return parsed.reduce((result, item) => {
          const field = String(item?.field || item?.name || item?.fieldName || '').trim();
          if (field) result[field] = normalizeFieldPermission(item?.permission || item?.auth || item?.type);
          return result;
        }, {} as Record<string, string>);
      }
      if (parsed && typeof parsed === 'object') {
        return Object.entries(parsed).reduce((result, [field, permission]) => {
          if (String(field).trim()) result[String(field)] = normalizeFieldPermission(permission);
          return result;
        }, {} as Record<string, string>);
      }
    } catch {
      // Keep the raw JSON in the advanced editor until the user corrects it.
    }
    return {} as Record<string, string>;
  }

  function syncFieldPermissionRows() {
    const permissions = readFieldPermissionMap(properties.fieldsPermission);
    const fields = props.formFields?.length
      ? props.formFields
      : Object.keys(permissions).map((field) => ({ label: field, value: field }));
    fieldPermissionRows.value = fields.map((field) => ({
      field: field.value,
      label: field.label || field.value,
      permission: permissions[field.value] || 'READ',
    }));
  }

  function updateFieldPermissions() {
    const permissionMap = fieldPermissionRows.value.reduce((result, item) => {
      result[item.field] = normalizeFieldPermission(item.permission);
      return result;
    }, {} as Record<string, string>);
    properties.fieldsPermission = JSON.stringify(permissionMap);
    updateExtension('formFieldsPermission', properties.fieldsPermission);
  }

  function setAllFieldPermissions(permission: string) {
    fieldPermissionRows.value.forEach((item) => (item.permission = permission));
    updateFieldPermissions();
  }

  function updateFieldPermissionsFromJson() {
    const parsed = readFieldPermissionMap(properties.fieldsPermission);
    if (!Object.keys(parsed).length && properties.fieldsPermission.trim() !== '{}' && properties.fieldsPermission.trim() !== '[]') return;
    properties.fieldsPermission = JSON.stringify(parsed);
    syncFieldPermissionRows();
    updateExtension('formFieldsPermission', properties.fieldsPermission);
  }

  function updateAsyncProperties() {
    if (!selectedElement.value) return;
    const asyncBefore = Boolean(properties.asyncBefore);
    const asyncAfter = Boolean(properties.asyncAfter);
    service<any>('modeling').updateProperties(selectedElement.value, {
      asyncBefore,
      asyncAfter,
      exclusive: asyncBefore || asyncAfter ? Boolean(properties.exclusive) : false,
    });
  }

  function updateSubProcess() {
    if (!selectedElement.value || !isSubProcess.value) return;
    service<any>('modeling').updateProperties(selectedElement.value, {
      isExpanded: Boolean(properties.subProcessExpanded),
      triggeredByEvent: Boolean(properties.subProcessTriggeredByEvent),
    });
  }

  function updateServiceTask() {
    if (!selectedElement.value || !isServiceTask.value) return;
    const type = properties.serviceExecuteType;
    const attributes: Record<string, unknown> = {
      class: type === 'class' ? properties.serviceClass || undefined : undefined,
      expression: type === 'expression' ? properties.serviceExpression || undefined : undefined,
      delegateExpression: type === 'delegateExpression' ? properties.serviceDelegateExpression || undefined : undefined,
      'flowable:type': type === 'http' ? 'http' : undefined,
    };
    service<any>('modeling').updateProperties(selectedElement.value, attributes);
    updateHttpServiceFields(type === 'http');
  }

  function updateHttpServiceFields(enabled: boolean) {
    if (!selectedElement.value) return;
    const businessObject = selectedElement.value.businessObject;
    const existing = businessObject.extensionElements?.values || [];
    const fieldNames = new Set([
      'requestMethod',
      'requestUrl',
      'requestHeaders',
      'disallowRedirects',
      'ignoreException',
      'saveResponseParameters',
      'saveResponseParametersTransient',
      'saveResponseVariableAsJson',
      'resultVariablePrefix',
    ]);
    const other = existing.filter((item: any) => !(item.$type === 'flowable:Field' || item.$type?.endsWith(':Field')) || !fieldNames.has(item.name));
    if (enabled) {
      const fields = [
        ['requestMethod', properties.httpMethod || 'GET'],
        ['requestUrl', properties.httpUrl || ''],
        ['requestHeaders', properties.httpHeaders || '{}'],
        ['disallowRedirects', String(Boolean(properties.httpDisallowRedirects))],
        ['saveResponseParameters', String(Boolean(properties.httpSaveResponseParameters))],
        ['saveResponseParametersTransient', String(Boolean(properties.httpSaveResponseParametersTransient))],
        ['saveResponseVariableAsJson', String(Boolean(properties.httpSaveResponseVariableAsJson))],
        ['resultVariablePrefix', properties.httpResultVariablePrefix || ''],
        ['ignoreException', String(Boolean(properties.httpIgnoreException))],
      ].filter(([, value]) => String(value).trim() !== '');
      fields.forEach(([name, value]) => other.push(service<any>('bpmnFactory').createAny('flowable:Field', 'http://flowable.org/bpmn', {
        name,
        string: String(value),
      })));
    }
    const extensionElements = service<any>('bpmnFactory').create('bpmn:ExtensionElements', { values: other });
    service<any>('modeling').updateProperties(selectedElement.value, { extensionElements });
  }

  function updateCallActivity(name: string) {
    if (!selectedElement.value || !isCallActivity.value) return;
    const value = properties[name as keyof typeof properties];
    updateProperty(name === 'calledElement' || name === 'processInstanceName' || name === 'businessKey' ? name : `flowable:${name}`, value);
  }

  function updateScriptTask() {
    if (!selectedElement.value || !isScriptTask.value) return;
    const inline = properties.scriptType === 'inline';
    service<any>('modeling').updateProperties(selectedElement.value, {
      scriptFormat: properties.scriptFormat || undefined,
      script: inline ? properties.script || undefined : undefined,
      resource: inline ? undefined : properties.resource || undefined,
      resultVariable: properties.resultVariable || undefined,
    });
  }

  function updateReceiveTask() {
    if (!selectedElement.value || !isReceiveTask.value) return;
    const name = properties.receiveMessageName.trim();
    const definitions = modeler.value?.getDefinitions();
    const rootElements = definitions?.rootElements || [];
    if (!name) {
      properties.receiveMessageId = '';
      service<any>('modeling').updateProperties(selectedElement.value, { messageRef: null });
      return;
    }
    let message = rootElements.find((item: any) => item.$type === 'bpmn:Message' && (item.id === properties.receiveMessageId || item.name === name));
    if (!message) {
      const id = properties.receiveMessageId || `${selectedElement.value.id}_message`;
      message = service<any>('bpmnFactory').create('bpmn:Message', { id, name });
      rootElements.push(message);
      properties.receiveMessageId = id;
    } else if (message.name !== name) {
      message.name = name;
    }
    service<any>('modeling').updateProperties(selectedElement.value, { messageRef: message });
  }

  function updateCallVariables(type: 'In' | 'Out', value: string) {
    if (!selectedElement.value || !isCallActivity.value) return;
    let parsed: Array<{ source?: string; target?: string }>;
    try {
      parsed = JSON.parse(value || '[]');
      if (!Array.isArray(parsed)) throw new Error('参数必须是数组');
    } catch {
      return;
    }
    const businessObject = selectedElement.value.businessObject;
    const current = businessObject.extensionElements;
    const values = (current?.values || []).filter((item: any) => !(item.$type === `flowable:${type}` || item.$type?.endsWith(`:${type}`)));
    parsed
      .filter((item) => item && String(item.source || '').trim() && String(item.target || '').trim())
      .forEach((item) => values.push(service<any>('bpmnFactory').createAny(`flowable:${type}`, 'http://flowable.org/bpmn', {
        source: String(item.source).trim(),
        target: String(item.target).trim(),
      })));
    const extensionElements = service<any>('bpmnFactory').create('bpmn:ExtensionElements', { values });
    service<any>('modeling').updateProperties(selectedElement.value, { extensionElements });
  }

  function updateGatewayDefaultFlow(flowId?: string) {
    if (!selectedElement.value || !isGateway.value) return;
    const target = (selectedElement.value.outgoing || []).find((flow: any) => flow.id === flowId);
    service<any>('modeling').updateProperties(selectedElement.value, { default: target?.businessObject });
  }

  function updateExtension(name: string, value: unknown) {
    if (!selectedElement.value) return;
    updateElementExtension(selectedElement.value, name, value);
  }

  function updateCandidateStrategy() {
    const strategy = Number(properties.candidateStrategy);
    if ([34, 35, 36].includes(strategy)) {
      properties.candidateParam = '';
    } else if ([23, 51].includes(strategy)) {
      properties.candidateParam = `${candidateBaseParam.value || ''}|${candidateLevel.value}`;
    } else if ([37, 38].includes(strategy)) {
      properties.candidateParam = String(candidateLevel.value);
    } else if (![10, 20, 21, 22, 30, 40, 50, 60].includes(strategy)) {
      properties.candidateParam = '';
    }
    updateExtension('candidateStrategy', strategy);
    updateExtension('candidateParam', properties.candidateParam);
  }

  function updateApproveMethod(value?: number | string) {
    properties.approveMethod = Number(value) || 1;
    updateExtension('approveMethod', properties.approveMethod);
    updateMultiInstanceLoop();
  }

  function updateApproveRatio(value?: number | string | null) {
    properties.approveRatio = Math.min(100, Math.max(1, Number(value) || 100));
    updateExtension('approveRatio', properties.approveRatio);
    updateMultiInstanceLoop();
  }

  function updateEnabledButtons(value: unknown[]) {
    properties.enabledButtons = (value || []).map(Number).filter((item) => Number.isInteger(item));
    // Keep an explicit empty selection distinguishable from a legacy XML that
    // has no enabledButtons extension (legacy models default to all buttons).
    updateExtension('enabledButtons', properties.enabledButtons.length ? properties.enabledButtons.join(',') : '__none__');
  }

  /**
   * 将 BPMN 设计器里的审批方式落为 Flowable 原生多实例配置。
   * 候选集合由引擎在流程启动/节点流转前写入流程变量，
   * 这样 BPMN 设计器和简易设计器使用同一套候选人策略及租户边界。
   */
  function updateMultiInstanceLoop() {
    const element = selectedElement.value;
    if (!element || !isUserTask.value) return;
    const modeling = service<any>('modeling');
    const method = Number(properties.approveMethod) || 1;
    if (method === 1) {
      modeling.updateProperties(element, { loopCharacteristics: null });
      updateExtension('approveRatio', '');
      return;
    }
    const factory = service<any>('bpmnFactory');
    const loop = factory.create('bpmn:MultiInstanceLoopCharacteristics', {
      isSequential: method === 4,
      collection: `\${_workflowTaskAssignees_${element.id}}`,
      elementVariable: `_workflowTaskAssignee_${element.id}`,
    });
    const condition = method === 3
      ? '${nrOfCompletedInstances > 0}'
      : method === 2
        ? `\${nrOfCompletedInstances * 100 >= nrOfInstances * ${properties.approveRatio}}`
        : '${nrOfCompletedInstances >= nrOfInstances}';
    loop.completionCondition = factory.create('bpmn:FormalExpression', { body: condition });
    modeling.updateProperties(element, { loopCharacteristics: loop });
  }

  function setCandidateValue(value: string | string[]) {
    properties.candidateParam = Array.isArray(value) ? value.join(',') : value;
    updateExtension('candidateParam', properties.candidateParam);
  }

  function setCandidateBaseParam(value: string | string[]) {
    const base = Array.isArray(value) ? value.join(',') : value;
    const strategy = Number(properties.candidateStrategy);
    properties.candidateParam = [23, 51].includes(strategy) ? `${base}|${candidateLevel.value}` : base;
    updateExtension('candidateParam', properties.candidateParam);
  }

  function setCandidateLevel(value: number | string | null) {
    const level = Math.max(1, Number(value) || 1);
    const strategy = Number(properties.candidateStrategy);
    if ([23, 51].includes(strategy)) {
      properties.candidateParam = `${candidateBaseParam.value || ''}|${level}`;
    } else {
      properties.candidateParam = String(level);
    }
    updateExtension('candidateParam', properties.candidateParam);
  }

  function updateProcessListeners(ids: number[]) {
    const process = selectedElement.value;
    if (!process || selectedType.value !== 'bpmn:Process') return;
    const factory = service<any>('bpmnFactory');
    const existing = process.businessObject.extensionElements?.values || [];
    const templates = processListenerRecords.value.filter((item) => item.type === 'execution');
    // Keep hand-written execution listeners; only replace listeners represented by
    // the template selector, matching the task-listener behavior below.
    const other = existing.filter((item: any) => item.$type !== 'flowable:ExecutionListener' && !item.$type?.endsWith(':ExecutionListener')
      || !templates.some((template) => template.event === item.event && listenerValue(template) === listenerValue(item)));
    for (const id of ids || []) {
      const template = processListenerRecords.value.find((item) => item.id === Number(id) && item.type === 'execution');
      if (!template) continue;
      const attrs: Record<string, string> = { event: template.event };
      const valueType = template.valueType?.toLowerCase();
      if (valueType === 'class') attrs.class = template.value;
      else if (valueType === 'expression') attrs.expression = template.value;
      else attrs.delegateExpression = template.value;
      other.push(factory.createAny('flowable:ExecutionListener', 'http://flowable.org/bpmn', attrs));
    }
    const extensionElements = factory.create('bpmn:ExtensionElements', { values: other });
    service<any>('modeling').updateProperties(process, { extensionElements });
    properties.processListenerIds = (ids || []).map((id) => Number(id)).filter((id) => Number.isInteger(id));
    properties.executionListeners = listenerSettingsJson({ extensionElements }, 'ExecutionListener');
    executionListenerRows.value = parseListenerRows(properties.executionListeners);
  }

  function updateTaskListeners(ids: number[]) {
    const task = selectedElement.value;
    if (!task || !isUserTask.value) return;
    const factory = service<any>('bpmnFactory');
    const existing = task.businessObject.extensionElements?.values || [];
    const templates = processListenerRecords.value.filter((item) => item.type === 'task');
    const other = existing.filter((item: any) => item.$type !== 'flowable:TaskListener' && !item.$type?.endsWith(':TaskListener')
      || !templates.some((template) => template.event === item.event && listenerValue(template) === listenerValue(item)));
    for (const id of ids || []) {
      const template = templates.find((item) => item.id === Number(id));
      if (!template) continue;
      const attrs: Record<string, string> = { event: template.event };
      const valueType = template.valueType?.toLowerCase();
      if (valueType === 'class') attrs.class = template.value;
      else if (valueType === 'expression') attrs.expression = template.value;
      else attrs.delegateExpression = template.value;
      other.push(factory.createAny('flowable:TaskListener', 'http://flowable.org/bpmn', attrs));
    }
    const extensionElements = factory.create('bpmn:ExtensionElements', { values: other });
    service<any>('modeling').updateProperties(task, { extensionElements });
  }

  function updateListenerJson(listenerType: 'TaskListener' | 'ExecutionListener', value: string) {
    const element = selectedElement.value;
    if (!element || (listenerType === 'TaskListener' && !isUserTask.value)) return;
    let settings: Array<Record<string, any>>;
    try {
      const parsed = JSON.parse(value || '[]');
      if (!Array.isArray(parsed)) return;
      settings = parsed;
    } catch {
      return;
    }
    const factory = service<any>('bpmnFactory');
    const existing = element.businessObject.extensionElements?.values || [];
    const other = existing.filter((item: any) => item.$type !== `flowable:${listenerType}` && !item.$type?.endsWith(`:${listenerType}`));
    for (const setting of settings) {
      const event = String(setting.eventName || setting.event || '').trim();
      const implementation = String(setting.value || setting.implementation || setting.delegateExpression || '').trim();
      if (!event || !implementation) continue;
      const valueType = String(setting.valueType || setting.implementationType || 'delegateExpression').toLowerCase();
      const attrs: Record<string, string> = { event };
      if (valueType === 'class') attrs.class = implementation;
      else if (valueType === 'expression') attrs.expression = implementation;
      else attrs.delegateExpression = implementation.startsWith('${') ? implementation : `\${${implementation}}`;
      other.push(factory.createAny(`flowable:${listenerType}`, 'http://flowable.org/bpmn', attrs));
    }
    const extensionElements = factory.create('bpmn:ExtensionElements', { values: other });
    service<any>('modeling').updateProperties(element, { extensionElements });
  }

  function updateTaskListenerJson(value: string) {
    properties.taskListeners = value;
    taskListenerRows.value = parseListenerRows(value);
    updateListenerJson('TaskListener', value);
  }

  function updateExecutionListenerJson(value: string) {
    properties.executionListeners = value;
    executionListenerRows.value = parseListenerRows(value);
    updateListenerJson('ExecutionListener', value);
  }

  function updateElementExtension(element: any, name: string, value: unknown) {
    const businessObject = element.businessObject;
    const current = businessObject.extensionElements;
    const values = (current?.values || []).filter((item: any) => !(item.$type === `flowable:${name}` || item.$type?.endsWith(`:${name}`)));
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      const extension = service<any>('bpmnFactory').createAny(`flowable:${name}`, 'http://flowable.org/bpmn', { body: String(value) });
      values.push(extension);
    }
    const extensionElements = service<any>('bpmnFactory').create('bpmn:ExtensionElements', { values });
    service<any>('modeling').updateProperties(element, { extensionElements });
  }

  function timeoutBoundaryForSelected() {
    return selectedElement.value ? isTimeoutBoundaryFor(selectedElement.value, selectedElement.value.id) : undefined;
  }

  function toggleTimeoutBoundary(enabled: boolean | string | number) {
    const task = selectedElement.value;
    if (!task || !isUserTask.value) return;
    const existing = isTimeoutBoundaryFor(task, task.id);
    const modeling = service<any>('modeling');
    if (!enabled) {
      if (existing) modeling.removeShape(existing);
      return;
    }
    if (existing) return;
    const elementFactory = service<any>('elementFactory');
    const boundary = elementFactory.createShape({ type: 'bpmn:BoundaryEvent', eventDefinitionType: 'bpmn:TimerEventDefinition' });
    modeling.createShape(boundary, { x: task.x + task.width / 2, y: task.y + task.height }, task);
    const timer = createTimeoutDefinition();
    modeling.updateProperties(boundary, { cancelActivity: false, eventDefinitions: [timer] });
    updateElementExtension(boundary, 'boundaryEventType', 1);
    updateElementExtension(boundary, 'timeoutHandlerType', properties.timeoutHandlerType);
  }

  function updateTimeoutHandler(value: number | string) {
    const boundary = timeoutBoundaryForSelected();
    if (boundary) {
      properties.timeoutHandlerType = Number(value) || 2;
      updateElementExtension(boundary, 'timeoutHandlerType', properties.timeoutHandlerType);
      updateTimeoutTimer(boundary);
    }
  }

  function updateTimeoutDuration() {
    const boundary = timeoutBoundaryForSelected();
    if (boundary) updateTimeoutTimer(boundary);
  }

  function updateTimeoutMaxRemindCount(value: number | string | null) {
    properties.timeoutMaxRemindCount = Math.min(99, Math.max(1, Number(value) || 1));
    const boundary = timeoutBoundaryForSelected();
    if (boundary) updateTimeoutTimer(boundary);
  }

  function createTimeoutDefinition() {
    const factory = service<any>('bpmnFactory');
    const duration = properties.timeoutDuration || 'P7D';
    const repeat = Number(properties.timeoutMaxRemindCount) || 1;
    return factory.create('bpmn:TimerEventDefinition', {
      timeDuration: properties.timeoutHandlerType === 1 && repeat > 1 ? undefined : duration,
      timeCycle: properties.timeoutHandlerType === 1 && repeat > 1 ? `R${repeat}/${duration}` : undefined,
    });
  }

  function updateTimeoutTimer(boundary: any) {
    service<any>('modeling').updateProperties(boundary, { eventDefinitions: [createTimeoutDefinition()] });
  }

  function updateBoundaryDefinition() {
    const element = selectedElement.value;
    if (!element || !isBoundaryEvent.value) return;
    const factory = service<any>('bpmnFactory');
    const definitions = modeler.value?.getDefinitions();
    const rootElements = definitions?.rootElements || [];
    const type = properties.boundaryType;
    let definition: any;
    if (type === 'error') {
      const errorId = `${element.id}_error`;
      let error = rootElements.find((item: any) => item.$type === 'bpmn:Error' && item.id === errorId);
      if (!error) {
        error = factory.create('bpmn:Error', { id: errorId });
        rootElements.push(error);
      }
      error.errorCode = properties.boundaryCode || undefined;
      error.name = properties.boundaryCode || errorId;
      definition = factory.create('bpmn:ErrorEventDefinition', { errorRef: error });
    } else if (type === 'message') {
      const messageId = `${element.id}_message`;
      let message = rootElements.find((item: any) => item.$type === 'bpmn:Message' && item.id === messageId);
      if (!message) {
        message = factory.create('bpmn:Message', { id: messageId });
        rootElements.push(message);
      }
      message.name = properties.boundaryCode || messageId;
      definition = factory.create('bpmn:MessageEventDefinition', { messageRef: message });
    } else if (type === 'signal') {
      const signalId = `${element.id}_signal`;
      let signal = rootElements.find((item: any) => item.$type === 'bpmn:Signal' && item.id === signalId);
      if (!signal) {
        signal = factory.create('bpmn:Signal', { id: signalId });
        rootElements.push(signal);
      }
      signal.name = properties.boundaryCode || signalId;
      definition = factory.create('bpmn:SignalEventDefinition', { signalRef: signal });
    } else if (type === 'conditional') {
      const condition = properties.boundaryExpression.trim()
        ? factory.create('bpmn:FormalExpression', { body: properties.boundaryExpression.trim() })
        : undefined;
      definition = factory.create('bpmn:ConditionalEventDefinition', { condition });
    } else {
      definition = factory.create('bpmn:TimerEventDefinition', { timeDuration: properties.timeoutDuration || 'P7D' });
    }
    service<any>('modeling').updateProperties(element, {
      eventDefinitions: [definition],
      cancelActivity: properties.boundaryCancelActivity,
    });
    updateElementExtension(element, 'boundaryEventType', type === 'timer' ? 1 : '');
    updateElementExtension(element, 'timeoutHandlerType', type === 'timer' ? properties.timeoutHandlerType : '');
  }

  function updateEventDefinition() {
    const element = selectedElement.value;
    if (!element || !isConfigurableEvent.value) return;
    const type = properties.eventDefinitionType;
    const factory = service<any>('bpmnFactory');
    const definitions = modeler.value?.getDefinitions();
    const rootElements = definitions?.rootElements || [];
    let definition: any;
    if (type === 'message') {
      const id = `${element.id}_message`;
      let message = rootElements.find((item: any) => item.$type === 'bpmn:Message' && item.id === id);
      if (!message) {
        message = factory.create('bpmn:Message', { id, name: properties.eventDefinitionName || id });
        rootElements.push(message);
      } else {
        message.name = properties.eventDefinitionName || id;
      }
      definition = factory.create('bpmn:MessageEventDefinition', { messageRef: message });
    } else if (type === 'signal') {
      const id = `${element.id}_signal`;
      let signal = rootElements.find((item: any) => item.$type === 'bpmn:Signal' && item.id === id);
      if (!signal) {
        signal = factory.create('bpmn:Signal', { id, name: properties.eventDefinitionName || id });
        rootElements.push(signal);
      } else {
        signal.name = properties.eventDefinitionName || id;
      }
      definition = factory.create('bpmn:SignalEventDefinition', { signalRef: signal });
    } else if (type === 'conditional') {
      const condition = properties.eventDefinitionExpression.trim()
        ? factory.create('bpmn:FormalExpression', { body: properties.eventDefinitionExpression.trim() })
        : undefined;
      definition = factory.create('bpmn:ConditionalEventDefinition', { condition });
    } else if (type === 'timer') {
      definition = factory.create('bpmn:TimerEventDefinition', { timeDuration: properties.eventDefinitionDuration || 'P1D' });
    }
    service<any>('modeling').updateProperties(element, { eventDefinitions: definition ? [definition] : undefined });
  }

  function updateBoundaryCancelActivity(value: boolean | { target?: { checked?: boolean } }) {
    if (!selectedElement.value || !isBoundaryEvent.value) return;
    const checked = typeof value === 'boolean' ? value : Boolean(value?.target?.checked);
    properties.boundaryCancelActivity = checked;
    service<any>('modeling').updateProperties(selectedElement.value, { cancelActivity: checked });
  }

  function updateConditionExpression() {
    if (!selectedElement.value) return;
    const expression = properties.conditionExpression.trim()
      ? service<any>('bpmnFactory').create('bpmn:FormalExpression', { body: properties.conditionExpression.trim() })
      : undefined;
    service<any>('modeling').updateProperties(selectedElement.value, { conditionExpression: expression });
  }

  function zoom(delta: number) {
    const canvas = service<any>('canvas');
    const current = Number(canvas.zoom()) || 1;
    canvas.zoom(Math.max(0.2, Math.min(4, current + delta)));
  }

  function resetZoom() {
    nextTick(() => service<any>('canvas')?.zoom('fit-viewport'));
  }

  function undo() {
    service<any>('commandStack')?.undo();
  }

  function redo() {
    service<any>('commandStack')?.redo();
  }

  onMounted(async () => {
    const { default: BpmnModeler } = await import('bpmn-js/lib/Modeler');
    modeler.value = new BpmnModeler({
      container: canvasRef.value,
      additionalModules: [bpmnPalette, bpmnTranslation],
      moddleExtensions: { flowable: flowableDescriptor },
    });
    const eventBus = service<any>('eventBus');
    const syncPaletteTooltips = () => {
      nextTick(() => {
        canvasRef.value?.querySelectorAll<HTMLElement>('.djs-palette .entry[aria-label]').forEach((entry) => {
          entry.title = entry.getAttribute('aria-label') || '';
        });
      });
    };
    eventBus.on('palette.create', syncPaletteTooltips);
    eventBus.on('palette.changed', syncPaletteTooltips);
    eventBus.on('selection.changed', (event: any) => loadProperties(event.newSelection?.[0]));
    eventBus.on('element.changed', (event: any) => {
      if (event.element?.id === selectedElement.value?.id) loadProperties(event.element);
    });
    eventBus.on('commandStack.changed', scheduleEmit);
    syncPaletteTooltips();
    try {
      const models = await getModelList();
      childProcessOptions.value = (models || [])
        .filter((model: any) => model?.key)
        .map((model: any) => ({ label: model.name ? `${model.name} (${model.key})` : model.key, value: model.key }));
    } catch {
      childProcessOptions.value = [];
    }
    try {
      const page = await getProcessListenerPage({ pageNo: 1, pageSize: 100, status: 0 });
      processListenerRecords.value = page.records || [];
    } catch {
      processListenerRecords.value = [];
    }
    if (props.modelValue?.trim()) {
      await importXml(props.modelValue);
    } else {
      // bpmn-js only creates the canvas shell for an empty XML string. Create
      // the initial diagram as well so the palette and a usable start node are
      // visible immediately when creating a new BPMN model.
      await modeler.value.createDiagram();
      ensureDefaultNodeNames();
      resetZoom();
      await emitXml();
    }
  });

  watch(
    () => props.modelValue,
    (value) => importXml(value)
  );

  watch(processListenerRecords, () => {
    if (selectedElement.value?.businessObject?.$type === 'bpmn:Process') loadProperties(selectedElement.value);
  });

  watch(
    () => props.formFields,
    () => syncFieldPermissionRows(),
    { deep: true },
  );

  onBeforeUnmount(() => {
    if (changeTimer) clearTimeout(changeTimer);
    modeler.value?.destroy();
  });
</script>

<style lang="less" scoped>
  .bpmn-designer {
    overflow: hidden;
    border: 1px solid #d9d9d9;
    border-radius: 6px;
    background: #fff;
  }

  .designer-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    border-bottom: 1px solid #f0f0f0;
  }

  .toolbar-tip {
    color: #8c8c8c;
    font-size: 12px;
  }

  .listener-hint {
    margin-top: 6px;
    color: #8c8c8c;
    font-size: 12px;
    line-height: 1.5;
  }

  .designer-body {
    display: flex;
    height: 58vh;
    min-height: 520px;
  }

  .designer-canvas {
    min-width: 0;
    flex: 1;
    background-color: #fafafa;
    background-image: linear-gradient(#eee 1px, transparent 1px), linear-gradient(90deg, #eee 1px, transparent 1px);
    background-size: 20px 20px;
  }

  .property-panel {
    width: 380px;
    overflow-y: auto;
    padding: 14px;
    border-left: 1px solid #f0f0f0;

    h3 {
      margin-bottom: 14px;
    }
  }

  .field-permission-editor {
    overflow: hidden;
    margin-bottom: 8px;
    border: 1px solid #f0f0f0;
    border-radius: 4px;
  }

  .field-permission-head,
  .field-permission-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    min-height: 34px;
    padding: 5px 8px;
    border-bottom: 1px solid #f5f5f5;
  }

  .field-permission-head {
    color: #595959;
    font-size: 12px;
    background: #fafafa;

    a {
      margin-left: 8px;
    }
  }

  .field-permission-row:last-child {
    border-bottom: 0;
  }

  .field-permission-label {
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .advanced-json {
    margin-top: 6px;
  }

  .listener-editor {
    display: flex;
    flex-direction: column;
    gap: 6px;
    margin-bottom: 6px;
  }

  .listener-row {
    display: flex;
    align-items: center;
    gap: 6px;

    .ant-input {
      min-width: 0;
    }
  }

</style>

<!-- bpmn-js renders the palette outside Vue's scoped-style attribute. Keep its
     vendor CSS and the small visibility overrides global so the draggable
     entries are styled correctly after the modeler is mounted. -->
<style lang="less">
  @import 'bpmn-js/dist/assets/diagram-js.css';
  @import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css';

  .bpmn-designer .djs-palette {
    z-index: 20;
    display: block;
    background: #fff;
  }

  .bpmn-designer .djs-palette .entry {
    cursor: grab;
  }

  .bpmn-designer .djs-palette .entry:active {
    cursor: grabbing;
  }
</style>
