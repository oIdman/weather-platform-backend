<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="process-create-page">
    <a-card v-if="!selectedDefinition" title="全部流程" :loading="loading" :bordered="false">
      <template #extra>
        <a-input-search v-model:value="keyword" allow-clear placeholder="请输入流程名称检索" style="width: 320px" @search="loadDefinitions" />
      </template>
      <a-tabs v-if="availableGroups.length" v-model:active-key="activeCategory" tab-position="left">
        <a-tab-pane v-for="group in availableGroups" :key="group.code" :tab="group.name">
          <a-row :gutter="[16, 16]">
            <a-col v-for="definition in group.definitions" :key="definition.id" :xs="24" :sm="12" :lg="8" :xl="6">
              <a-card hoverable class="definition-card" @click="selectDefinition(definition)">
                <div class="definition-content">
                  <a-avatar v-if="definition.icon" :src="definition.icon" :size="48" shape="square" />
                  <div v-else class="definition-icon">{{ definition.name?.slice(0, 2) }}</div>
                  <div class="definition-info">
                    <a-tooltip :title="definition.name"
                      ><div class="definition-name">{{ definition.name }}</div></a-tooltip
                    >
                    <div class="definition-meta">版本 {{ definition.version }} · {{ group.name }}</div>
                  </div>
                </div>
              </a-card>
            </a-col>
          </a-row>
        </a-tab-pane>
      </a-tabs>
      <a-empty v-else :description="keyword ? '没有找到搜索结果' : '暂无可发起的流程'" class="empty-block" />
    </a-card>

    <a-card v-else :title="`流程表单 - ${selectedDefinition.name}`" :bordered="false" class="process-form-card">
      <template #extra><a-button @click="cancelSelection">← 返回</a-button></template>

      <a-tabs v-model:active-key="activeTab" class="process-form-tabs">
        <a-tab-pane key="form" tab="表单填写">
          <a-row :gutter="[48, 16]" class="form-layout">
            <a-col :xs="24" :md="17" :xl="18" class="form-column">
              <div v-if="formRules.length" class="dynamic-form">
                <FormCreate v-model="formVariables" v-model:api="formApi" :option="formOption" :rule="formRules" />
              </div>
              <a-empty v-else description="该流程未配置可填写的流程表单" />
            </a-col>

            <a-col :xs="24" :md="7" :xl="6" class="approval-column">
              <a-spin :spinning="previewLoading">
                <a-steps v-if="activityNodes.length" direction="vertical" size="small" :current="-1" class="approval-preview">
                  <a-step v-for="node in activityNodes" :key="`preview-${node.id}`" :title="node.name" :description="candidateDescription(node)" />
                </a-steps>
                <a-empty v-else description="暂无审批路径" :image-style="{ height: '48px' }" />

                <template v-if="startUserSelectTasks.length">
                  <a-divider orientation="left">选择审批人</a-divider>
                  <a-form layout="vertical">
                    <a-form-item v-for="node in startUserSelectTasks" :key="node.id" :label="node.name" required>
                      <JSelectUser
                        v-model:value="startUserSelectAssignees[node.id]"
                        row-key="id"
                        label-key="realname"
                        multiple="multiple"
                        :placeholder="`请选择${node.name}的审批人`"
                      />
                    </a-form-item>
                  </a-form>
                </template>
              </a-spin>
            </a-col>
          </a-row>
        </a-tab-pane>

        <a-tab-pane key="flow" tab="流程图">
          <div class="flow-preview">
            <template v-for="(node, index) in activityNodes" :key="`flow-${node.id}`">
              <div v-if="index" class="flow-arrow">↓</div>
              <div class="flow-node">
                <div class="flow-node-title">{{ node.name }}</div>
                <div v-if="candidateDescription(node)" class="flow-node-description">{{ candidateDescription(node) }}</div>
              </div>
            </template>
            <a-empty v-if="!activityNodes.length" description="暂无流程图数据" />
          </div>
        </a-tab-pane>
      </a-tabs>

      <template #actions>
        <a-space v-if="activeTab === 'form'">
          <a-button type="primary" :loading="starting" @click="submitStart">✓ 发起</a-button>
          <a-button @click="cancelSelection">× 取消</a-button>
        </a-space>
      </template>
    </a-card>
  </div>
</template>

<script lang="ts" name="bpm-process-instance-create" setup>
  import { computed, onActivated, onMounted, ref, watch } from 'vue';
  import formCreate, { type Rule } from '@form-create/ant-design-vue';
  import { useRouter } from 'vue-router';
  import { getCategorySimpleList, type BpmCategory } from '/@/api/bpm/category';
  import { getProcessDefinitionList, type BpmProcessDefinition } from '/@/api/bpm/definition';
  import JSelectUser from '/@/components/Form/src/jeecg/components/JSelectUser.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getApprovalDetail, startProcess, type WorkflowActivityNode } from '/@/views/workflow/workflow.api';

  interface DefinitionGroup {
    code: string;
    name: string;
    definitions: BpmProcessDefinition[];
  }

  const { createMessage } = useMessage();
  const router = useRouter();
  const loading = ref(false);
  const starting = ref(false);
  const previewLoading = ref(false);
  const keyword = ref('');
  const activeCategory = ref('');
  const activeTab = ref('form');
  const definitions = ref<BpmProcessDefinition[]>([]);
  const categories = ref<BpmCategory[]>([]);
  const selectedDefinition = ref<BpmProcessDefinition>();
  const formRules = ref<Rule[]>([]);
  const formOption = ref<Record<string, unknown>>({});
  const formVariables = ref<Record<string, unknown>>({});
  const formApi = ref<any>();
  const activityNodes = ref<WorkflowActivityNode[]>([]);
  const startUserSelectTasks = ref<WorkflowActivityNode[]>([]);
  const startUserSelectAssignees = ref<Record<string, string[]>>({});

  const availableGroups = computed<DefinitionGroup[]>(() => {
    const search = keyword.value.trim().toLowerCase();
    const activeDefinitions = definitions.value.filter(
      (item) => !item.suspended && item.visible !== false && (!search || item.name?.toLowerCase().includes(search))
    );
    const groups = categories.value
      .filter((category) => category.status === 0)
      .map((category) => ({
        code: category.code,
        name: category.name,
        definitions: activeDefinitions.filter((definition) => definition.category === category.code),
      }))
      .filter((group) => group.definitions.length);
    const knownCodes = new Set(categories.value.map((category) => category.code));
    const uncategorized = activeDefinitions.filter((definition) => !definition.category || !knownCodes.has(definition.category));
    if (uncategorized.length) groups.push({ code: '_uncategorized', name: '其它流程', definitions: uncategorized });
    return groups;
  });

  watch(
    availableGroups,
    (groups) => {
      if (!groups.some((group) => group.code === activeCategory.value)) activeCategory.value = groups[0]?.code || '';
    },
    { immediate: true }
  );

  async function loadDefinitions() {
    loading.value = true;
    try {
      const [definitionList, categoryList] = await Promise.all([getProcessDefinitionList({ suspensionState: 1 }), getCategorySimpleList()]);
      definitions.value = definitionList;
      categories.value = categoryList;
    } finally {
      loading.value = false;
    }
  }

  async function selectDefinition(definition: BpmProcessDefinition) {
    if (definition.formType === 20 && definition.formCustomCreatePath) {
      router.push({ path: definition.formCustomCreatePath, query: { processDefinitionId: definition.id, processDefinitionKey: definition.key } });
      return;
    }
    selectedDefinition.value = definition;
    activeTab.value = 'form';
    formVariables.value = {};
    const option = definition.formConf ? formCreate.parseJson(definition.formConf) : {};
    formOption.value = { ...option, submitBtn: false, resetBtn: false };
    formRules.value = (definition.formFields || []).map((item) => formCreate.parseJson(item));
    activityNodes.value = [];
    startUserSelectTasks.value = [];
    startUserSelectAssignees.value = {};
    await loadApprovalPreview({});
  }

  function cancelSelection() {
    selectedDefinition.value = undefined;
    activeTab.value = 'form';
    formRules.value = [];
    formVariables.value = {};
    activityNodes.value = [];
    startUserSelectTasks.value = [];
    startUserSelectAssignees.value = {};
  }

  async function loadApprovalPreview(processVariables: Record<string, unknown>) {
    if (!selectedDefinition.value) return;
    previewLoading.value = true;
    try {
      const detail = await getApprovalDetail({
        processDefinitionId: selectedDefinition.value.id,
        activityId: 'StartUserNode',
        processVariablesStr: JSON.stringify(processVariables),
      });
      activityNodes.value = detail.activityNodes || [];
      startUserSelectTasks.value = activityNodes.value.filter((node) => node.candidateStrategy === 35);
      const previous = startUserSelectAssignees.value;
      startUserSelectAssignees.value = Object.fromEntries(startUserSelectTasks.value.map((node) => [node.id, previous[node.id] || []]));
    } finally {
      previewLoading.value = false;
    }
  }

  function candidateDescription(node: WorkflowActivityNode) {
    if (node.nodeType === 10) return '当前登录用户';
    if (node.nodeType === 1) return '';
    if (node.status === -2) return '该节点满足跳过条件，将自动跳过';
    if (node.candidateStrategy === 35) return '审批人由发起人选择';
    const names = (node.candidateUsers || []).map((user) => user.nickname || user.username).filter(Boolean);
    return names.length ? `预计审批人：${names.join('、')}` : '审批人将在流程运行时确定';
  }

  async function submitStart() {
    if (!selectedDefinition.value) return;
    if (formApi.value) {
      try {
        await formApi.value.validate();
      } catch {
        createMessage.warning('请完整填写申请表单');
        return;
      }
    }
    const variables = { ...formVariables.value };
    await loadApprovalPreview(variables);
    for (const node of startUserSelectTasks.value) {
      if (!startUserSelectAssignees.value[node.id]?.length) {
        createMessage.warning(`请选择${node.name}的审批人`);
        return;
      }
    }
    starting.value = true;
    try {
      await startProcess({
        processDefinitionId: selectedDefinition.value.id,
        variables,
        startUserSelectAssignees: startUserSelectAssignees.value,
      });
      createMessage.success('流程发起成功');
      await router.push('/bpm/task/my');
    } finally {
      starting.value = false;
    }
  }

  onMounted(loadDefinitions);
  onActivated(loadDefinitions);
</script>

<style lang="less" scoped>
  .process-create-page {
    padding: 16px;

    .definition-card {
      cursor: pointer;
    }

    .definition-content {
      display: flex;
      align-items: center;
      min-width: 0;
    }

    .definition-icon {
      display: flex;
      width: 48px;
      height: 48px;
      flex: none;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      background: @primary-color;
      color: #fff;
      font-size: 13px;
    }

    .definition-info {
      min-width: 0;
      margin-left: 12px;
    }

    .definition-name {
      overflow: hidden;
      color: @text-color;
      font-size: 15px;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .definition-meta {
      margin-top: 6px;
      color: @text-color-secondary;
      font-size: 12px;
    }

    .empty-block {
      padding: 96px 0;
    }
  }

  .process-form-card {
    min-height: calc(100vh - 150px);
  }

  .process-form-tabs {
    min-height: calc(100vh - 270px);
  }

  .form-layout {
    padding: 20px 8px;
  }

  .form-column {
    min-height: 420px;
  }

  .approval-column {
    padding-top: 8px;
  }

  .approval-preview {
    padding: 0 12px;
  }

  .dynamic-form {
    width: 100%;
    padding: 4px 12px;
  }

  .flow-preview {
    display: flex;
    min-height: 440px;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 32px;
  }

  .flow-node {
    width: 240px;
    padding: 14px 20px;
    border: 1px solid fade(@primary-color, 35%);
    border-radius: 8px;
    background: fade(@primary-color, 7%);
    text-align: center;
  }

  .flow-node-title {
    color: @text-color;
    font-weight: 600;
  }

  .flow-node-description {
    margin-top: 4px;
    color: @text-color-secondary;
    font-size: 12px;
  }

  .flow-arrow {
    height: 34px;
    color: @primary-color;
    font-size: 22px;
    line-height: 34px;
  }
</style>
