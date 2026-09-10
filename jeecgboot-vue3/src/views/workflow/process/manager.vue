<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <BasicTable @register="registerTable">
    <template #action="{ record }"><TableAction :actions="actions(record)" /></template>
  </BasicTable>
</template>

<script lang="ts" name="workflow-process-manager" setup>
  import { useRouter } from 'vue-router';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { cancelProcess, deleteProcessInstance, getManagerProcessPage } from '../workflow.api';
  import { processColumns, processManagerSearch } from '../workflow.data';

  const router = useRouter();
  const { tableContext } = useListPage({
    designScope: 'workflow-process-manager',
    tableProps: {
      title: '流程实例',
      api: getManagerProcessPage,
      columns: processColumns,
      formConfig: { schemas: processManagerSearch },
      rowKey: 'id',
      useSearchForm: true,
      actionColumn: { width: 150 },
    },
  });
  const [registerTable, { reload }] = tableContext;
  const actions = (record) => [
    { label: '详情', onClick: () => router.push({ path: '/bpm/process-instance/detail', query: { id: record.id } }) },
    {
      label: '终止',
      color: 'error',
      ifShow: record.status === 'RUNNING',
      popConfirm: { title: '确认终止该流程？', confirm: () => cancelProcess({ id: record.id, reason: '管理员终止' }, true).then(() => reload()) },
    },
    {
      label: '删除',
      color: 'error',
      ifShow: record.status !== 'RUNNING',
      popConfirm: { title: '确认删除该已结束流程及其历史记录？', confirm: () => deleteProcessInstance(record.id).then(() => reload()) },
    },
  ];
</script>
