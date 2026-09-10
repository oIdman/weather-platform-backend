<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <BasicTable @register="registerTable">
    <template #action="{ record }"><TableAction :actions="actions(record)" /></template>
  </BasicTable>
</template>

<script lang="ts" name="bpm-task-manager" setup>
  import { useRouter } from 'vue-router';
  import { BasicColumn, BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { getManagerTaskPage } from '/@/views/workflow/workflow.api';
  import { managerTaskSearch } from '/@/views/workflow/workflow.data';

  const router = useRouter();
  const columns: BasicColumn[] = [
    { title: '任务名称', dataIndex: 'name', align: 'left' },
    { title: '流程实例', dataIndex: 'processInstanceName', align: 'left' },
    { title: '业务标识', dataIndex: 'businessKey', width: 180, align: 'left' },
    { title: '发起人', dataIndex: 'startUserName', width: 180, align: 'left' },
    {
      title: '处理人',
      dataIndex: 'assignee',
      width: 180,
      align: 'left',
      customRender: ({ text, record }) => record.assigneeName || record.ownerName || text || '-',
    },
    { title: '状态', dataIndex: 'status', width: 100, align: 'center' },
    { title: '创建时间', dataIndex: 'createTime', width: 180 },
    { title: '完成时间', dataIndex: 'endTime', width: 180 },
  ];
  const { tableContext } = useListPage({
    designScope: 'bpm-task-manager',
    tableProps: {
      title: '流程任务',
      api: getManagerTaskPage,
      columns,
      formConfig: { schemas: managerTaskSearch },
      rowKey: 'id',
      useSearchForm: true,
      actionColumn: { width: 90 },
    },
  });
  const [registerTable] = tableContext;
  const actions = (record) => [
    {
      label: '详情',
      onClick: () =>
        router.push({
          path: '/bpm/process-instance/detail',
          query: { id: record.processInstanceId, taskId: record.status === 'TODO' ? record.id : undefined },
        }),
    },
  ];
</script>
