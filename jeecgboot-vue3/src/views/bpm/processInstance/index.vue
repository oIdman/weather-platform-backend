<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div>
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <a-button v-auth="'bpm:process-instance:create'" type="primary" @click="goCreate">发起流程</a-button>
      </template>
      <template #action="{ record }"><TableAction :actions="actions(record)" /></template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="bpm-process-instance-my" setup>
  import { useRouter } from 'vue-router';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { cancelProcess, getMyProcessPage, type WorkflowInstance } from '/@/views/workflow/workflow.api';
  import { processColumns, processSearch } from '/@/views/workflow/workflow.data';

  const router = useRouter();
  const { createMessage } = useMessage();
  const { tableContext } = useListPage({
    designScope: 'bpm-process-instance-my',
    tableProps: {
      title: '我的流程',
      api: getMyProcessPage,
      columns: processColumns,
      formConfig: { schemas: processSearch },
      rowKey: 'id',
      useSearchForm: true,
      actionColumn: { width: 210 },
    },
  });
  const [registerTable, { reload }] = tableContext;

  function goCreate() {
    router.push('/bpm/task/create');
  }

  function actions(record: WorkflowInstance) {
    return [
      {
        label: '详情',
        onClick: () => router.push({ path: '/bpm/process-instance/detail', query: { id: record.id } }),
      },
      {
        label: '重新发起',
        auth: 'bpm:process-instance:create',
        ifShow: record.status !== 'RUNNING',
        onClick: goCreate,
      },
      {
        label: '取消',
        auth: 'bpm:process-instance:cancel',
        color: 'error',
        ifShow: record.status === 'RUNNING',
        popConfirm: {
          title: '确认取消该流程？',
          confirm: async () => {
            await cancelProcess({ id: record.id, reason: '申请人取消' });
            createMessage.success('流程已取消');
            reload();
          },
        },
      },
    ];
  }
</script>
