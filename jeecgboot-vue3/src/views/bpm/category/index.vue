<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div>
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <a-button v-auth="'bpm:category:create'" type="primary" @click="openEditor()">新增流程分类</a-button>
      </template>
      <template #action="{ record }"><TableAction :actions="actions(record)" /></template>
    </BasicTable>
    <a-modal v-model:open="visible" :title="form.id ? '编辑流程分类' : '新增流程分类'" :confirm-loading="saving" @ok="submit">
      <div class="bpm-modal-form-content">
        <a-form layout="vertical">
          <a-form-item label="分类名称" required><a-input v-model:value="form.name" :maxlength="64" /></a-form-item>
          <a-form-item label="分类标识" required><a-input v-model:value="form.code" :maxlength="64" /></a-form-item>
          <a-form-item label="分类描述"><a-textarea v-model:value="form.description" :rows="3" :maxlength="255" /></a-form-item>
          <a-form-item label="显示顺序" required><a-input-number v-model:value="form.sort" :min="0" style="width: 100%" /></a-form-item>
          <a-form-item label="状态" required>
            <a-radio-group v-model:value="form.status"> <a-radio :value="0">开启</a-radio><a-radio :value="1">关闭</a-radio> </a-radio-group>
          </a-form-item>
        </a-form>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" name="bpm-category" setup>
  import { reactive, ref } from 'vue';
  import { ActionItem, BasicColumn, BasicTable, FormSchema, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BpmCategory, createCategory, deleteCategory, getCategoryPage, updateCategory } from '/@/api/bpm/category';

  const { createMessage } = useMessage();
  const columns: BasicColumn[] = [
    { title: '分类名称', dataIndex: 'name', align: 'left' },
    { title: '分类标识', dataIndex: 'code', width: 180, align: 'left' },
    { title: '分类描述', dataIndex: 'description', align: 'left' },
    { title: '显示顺序', dataIndex: 'sort', width: 100, align: 'center' },
    { title: '状态', dataIndex: 'status', width: 90, align: 'center', customRender: ({ text }) => (text === 0 ? '开启' : '关闭') },
    { title: '创建时间', dataIndex: 'createTime', width: 180 },
  ];
  const searchSchema: FormSchema[] = [
    { field: 'name', label: '分类名称', component: 'Input', colProps: { span: 6 } },
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
    designScope: 'bpm-category',
    tableProps: {
      title: '流程分类',
      api: getCategoryPage,
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
  const form = reactive<BpmCategory>({ name: '', code: '', description: '', status: 0, sort: 0 });

  function openEditor(record?: BpmCategory) {
    Object.assign(form, { id: undefined, name: '', code: '', description: '', status: 0, sort: 0 }, record || {});
    visible.value = true;
  }

  async function submit() {
    if (!form.name.trim() || !form.code.trim()) {
      createMessage.warning('请填写分类名称和分类标识');
      return;
    }
    saving.value = true;
    try {
      await (form.id ? updateCategory({ ...form }) : createCategory({ ...form }));
      createMessage.success('保存成功');
      visible.value = false;
      reload();
    } finally {
      saving.value = false;
    }
  }

  const actions = (record: BpmCategory): ActionItem[] => [
    { label: '编辑', auth: 'bpm:category:update', onClick: () => openEditor(record) },
    {
      label: '删除',
      auth: 'bpm:category:delete',
      color: 'error',
      popConfirm: { title: '确认删除该流程分类？', confirm: () => deleteCategory(record.id!).then(() => reload()) },
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
