<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div>
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <a-button v-auth="'bpm:process-listener:create'" type="primary" @click="openEditor()">新增流程监听器</a-button>
      </template>
      <template #action="{ record }"><TableAction :actions="actions(record)" /></template>
    </BasicTable>
    <a-modal v-model:open="visible" :title="form.id ? '编辑流程监听器' : '新增流程监听器'" :confirm-loading="saving" width="720px" @ok="submit">
      <div class="bpm-modal-form-content">
        <a-form layout="vertical">
          <a-form-item label="监听器名称" required><a-input v-model:value="form.name" :maxlength="64" /></a-form-item>
          <a-row :gutter="24">
            <a-col :span="12"
              ><a-form-item label="监听类型" required><a-select v-model:value="form.type" :options="typeOptions" @change="resetEvent" /></a-form-item
            ></a-col>
            <a-col :span="12"
              ><a-form-item label="监听事件" required><a-select v-model:value="form.event" :options="eventOptions" /></a-form-item
            ></a-col>
          </a-row>
          <a-form-item label="值类型" required><a-select v-model:value="form.valueType" :options="valueTypeOptions" /></a-form-item>
          <a-form-item label="监听器值" required><a-textarea v-model:value="form.value" :rows="5" :maxlength="1024" /></a-form-item>
          <a-form-item label="状态" required>
            <a-radio-group v-model:value="form.status"><a-radio :value="0">开启</a-radio><a-radio :value="1">关闭</a-radio></a-radio-group>
          </a-form-item>
        </a-form>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" name="bpm-process-listener" setup>
  import { computed, reactive, ref } from 'vue';
  import { ActionItem, BasicColumn, BasicTable, FormSchema, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    BpmProcessListener,
    createProcessListener,
    deleteProcessListener,
    getProcessListenerPage,
    updateProcessListener,
  } from '/@/api/bpm/processListener';

  const { createMessage } = useMessage();
  const typeOptions = [
    { label: '执行监听器', value: 'execution' },
    { label: '任务监听器', value: 'task' },
  ];
  const executionEvents = [
    { label: '开始', value: 'start' },
    { label: '结束', value: 'end' },
    { label: '流转', value: 'take' },
  ];
  const taskEvents = ['create', 'assignment', 'complete', 'delete', 'update', 'timeout'].map((value) => ({ label: value, value }));
  const valueTypeOptions = [
    { label: 'Java 类', value: 'class' },
    { label: '委托表达式', value: 'delegateExpression' },
    { label: '表达式', value: 'expression' },
  ];
  const columns: BasicColumn[] = [
    { title: '监听器名称', dataIndex: 'name', width: 200, align: 'left' },
    { title: '监听类型', dataIndex: 'type', width: 130, align: 'center' },
    { title: '监听事件', dataIndex: 'event', width: 120, align: 'center' },
    { title: '值类型', dataIndex: 'valueType', width: 160, align: 'center' },
    { title: '监听器值', dataIndex: 'value', align: 'left' },
    { title: '状态', dataIndex: 'status', width: 90, align: 'center', customRender: ({ text }) => (text === 0 ? '开启' : '关闭') },
  ];
  const searchSchema: FormSchema[] = [
    { field: 'name', label: '监听器名称', component: 'Input', colProps: { span: 6 } },
    { field: 'type', label: '监听类型', component: 'Select', componentProps: { options: typeOptions }, colProps: { span: 6 } },
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
    designScope: 'bpm-process-listener',
    tableProps: {
      title: '流程监听器',
      api: getProcessListenerPage,
      columns,
      formConfig: { schemas: searchSchema },
      rowKey: 'id',
      useSearchForm: true,
      actionColumn: { width: 150 },
    },
  });
  const [registerTable, { reload }] = tableContext;
  const visible = ref(false);
  const saving = ref(false);
  const form = reactive<BpmProcessListener>({ name: '', type: 'execution', status: 0, event: 'start', valueType: 'class', value: '' });
  const eventOptions = computed(() => (form.type === 'task' ? taskEvents : executionEvents));

  function resetEvent() {
    form.event = eventOptions.value[0].value;
  }

  function openEditor(record?: BpmProcessListener) {
    Object.assign(form, { id: undefined, name: '', type: 'execution', status: 0, event: 'start', valueType: 'class', value: '' }, record || {});
    visible.value = true;
  }

  async function submit() {
    if (!form.name.trim() || !form.type || !form.event || !form.valueType || !form.value.trim()) {
      createMessage.warning('请完整填写监听器配置');
      return;
    }
    saving.value = true;
    try {
      await (form.id ? updateProcessListener({ ...form }) : createProcessListener({ ...form }));
      createMessage.success('保存成功');
      visible.value = false;
      reload();
    } finally {
      saving.value = false;
    }
  }

  const actions = (record: BpmProcessListener): ActionItem[] => [
    { label: '编辑', auth: 'bpm:process-listener:update', onClick: () => openEditor(record) },
    {
      label: '删除',
      auth: 'bpm:process-listener:delete',
      color: 'error',
      popConfirm: { title: '确认删除该流程监听器？', confirm: () => deleteProcessListener(record.id!).then(() => reload()) },
    },
  ];
</script>

<style scoped lang="less">
  .bpm-modal-form-content {
    padding: 8px 24px 16px;

    :deep(.ant-form-item) {
      margin-bottom: 20px;
    }
  }
</style>
