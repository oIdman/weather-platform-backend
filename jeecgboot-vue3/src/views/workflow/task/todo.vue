<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <BasicTable @register="registerTable">
    <template #action="{ record }"><TableAction :actions="actions(record)" /></template>
  </BasicTable>
</template>

<script lang="ts" name="workflow-task-todo" setup>
  import { useRouter } from 'vue-router';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { getTodoPage } from '../workflow.api';
  import { todoTaskColumns, todoTaskSearch } from '../workflow.data';

  const router = useRouter();
  const { tableContext } = useListPage({
    designScope: 'workflow-task-todo',
    tableProps: {
      title: '我的待办',
      api: getTodoPage,
      columns: todoTaskColumns,
      formConfig: { schemas: todoTaskSearch },
      rowKey: 'id',
      useSearchForm: true,
      actionColumn: { width: 90 },
    },
  });
  const [registerTable] = tableContext;
  const actions = (record) => [
    {
      label: '办理',
      onClick: () => router.push({ path: '/bpm/process-instance/detail', query: { id: record.processInstanceId, taskId: record.id } }),
    },
  ];
</script>
