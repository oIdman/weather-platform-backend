<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div>
    <BasicTable @register="registerTable">
      <template #tableTitle>
        <a-button type="primary" @click="openDeploy">部署 BPMN</a-button>
      </template>
    </BasicTable>
    <a-modal v-model:open="deployVisible" title="部署 BPMN 流程" :confirm-loading="deploying" width="760px" @ok="submitDeploy">
      <a-form layout="vertical">
        <a-form-item label="流程名称" required><a-input v-model:value="deployForm.name" /></a-form-item>
        <a-form-item label="资源文件名" required><a-input v-model:value="deployForm.resourceName" placeholder="例如 project-approval.bpmn20.xml" /></a-form-item>
        <a-form-item label="分类"><a-input v-model:value="deployForm.category" /></a-form-item>
        <a-form-item label="BPMN XML" required><a-textarea v-model:value="deployForm.bpmnXml" :rows="14" /></a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" name="workflow-definition" setup>
  import { reactive, ref } from 'vue';
  import { BasicTable } from '/@/components/Table';
  import { useListPage } from '/@/hooks/system/useListPage';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { definitionColumns, definitionSearch } from '../workflow.data';
  import { deployDefinition, getDefinitionPage } from '../workflow.api';

  const { createMessage } = useMessage();
  const deployVisible = ref(false);
  const deploying = ref(false);
  const deployForm = reactive({ name: '', resourceName: '', category: '', bpmnXml: '' });
  const { tableContext } = useListPage({
    designScope: 'workflow-definition',
    tableProps: {
      title: '流程定义',
      api: getDefinitionPage,
      columns: definitionColumns,
      formConfig: { schemas: definitionSearch },
      rowKey: 'id',
      useSearchForm: true,
    },
  });
  const [registerTable, { reload }] = tableContext;

  function openDeploy() {
    Object.assign(deployForm, { name: '', resourceName: '', category: '', bpmnXml: '' });
    deployVisible.value = true;
  }

  async function submitDeploy() {
    if (!deployForm.name || !deployForm.resourceName || !deployForm.bpmnXml) {
      createMessage.warning('请填写流程名称、资源文件名和 BPMN XML');
      return;
    }
    deploying.value = true;
    try {
      await deployDefinition({ ...deployForm });
      createMessage.success('流程部署成功');
      deployVisible.value = false;
      reload();
    } finally {
      deploying.value = false;
    }
  }
</script>
