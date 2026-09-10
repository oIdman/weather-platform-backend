<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div>
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <a-button v-auth="'bpm:user-group:create'" type="primary" @click="openEditor()">新增用户组</a-button>
      </template>
      <template #action="{ record }"><TableAction :actions="actions(record)" /></template>
    </BasicTable>
    <a-modal v-model:open="visible" :title="form.id ? '编辑用户组' : '新增用户组'" :confirm-loading="saving" width="720px" @ok="submit">
      <div class="bpm-modal-form-content">
        <a-form layout="vertical">
          <a-form-item label="组名" required><a-input v-model:value="form.name" :maxlength="64" /></a-form-item>
          <a-form-item label="成员" required>
            <JSelectUser v-model:value="form.userIds" row-key="id" label-key="realname" :multiple="true" />
          </a-form-item>
          <a-form-item label="描述"><a-textarea v-model:value="form.description" :rows="3" :maxlength="255" /></a-form-item>
          <a-form-item label="状态" required>
            <a-radio-group v-model:value="form.status"><a-radio :value="0">开启</a-radio><a-radio :value="1">关闭</a-radio></a-radio-group>
          </a-form-item>
        </a-form>
      </div>
    </a-modal>
  </div>
</template>

<script lang="ts" name="bpm-user-group" setup>
  import { reactive, ref } from 'vue';
  import { ActionItem, BasicColumn, BasicTable, FormSchema, TableAction } from '/@/components/Table';
  import JSelectUser from '/@/components/Form/src/jeecg/components/JSelectUser.vue';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { BpmUserGroup, createUserGroup, deleteUserGroup, getUserGroupPage, updateUserGroup } from '/@/api/bpm/userGroup';

  const { createMessage } = useMessage();
  const columns: BasicColumn[] = [
    { title: '组名', dataIndex: 'name', width: 220, align: 'left' },
    { title: '描述', dataIndex: 'description', align: 'left' },
    { title: '成员数量', dataIndex: 'userIds', width: 100, align: 'center', customRender: ({ text }) => (Array.isArray(text) ? text.length : 0) },
    { title: '状态', dataIndex: 'status', width: 90, align: 'center', customRender: ({ text }) => (text === 0 ? '开启' : '关闭') },
    { title: '创建时间', dataIndex: 'createTime', width: 180 },
  ];
  const searchSchema: FormSchema[] = [
    { field: 'name', label: '组名', component: 'Input', colProps: { span: 6 } },
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
    designScope: 'bpm-user-group',
    tableProps: {
      title: '用户组',
      api: getUserGroupPage,
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
  const form = reactive<BpmUserGroup>({ name: '', description: '', userIds: [], status: 0 });

  function openEditor(record?: BpmUserGroup) {
    Object.assign(form, { id: undefined, name: '', description: '', userIds: [], status: 0 }, record || {});
    form.userIds = [...(record?.userIds || [])];
    visible.value = true;
  }

  async function submit() {
    if (!form.name.trim() || !form.userIds.length) {
      createMessage.warning('请填写组名并至少选择一名成员');
      return;
    }
    saving.value = true;
    try {
      await (form.id ? updateUserGroup({ ...form }) : createUserGroup({ ...form }));
      createMessage.success('保存成功');
      visible.value = false;
      reload();
    } finally {
      saving.value = false;
    }
  }

  const actions = (record: BpmUserGroup): ActionItem[] => [
    { label: '编辑', auth: 'bpm:user-group:update', onClick: () => openEditor(record) },
    {
      label: '删除',
      auth: 'bpm:user-group:delete',
      color: 'error',
      popConfirm: { title: '确认删除该用户组？', confirm: () => deleteUserGroup(record.id!).then(() => reload()) },
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
