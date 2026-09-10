<template>
  <div class="approval-node-config">
    <div class="node-name-row">
      <span>节点名称：</span>
      <a-input v-model:value="node.name" :maxlength="64" />
    </div>
    <div class="approve-type-row">
      <span>审批类型：</span>
      <a-radio-group v-model:value="node.approveType" button-style="solid">
        <a-radio-button :value="1">人工审批</a-radio-button>
        <a-radio-button :value="2">自动通过</a-radio-button>
        <a-radio-button :value="3">自动拒绝</a-radio-button>
      </a-radio-group>
    </div>

    <a-tabs v-model:active-key="activeTab">
      <a-tab-pane key="approver" tab="审批人">
        <div v-if="node.approveType === 1">
          <a-form-item label="审批人设置" required>
            <a-radio-group v-model:value="node.candidateStrategy" class="strategy-grid" @change="normalizeCandidate">
              <a-radio v-for="item in candidateStrategies" :key="item.value" :value="item.value">{{ item.label }}</a-radio>
            </a-radio-group>
          </a-form-item>

          <a-form-item v-if="node.candidateStrategy === 30" label="指定用户" required>
            <JSelectUser :value="node.candidateParam" row-key="id" label-key="realname" :multiple="true" @update:value="setCandidateValue" />
          </a-form-item>
          <a-form-item v-else-if="node.candidateStrategy === 10" label="指定角色" required>
            <JSelectRole :value="node.candidateParam" row-key="roleCode" :multiple="true" @update:value="setCandidateValue" />
          </a-form-item>
          <template v-else-if="[20, 21, 23].includes(node.candidateStrategy)">
            <a-form-item label="指定部门" required>
              <JSelectDept :value="candidateBaseParam" row-key="id" :multiple="true" @update:value="setCandidateBaseParam" />
            </a-form-item>
            <a-form-item v-if="node.candidateStrategy === 23" label="连续部门层级">
              <a-input-number :value="candidateLevel" :min="1" :max="15" @update:value="setCandidateLevel" />
            </a-form-item>
          </template>
          <a-form-item v-else-if="node.candidateStrategy === 22" label="指定岗位" required>
            <JSelectPosition :value="node.candidateParam" row-key="id" :multiple="true" @update:value="setCandidateValue" />
          </a-form-item>
          <a-form-item v-else-if="node.candidateStrategy === 40" label="指定用户组" required>
            <a-select v-model:value="userGroupParam" mode="multiple" :options="userGroupOptions" placeholder="请选择用户组" />
          </a-form-item>
          <a-form-item v-else-if="[34, 35, 36].includes(node.candidateStrategy)" label="审批人">
            <a-input :value="candidateStrategyHint" disabled />
          </a-form-item>
          <a-form-item v-else-if="[37, 38].includes(node.candidateStrategy)" label="部门层级">
            <a-input-number :value="candidateLevel" :min="1" :max="15" @update:value="setCandidateLevel" />
          </a-form-item>
          <a-form-item v-else-if="node.candidateStrategy === 50" label="表单内用户字段" required>
            <a-select v-model:value="node.candidateParam" :options="formFields" show-search option-filter-prop="label" placeholder="请选择用户字段" />
          </a-form-item>
          <template v-else-if="node.candidateStrategy === 51">
            <a-form-item label="表单内部门字段" required>
              <a-select :value="candidateBaseParam" :options="formFields" show-search option-filter-prop="label" placeholder="请选择部门字段" @update:value="setCandidateBaseParam" />
            </a-form-item>
            <a-form-item label="连续部门层级">
              <a-input-number :value="candidateLevel" :min="1" :max="15" @update:value="setCandidateLevel" />
            </a-form-item>
          </template>
          <a-form-item v-else-if="node.candidateStrategy === 60" label="流程表达式" required>
            <a-textarea v-model:value="node.candidateParam" :rows="3" placeholder="例如：${assigneeId}" />
          </a-form-item>

          <a-form-item label="多人审批方式" required>
            <a-radio-group v-model:value="node.approveMethod" class="vertical-radios">
              <a-radio :value="4">按顺序依次审批</a-radio>
              <a-radio :value="2">会签（可同时审批，达到通过比例后完成）</a-radio>
              <a-radio :value="3">或签（可同时审批，有一人通过即可）</a-radio>
              <a-radio :value="1">随机挑选一人审批</a-radio>
            </a-radio-group>
          </a-form-item>
          <a-form-item v-if="node.approveMethod === 2" label="会签通过比例" required>
            <a-input-number v-model:value="node.approveRatio" :min="1" :max="100" addon-after="%" />
          </a-form-item>

          <section class="setting-grid">
            <div>
              <a-divider orientation="left">审批人拒绝时</a-divider>
              <a-radio-group v-model:value="node.rejectHandler.type" class="vertical-radios">
                <a-radio :value="1">终止流程</a-radio>
                <a-radio :value="2">驳回到指定节点</a-radio>
              </a-radio-group>
              <a-select
                v-if="node.rejectHandler.type === 2"
                v-model:value="node.rejectHandler.returnNodeId"
                class="section-control"
                :options="returnNodeOptions"
                placeholder="请选择流程中的任务节点"
              />
            </div>
            <div>
              <a-divider orientation="left">审批人超时未处理时</a-divider>
              <a-switch v-model:checked="node.timeoutHandler.enable" checked-children="开启" un-checked-children="关闭" />
              <div v-if="node.timeoutHandler.enable" class="inline-settings">
                <a-select v-model:value="node.timeoutHandler.type" :options="timeoutTypes" />
                <a-input-number v-model:value="timeoutAmount" :min="1" />
                <a-select v-model:value="timeoutUnit" :options="timeUnits" />
                <a-input-number v-if="node.timeoutHandler.type === 1" v-model:value="node.timeoutHandler.maxRemindCount" :min="1" :max="99" addon-after="次" />
              </div>
            </div>
            <div>
              <a-divider orientation="left">审批人与提交人为同一人时</a-divider>
              <a-radio-group v-model:value="node.assignStartUserHandlerType" class="vertical-radios">
                <a-radio :value="1">由发起人对自己审批</a-radio>
                <a-radio :value="2">自动跳过</a-radio>
                <a-radio :value="3">转交给部门负责人审批</a-radio>
              </a-radio-group>
            </div>
            <div>
              <a-divider orientation="left">审批人为空时</a-divider>
              <a-radio-group v-model:value="node.assignEmptyHandler.type" class="vertical-radios" @change="normalizeEmptyHandler">
                <a-radio :value="1">自动通过</a-radio>
                <a-radio :value="2">自动拒绝</a-radio>
                <a-radio :value="3">指定成员审批</a-radio>
                <a-radio :value="4">转交给流程管理员</a-radio>
              </a-radio-group>
              <JSelectUser
                v-if="node.assignEmptyHandler.type === 3"
                v-model:value="node.assignEmptyHandler.userIds"
                class="section-control"
                row-key="id"
                label-key="realname"
                :multiple="true"
              />
            </div>
            <div>
              <a-divider orientation="left">是否需要签名</a-divider>
              <a-switch v-model:checked="node.signEnable" checked-children="是" un-checked-children="否" />
            </div>
            <div>
              <a-divider orientation="left">审批意见</a-divider>
              <a-switch v-model:checked="node.reasonRequire" checked-children="必填" un-checked-children="非必填" />
            </div>
          </section>
          <a-divider orientation="left">跳过表达式</a-divider>
          <a-textarea v-model:value="node.skipExpression" :rows="3" allow-clear placeholder="例如：${amount > 1000}" />
        </div>
        <a-alert v-else :message="node.approveType === 2 ? '进入该节点后自动通过' : '进入该节点后自动拒绝'" type="info" show-icon />
      </a-tab-pane>

      <a-tab-pane key="buttons" tab="操作按钮设置">
        <h3>操作按钮</h3>
        <div class="setting-table">
          <div class="setting-table-row setting-table-head"><span>操作按钮</span><span>显示名称</span><span>启用</span></div>
          <div v-for="item in node.buttonsSetting" :key="item.id" class="setting-table-row">
            <span>{{ buttonNames[item.id] }}</span>
            <a-input v-model:value="item.displayName" :placeholder="buttonNames[item.id]" :maxlength="20" />
            <a-switch v-model:checked="item.enable" />
          </div>
        </div>
      </a-tab-pane>

      <a-tab-pane key="fields" tab="表单字段权限">
        <h3>字段权限</h3>
        <a-empty v-if="!node.fieldsPermission.length" description="请先在“表单设计”中选择流程表单" />
        <div v-else class="setting-table fields-table">
          <div class="setting-table-row setting-table-head">
            <span>字段名称</span>
            <span class="permission-head"><a @click="setAllPermissions('READ')">只读</a><a @click="setAllPermissions('WRITE')">可编辑</a><a @click="setAllPermissions('NONE')">隐藏</a></span>
          </div>
          <div v-for="item in node.fieldsPermission" :key="item.field" class="setting-table-row">
            <span>{{ item.title || item.field }}</span>
            <a-radio-group v-model:value="item.permission" class="permission-radios">
              <a-radio value="READ" aria-label="只读" />
              <a-radio value="WRITE" aria-label="可编辑" />
              <a-radio value="NONE" aria-label="隐藏" />
            </a-radio-group>
          </div>
        </div>
      </a-tab-pane>

      <a-tab-pane key="listeners" tab="监听器">
        <section v-for="listener in listenerTypes" :key="listener.key" class="listener-section">
          <a-divider orientation="left">{{ listener.label }}</a-divider>
          <a-switch v-model:checked="node[listener.key].enable" checked-children="开启" un-checked-children="关闭" />
          <template v-if="node[listener.key].enable">
            <a-alert class="listener-tip" message="仅支持 POST 请求，以请求体方式接收参数" type="warning" show-icon />
            <a-form-item label="请求地址" required>
              <a-input v-model:value="node[listener.key].path" placeholder="https://example.com/callback" />
            </a-form-item>
            <HttpSettingRows v-model="node[listener.key].header" label="请求头" />
            <HttpSettingRows v-model="node[listener.key].body" label="请求体" />
          </template>
        </section>
        <a-collapse ghost>
          <a-collapse-panel key="advanced" header="高级 Flowable 监听器">
            <div>
              <a-form-item label="任务监听器 JSON">
                <a-textarea v-model:value="node.taskListeners" :rows="4" placeholder='[{"eventName":"create","value":"taskListenerBean"}]' />
              </a-form-item>
              <a-form-item label="执行监听器 JSON">
                <a-textarea v-model:value="node.executionListeners" :rows="4" placeholder='[{"eventName":"start","value":"executionListenerBean"}]' />
              </a-form-item>
            </div>
          </a-collapse-panel>
        </a-collapse>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup lang="ts">
  import { computed, onMounted, ref, watch } from 'vue';
  import { getUserGroupSimpleList } from '/@/api/bpm/userGroup';
  import JSelectDept from '/@/components/Form/src/jeecg/components/JSelectDept.vue';
  import JSelectPosition from '/@/components/Form/src/jeecg/components/JSelectPosition.vue';
  import JSelectRole from '/@/components/Form/src/jeecg/components/JSelectRole.vue';
  import JSelectUser from '/@/components/Form/src/jeecg/components/JSelectUser.vue';
  import HttpSettingRows from '../model/HttpSettingRows.vue';

  type SimpleNode = Record<string, any>;
  type FormFieldOption = { label: string; value: string };

  const props = defineProps<{ node: SimpleNode; formFields: FormFieldOption[]; returnNodeOptions: FormFieldOption[] }>();
  const activeTab = ref('approver');
  const userGroupOptions = ref<{ label: string; value: string }[]>([]);
  const candidateStrategies = [
    { label: '指定成员', value: 30 }, { label: '指定角色', value: 10 }, { label: '指定岗位', value: 22 },
    { label: '部门成员', value: 20 }, { label: '部门负责人', value: 21 }, { label: '连续多级部门负责人', value: 23 },
    { label: '发起人自选', value: 35 }, { label: '审批人自选', value: 34 }, { label: '发起人本人', value: 36 },
    { label: '发起人部门负责人', value: 37 }, { label: '发起人连续部门负责人', value: 38 }, { label: '用户组', value: 40 },
    { label: '表单内用户字段', value: 50 }, { label: '表单内部门负责人', value: 51 }, { label: '流程表达式', value: 60 },
  ];
  const buttonNames: Record<number, string> = { 1: '通过', 2: '拒绝', 3: '转办', 4: '委派', 5: '加签', 6: '退回' };
  const timeoutTypes = [{ label: '自动提醒', value: 1 }, { label: '自动同意', value: 2 }, { label: '自动拒绝', value: 3 }];
  const timeUnits = [{ label: '分钟', value: 'M' }, { label: '小时', value: 'H' }, { label: '天', value: 'D' }];
  const listenerTypes = [
    { key: 'taskCreateListener', label: '创建任务' },
    { key: 'taskAssignListener', label: '指派任务执行人员' },
    { key: 'taskCompleteListener', label: '完成任务' },
  ];

  const candidateParts = computed(() => String(props.node.candidateParam || '').split('|'));
  const candidateBaseParam = computed(() => candidateParts.value[0] || '');
  const candidateLevel = computed(() => Number(candidateParts.value[1] || candidateParts.value[0] || 1));
  const candidateStrategyHint = computed(() => {
    if (props.node.candidateStrategy === 34) return '由上一审批人在通过时选择';
    if (props.node.candidateStrategy === 35) return '由流程发起人在提交时选择';
    if (props.node.candidateStrategy === 36) return '流程发起人本人';
    return '系统自动计算';
  });
  const timeoutAmount = computed({
    get: () => Number(String(props.node.timeoutHandler?.timeDuration || 'PT6H').match(/\d+/)?.[0] || 1),
    set: (value: number) => updateTimeoutDuration(value, timeoutUnit.value),
  });
  const timeoutUnit = computed({
    get: () => String(props.node.timeoutHandler?.timeDuration || 'PT6H').endsWith('D') ? 'D' : String(props.node.timeoutHandler?.timeDuration || 'PT6H').endsWith('M') ? 'M' : 'H',
    set: (value: string) => updateTimeoutDuration(timeoutAmount.value, value),
  });
  const userGroupParam = computed({
    get: () => String(props.node.candidateParam || '').split(',').filter(Boolean),
    set: (value: string[]) => (props.node.candidateParam = value.join(',')),
  });

  watch(() => props.node.buttonsSetting, syncEnabledButtons, { deep: true });
  watch(() => props.formFields, ensureFieldPermissions, { deep: true, immediate: true });
  onMounted(async () => {
    const groups = await getUserGroupSimpleList();
    userGroupOptions.value = (groups || []).map((item) => ({ label: item.name, value: String(item.id) }));
  });

  function setCandidateValue(value: string | string[]) { props.node.candidateParam = Array.isArray(value) ? value.join(',') : value; }
  function setCandidateBaseParam(value: string | string[]) {
    const base = Array.isArray(value) ? value.join(',') : value;
    props.node.candidateParam = `${base || ''}|${candidateLevel.value}`;
  }
  function setCandidateLevel(value: number | null) {
    const level = value || 1;
    props.node.candidateParam = [23, 51].includes(props.node.candidateStrategy) ? `${candidateBaseParam.value}|${level}` : String(level);
  }
  function normalizeCandidate() {
    props.node.assignee = undefined;
    props.node.candidateParam = [34, 35, 36].includes(props.node.candidateStrategy) ? undefined : [23, 51].includes(props.node.candidateStrategy) ? '|1' : [37, 38].includes(props.node.candidateStrategy) ? '1' : '';
  }
  function normalizeEmptyHandler() {
    if (props.node.assignEmptyHandler.type !== 3) delete props.node.assignEmptyHandler.userIds;
  }
  function updateTimeoutDuration(value: number, unit: string) {
    props.node.timeoutHandler.timeDuration = unit === 'D' ? `P${value || 1}D` : `PT${value || 1}${unit}`;
  }
  function syncEnabledButtons() {
    props.node.enabledButtons = (props.node.buttonsSetting || []).filter((item: any) => item.enable).map((item: any) => item.id);
  }
  function ensureFieldPermissions() {
    const existing = new Map<string, any>((props.node.fieldsPermission || []).map((item: any) => [String(item.field), item]));
    if (!props.formFields.length) {
      props.node.fieldsPermission = Array.from(existing.values());
      return;
    }
    props.node.fieldsPermission = props.formFields.map((field) => ({
      field: field.value,
      title: field.label,
      permission: existing.get(String(field.value))?.permission || 'READ',
    }));
  }
  function setAllPermissions(permission: string) {
    props.node.fieldsPermission.forEach((item: any) => (item.permission = permission));
  }
</script>

<style scoped lang="less">
  .approval-node-config { padding: 0 2px 16px; }
  .node-name-row, .approve-type-row { display: flex; align-items: center; gap: 12px; margin-bottom: 12px; }
  .node-name-row :deep(.ant-input) { width: 320px; }
  .approve-type-row { margin-bottom: 6px; }
  .strategy-grid { display: grid; grid-template-columns: repeat(3, minmax(190px, 1fr)); gap: 14px 24px; width: 100%; }
  .vertical-radios { display: flex; flex-direction: column; gap: 12px; }
  .setting-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0 28px; }
  .section-control { width: 100%; margin-top: 12px; }
  .inline-settings { display: grid; grid-template-columns: 1.5fr 1fr 1fr 1fr; gap: 8px; margin-top: 12px; }
  .setting-table { border-top: 1px solid #e5e7eb; border-left: 1px solid #e5e7eb; }
  .setting-table-row { display: grid; grid-template-columns: 1fr 1.5fr 120px; align-items: center; min-height: 48px; border-right: 1px solid #e5e7eb; border-bottom: 1px solid #e5e7eb; }
  .setting-table-row > * { padding: 8px 16px; }
  .setting-table-head { background: #fafafa; font-weight: 600; }
  .fields-table .setting-table-row { grid-template-columns: 1fr 2fr; }
  .permission-head, .permission-radios { display: grid; grid-template-columns: repeat(3, 1fr); text-align: center; }
  .permission-radios :deep(.ant-radio-wrapper) { justify-content: center; margin: 0; }
  .listener-section { margin-bottom: 28px; }
  .listener-tip { margin: 18px 0; }
  @media (max-width: 900px) {
    .strategy-grid, .setting-grid { grid-template-columns: 1fr; }
    .inline-settings { grid-template-columns: 1fr 1fr; }
  }
</style>
