<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <BasicTable @register="registerTable">
    <template #action="{ record }"><TableAction :actions="actions(record)" /></template>
  </BasicTable>
</template>

<script lang="ts" name="bpm-task-copy" setup>
  import { useRouter } from 'vue-router';
  import { BasicColumn, BasicTable, FormSchema, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { getProcessCopyPage } from '/@/views/workflow/workflow.api';

  const router = useRouter();
  const columns: BasicColumn[] = [
    { title: '流程实例', dataIndex: 'processInstanceName', align: 'left' },
    { title: '流程分类', dataIndex: 'categoryName', width: 140, align: 'left' },
    { title: '抄送节点', dataIndex: 'activityName', width: 160, align: 'left' },
    { title: '发起人', dataIndex: 'startUserName', width: 140, align: 'left' },
    { title: '抄送人', dataIndex: 'userName', width: 140, align: 'left' },
    { title: '抄送意见', dataIndex: 'reason', align: 'left' },
    { title: '流程发起时间', dataIndex: 'processInstanceStartTime', width: 180 },
    { title: '抄送时间', dataIndex: 'createTime', width: 180 },
  ];
  const searchSchema: FormSchema[] = [{ field: 'processInstanceName', label: '流程实例', component: 'Input', colProps: { span: 6 } }];
  const { tableContext } = useListPage({
    designScope: 'bpm-task-copy',
    tableProps: {
      title: '抄送我的',
      api: getProcessCopyPage,
      columns,
      formConfig: { schemas: searchSchema },
      rowKey: 'id',
      useSearchForm: true,
      actionColumn: { width: 90 },
    },
  });
  const [registerTable] = tableContext;
  const actions = (record) => [
    { label: '详情', onClick: () => router.push({ path: '/bpm/process-instance/detail', query: { id: record.processInstanceId } }) },
  ];
</script>
