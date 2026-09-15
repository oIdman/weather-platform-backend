<template>
  <div class="designer-shell">
    <div class="designer-toolbar">
      <a-upload accept=".json" :show-upload-list="false" :before-upload="importDesign"><a-button>导入</a-button></a-upload>
      <a-button @click="exportDesign">导出</a-button>
      <a-button aria-label="缩小" @click="zoom = Math.max(50, zoom - 10)">−</a-button>
      <a-button @click="zoom = 100">{{ zoom }}%</a-button>
      <a-button aria-label="放大" @click="zoom = Math.min(150, zoom + 10)">＋</a-button>
    </div>
    <main class="canvas">
      <div :style="{ zoom: zoom / 100 }">
        <SimpleProcessTree
          :node="localModel"
          :selected-id="propertiesOpen ? selectedNode?.id : ''"
          @select="selectNode"
          @add="insertAfter"
          @delete="deleteNode"
        />
      </div>
    </main>
    <a-drawer
      v-model:open="propertiesOpen"
      :title="selectedNode?.name || '节点配置'"
      :width="[11, 13].includes(selectedNode?.type) ? 1040 : selectedNode?.type === 10 ? 620 : 420"
      :z-index="1100"
    >
      <template v-if="selectedNode">
        <a-form layout="vertical">
          <a-form-item label="节点类型"
            ><a-tag>{{ nodeTypeName(selectedNode.type) }}</a-tag></a-form-item
          >
          <a-form-item v-if="selectedNode.type !== 11" label="节点名称"><a-input v-model:value="selectedNode.name" :disabled="[1, 10].includes(selectedNode.type)" /></a-form-item>

          <ApprovalNodeConfig
            v-if="[11, 13].includes(selectedNode.type)"
            :node="selectedNode"
            :form-fields="formFields"
            :return-node-options="returnNodeOptions"
            :handler-mode="selectedNode.type === 13"
          />

          <StartUserNodeConfig
            v-else-if="selectedNode.type === 10"
            :node="selectedNode"
            :form-fields="formFields"
            :start-user-ids="startUserIds"
            :start-dept-ids="startDeptIds"
          />

          <template v-else-if="selectedNode.type === 12">
            <a-form-item label="候选人策略">
              <a-select v-model:value="selectedNode.candidateStrategy" :options="candidateStrategies" @change="normalizeCandidate" />
            </a-form-item>
            <a-form-item v-if="[34, 35, 36].includes(selectedNode.candidateStrategy)" label="审批人">
              <a-input :value="candidateStrategyHint(selectedNode.candidateStrategy)" disabled />
            </a-form-item>
            <a-form-item v-else-if="selectedNode.candidateStrategy === 60" label="流程表达式">
              <a-textarea v-model:value="selectedNode.candidateParam" :rows="3" placeholder="例如：${assigneeId}" />
            </a-form-item>
            <a-form-item v-else-if="selectedNode.candidateStrategy === 30" label="指定成员">
              <JSelectUser :value="selectedNode.candidateParam" row-key="id" label-key="realname" multiple="multiple" @update:value="setCandidateUsers" />
            </a-form-item>
            <a-form-item v-else-if="selectedNode.candidateStrategy === 10" label="指定角色">
              <JSelectRole :value="selectedNode.candidateParam" row-key="roleCode" multiple="multiple" @update:value="setCandidateValue" />
            </a-form-item>
            <template v-else-if="[20, 21, 23].includes(selectedNode.candidateStrategy)">
              <a-form-item label="指定部门">
                <JSelectDept :value="candidateBaseParam()" row-key="id" multiple="multiple" @update:value="setCandidateBaseParam" />
              </a-form-item>
              <a-form-item v-if="selectedNode.candidateStrategy === 23" label="连续部门层级">
                <a-input-number :value="candidateLevel()" :min="1" style="width: 100%" @update:value="setCandidateLevel" />
              </a-form-item>
            </template>
            <a-form-item v-else-if="selectedNode.candidateStrategy === 22" label="指定岗位">
              <JSelectPosition :value="selectedNode.candidateParam" row-key="id" multiple="multiple" @update:value="setCandidateValue" />
            </a-form-item>
            <a-form-item v-else-if="[37, 38].includes(selectedNode.candidateStrategy)" label="部门层级">
              <a-input-number :value="candidateLevel()" :min="1" style="width: 100%" @update:value="setCandidateLevel" />
            </a-form-item>
            <a-form-item v-else-if="selectedNode.candidateStrategy === 50" label="表单用户字段">
              <a-input v-model:value="selectedNode.candidateParam" placeholder="填写表单字段名称，例如 approverUserIds" />
            </a-form-item>
            <template v-else-if="selectedNode.candidateStrategy === 51">
              <a-form-item label="表单部门字段">
                <a-input :value="candidateBaseParam()" placeholder="填写表单字段名称，例如 departmentIds" @update:value="setCandidateBaseParam" />
              </a-form-item>
              <a-form-item label="连续部门层级">
                <a-input-number :value="candidateLevel()" :min="1" style="width: 100%" @update:value="setCandidateLevel" />
              </a-form-item>
            </template>
            <a-form-item v-else label="候选参数">
              <a-input v-model:value="selectedNode.candidateParam" placeholder="多个用户组编号用英文逗号分隔" />
            </a-form-item>
            <a-form-item v-if="[11, 13].includes(selectedNode.type)" label="直接办理人（高级）">
              <a-input v-model:value="selectedNode.assignee" allow-clear placeholder="填写后优先于候选人策略" />
            </a-form-item>
            <a-form-item v-if="[11, 13].includes(selectedNode.type)" label="多人审批方式">
              <a-radio-group v-model:value="selectedNode.approveMethod">
                <a-radio :value="4">依次审批</a-radio>
                <a-radio :value="3">或签</a-radio>
                <a-radio :value="2">会签</a-radio>
                <a-radio :value="1">随机一人</a-radio>
              </a-radio-group>
            </a-form-item>
            <a-form-item v-if="[11, 13].includes(selectedNode.type) && selectedNode.approveMethod === 2" label="会签通过比例">
              <a-input-number v-model:value="selectedNode.approveRatio" :min="1" :max="100" addon-after="%" style="width: 100%" />
            </a-form-item>
            <template v-if="[11, 13].includes(selectedNode.type)">
              <a-divider orientation="left">{{ selectedNode.type === 11 ? '审批' : '办理' }}人为空时</a-divider>
              <a-radio-group v-model:value="selectedNode.assignEmptyHandler.type" @change="normalizeEmptyHandler">
                <a-space direction="vertical">
                  <a-radio :value="1">自动通过</a-radio>
                  <a-radio :value="2">自动拒绝</a-radio>
                  <a-radio :value="3">指定人员审批</a-radio>
                  <a-radio :value="4">转交给流程管理员</a-radio>
                </a-space>
              </a-radio-group>
              <a-form-item v-if="selectedNode.assignEmptyHandler.type === 3" class="mt-3" label="指定用户" required>
                <JSelectUser
                  v-model:value="selectedNode.assignEmptyHandler.userIds"
                  row-key="id"
                  label-key="realname"
                  multiple="multiple"
                />
              </a-form-item>
            </template>
            <template v-if="[11, 13].includes(selectedNode.type)">
              <a-divider orientation="left">拒绝后处理</a-divider>
              <a-radio-group v-model:value="selectedNode.rejectHandler.type">
                <a-space direction="vertical">
                  <a-radio :value="1">直接终止流程</a-radio>
                  <a-radio :value="2">驳回到指定任务节点</a-radio>
                </a-space>
              </a-radio-group>
              <a-form-item v-if="selectedNode.rejectHandler?.type === 2" class="mt-3" label="退回节点" required>
                <a-select v-model:value="selectedNode.rejectHandler.returnNodeId" :options="returnNodeOptions" placeholder="请选择流程中的任务节点" />
              </a-form-item>
            </template>
            <template v-if="[11, 13].includes(selectedNode.type)">
              <a-divider orientation="left">审批超时</a-divider>
              <a-form-item label="启用超时处理">
                <a-switch v-model:checked="selectedNode.timeoutHandler.enable" checked-children="启用" un-checked-children="关闭" />
              </a-form-item>
              <template v-if="selectedNode.timeoutHandler.enable">
                <a-form-item label="超时处理方式">
                  <a-select
                    v-model:value="selectedNode.timeoutHandler.type"
                    :options="[
                      { label: '自动提醒', value: 1 },
                      { label: '自动同意', value: 2 },
                      { label: '自动拒绝', value: 3 },
                    ]"
                  />
                </a-form-item>
                <a-form-item label="超时时间（ISO-8601）">
                  <a-input v-model:value="selectedNode.timeoutHandler.timeDuration" placeholder="例如 PT30M、PT6H、P7D" />
                </a-form-item>
                <a-form-item v-if="selectedNode.timeoutHandler.type === 1" label="最大提醒次数">
                  <a-input-number v-model:value="selectedNode.timeoutHandler.maxRemindCount" :min="1" :max="99" style="width: 100%" />
                </a-form-item>
              </template>
            </template>
            <template v-if="selectedNode.type === 11">
              <a-divider orientation="left">审批人与发起人相同时</a-divider>
              <a-radio-group v-model:value="selectedNode.assignStartUserHandlerType">
                <a-space direction="vertical">
                  <a-radio :value="1">由发起人对自己审批</a-radio>
                  <a-radio :value="2">自动跳过</a-radio>
                  <a-radio :value="3">转交给部门负责人审批</a-radio>
                </a-space>
              </a-radio-group>
            </template>
            <template v-if="[11, 13].includes(selectedNode.type)">
              <a-divider orientation="left">跳过设置</a-divider>
              <a-form-item label="跳过表达式">
                <a-textarea
                  v-model:value="selectedNode.skipExpression"
                  :rows="2"
                  allow-clear
                  placeholder="例如：${amount > 1000}，满足时自动跳过该审批节点"
                />
              </a-form-item>
              <a-form-item label="字段权限 JSON">
                <a-textarea
                  v-model:value="selectedNode.fieldsPermission"
                  :rows="3"
                  allow-clear
                  placeholder='例如：[{"field":"amount","permission":"WRITE"}]'
                />
              </a-form-item>
              <a-form-item label="任务监听器 JSON">
                <a-textarea
                  v-model:value="selectedNode.taskListeners"
                  :rows="3"
                  allow-clear
                  placeholder='例如：[{"eventName":"create","value":"yourTaskListenerBean"}]'
                />
              </a-form-item>
              <a-form-item label="执行监听器 JSON">
                <a-textarea
                  v-model:value="selectedNode.executionListeners"
                  :rows="3"
                  allow-clear
                  placeholder='例如：[{"eventName":"start","value":"yourExecutionListenerBean"}]'
                />
              </a-form-item>
            </template>
            <template v-if="selectedNode.type === 11">
              <a-divider orientation="left">操作能力</a-divider>
              <a-checkbox-group v-model:value="selectedNode.enabledButtons" :options="buttonOptions" />
              <a-form-item class="mt-3" label="审批签名">
                <a-switch v-model:checked="selectedNode.signEnable" checked-children="需要" un-checked-children="不需要" />
              </a-form-item>
              <a-form-item class="mt-3" label="审批意见">
                <a-switch v-model:checked="selectedNode.reasonRequire" checked-children="必填" un-checked-children="选填" />
              </a-form-item>
            </template>
          </template>

          <template v-if="selectedNode.type === 14">
            <a-form-item label="等待时长（ISO-8601）">
              <a-input v-model:value="selectedNode.delaySetting.duration" placeholder="例如 PT30M、PT2H、P1D" />
            </a-form-item>
          </template>

           <template v-if="selectedNode.type === 15">
             <a-form-item label="触发器类型">
              <a-select v-model:value="selectedNode.triggerSetting.type" :options="triggerTypes" @change="normalizeTriggerSetting" />
             </a-form-item>
            <a-form-item v-if="[1, 2].includes(selectedNode.triggerSetting.type)" label="请求地址">
              <a-input v-model:value="selectedNode.triggerSetting.httpRequestSetting.url" placeholder="https://example.com/callback" />
            </a-form-item>
            <template v-if="[1, 2].includes(selectedNode.triggerSetting.type)">
              <HttpSettingRows v-model="selectedNode.triggerSetting.httpRequestSetting.header" label="请求头" />
              <HttpSettingRows v-model="selectedNode.triggerSetting.httpRequestSetting.body" label="请求体" />
              <TriggerResponseRows
                v-if="selectedNode.triggerSetting.type === 1"
                v-model="selectedNode.triggerSetting.httpRequestSetting.response"
                label="响应字段映射"
              />
            </template>
            <TriggerFormSettings
              v-else
              v-model="selectedNode.triggerSetting.formSettings"
              :mode="selectedNode.triggerSetting.type === 10 ? 'update' : 'delete'"
              :form-fields="formFields"
            />
          </template>

          <template v-if="selectedNode.type === 20">
            <a-form-item label="子流程标识" required>
              <a-input v-model:value="selectedNode.childProcessSetting.calledProcessDefinitionKey" placeholder="已发布流程定义 key" />
            </a-form-item>
            <a-form-item label="子流程实例名">
              <a-input v-model:value="selectedNode.childProcessSetting.calledProcessDefinitionName" placeholder="可为空，例如 ${title} 的子流程" />
            </a-form-item>
            <a-form-item label="异步启动">
              <a-switch v-model:checked="selectedNode.childProcessSetting.async" />
            </a-form-item>
            <a-form-item label="跳过子流程发起人节点">
              <a-switch v-model:checked="selectedNode.childProcessSetting.skipStartUserNode" checked-children="跳过" un-checked-children="不跳过" />
            </a-form-item>
            <a-divider orientation="left">子流程发起人</a-divider>
            <a-form-item label="发起人来源">
              <a-select v-model:value="selectedNode.childProcessSetting.startUserSetting.type" :options="[
                { label: '同主流程发起人', value: 1 },
                { label: '表单用户字段', value: 2 },
              ]" />
            </a-form-item>
            <a-form-item v-if="selectedNode.childProcessSetting.startUserSetting.type === 2" label="用户字段" required>
              <a-select v-model:value="selectedNode.childProcessSetting.startUserSetting.formField" :options="formFields" show-search option-filter-prop="label" placeholder="请选择用户字段" />
            </a-form-item>
            <a-form-item v-if="selectedNode.childProcessSetting.startUserSetting.type === 2" label="字段为空时">
              <a-select v-model:value="selectedNode.childProcessSetting.startUserSetting.emptyType" :options="[
                { label: '同主流程发起人', value: 1 },
                { label: '子流程管理员', value: 2 },
                { label: '主流程管理员', value: 3 },
              ]" />
            </a-form-item>
            <a-form-item label="输入变量 JSON">
              <a-textarea
                v-model:value="selectedNode.childProcessSetting.inVariables"
                :rows="3"
                allow-clear
                placeholder='例如：[{"source":"applyUserId","target":"applyUserId"}]'
              />
            </a-form-item>
            <a-form-item label="输出变量 JSON">
              <a-textarea
                v-model:value="selectedNode.childProcessSetting.outVariables"
                :rows="3"
                allow-clear
                placeholder='例如：[{"source":"result","target":"subResult"}]'
              />
            </a-form-item>
            <a-divider orientation="left">子流程超时</a-divider>
            <a-form-item label="启用超时处理">
              <a-switch v-model:checked="selectedNode.childProcessSetting.timeoutSetting.enable" checked-children="启用" un-checked-children="关闭" />
            </a-form-item>
            <template v-if="selectedNode.childProcessSetting.timeoutSetting.enable">
              <a-form-item label="超时类型">
                <a-select v-model:value="selectedNode.childProcessSetting.timeoutSetting.type" :options="[
                  { label: '固定时长', value: 1 },
                  { label: '固定日期时间', value: 2 },
                ]" />
              </a-form-item>
              <a-form-item label="超时时间表达式" required>
                <a-input v-model:value="selectedNode.childProcessSetting.timeoutSetting.timeExpression" placeholder="例如 PT2H 或 2026-10-01T18:00:00" />
              </a-form-item>
            </template>
            <a-divider orientation="left">子流程多实例</a-divider>
            <a-form-item label="启用多实例">
              <a-switch v-model:checked="selectedNode.childProcessSetting.multiInstanceSetting.enable" checked-children="启用" un-checked-children="关闭" />
            </a-form-item>
            <template v-if="selectedNode.childProcessSetting.multiInstanceSetting.enable">
              <a-form-item label="是否串行">
                <a-switch v-model:checked="selectedNode.childProcessSetting.multiInstanceSetting.sequential" checked-children="是" un-checked-children="否" />
              </a-form-item>
              <a-form-item label="完成比例">
                <a-input-number v-model:value="selectedNode.childProcessSetting.multiInstanceSetting.approveRatio" :min="1" :max="100" addon-after="%" style="width: 100%" />
              </a-form-item>
              <a-form-item label="实例来源">
                <a-select v-model:value="selectedNode.childProcessSetting.multiInstanceSetting.sourceType" :options="[
                  { label: '固定数量', value: 1 },
                  { label: '数字表单字段', value: 2 },
                  { label: '多选表单字段', value: 3 },
                ]" />
              </a-form-item>
              <a-form-item label="实例数量/字段" required>
                <a-input-number v-if="selectedNode.childProcessSetting.multiInstanceSetting.sourceType === 1" v-model:value="selectedNode.childProcessSetting.multiInstanceSetting.source" :min="1" style="width: 100%" />
                <a-select v-else v-model:value="selectedNode.childProcessSetting.multiInstanceSetting.source" :options="formFields" show-search option-filter-prop="label" placeholder="请选择表单字段" />
              </a-form-item>
            </template>
          </template>

          <template v-if="selectedNode.type === 50">
            <a-form-item label="条件表达式">
              <a-textarea
                v-model:value="selectedNode.conditionSetting.conditionExpression"
                :disabled="selectedNode.conditionSetting.defaultFlow"
                :rows="3"
                placeholder="例如 amount > 1000"
              />
            </a-form-item>
            <a-form-item label="默认分支">
              <a-switch v-model:checked="selectedNode.conditionSetting.defaultFlow" @change="markDefaultBranch" />
            </a-form-item>
          </template>

          <template v-if="isBranch(selectedNode.type)">
            <a-divider orientation="left">分支管理</a-divider>
            <a-space direction="vertical" style="width: 100%">
              <div v-for="(branch, index) in selectedNode.conditionNodes" :key="branch.id" class="branch-row">
                <a-input v-model:value="branch.name" />
                <a-button size="small" @click="selectNode(branch)">配置</a-button>
                <a-button size="small" danger :disabled="selectedNode.conditionNodes.length <= 2 || isProtectedBranch(branch, index)" @click="removeBranch(branch)">删除</a-button>
              </div>
              <a-button block type="dashed" @click="addBranch">添加分支</a-button>
            </a-space>
          </template>
        </a-form>
      </template>
      <a-empty v-else description="请选择流程节点" />
      <template #footer><a-space><a-button type="primary" @click="propertiesOpen = false">确定</a-button></a-space></template>
    </a-drawer>
  </div>
</template>

<script lang="ts" name="SimpleProcessDesigner" setup>
  import { computed, ref, watch } from 'vue';
  import ApprovalNodeConfig from './ApprovalNodeConfig.vue';
  import SimpleProcessTree from './SimpleProcessTree.vue';
  import StartUserNodeConfig from './StartUserNodeConfig.vue';
  import TriggerFormSettings from './TriggerFormSettings.vue';
  import TriggerResponseRows from './TriggerResponseRows.vue';
  import HttpSettingRows from '../model/HttpSettingRows.vue';
  import JSelectDept from '/@/components/Form/src/jeecg/components/JSelectDept.vue';
  import JSelectPosition from '/@/components/Form/src/jeecg/components/JSelectPosition.vue';
  import JSelectRole from '/@/components/Form/src/jeecg/components/JSelectRole.vue';
  import JSelectUser from '/@/components/Form/src/jeecg/components/JSelectUser.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  const { createMessage } = useMessage();
  const propertiesOpen = ref(false);
  const zoom = ref(100);

  type SimpleNode = Record<string, any>;

  const props = withDefaults(
    defineProps<{
      modelValue?: SimpleNode;
      formFields?: { label: string; value: string }[];
      startUserIds?: string[];
      startDeptIds?: string[];
    }>(),
    { formFields: () => [], startUserIds: () => [], startDeptIds: () => [] }
  );
  const emit = defineEmits<{ 'update:modelValue': [value: SimpleNode] }>();

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
  const triggerTypes = [
    { label: '发送 HTTP 请求', value: 1 },
    { label: '等待 HTTP 回调', value: 2 },
    { label: '更新表单数据', value: 10 },
    { label: '删除表单数据', value: 11 },
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
  const typeNames: Record<number, string> = {
    1: '结束',
    10: '发起人',
    11: '审批人',
    12: '抄送人',
    13: '办理人',
    14: '延迟器',
    15: '触发器',
    20: '子流程',
    50: '条件',
    51: '条件分支',
    52: '并行分支',
    53: '包容分支',
    54: '路由分支',
  };

  const defaultModel = (): SimpleNode => ({
    id: 'StartUserNode',
    type: 10,
    name: '发起人',
    showText: '请设置发起人',
    childNode: { id: 'EndEvent', type: 1, name: '结束' },
  });
  const clone = (value: any) => JSON.parse(JSON.stringify(value));
  const localModel = ref<SimpleNode>(props.modelValue && Object.keys(props.modelValue).length ? clone(props.modelValue) : defaultModel());
  const selectedNode = ref<SimpleNode | null>(localModel.value);

  watch(
    () => props.modelValue,
    (value) => {
      if (JSON.stringify(value || {}) === JSON.stringify(localModel.value)) return;
      localModel.value = value && Object.keys(value).length ? clone(value) : defaultModel();
      selectedNode.value = localModel.value;
    }
  );
  watch(localModel, (value) => emit('update:modelValue', clone(value)), { deep: true });

  const canInsert = computed(() => !!selectedNode.value && selectedNode.value.type !== 1);
  const canDelete = computed(() => !!selectedNode.value && ![1, 10, 50].includes(selectedNode.value.type));
  const returnNodeOptions = computed(() => {
    if (!selectedNode.value) return [];
    const options: { label: string; value: string }[] = [];
    const visit = (node: SimpleNode | undefined) => {
      if (!node) return;
      if ([10, 11, 13].includes(node.type) && node.id !== selectedNode.value?.id) {
        options.push({ label: node.name || node.id, value: node.id });
      }
      visit(node.childNode);
      (node.conditionNodes || []).forEach(visit);
    };
    visit(localModel.value);
    return options;
  });

  function nodeTypeName(type: number) {
    return typeNames[type] || `节点 ${type}`;
  }

  function isBranch(type: number) {
    return [51, 52, 53, 54].includes(type);
  }

  function selectNode(node: SimpleNode) {
    selectedNode.value = node;
    ensureNodeSettings(node);
    propertiesOpen.value = true;
  }

  function setCandidateUsers(value: string | string[]) {
    setCandidateValue(value);
  }
  function setCandidateValue(value: string | string[]) {
    if (!selectedNode.value) return;
    selectedNode.value.candidateParam = Array.isArray(value) ? value.join(',') : value;
    delete selectedNode.value.showText;
  }
  function candidateBaseParam() {
    return String(selectedNode.value?.candidateParam || '').split('|')[0];
  }
  function candidateLevel() {
    if (!selectedNode.value) return 1;
    const value = String(selectedNode.value.candidateParam || '');
    if ([37, 38].includes(selectedNode.value.candidateStrategy)) return Math.max(1, Number(value) || 1);
    return Math.max(1, Number(value.split('|')[1]) || 1);
  }
  function setCandidateBaseParam(value: string | string[]) {
    if (!selectedNode.value) return;
    const base = Array.isArray(value) ? value.join(',') : value || '';
    if ([23, 51].includes(selectedNode.value.candidateStrategy)) {
      selectedNode.value.candidateParam = `${base}|${candidateLevel()}`;
    } else {
      selectedNode.value.candidateParam = base;
    }
    delete selectedNode.value.showText;
  }
  function setCandidateLevel(value: number | null) {
    if (!selectedNode.value) return;
    const level = Math.max(1, Number(value) || 1);
    if ([37, 38].includes(selectedNode.value.candidateStrategy)) {
      selectedNode.value.candidateParam = String(level);
    } else {
      selectedNode.value.candidateParam = `${candidateBaseParam()}|${level}`;
    }
    delete selectedNode.value.showText;
  }
  function insertAfter(node: SimpleNode, type: number) {
    selectedNode.value = node;
    insertNode(type);
  }
  function exportDesign() {
    const url = URL.createObjectURL(new Blob([JSON.stringify(localModel.value, null, 2)], { type: 'application/json' }));
    const link = document.createElement('a'); link.href = url; link.download = 'simple-process.json'; link.click(); URL.revokeObjectURL(url);
  }
  async function importDesign(file: File) {
    try {
      if (file.size > 1024 * 1024) throw new Error('流程文件不能超过 1 MB');
      const data = JSON.parse(await file.text());
      const model = data.simpleModel || data;
      if (!model || model.type !== 10 || !model.id) throw new Error('请选择以发起人节点开始的简易流程 JSON');
      localModel.value = model; selectedNode.value = model; propertiesOpen.value = false;
    } catch (error: any) { createMessage.error(error.message || '导入失败'); }
    return false;
  }

  function insertNode(type: number) {
    if (!selectedNode.value || !canInsert.value) return;
    const current = selectedNode.value;
    const next = current.childNode;
    const node = createNode(type);
    node.childNode = next;
    current.childNode = node;
    selectedNode.value = node;
    ensureNodeSettings(node);
  }

  function createNode(type: number): SimpleNode {
    const id = `${type === 11 || type === 13 ? 'Activity' : 'Node'}_${crypto.randomUUID()}`;
    const node: SimpleNode = { id, type, name: nodeTypeName(type) };
    if ([11, 13].includes(type)) {
      Object.assign(node, {
        candidateStrategy: 30,
        candidateParam: '',
        approveType: 1,
        approveMethod: 4,
        approveRatio: 100,
        enabledButtons: [1, 2, 3, 4, 5, 6, 8],
        buttonsSetting: defaultButtonSettings(),
        fieldsPermission: [],
        reasonRequire: false,
        signEnable: false,
        rejectHandler: { type: 1 },
        timeoutHandler: { enable: false, type: 2, timeDuration: 'P7D', maxRemindCount: 1 },
        assignEmptyHandler: { type: 1 },
        assignStartUserHandlerType: 1,
        taskCreateListener: { enable: false, path: '', header: [], body: [] },
        taskAssignListener: { enable: false, path: '', header: [], body: [] },
        taskCompleteListener: { enable: false, path: '', header: [], body: [] },
      });
    } else if (type === 12) {
      Object.assign(node, { candidateStrategy: 30, candidateParam: '' });
    } else if (type === 14) {
      node.delaySetting = { delayType: 1, duration: 'PT30M' };
    } else if (type === 15) {
      node.triggerSetting = { type: 1, httpRequestSetting: { url: '', header: [], body: [], response: [] } };
    } else if (type === 20) {
      node.childProcessSetting = {
        calledProcessDefinitionKey: '',
        calledProcessDefinitionName: '',
        async: false,
        skipStartUserNode: false,
        startUserSetting: { type: 1, emptyType: 1, formField: '' },
        timeoutSetting: { enable: false, type: 1, timeExpression: 'PT1H' },
        multiInstanceSetting: { enable: false, sequential: false, approveRatio: 100, sourceType: 1, source: '' },
        inVariables: '[]',
        outVariables: '[]',
      };
    } else if (isBranch(type)) {
      node.conditionNodes = [createBranch(1, false), createBranch(2, true)];
    }
    return node;
  }

  function createBranch(index: number, defaultFlow: boolean): SimpleNode {
    return {
      id: `Condition_${crypto.randomUUID()}`,
      type: 50,
      name: defaultFlow ? '其他情况' : `条件 ${index}`,
      conditionSetting: { conditionType: 1, conditionExpression: '', defaultFlow },
    };
  }

  function ensureNodeSettings(node: SimpleNode) {
    if ([11, 12, 13].includes(node.type)) {
      node.candidateStrategy ||= 30;
      node.candidateParam ||= '';
    }
    if ([11, 13].includes(node.type)) {
      node.approveMethod ||= 4;
      node.approveRatio ||= 100;
      node.rejectHandler ||= { type: 1 };
      node.rejectHandler.type ||= 1;
      if (node.rejectHandler.type === 2 && !node.rejectHandler.returnNodeId) {
        node.rejectHandler.returnNodeId = undefined;
      }
      node.assignEmptyHandler ||= { type: 1 };
      node.assignEmptyHandler.type ||= 1;
      node.assignStartUserHandlerType ||= 1;
      node.signEnable ||= false;
      node.approveType ||= 1;
      node.buttonsSetting = normalizeButtonSettings(node.buttonsSetting, node.enabledButtons);
      node.enabledButtons = node.buttonsSetting.filter((item: any) => item.enable).map((item: any) => item.id);
      node.fieldsPermission = normalizeFieldPermissions(node.fieldsPermission);
      node.taskCreateListener ||= { enable: false, path: '', header: [], body: [] };
      node.taskAssignListener ||= { enable: false, path: '', header: [], body: [] };
      node.taskCompleteListener ||= { enable: false, path: '', header: [], body: [] };
      node.timeoutHandler ||= { enable: false, type: 2, timeDuration: 'P7D', maxRemindCount: 1 };
      node.timeoutHandler.type ||= 2;
      node.timeoutHandler.timeDuration ||= 'P7D';
      node.timeoutHandler.maxRemindCount ||= 1;
      if ([37, 38].includes(node.candidateStrategy) && !node.candidateParam) node.candidateParam = '1';
    }
    if (node.type === 10) {
      node.fieldsPermission = normalizeFieldPermissions(node.fieldsPermission);
      node.buttonsSetting ||= defaultStartButtonSettings();
    }
    if (node.type === 14) node.delaySetting ||= { delayType: 1, duration: 'PT30M' };
    if (node.type === 15) {
      node.triggerSetting ||= { type: 1 };
      if ([1, 2].includes(node.triggerSetting.type)) {
        node.triggerSetting.httpRequestSetting ||= { url: '', header: [], body: [], response: [] };
        node.triggerSetting.httpRequestSetting.header ||= [];
        node.triggerSetting.httpRequestSetting.body ||= [];
        node.triggerSetting.httpRequestSetting.response ||= [];
      } else {
        node.triggerSetting.formSettings ||= [{ updateFormFields: {}, deleteFields: [] }];
      }
    }
    if (node.type === 20) {
      node.childProcessSetting ||= { calledProcessDefinitionKey: '', calledProcessDefinitionName: '', async: false };
      node.childProcessSetting.calledProcessDefinitionName ||= '';
      node.childProcessSetting.skipStartUserNode ??= false;
      node.childProcessSetting.startUserSetting ||= { type: 1, emptyType: 1, formField: '' };
      node.childProcessSetting.startUserSetting.type ||= 1;
      node.childProcessSetting.startUserSetting.emptyType ||= 1;
      node.childProcessSetting.startUserSetting.formField ||= '';
      node.childProcessSetting.timeoutSetting ||= { enable: false, type: 1, timeExpression: 'PT1H' };
      node.childProcessSetting.timeoutSetting.enable ??= false;
      node.childProcessSetting.timeoutSetting.type ||= 1;
      node.childProcessSetting.timeoutSetting.timeExpression ||= 'PT1H';
      node.childProcessSetting.multiInstanceSetting ||= { enable: false, sequential: false, approveRatio: 100, sourceType: 1, source: '' };
      node.childProcessSetting.multiInstanceSetting.enable ??= false;
      node.childProcessSetting.multiInstanceSetting.sequential ??= false;
      node.childProcessSetting.multiInstanceSetting.approveRatio ||= 100;
      node.childProcessSetting.multiInstanceSetting.sourceType ||= 1;
      node.childProcessSetting.multiInstanceSetting.source ||= '';
      node.childProcessSetting.inVariables ||= '[]';
      node.childProcessSetting.outVariables ||= '[]';
    }
    if (node.type === 50) node.conditionSetting ||= { conditionType: 1, conditionExpression: '', defaultFlow: false };
    if (isBranch(node.type)) node.conditionNodes ||= [createBranch(1, false), createBranch(2, true)];
  }

  function findParent(root: SimpleNode, target: SimpleNode): SimpleNode | null {
    if (root.childNode === target) return root;
    for (const branch of root.conditionNodes || []) {
      if (branch === target) return root;
      const inBranch = findParent(branch, target);
      if (inBranch) return inBranch;
    }
    return root.childNode ? findParent(root.childNode, target) : null;
  }

  function deleteSelected() {
    if (!selectedNode.value || !canDelete.value) return;
    const parent = findParent(localModel.value, selectedNode.value);
    if (!parent) return;
    if (parent.childNode === selectedNode.value) {
      parent.childNode = selectedNode.value.childNode;
      selectedNode.value = parent;
      return;
    }
    const branchIndex = parent.conditionNodes?.indexOf(selectedNode.value) ?? -1;
    if (branchIndex >= 0) {
      parent.conditionNodes.splice(branchIndex, 1);
      selectedNode.value = parent;
    }
  }

  function deleteNode(node: SimpleNode) {
    if (![11, 12, 13, 14, 15, 20, 51, 52, 53, 54].includes(node.type)) return;
    selectedNode.value = node;
    deleteSelected();
    propertiesOpen.value = false;
  }

  function addBranch() {
    if (!selectedNode.value || !isBranch(selectedNode.value.type)) return;
    const branch = createBranch(selectedNode.value.conditionNodes.length + 1, false);
    selectedNode.value.conditionNodes.push(branch);
    selectedNode.value = branch;
  }

  function removeBranch(branch: SimpleNode) {
    if (!selectedNode.value || !isBranch(selectedNode.value.type) || selectedNode.value.conditionNodes.length <= 2) return;
    const index = selectedNode.value.conditionNodes.indexOf(branch);
    if (index < 0 || isProtectedBranch(branch, index)) return;
    selectedNode.value.conditionNodes = selectedNode.value.conditionNodes.filter((item: SimpleNode) => item !== branch);
  }

  function isProtectedBranch(branch: SimpleNode, index: number) {
    if (!selectedNode.value?.conditionNodes?.length) return true;
    return index === selectedNode.value.conditionNodes.length - 1 || branch.conditionSetting?.defaultFlow === true;
  }

  function markDefaultBranch(checked: boolean) {
    if (!checked || !selectedNode.value) return;
    const branch = selectedNode.value;
    const parent = findParent(localModel.value, branch);
    if (!parent?.conditionNodes) return;
    parent.conditionNodes.forEach((item: SimpleNode) => {
      ensureNodeSettings(item);
      item.conditionSetting.defaultFlow = item === branch;
    });
  }

  function normalizeCandidate() {
    if (!selectedNode.value) return;
    selectedNode.value.assignee = undefined;
    const strategy = selectedNode.value.candidateStrategy;
    if ([34, 35, 36].includes(strategy)) selectedNode.value.candidateParam = undefined;
    else if ([37, 38].includes(strategy)) selectedNode.value.candidateParam = '1';
    else if ([23, 51].includes(strategy)) selectedNode.value.candidateParam = '|1';
    else selectedNode.value.candidateParam = '';
  }

  function normalizeEmptyHandler() {
    if (!selectedNode.value?.assignEmptyHandler || selectedNode.value.assignEmptyHandler.type === 3) return;
    delete selectedNode.value.assignEmptyHandler.userIds;
  }

  function candidateStrategyHint(strategy: number) {
    if (strategy === 34) return '由上一审批人在通过时选择';
    if (strategy === 35) return '由流程发起人在提交时选择';
    return '流程发起人本人';
  }

  function defaultButtonSettings() {
    return buttonOptions.map((item) => ({ id: item.value, displayName: item.label, enable: item.value <= 6 || item.value === 8 }));
  }

  function defaultStartButtonSettings() {
    return [{ id: 1, displayName: '提交', enable: true }];
  }

  function normalizeFieldPermissions(value: unknown) {
    if (Array.isArray(value)) return value;
    if (typeof value !== 'string' || !value.trim()) return [];
    try {
      const parsed = JSON.parse(value);
      return Array.isArray(parsed) ? parsed : [];
    } catch {
      return [];
    }
  }

  function normalizeButtonSettings(value: unknown, enabledButtons: unknown) {
    let parsed: any[] = [];
    if (Array.isArray(value)) {
      parsed = value;
    } else if (typeof value === 'string' && value.trim()) {
      try {
        const candidate = JSON.parse(value);
        if (Array.isArray(candidate)) parsed = candidate;
      } catch {
        // 非法高级配置退回默认按钮，保存时由向导校验提示用户修正。
      }
    }
    const enabled = Array.isArray(enabledButtons) ? enabledButtons.map(Number) : null;
    const existing = new Map(parsed.map((item: any) => [Number(item?.id), item]));
    return defaultButtonSettings().map((item) => {
      const configured = existing.get(item.id);
      return {
        ...item,
        ...(configured || {}),
        id: item.id,
        enable: configured?.enable !== undefined ? Boolean(configured.enable) : enabled ? enabled.includes(item.id) : item.enable,
      };
    });
  }

  function normalizeTriggerSetting() {
    const setting = selectedNode.value?.triggerSetting;
    if (!setting) return;
    if ([1, 2].includes(setting.type)) {
      setting.httpRequestSetting ||= { url: '', header: [], body: [], response: [] };
      setting.httpRequestSetting.header ||= [];
      setting.httpRequestSetting.body ||= [];
      setting.httpRequestSetting.response ||= [];
      delete setting.formSettings;
    } else {
      setting.formSettings ||= [{ updateFormFields: {}, deleteFields: [] }];
      setting.formSettings.forEach((formSetting: SimpleNode) => {
        if (setting.type === 10) {
          delete formSetting.deleteFields;
          formSetting.updateFormFields ||= {};
        } else {
          delete formSetting.updateFormFields;
          formSetting.deleteFields ||= [];
        }
      });
      delete setting.httpRequestSetting;
    }
  }
</script>

<style scoped lang="less">
  .designer-shell { position: relative; min-height: 64vh; border: 1px solid #edf0f5; background: #fafafa; }
  .designer-toolbar { display: flex; justify-content: flex-end; gap: 4px; padding: 8px 12px; border-bottom: 1px solid #edf0f5; background: white; }
  .canvas { min-height: 60vh; padding: 40px 80px; overflow: auto; background-image: radial-gradient(#aab5c8 1px, transparent 1px); background-size: 24px 24px; }
  .branch-row { display: grid; grid-template-columns: 1fr auto auto; align-items: center; gap: 6px; }
</style>
