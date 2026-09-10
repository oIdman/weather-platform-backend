<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <BasicTable @register="registerTable">
    <template #action="{ record }"><TableAction :actions="actions(record)" /></template>
  </BasicTable>
</template>

<script lang="ts" name="workflow-task-done" setup>
  import { useRouter } from 'vue-router';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getDonePage, withdrawTask } from '../workflow.api';
  import { doneTaskSearch, taskColumns } from '../workflow.data';

  const router = useRouter();
  const { createMessage } = useMessage();
  const { tableContext } = useListPage({
    designScope: 'workflow-task-done',
    tableProps: {
      title: '我的已办',
      api: getDonePage,
      columns: taskColumns,
      formConfig: { schemas: doneTaskSearch },
      rowKey: 'id',
      useSearchForm: true,
      actionColumn: { width: 140 },
    },
  });
  const [registerTable, { reload }] = tableContext;
  const actions = (record) => [
    { label: '详情', onClick: () => router.push({ path: '/bpm/process-instance/detail', query: { id: record.processInstanceId } }) },
    {
      label: '撤回',
      color: 'error',
      ifShow: record.processInstanceStatus === 'RUNNING',
      popConfirm: {
        title: '确认撤回到该任务？',
        confirm: async () => {
          await withdrawTask(record.id);
          createMessage.success('任务已撤回');
          reload();
        },
      },
    },
  ];
</script>
