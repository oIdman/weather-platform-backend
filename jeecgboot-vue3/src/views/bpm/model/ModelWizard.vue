<template>
  <a-modal :open="true" width="100%" wrap-class-name="bpm-model-wizard" :closable="false" :keyboard="false" :mask-closable="false" :footer="null">
    <div class="wizard-shell">
      <header class="wizard-header">
        <div class="wizard-name">{{ draft.name || '新建流程模型' }}</div>
        <nav class="wizard-steps" aria-label="流程编辑步骤">
          <button v-for="(label, index) in steps" :key="label" :class="{ active: step === index }" :aria-current="step === index ? 'step' : undefined" @click="step = index">
            <span>{{ index + 1 }}</span>{{ label }}
          </button>
        </nav>
        <a-button @click="close">返回</a-button>
      </header>
      <main class="wizard-body" :class="{ 'design-body': step === 2 }">
        <a-form v-show="step === 0" class="wizard-form" :label-col="{ flex: '150px' }" :wrapper-col="{ flex: '1' }">
          <a-form-item label="流程标识" required><a-input v-model:value="draft.key" :disabled="!!draft.id" :maxlength="64" placeholder="请输入以字母开头的流程标识" /></a-form-item>
          <a-form-item label="流程名称" required><a-input v-model:value="draft.name" :maxlength="64" placeholder="请输入流程名称" /></a-form-item>
          <a-form-item label="流程分类" required><a-select v-model:value="draft.category" :options="categoryOptions" placeholder="请选择流程分类" /></a-form-item>
          <a-form-item label="流程图标"><JImageUpload v-model:value="draft.icon" :file-max="1" biz-path="bpm/model" /></a-form-item>
          <a-form-item label="流程描述"><a-textarea v-model:value="draft.description" :rows="3" :maxlength="500" /></a-form-item>
          <a-form-item label="流程类型" required><a-radio-group v-model:value="draft.type" :disabled="!!draft.id"><a-radio :value="10">BPMN 设计器</a-radio><a-radio :value="20">SIMPLE 设计器</a-radio></a-radio-group></a-form-item>
          <a-form-item label="是否可见" required><a-radio-group v-model:value="draft.visible"><a-radio :value="true">是</a-radio><a-radio :value="false">否</a-radio></a-radio-group></a-form-item>
          <a-form-item label="谁可以发起">
            <a-select v-model:value="startScope" :options="scopeOptions" />
            <JSelectUser v-if="startScope === 'users'" v-model:value="draft.startUserIds" class="scope-select" row-key="id" label-key="realname" :multiple="true" />
            <JSelectDept v-if="startScope === 'depts'" v-model:value="draft.startDeptIds" class="scope-select" row-key="id" :multiple="true" />
          </a-form-item>
          <a-form-item label="流程管理员" required><JSelectUser v-model:value="draft.managerUserIds" row-key="id" label-key="realname" :multiple="true" /></a-form-item>
        </a-form>

        <div v-show="step === 1" class="form-design-step">
          <a-form class="wizard-form" :label-col="{ flex: '150px' }" :wrapper-col="{ flex: '1' }">
            <a-form-item label="表单类型" required><a-radio-group v-model:value="draft.formType"><a-radio :value="10">流程表单</a-radio><a-radio :value="20">业务表单</a-radio></a-radio-group></a-form-item>
            <a-form-item v-if="draft.formType === 10" label="流程表单" required><a-select v-model:value="draft.formId" :options="formOptions" placeholder="请选择流程表单" /></a-form-item>
            <template v-else>
              <a-form-item label="提交页面路径" required>
                <a-input v-model:value="draft.formCustomCreatePath" placeholder="业务表单的提交页面路径" />
                <div class="field-hint">自定义表单的提交路径，使用 Vue 的路由地址，例如说: /bpm/oa/leave/create.vue</div>
              </a-form-item>
              <a-form-item label="查看页面路径" required>
                <a-input v-model:value="draft.formCustomViewPath" placeholder="业务表单的详情页面路径" />
                <div class="field-hint">自定义表单的查看组件地址，使用 Vue 的组件地址，例如说：/bpm/oa/leave/detail.vue</div>
              </a-form-item>
            </template>
          </a-form>
          <section v-if="draft.formType === 10" class="form-preview">
            <h3>表单预览</h3>
            <a-spin :spinning="previewLoading">
              <a-alert v-if="previewError" :message="previewError" type="error" show-icon />
              <FormCreate v-else-if="previewRules.length" :key="draft.formId" :rule="previewRules" :option="previewOption" />
              <a-empty v-else description="选择流程表单后显示预览" />
            </a-spin>
          </section>
        </div>

        <template v-if="step === 2">
          <SimpleProcessDesigner
            v-if="draft.type === 20"
            v-model="draft.simpleModel"
            :form-fields="fieldOptions"
            :start-user-ids="draft.startUserIds"
            :start-dept-ids="draft.startDeptIds"
          />
          <BpmnDesigner v-else v-model="draft.bpmnXml" :form-fields="fieldOptions" />
        </template>

        <a-form v-show="step === 3" class="wizard-form extra-form" :label-col="{ flex: '150px' }" :wrapper-col="{ flex: '1' }">
          <a-form-item label="提交人权限"><a-checkbox v-model:checked="draft.allowCancelRunningProcess">允许撤销审批中的申请</a-checkbox></a-form-item>
          <a-form-item label="审批人权限"><a-checkbox v-model:checked="draft.allowWithdrawTask">允许审批人撤回任务</a-checkbox><div class="field-hint">审批人可撤回正在审批节点的前一节点</div></a-form-item>
          <a-form-item label="流程编码">
            <div class="id-rule"><a-checkbox v-model:checked="draft.processIdRule.enable" /><a-input v-model:value="draft.processIdRule.prefix" :disabled="!draft.processIdRule.enable" placeholder="前缀" /><a-select v-model:value="draft.processIdRule.infix" :disabled="!draft.processIdRule.enable" :options="timeOptions" /><a-input v-model:value="draft.processIdRule.postfix" :disabled="!draft.processIdRule.enable" placeholder="后缀" /><a-input-number v-model:value="draft.processIdRule.length" :disabled="!draft.processIdRule.enable" :min="5" :max="32" /></div>
          </a-form-item>
          <a-form-item label="自动去重"><div>同一审批人在流程中重复出现时：</div><a-radio-group v-model:value="draft.autoApprovalType" class="vertical-options"><a-radio :value="0">不自动通过</a-radio><a-radio :value="1">仅审批一次，后续重复的审批节点均自动通过</a-radio><a-radio :value="2">仅针对连续审批的节点自动通过</a-radio></a-radio-group></a-form-item>
          <a-form-item label="标题设置"><a-radio-group v-model:value="draft.titleSetting.enable" class="vertical-options"><a-radio :value="false">系统默认 <span class="field-hint">展示流程名称</span></a-radio><a-radio :value="true">自定义标题</a-radio></a-radio-group><a-mentions v-if="draft.titleSetting.enable" v-model:value="draft.titleSetting.title" prefix="{" split="}" :options="titleFields" placeholder="输入 { 选择表单字段，或输入标题文本" /></a-form-item>
          <a-form-item v-if="draft.formType === 10" label="摘要设置"><a-radio-group v-model:value="draft.summarySetting.enable" class="vertical-options"><a-radio :value="false">系统默认 <span class="field-hint">展示表单前 3 个字段</span></a-radio><a-radio :value="true">自定义摘要</a-radio></a-radio-group><a-select v-if="draft.summarySetting.enable" v-model:value="draft.summarySetting.summary" mode="multiple" :options="fieldOptions" placeholder="请选择摘要字段" /></a-form-item>
          <a-form-item v-for="item in notifications" :key="item.key" :label="item.label">
            <a-space><a-switch :checked="!!draft[item.key]" @change="setNotification(item.key, $event)" /><span>{{ item.hint }}</span></a-space>
            <div v-if="draft[item.key]" class="notification-settings">
              <a-input v-model:value="draft[item.key].url" placeholder="通知地址：https://example.com/callback" />
              <HttpSettingRows v-for="part in httpParts" :key="part.key" v-model="draft[item.key][part.key]" :label="part.label" />
            </div>
          </a-form-item>
          <a-form-item label="自定义打印模板"><a-switch v-model:checked="draft.printTemplateSetting.enable" /><div v-if="draft.printTemplateSetting.enable" class="notification-settings"><Tinymce v-model="draft.printTemplateSetting.template" :height="320" /></div></a-form-item>
        </a-form>
      </main>
      <footer class="wizard-footer"><a-button @click="close">取消</a-button><a-button v-if="step > 0" @click="step--">上一步</a-button><a-button v-if="step < 3" type="primary" @click="next">下一步</a-button><a-button v-auth="draft.id ? 'bpm:model:update' : 'bpm:model:create'" :loading="saving" :type="step === 3 ? 'primary' : 'default'" @click="save">保存</a-button></footer>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { computed, defineAsyncComponent, reactive, ref, watch } from 'vue';
  import formCreate from '@form-create/ant-design-vue';
  import { createModel, updateModel, type BpmModel } from '/@/api/bpm/model';
  import { getForm, type BpmForm } from '/@/api/bpm/form';
  import type { BpmCategory } from '/@/api/bpm/category';
  import { useMessage } from '/@/hooks/web/useMessage';
  import JSelectUser from '/@/components/Form/src/jeecg/components/JSelectUser.vue';
  import JSelectDept from '/@/components/Form/src/jeecg/components/JSelectDept.vue';
  import JImageUpload from '/@/components/Form/src/jeecg/components/JImageUpload.vue';
  import HttpSettingRows from './HttpSettingRows.vue';

  const SimpleProcessDesigner = defineAsyncComponent(() => import('../components/SimpleProcessDesigner.vue'));
  const BpmnDesigner = defineAsyncComponent(() => import('../components/BpmnDesigner.vue'));
  const Tinymce = defineAsyncComponent(() => import('/@/components/Tinymce/src/Editor.vue'));
  const props = defineProps<{ model: BpmModel; categories: BpmCategory[]; forms: BpmForm[]; initialStep?: number }>();
  const emit = defineEmits<{ close: []; saved: [] }>();
  const { createMessage, createConfirm } = useMessage();
  const steps = ['基本信息', '表单设计', '流程设计', '更多设置'];
  const step = ref(props.initialStep || 0);
  const saving = ref(false);
  const draft = reactive<any>(JSON.parse(JSON.stringify(props.model)));
  draft.startUserIds ||= [];
  draft.startDeptIds ||= [];
  draft.processIdRule ||= { enable: false, prefix: '', infix: '', postfix: '', length: 5 };
  draft.titleSetting ||= { enable: false, title: '' };
  draft.summarySetting ||= { enable: false, summary: [] };
  draft.printTemplateSetting ||= { enable: false, template: '' };
  draft.autoApprovalType ??= 0;
  draft.simpleModel = draft.simpleModel && Object.keys(draft.simpleModel).length ? draft.simpleModel : { id: 'StartUserNode', type: 10, name: '发起人', childNode: { id: 'EndEvent', type: 1, name: '结束' } };
  const initial = JSON.stringify(draft);
  const startScope = ref(draft.startUserIds.length && draft.startDeptIds.length ? 'mixed' : draft.startUserIds.length ? 'users' : draft.startDeptIds.length ? 'depts' : 'all');
  const scopeOptions = [{ value: 'all', label: '全员' }, { value: 'users', label: '指定人员' }, { value: 'depts', label: '指定部门' }, ...(startScope.value === 'mixed' ? [{ value: 'mixed', label: '指定人员和部门（保留现有范围）' }] : [])];
  const categoryOptions = computed(() => props.categories.map((item) => ({ label: item.name, value: item.code })));
  const formOptions = computed(() => props.forms.map((item) => ({ label: item.name, value: item.id })));
  const timeOptions = [{ label: '无', value: '' }, { label: '精确到日', value: 'DAY' }, { label: '精确到时', value: 'HOUR' }, { label: '精确到分', value: 'MINUTE' }, { label: '精确到秒', value: 'SECOND' }];
  const notifications = [
    { key: 'processBeforeTriggerSetting', label: '流程前置通知', hint: '流程启动后通知' },
    { key: 'processAfterTriggerSetting', label: '流程后置通知', hint: '流程结束后通知' },
    { key: 'taskBeforeTriggerSetting', label: '任务前置通知', hint: '任务执行时通知' },
    { key: 'taskAfterTriggerSetting', label: '任务后置通知', hint: '任务结束后通知' },
  ];
  const httpParts = [{ key: 'header', label: '请求头' }, { key: 'body', label: '请求体' }, { key: 'response', label: '响应映射' }];
  function setNotification(key: string, checked: boolean | string | number) { draft[key] = checked ? { url: '', header: [], body: [], response: [] } : null; }
  const previewRules = ref<any[]>([]);
  const previewOption = ref<any>({});
  const previewLoading = ref(false);
  const previewError = ref('');
  const fieldOptions = ref<{ label: string; value: string }[]>([]);
  const titleFields = computed(() => [{ label: '流程名称', value: 'PROCESS_DEFINITION_NAME' }, { label: '发起人', value: 'START_USER_ID' }, { label: '发起时间', value: 'START_TIME' }, ...fieldOptions.value]);
  let previewRequest = 0;
  watch(() => [draft.formId, draft.formType], async () => {
    const request = ++previewRequest;
    previewRules.value = [];
    fieldOptions.value = [];
    previewError.value = '';
    previewLoading.value = false;
    if (!draft.formId || draft.formType !== 10) return;
    previewLoading.value = true;
    try {
      const form = await getForm(draft.formId);
      if (request !== previewRequest) return;
      previewRules.value = (form.fields || []).map((field) => formCreate.parseJson(field));
      previewOption.value = { ...formCreate.parseJson(form.conf || '{}'), submitBtn: false, resetBtn: false, disabled: true };
      function collect(rules: any[]) { for (const rule of rules) { if (rule.field) fieldOptions.value.push({ label: rule.title || rule.field, value: rule.field }); if (Array.isArray(rule.children)) collect(rule.children); } }
      collect(previewRules.value);
    } catch { if (request === previewRequest) previewError.value = '表单预览加载失败，请重新选择表单'; }
    finally { if (request === previewRequest) previewLoading.value = false; }
  }, { immediate: true });

  function validate(index: number) {
    let error = '';
    if (index === 0) {
      if (!draft.key?.trim() || !draft.name?.trim()) error = '请填写流程标识和流程名称';
      else if (!/^[a-zA-Z][a-zA-Z0-9_]*$/.test(draft.key)) error = '流程标识须以字母开头，只能包含字母、数字和下划线';
      else if (!draft.category) error = '请选择流程分类';
      else if (!draft.managerUserIds?.length) error = '请至少选择一名流程管理员';
      else if (startScope.value === 'users' && !draft.startUserIds?.length) error = '请选择可以发起流程的人员';
      else if (startScope.value === 'depts' && !draft.startDeptIds?.length) error = '请选择可以发起流程的部门';
    } else if (index === 1) {
      if (draft.formType === 10 && !draft.formId) error = '请选择流程表单';
      if (draft.formType === 20 && (!draft.formCustomCreatePath?.trim() || !draft.formCustomViewPath?.trim())) error = '请填写业务表单的提交和查看页面路径';
    } else if (index === 2 && draft.type === 20) {
      error = validateSimpleModel(draft.simpleModel);
    } else if (index === 3) {
      if (draft.titleSetting.enable && !draft.titleSetting.title?.trim()) error = '请填写自定义标题';
      else if (draft.summarySetting.enable && draft.formType === 10 && !draft.summarySetting.summary?.length) error = '请选择摘要字段';
      else if (draft.printTemplateSetting.enable && !draft.printTemplateSetting.template?.trim()) error = '请填写打印模板';
      else if (notifications.some(({ key }) => draft[key] && !/^https?:\/\/\S+$/i.test(draft[key].url || ''))) error = '请填写有效的 HTTP 通知地址';
    }
    if (error) { step.value = index; createMessage.warning(error); return false; }
    return true;
  }

  function validateSimpleModel(model: any) {
    let error = '';
    const visit = (node: any) => {
      if (!node || error) return;
      if ([11, 13].includes(Number(node.type)) && Number(node.approveType || 1) === 1) {
        const strategy = Number(node.candidateStrategy);
        const strategyError = validateCandidateStrategy(node, strategy);
        if (strategyError) {
          error = strategyError;
          return;
        }
        const requiresParam = [10, 20, 21, 22, 23, 30, 40, 50, 51, 60].includes(strategy);
        if (requiresParam && !String(node.candidateParam || '').trim()) {
          error = `节点“${node.name || '未命名'}”尚未配置审批人策略参数`;
          return;
        }
        if ([37, 38].includes(strategy) && (!Number(node.candidateParam) || Number(node.candidateParam) < 1)) {
          error = `节点“${node.name || '未命名'}”的发起人部门层级无效`;
          return;
        }
        if (Number(node.approveMethod) === 2 && (!Number(node.approveRatio) || Number(node.approveRatio) < 1 || Number(node.approveRatio) > 100)) {
          error = `节点“${node.name || '未命名'}”的会签通过比例须在 1%～100% 之间`;
          return;
        }
        if (Number(node.assignEmptyHandler?.type) === 3 && !node.assignEmptyHandler?.userIds?.length) {
          error = `节点“${node.name || '未命名'}”的审批人为空处理缺少指定成员`;
          return;
        }
        if (Number(node.rejectHandler?.type) === 2 && !String(node.rejectHandler?.returnNodeId || '').trim()) {
          error = `节点“${node.name || '未命名'}”尚未选择拒绝后的退回节点`;
          return;
        }
        error = validateApprovalSettings(node);
        if (error) return;
      }
      if (node.type === 15 && node.triggerSetting) {
        const setting = node.triggerSetting;
        if ([1, 2].includes(Number(setting.type))) {
          if (!/^https?:\/\/\S+$/i.test(String(setting.httpRequestSetting?.url || '').trim())) {
            error = `触发器“${node.name || '未命名'}”的请求地址无效`;
            return;
          }
        } else {
          const formSettings = Array.isArray(setting.formSettings) ? setting.formSettings : [];
          if (!formSettings.length) {
            error = `触发器“${node.name || '未命名'}”至少需要一条表单设置`;
            return;
          }
          for (let index = 0; index < formSettings.length; index++) {
            const formSetting = formSettings[index] || {};
            if (Number(formSetting.conditionType) === 1 && !String(formSetting.conditionExpression || '').trim()) {
              error = `触发器“${node.name || '未命名'}”第 ${index + 1} 条条件表达式不能为空`;
              return;
            }
            if (Number(formSetting.conditionType) === 2 && !validConditionGroups(formSetting.conditionGroups)) {
              error = `触发器“${node.name || '未命名'}”第 ${index + 1} 条条件组规则不完整`;
              return;
            }
            if (Number(setting.type) === 10) {
              const fields = formSetting.updateFormFields;
              if (!fields || typeof fields !== 'object' || Array.isArray(fields) || !Object.keys(fields).length
                || Object.entries(fields).some(([key, value]) => !String(key).trim() || !String(value ?? '').trim())) {
                error = `触发器“${node.name || '未命名'}”第 ${index + 1} 条修改字段配置不完整`;
                return;
              }
            } else if (!Array.isArray(formSetting.deleteFields) || !formSetting.deleteFields.length) {
              error = `触发器“${node.name || '未命名'}”第 ${index + 1} 条删除字段不能为空`;
              return;
            }
          }
        }
      }
      visit(node.childNode);
      (node.conditionNodes || []).forEach(visit);
    };
    visit(model);
    return error;
  }

  function validateCandidateStrategy(node: any, strategy: number) {
    const nodeName = node.name || '未命名';
    const raw = String(node.candidateParam || '').trim();
    const simpleStrategies = [32, 33, 34, 35, 36];
    if (simpleStrategies.includes(strategy) || Number.isNaN(strategy)) return '';
    const values = raw.split(',').map((item) => item.trim()).filter(Boolean);
    if ([10, 20, 21, 22, 30].includes(strategy) && !values.length) {
      return `节点“${nodeName}”尚未选择候选人参数`;
    }
    if (strategy === 40 && values.some((value) => !/^\d+$/.test(value))) {
      return `节点“${nodeName}”的用户组编号必须为数字`;
    }
    if ([23, 51].includes(strategy)) {
      const parts = raw.split('|');
      const base = parts[0]?.split(',').map((item) => item.trim()).filter(Boolean) || [];
      const level = parts[1]?.trim();
      if (!base.length || !level || !/^\d+$/.test(level) || Number(level) < 1) {
        return `节点“${nodeName}”的候选参数应为“字段或部门|正整数层级”`;
      }
      if (strategy === 51 && base.length !== 1) {
        return `节点“${nodeName}”的表单部门负责人策略只能选择一个部门字段`;
      }
      if (strategy === 51 && draft.formType === 10 && fieldOptions.value.length
        && !fieldOptions.value.some((field) => field.value === base[0])) {
        return `节点“${nodeName}”的表单部门字段不存在`;
      }
    }
    if ([37, 38].includes(strategy) && (!/^\d+$/.test(raw) || Number(raw) < 1)) {
      return `节点“${nodeName}”的发起人部门层级必须为正整数`;
    }
    if (strategy === 50) {
      if (!/^[\w.-]+$/.test(raw)) return `节点“${nodeName}”的表单用户字段名称无效`;
      if (draft.formType === 10 && fieldOptions.value.length
        && !fieldOptions.value.some((field) => field.value === raw)) {
        return `节点“${nodeName}”的表单用户字段不存在`;
      }
    }
    if (strategy === 60 && !raw) {
      return `节点“${nodeName}”的流程表达式不能为空`;
    }
    // 芋道的表达式策略由 Flowable ExpressionManager 解析，不限定为单一的
    // `${...}` 形式；例如 `#{...}`、已注册的表达式函数或组合表达式也应能保存。
    // 这里只校验非空，具体语法交给发布/运行时表达式引擎处理。
    return '';
  }

  function validConditionGroups(groups: any) {
    if (!groups || !Array.isArray(groups.conditions) || !groups.conditions.length) return false;
    return groups.conditions.every((condition: any) => Array.isArray(condition?.rules) && condition.rules.length
      && condition.rules.every((rule: any) => String(rule.leftSide || '').trim()
        && String(rule.opCode || '').trim() && String(rule.rightSide ?? '').trim()));
  }

  function validateApprovalSettings(node: any) {
    const nodeName = node.name || '未命名';
    const permissions = parseJsonArray(node.fieldsPermission, `节点“${nodeName}”的字段权限 JSON 格式不正确`);
    if (permissions.error) return permissions.error;
    if (permissions.value.some((item: any) => !String(item?.field || item?.name || item?.fieldName || '').trim()
      || !['READ', 'WRITE', 'NONE', 'READONLY', 'EDITABLE', 'HIDE'].includes(String(item?.permission || item?.auth || item?.type || '').toUpperCase()))) {
      return `节点“${nodeName}”的字段权限必须包含字段名和 READ/WRITE/NONE 权限`;
    }

    const enabledButtons = Array.isArray(node.enabledButtons) ? node.enabledButtons : [];
    if (enabledButtons.some((id: any) => ![1, 2, 3, 4, 5, 6, 7, 8].includes(Number(id)))) {
      return `节点“${nodeName}”包含无效的操作按钮编号`;
    }
    if (Number(node.approveType || 1) === 1 && !enabledButtons.some((id: any) => [1, 2].includes(Number(id)))) {
      return `节点“${nodeName}”至少需要启用“通过”或“拒绝”按钮`;
    }
    if (node.buttonsSetting != null && !Array.isArray(node.buttonsSetting)) {
      return `节点“${nodeName}”的操作按钮配置格式不正确`;
    }
    if (Array.isArray(node.buttonsSetting) && node.buttonsSetting.some((item: any) => ![1, 2, 3, 4, 5, 6, 7, 8].includes(Number(item?.id)))) {
      return `节点“${nodeName}”包含无效的操作按钮配置`;
    }

    for (const [key, label] of [['taskCreateListener', '创建任务'], ['taskAssignListener', '指派任务'], ['taskCompleteListener', '完成任务']] as const) {
      const setting = node[key];
      if (setting?.enable && !/^https?:\/\/\S+$/i.test(String(setting.path || '').trim())) {
        return `节点“${nodeName}”的${label}监听器请求地址无效`;
      }
    }
    for (const [key, label] of [['taskListeners', '任务'], ['executionListeners', '执行']] as const) {
      const listeners = parseJsonArray(node[key], `节点“${nodeName}”的${label}监听器 JSON 格式不正确`);
      if (listeners.error) return listeners.error;
      if (listeners.value.some((item: any) => !String(item?.eventName || item?.event || '').trim()
        || !String(item?.implementation || item?.value || item?.delegateExpression || item?.className || '').trim())) {
        return `节点“${nodeName}”的${label}监听器必须包含事件和实现`;
      }
    }
    return '';
  }

  function parseJsonArray(raw: any, message: string) {
    if (raw == null || raw === '' || (Array.isArray(raw) && !raw.length)) return { value: [] as any[], error: '' };
    if (Array.isArray(raw)) return { value: raw, error: '' };
    if (typeof raw !== 'string') return { value: [] as any[], error: message };
    try {
      const value = JSON.parse(raw);
      return Array.isArray(value) ? { value, error: '' } : { value: [] as any[], error: message };
    } catch {
      return { value: [] as any[], error: message };
    }
  }
  function next() { if (validate(step.value)) step.value++; }
  function close() {
    if (saving.value) return;
    if (JSON.stringify(draft) === initial) emit('close');
    else createConfirm({ title: '退出流程编辑', content: '尚未保存的修改将丢失，确定退出？', onOk: () => emit('close') });
  }
  async function save() {
    if (saving.value || ![0, 1, 2, 3].every(validate)) return;
    saving.value = true;
    try {
      const data = JSON.parse(JSON.stringify(draft));
      if (startScope.value !== 'mixed') { data.startUserIds = startScope.value === 'users' ? data.startUserIds : []; data.startDeptIds = startScope.value === 'depts' ? data.startDeptIds : []; }
      if (data.formType === 10) { data.formCustomCreatePath = ''; data.formCustomViewPath = ''; } else { data.formId = null; }
      if (data.id) await updateModel(data); else draft.id = await createModel(data);
      createMessage.success('流程模型已保存');
      emit('saved');
    } finally { saving.value = false; }
  }
</script>

<style lang="less">
  .bpm-model-wizard {
    .ant-modal { top: 0; max-width: 100vw; margin: 0; padding: 0; }
    .ant-modal-content { padding: 0; border-radius: 0; }
    .ant-modal-body { padding: 0; }
  }
</style>
<style scoped lang="less">
  .wizard-shell { display: flex; height: 100vh; flex-direction: column; background: #fff; }
  .wizard-header { display: flex; min-height: 68px; align-items: center; justify-content: space-between; gap: 24px; padding: 0 24px; border-bottom: 1px solid #e4e7ed; }
  .wizard-name { width: 240px; overflow: hidden; font-size: 18px; text-overflow: ellipsis; white-space: nowrap; }
  .wizard-steps { display: flex; gap: 40px; }
  .wizard-steps button { display: flex; height: 68px; align-items: center; gap: 10px; border: 0; border-bottom: 2px solid transparent; background: none; color: #707985; font-size: 19px; font-weight: 600; cursor: pointer; white-space: nowrap; }
  .wizard-steps span { display: grid; width: 32px; height: 32px; place-items: center; border: 1px solid #cbd2dc; border-radius: 50%; font-weight: 400; }
  .wizard-steps .active { border-color: #2f80ff; color: #2f80ff; }
  .wizard-steps .active span { border-color: #2f80ff; background: #2f80ff; color: white; }
  .wizard-body { flex: 1; overflow: auto; padding: 36px 28px; }
  .wizard-body.design-body { padding: 24px 16px; background: #f8f9fb; }
  .wizard-form { width: 100%; max-width: 1200px; margin: 0 auto; }
  .wizard-form :deep(.ant-form-item) { margin-bottom: 30px; }
  .wizard-form :deep(.ant-form-item-row) { display: flex; flex-wrap: nowrap; }
  .wizard-form :deep(.ant-form-item-label) { padding-right: 12px; }
  .wizard-form :deep(.ant-form-item-control) { min-width: 0; }
  .wizard-form :deep(.ant-input), .wizard-form :deep(.ant-select-selector) { border-radius: 8px; }
  .wizard-footer { display: flex; min-height: 64px; align-items: center; justify-content: flex-end; gap: 12px; padding: 12px 28px; border-top: 1px solid #e4e7ed; }
  .form-preview { max-width: 1200px; margin: 20px auto; padding: 24px; border: 1px solid #e0e5ec; border-radius: 6px; }
  .form-preview h3 { margin-bottom: 24px; padding-left: 12px; border-left: 5px solid #2f80ff; font-size: 18px; }
  .field-hint { color: #98a2b3; font-size: 13px; }
  .vertical-options { display: flex; flex-direction: column; align-items: flex-start; gap: 10px; padding: 8px 0; }
  .id-rule { display: grid; grid-template-columns: 24px 1fr 1fr 1fr 90px; align-items: center; gap: 8px; }
  .notification-settings, .scope-select { margin-top: 16px; }
  @media (max-width: 1000px) { .wizard-header { flex-wrap: wrap; gap: 0; } .wizard-name { width: auto; } .wizard-steps { order: 3; width: 100%; justify-content: center; gap: 20px; } .wizard-steps button { height: 54px; font-size: 15px; } .wizard-body { padding: 24px 16px; } }
</style>
