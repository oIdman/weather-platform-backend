<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div>
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <a-button v-auth="'bpm:process-expression:create'" type="primary" @click="openEditor()">新增流程表达式</a-button>
      </template>
      <template #action="{ record }"><TableAction :actions="actions(record)" /></template>
    </BasicTable>
    <a-modal v-model:open="visible" :title="form.id ? '编辑流程表达式' : '新增流程表达式'" :confirm-loading="saving" width="720px" @ok="submit">
      <div class="bpm-modal-form-content">
        <a-form layout="vertical">
          <a-form-item label="表达式名称" required><a-input v-model:value="form.name" :maxlength="64" /></a-form-item>
          <a-form-item label="表达式" required>
            <a-textarea v-model:value="form.expression" :rows="6" :maxlength="1024" placeholder="例如：${flowableUserResolver.resolve(execution)}" />
          </a-form-item>
          <a-form-item label="状态" required>
            <a-radio-group v-model:value="form.status"><a-radio :value="0">开启</a-radio><a-radio :value="1">关闭</a-radio></a-radio-group>
          </a-form-item>
        </a-form>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" name="bpm-process-expression" setup>
  import { reactive, ref } from 'vue';
  import { ActionItem, BasicColumn, BasicTable, FormSchema, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    BpmProcessExpression,
    createProcessExpression,
    deleteProcessExpression,
    getProcessExpressionPage,
    updateProcessExpression,
  } from '/@/api/bpm/processExpression';

  const { createMessage } = useMessage();
  const columns: BasicColumn[] = [
    { title: '表达式名称', dataIndex: 'name', width: 220, align: 'left' },
    { title: '表达式', dataIndex: 'expression', align: 'left' },
    { title: '状态', dataIndex: 'status', width: 90, align: 'center', customRender: ({ text }) => (text === 0 ? '开启' : '关闭') },
    { title: '创建时间', dataIndex: 'createTime', width: 180 },
  ];
  const searchSchema: FormSchema[] = [
    { field: 'name', label: '表达式名称', component: 'Input', colProps: { span: 6 } },
    {
      field: 'status',
      label: '状态',
      component: 'Select',
      colProps: { span: 6 },
      componentProps: {
        options: [
          { label: '开启', value: 0 },
          { label: '关闭', value: 1 },
        ],
      },
    },
  ];
  const { tableContext } = useListPage({
    designScope: 'bpm-process-expression',
    tableProps: {
      title: '流程表达式',
      api: getProcessExpressionPage,
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
  const form = reactive<BpmProcessExpression>({ name: '', expression: '', status: 0 });

  function openEditor(record?: BpmProcessExpression) {
    Object.assign(form, { id: undefined, name: '', expression: '', status: 0 }, record || {});
    visible.value = true;
  }

  async function submit() {
    if (!form.name.trim() || !form.expression.trim()) {
      createMessage.warning('请填写表达式名称和表达式');
      return;
    }
    saving.value = true;
    try {
      await (form.id ? updateProcessExpression({ ...form }) : createProcessExpression({ ...form }));
      createMessage.success('保存成功');
      visible.value = false;
      reload();
    } finally {
      saving.value = false;
    }
  }

  const actions = (record: BpmProcessExpression): ActionItem[] => [
    { label: '编辑', auth: 'bpm:process-expression:update', onClick: () => openEditor(record) },
    {
      label: '删除',
      auth: 'bpm:process-expression:delete',
      color: 'error',
      popConfirm: { title: '确认删除该流程表达式？', confirm: () => deleteProcessExpression(record.id!).then(() => reload()) },
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
