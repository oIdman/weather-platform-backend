<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div>
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <a-button v-auth="'bpm:form:create'" type="primary" @click="openEditor()">新增流程表单</a-button>
      </template>
      <template #action="{ record }"><TableAction :actions="actions(record)" /></template>
    </BasicTable>

    <a-modal
      v-model:open="visible"
      :title="form.id ? '编辑流程表单' : '新增流程表单'"
      :confirm-loading="saving"
      width="96%"
      destroy-on-close
      @ok="submit"
    >
      <div class="form-meta">
        <div class="form-meta-item">
          <label class="form-meta-label"><span class="form-meta-required">*</span>表单名称</label>
          <a-input v-model:value="form.name" :maxlength="128" />
        </div>
        <div class="form-meta-item">
          <label class="form-meta-label"><span class="form-meta-required">*</span>状态</label>
          <a-radio-group v-model:value="form.status"><a-radio :value="0">开启</a-radio><a-radio :value="1">关闭</a-radio></a-radio-group>
        </div>
        <div class="form-meta-item">
          <label class="form-meta-label">备注</label>
          <a-input v-model:value="form.remark" :maxlength="255" />
        </div>
      </div>
      <FcDesigner ref="designerRef" height="66vh" :config="designerConfig" />
    </a-modal>

    <a-modal v-model:open="previewVisible" title="流程表单预览" width="760px" :footer="null" destroy-on-close>
      <FormCreate v-if="previewVisible" v-model="previewValue" :option="previewOption" :rule="previewRules" />
    </a-modal>
  </div>
</template>

<script lang="ts" name="bpm-form" setup>
  import { nextTick, reactive, ref } from 'vue';
  import formCreate, { type Rule } from '@form-create/ant-design-vue';
  import FcDesigner from '@form-create/antd-designer';
  import { ActionItem, BasicColumn, BasicTable, FormSchema, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BpmForm, createForm, deleteForm, getFormPage, updateForm } from '/@/api/bpm/form';

  const { createMessage } = useMessage();
  const columns: BasicColumn[] = [
    { title: '表单名称', dataIndex: 'name', align: 'left' },
    { title: '字段数量', dataIndex: 'fields', width: 100, align: 'center', customRender: ({ text }) => (Array.isArray(text) ? text.length : 0) },
    { title: '备注', dataIndex: 'remark', align: 'left' },
    { title: '状态', dataIndex: 'status', width: 90, align: 'center', customRender: ({ text }) => (text === 0 ? '开启' : '关闭') },
    { title: '创建时间', dataIndex: 'createTime', width: 180 },
  ];
  const searchSchema: FormSchema[] = [
    { field: 'name', label: '表单名称', component: 'Input', colProps: { span: 6 } },
    {
      field: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        options: [
          { label: '开启', value: 0 },
          { label: '关闭', value: 1 },
        ],
      },
      colProps: { span: 6 },
    },
  ];
  const { tableContext } = useListPage({
    designScope: 'bpm-form',
    tableProps: {
      title: '流程表单',
      api: getFormPage,
      columns,
      formConfig: { schemas: searchSchema },
      rowKey: 'id',
      useSearchForm: true,
      actionColumn: { width: 220 },
    },
  });
  const [registerTable, { reload }] = tableContext;
  const visible = ref(false);
  const previewVisible = ref(false);
  const saving = ref(false);
  const designerRef = ref<any>();
  const previewRules = ref<Rule[]>([]);
  const previewOption = ref<Record<string, unknown>>({});
  const previewValue = ref<Record<string, unknown>>({});
  const form = reactive<BpmForm>({ name: '', status: 0, conf: '{}', fields: [], remark: '' });
  const designerConfig = {
    showSaveBtn: false,
    showConfig: true,
    showFormConfig: true,
    showInputData: true,
    showDevice: true,
    formOptions: { form: { labelWidth: '110px' } },
  };

  function decodeRules(fields: string[]) {
    return (fields || []).map((item) => formCreate.parseJson(item));
  }

  async function openEditor(record?: BpmForm, copy = false) {
    Object.assign(form, { id: undefined, name: '', status: 0, conf: '{}', fields: [], remark: '' }, record || {});
    if (copy) {
      form.id = undefined;
      form.name = `${form.name}_copy`;
    }
    visible.value = true;
    await nextTick();
    designerRef.value?.setOption(formCreate.parseJson(form.conf || '{}'));
    designerRef.value?.setRule(decodeRules(form.fields || []));
  }

  function preview(record: BpmForm) {
    previewRules.value = decodeRules(record.fields || []);
    previewOption.value = formCreate.parseJson(record.conf || '{}');
    previewValue.value = {};
    previewVisible.value = true;
  }

  async function submit() {
    if (!form.name.trim()) {
      createMessage.warning('请填写表单名称');
      return;
    }
    const rules = designerRef.value?.getRule() || [];
    if (!rules.length) {
      createMessage.warning('请至少拖入一个表单字段');
      return;
    }
    saving.value = true;
    try {
      const data = {
        ...form,
        conf: formCreate.toJson(designerRef.value.getOption()),
        fields: rules.map((item) => formCreate.toJson(item)),
      };
      await (form.id ? updateForm(data) : createForm(data));
      createMessage.success('保存成功');
      visible.value = false;
      reload();
    } finally {
      saving.value = false;
    }
  }

  const actions = (record: BpmForm): ActionItem[] => [
    { label: '复制', auth: 'bpm:form:update', onClick: () => openEditor(record, true) },
    { label: '编辑', auth: 'bpm:form:update', onClick: () => openEditor(record) },
    { label: '预览', auth: 'bpm:form:query', onClick: () => preview(record) },
    {
      label: '删除',
      auth: 'bpm:form:delete',
      color: 'error',
      popConfirm: { title: '确认删除该流程表单？', confirm: () => deleteForm(record.id!).then(() => reload()) },
    },
  ];
</script>

<style scoped lang="less">
  .form-meta {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    column-gap: 24px;
    direction: ltr;
    margin-bottom: 14px;
    padding: 12px;
    border: 1px solid #f0f0f0;
    border-radius: 6px;
    background: #fafafa;

    .form-meta-item {
      display: flex;
      align-items: center;
      width: 100%;
      min-width: 0;
      direction: ltr;
    }

    .form-meta-label {
      flex: 0 0 auto;
      width: 76px;
      margin-right: 8px;
      color: rgba(0, 0, 0, 0.85);
      text-align: right;
      white-space: nowrap;
    }

    .form-meta-required {
      margin-right: 4px;
      color: #ff4d4f;
    }

    :deep(.ant-radio-group) {
      flex: 1 1 auto;
      min-width: 0;
    }

    :deep(.ant-input) {
      flex: 1 1 auto;
      width: 100%;
      min-width: 0;
    }
  }
</style>
