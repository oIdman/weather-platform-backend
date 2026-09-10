<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div>
    <BasicTable @register="registerTable">
      <template #tableTitle><a-button type="primary" @click="openStart">发起流程</a-button></template>
      <template #action="{ record }"><TableAction :actions="actions(record)" /></template>
    </BasicTable>
    <a-modal v-model:open="startVisible" title="发起流程" :confirm-loading="starting" @ok="submitStart">
      <a-form layout="vertical">
        <a-form-item label="流程定义" required>
          <a-select v-model:value="startForm.processDefinitionKey" :options="definitionOptions" />
        </a-form-item>
        <a-form-item label="实例名称" required><a-input v-model:value="startForm.name" /></a-form-item>
        <a-form-item label="流程变量 JSON">
          <a-textarea v-model:value="variablesJson" :rows="6" placeholder='例如 {"assignee":"用户ID"}' />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" name="workflow-process-my" setup>
  import { onMounted, reactive, ref } from 'vue';
  import { BasicTable, TableAction } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { cancelProcess, getDefinitionPage, getMyProcessPage, startProcess } from '../workflow.api';
  import { processColumns } from '../workflow.data';

  const { createMessage } = useMessage();
  const startVisible = ref(false);
  const starting = ref(false);
  const variablesJson = ref('{}');
  const definitionOptions = ref<{ label: string; value: string }[]>([]);
  const startForm = reactive({ processDefinitionKey: '', name: '' });
  const { tableContext } = useListPage({
    designScope: 'workflow-process-my',
    tableProps: { title: '我的申请', api: getMyProcessPage, columns: processColumns, rowKey: 'id', useSearchForm: false, actionColumn: { width: 100 } },
  });
  const [registerTable, { reload }] = tableContext;

  const actions = (record) =>
    record.status === 'RUNNING'
      ? [{ label: '取消', color: 'error', popConfirm: { title: '确认取消该流程？', confirm: () => cancelProcess({ id: record.id, reason: '申请人取消' }).then(() => reload()) } }]
      : [];

  async function loadDefinitions() {
    const page = await getDefinitionPage({ pageNo: 1, pageSize: 100 });
    definitionOptions.value = page.records.map((item) => ({ label: `${item.name}（v${item.version}）`, value: item.key }));
  }

  function openStart() {
    Object.assign(startForm, { processDefinitionKey: '', name: '' });
    variablesJson.value = '{}';
    startVisible.value = true;
  }

  async function submitStart() {
    if (!startForm.processDefinitionKey || !startForm.name) {
      createMessage.warning('请完整填写流程定义和实例名称');
      return;
    }
    let variables;
    try {
      variables = JSON.parse(variablesJson.value || '{}');
    } catch {
      createMessage.warning('流程变量必须是合法 JSON');
      return;
    }
    starting.value = true;
    try {
      await startProcess({ ...startForm, variables });
      createMessage.success('流程发起成功');
      startVisible.value = false;
      reload();
    } finally {
      starting.value = false;
    }
  }

  onMounted(loadDefinitions);
</script>
