<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="bpm-model-page">
    <a-card class="model-page-card" :bordered="false">
      <template #title>流程模型</template>
      <template #extra>
        <a-space>
          <a-input-search v-model:value="keyword" allow-clear placeholder="搜索流程" class="model-search" @search="loadModels" />
          <a-button v-auth="'bpm:model:create'" type="primary" class="create-model-button" @click="openEditor()"><PlusOutlined />新建模型</a-button>
          <a-upload v-auth="'bpm:model:import'" accept="application/json,.json" :before-upload="handleImport" :show-upload-list="false">
            <a-button class="import-model-button"><UploadOutlined />导入</a-button>
          </a-upload>
          <a-button class="settings-button"><SettingOutlined /></a-button>
        </a-space>
      </template>

      <a-spin :spinning="loading">
        <a-empty v-if="!groups.length" description="暂无流程模型" />
        <section v-for="group in groups" :key="group.code" class="model-group-panel">
          <div class="model-group-header">
            <button class="group-title" type="button" @click="toggleGroup(group.code)">
              <span>{{ group.name }} <em>({{ group.models.length }})</em></span>
              <UpOutlined v-if="!isGroupCollapsed(group.code)" />
              <DownOutlined v-else />
            </button>
            <a-space class="group-actions">
              <template v-if="isModelSorting(group.code)">
                <a-button type="link" @click="cancelSorting"><CloseOutlined />取消</a-button>
                <a-button type="link" :loading="savingSort" @click="submitSorting"><CheckOutlined />保存排序</a-button>
              </template>
              <template v-else>
                <a-button type="link" @click="beginSorting(group)"><SortAscendingOutlined />排序</a-button>
                <a-button type="link" @click="openCategoryManager"><SettingOutlined />分类</a-button>
              </template>
            </a-space>
          </div>
          <a-table
            v-if="!isGroupCollapsed(group.code)"
            class="model-table"
            :columns="columns"
            :data-source="group.models"
            :pagination="false"
            row-key="id"
            :custom-row="(record) => rowSortProps(record, group.code)"
            size="middle"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'name'">
                <div class="model-name-cell">
                  <a-tooltip v-if="isModelSorting(group.code)" title="拖动排序">
                    <HolderOutlined class="row-drag-handle" />
                  </a-tooltip>
                  <a-avatar :src="record.icon ? getFileAccessHttpUrl(record.icon) : ''" shape="square" :size="38">{{ record.name.slice(0, 1) }}</a-avatar>
                  <div>
                    <a-typography-link @click="openEditor(record)">{{ record.name }}</a-typography-link>
                    <div class="model-key">{{ record.key }}</div>
                  </div>
                </div>
              </template>
              <template v-else-if="column.key === 'visible'">{{ record.visible ? '全部可见' : '仅指定人员可见' }}</template>
              <template v-else-if="column.key === 'type'">
                <a-tag :color="record.type === 20 ? 'green' : 'blue'">{{ record.type === 20 ? 'SIMPLE 设计器' : 'BPMN 设计器' }}</a-tag>
              </template>
              <template v-else-if="column.key === 'form'">
                <a-typography-link v-if="record.formName" @click="previewForm(record)">{{ record.formName }}</a-typography-link>
                <span v-else class="muted-text">—</span>
              </template>
              <template v-else-if="column.key === 'published'">
                <template v-if="record.processDefinition">
                  <span>{{ record.processDefinition.deploymentTime || '已发布' }}</span>
                  <a-tag class="version-tag">v{{ record.processDefinition.version }}</a-tag>
                </template>
                <span v-else class="muted-text">未发布</span>
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space class="row-actions" :size="12">
                  <a-button v-auth="'bpm:model:update'" type="link" @click="openEditor(record)">修改</a-button>
                  <a-button v-auth="'bpm:model:deploy'" type="link" @click="publish(record)">发布</a-button>
                  <a-dropdown :trigger="['click']">
                    <a-button type="link">更多<DownOutlined /></a-button>
                    <template #overlay>
                      <a-menu>
                        <a-menu-item v-auth="'bpm:model:update'" @click="openDesigner(record)">设计</a-menu-item>
                        <a-menu-item v-if="record.processDefinition" v-auth="'bpm:model:update'" @click="toggleState(record)">
                          {{ record.processDefinition.suspensionState === 1 ? '停用' : '启用' }}
                        </a-menu-item>
                        <a-menu-item v-auth="'bpm:model:export'" @click="downloadModel(record)">导出</a-menu-item>
                        <a-menu-item v-auth="'bpm:model:clean'" @click="confirmClean(record)">清理</a-menu-item>
                        <a-menu-item v-auth="'bpm:model:delete'" danger @click="confirmDelete(record)">删除</a-menu-item>
                      </a-menu>
                    </template>
                  </a-dropdown>
                </a-space>
              </template>
            </template>
          </a-table>
        </section>
      </a-spin>
    </a-card>

    <ModelWizard v-if="editorVisible" :model="form" :categories="categories" :forms="forms" :initial-step="editorStep" @close="editorVisible = false" @saved="editorVisible = false; loadModels()" />

    <a-modal v-model:open="designerVisible" :confirm-loading="savingDesign" :title="`设计流程：${designForm.name}`" width="96%" @ok="saveDesign">
      <a-alert class="mb-3" message="BPMN 图形设计器保存的是 Flowable 可直接部署的标准 XML；源码模式用于高级属性和故障排查。" show-icon type="info" />
      <a-tabs v-model:active-key="designTab" @change="syncDesignSource">
        <a-tab-pane v-if="designForm.type === 10" key="bpmn-visual" tab="图形设计">
          <BpmnDesigner v-model="designForm.bpmnXml" />
        </a-tab-pane>
        <a-tab-pane v-if="designForm.type === 10" key="bpmn-xml" tab="BPMN XML">
          <a-textarea v-model:value="designForm.bpmnXml" class="source-editor" spellcheck="false" />
        </a-tab-pane>
        <a-tab-pane v-if="designForm.type === 20" key="simple-visual" tab="图形设计">
          <SimpleProcessDesigner v-model="simpleModel" />
        </a-tab-pane>
        <a-tab-pane v-if="designForm.type === 20" key="simple-json" tab="JSON（高级）">
          <a-textarea v-model:value="simpleJson" class="source-editor" spellcheck="false" />
        </a-tab-pane>
      </a-tabs>
    </a-modal>

    <a-modal v-model:open="formPreviewVisible" title="流程表单预览" width="820px" :footer="null" destroy-on-close>
      <FormCreate v-if="formPreviewVisible" v-model="previewFormValue" :option="previewFormOption" :rule="previewFormRules" />
    </a-modal>

  </div>
</template>

<script lang="ts" name="bpm-model" setup>
  import type { UploadProps } from 'ant-design-vue';
  import formCreate from '@form-create/ant-design-vue';
  import { CheckOutlined, CloseOutlined, DownOutlined, HolderOutlined, PlusOutlined, SettingOutlined, SortAscendingOutlined, UpOutlined, UploadOutlined } from '@ant-design/icons-vue';
  import { computed, defineAsyncComponent, onActivated, onMounted, reactive, ref } from 'vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { router } from '/@/router';
  import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
  import BpmnDesigner from '/@/views/bpm/components/BpmnDesigner.vue';
  import SimpleProcessDesigner from '/@/views/bpm/components/SimpleProcessDesigner.vue';
  import { BpmCategory, getCategorySimpleList } from '/@/api/bpm/category';
  import { BpmForm, getForm, getFormSimpleList } from '/@/api/bpm/form';
  import {
    BpmModel,
    cleanModel,
    deleteModel,
    deployModel,
    exportModel,
    getModel,
    getModelList,
    importModel,
    updateModelBpmn,
    updateModelSortBatch,
    updateModelState,
    updateSimpleModel,
  } from '/@/api/bpm/model';

  interface ModelGroup {
    code: string;
    name: string;
    models: BpmModel[];
  }

  const { createConfirm, createMessage } = useMessage();
  const keyword = ref('');
  const loading = ref(false);
  const ModelWizard = defineAsyncComponent(() => import('./ModelWizard.vue'));
  const editorStep = ref(0);
  const savingDesign = ref(false);
  const models = ref<BpmModel[]>([]);
  const categories = ref<BpmCategory[]>([]);
  const forms = ref<BpmForm[]>([]);
  const editorVisible = ref(false);
  const designerVisible = ref(false);
  const formPreviewVisible = ref(false);
  const previewFormRules = ref<any[]>([]);
  const previewFormOption = ref<Record<string, unknown>>({});
  const previewFormValue = ref<Record<string, unknown>>({});
  const savingSort = ref(false);
  const sortingGroupCode = ref<string | null>(null);
  const draggedModelId = ref('');
  const designTab = ref('bpmn');
  const simpleJson = ref('{}');
  const simpleModel = ref<Record<string, any>>({});
  const collapsedGroups = ref<Record<string, boolean>>({});

  const columns = [
    { title: '流程名称', key: 'name', dataIndex: 'name', width: '22%' },
    { title: '可见范围', key: 'visible', dataIndex: 'visible', width: '15%' },
    { title: '流程类型', key: 'type', dataIndex: 'type', width: '16%' },
    { title: '表单信息', key: 'form', dataIndex: 'formName', width: '16%' },
    { title: '最后发布', key: 'published', dataIndex: 'processDefinition', width: '19%' },
    { title: '操作', key: 'action', dataIndex: 'action', width: '20%', align: 'right' },
  ];

  const emptyForm = (): BpmModel => ({
    id: undefined,
    key: '',
    name: '',
    category: undefined,
    categoryName: undefined,
    icon: '',
    description: '',
    type: 20,
    formType: 10,
    formId: undefined,
    formName: undefined,
    formCustomCreatePath: '',
    formCustomViewPath: '',
    visible: true,
    startUserIds: [],
    startDeptIds: [],
    managerUserIds: [],
    sort: undefined,
    allowCancelRunningProcess: true,
    allowWithdrawTask: false,
    processIdRule: { enable: false, prefix: '', infix: '', postfix: '', length: 5 },
    autoApprovalType: 0,
    titleSetting: { enable: false, title: '' },
    summarySetting: { enable: false, summary: [] },
    processBeforeTriggerSetting: null,
    processAfterTriggerSetting: null,
    taskBeforeTriggerSetting: null,
    taskAfterTriggerSetting: null,
    printTemplateSetting: { enable: false, template: '' },
    createTime: undefined,
    updateTime: undefined,
    bpmnXml: undefined,
    simpleModel: undefined,
    status: undefined,
    processDefinition: undefined,
  });
  const form = reactive<BpmModel>(emptyForm());
  const designForm = reactive<BpmModel>(emptyForm());

  const groups = computed<ModelGroup[]>(() => {
    const known = categories.value.map((category) => ({
      code: category.code,
      name: category.name,
      models: models.value.filter((model) => model.category === category.code),
    }));
    const knownCodes = new Set(categories.value.map((item) => item.code));
    const uncategorized = models.value.filter((model) => !model.category || !knownCodes.has(model.category));
    if (uncategorized.length) known.push({ code: '__other__', name: '未分类', models: uncategorized });
    return known.filter((group) => group.models.length);
  });

  function isGroupCollapsed(code: string) {
    return collapsedGroups.value[code] === true;
  }

  function toggleGroup(code: string) {
    collapsedGroups.value[code] = !isGroupCollapsed(code);
  }

  function openCategoryManager() {
    router.push('/bpm/manager/category');
  }

  async function previewForm(model: BpmModel) {
    if (!model.formId) return;
    try {
      const form = await getForm(model.formId);
      previewFormRules.value = (form.fields || []).map((field) => formCreate.parseJson(field));
      previewFormOption.value = { ...formCreate.parseJson(form.conf || '{}'), submitBtn: false, resetBtn: false, disabled: true };
      previewFormValue.value = {};
      formPreviewVisible.value = true;
    } catch {
      createMessage.error('流程表单预览加载失败');
    }
  }

  function isModelSorting(code: string) {
    return sortingGroupCode.value === code;
  }

  function beginSorting(group: ModelGroup) {
    if (group.models.length < 2) {
      createMessage.info('该分类下只有一个流程模型，无需排序');
      return;
    }
    if (sortingGroupCode.value && sortingGroupCode.value !== group.code) {
      draggedModelId.value = '';
    }
    sortingGroupCode.value = group.code;
  }

  function rowSortProps(record: BpmModel, code: string) {
    if (!isModelSorting(code)) return {};
    return {
      class: 'drag-sort-row',
      draggable: true,
      onDragstart: (event: DragEvent) => {
        draggedModelId.value = record.id || '';
        if (event.dataTransfer) {
          event.dataTransfer.effectAllowed = 'move';
          event.dataTransfer.setData('text/plain', draggedModelId.value);
        }
      },
      onDragover: (event: DragEvent) => {
        if (draggedModelId.value && draggedModelId.value !== record.id) event.preventDefault();
      },
      onDrop: (event: DragEvent) => {
        event.preventDefault();
        if (!draggedModelId.value || draggedModelId.value === record.id) return;
        reorderModels(code, record.id!);
      },
      onDragend: () => {
        draggedModelId.value = '';
      },
    };
  }

  function reorderModels(code: string, targetId: string) {
    const group = groups.value.find((item) => item.code === code);
    if (!group) return;
    const sourceId = draggedModelId.value;
    const ids = group.models.map((model) => model.id).filter((id): id is string => !!id);
    const sourceIndex = ids.indexOf(sourceId);
    const targetIndex = ids.indexOf(targetId);
    if (sourceIndex < 0 || targetIndex < 0) return;
    ids.splice(sourceIndex, 1);
    ids.splice(targetIndex, 0, sourceId);
    const modelMap = new Map(models.value.map((model) => [model.id, model]));
    const sorted = ids.map((id) => modelMap.get(id)).filter((model): model is BpmModel => !!model);
    const categoryIds = new Set(ids);
    models.value = [...sorted, ...models.value.filter((model) => !categoryIds.has(model.id))];
  }

  async function cancelSorting() {
    sortingGroupCode.value = null;
    draggedModelId.value = '';
    await loadModels();
  }

  async function submitSorting() {
    if (!sortingGroupCode.value) return;
    const group = groups.value.find((item) => item.code === sortingGroupCode.value);
    const ids = (group?.models || []).map((model) => model.id).filter((id): id is string => !!id);
    if (!ids.length) return;
    savingSort.value = true;
    try {
      await updateModelSortBatch(ids);
      createMessage.success('流程顺序已保存');
      sortingGroupCode.value = null;
      draggedModelId.value = '';
      await loadModels();
    } finally {
      savingSort.value = false;
    }
  }

  async function loadModels() {
    loading.value = true;
    try {
      const [modelList, categoryList, formList] = await Promise.all([
        getModelList(keyword.value || undefined),
        getCategorySimpleList(),
        getFormSimpleList(),
      ]);
      models.value = modelList;
      categories.value = categoryList;
      forms.value = formList;
    } finally {
      loading.value = false;
    }
  }

  async function openEditor(record?: BpmModel, initialStep = 0) {
    editorStep.value = initialStep;
    const detail = record?.id ? await getModel(record.id) : emptyForm();
    Object.assign(form, emptyForm(), detail);
    form.managerUserIds = [...(detail.managerUserIds || [])];
    editorVisible.value = true;
  }

  async function openDesigner(model: BpmModel) {
    if (model.type === 20) { await openEditor(model, 2); return; }
    const detail = await getModel(model.id!);
    Object.assign(designForm, emptyForm(), detail);
    simpleModel.value = structuredClone(detail.simpleModel || {});
    simpleJson.value = JSON.stringify(simpleModel.value, null, 2);
    designTab.value = detail.type === 20 ? 'simple-visual' : 'bpmn-visual';
    designerVisible.value = true;
  }

  function syncDesignSource(activeKey: string | number) {
    if (designForm.type !== 20) return;
    if (activeKey === 'simple-json') {
      simpleJson.value = JSON.stringify(simpleModel.value, null, 2);
      return;
    }
    if (activeKey === 'simple-visual') {
      try {
        simpleModel.value = JSON.parse(simpleJson.value);
      } catch (error: any) {
        createMessage.error(error?.message || '简易流程 JSON 格式不正确');
        designTab.value = 'simple-json';
      }
    }
  }

  async function saveDesign() {
    savingDesign.value = true;
    try {
      if (designForm.type === 10) {
        if (!designForm.bpmnXml?.trim()) throw new Error('BPMN XML 不能为空');
        await updateModelBpmn(designForm.id!, designForm.bpmnXml);
      } else {
        const model = designTab.value === 'simple-json' ? JSON.parse(simpleJson.value) : simpleModel.value;
        await updateSimpleModel(designForm.id!, model);
      }
      createMessage.success('流程设计已保存');
      designerVisible.value = false;
      await loadModels();
    } catch (error: any) {
      createMessage.error(error?.message || '流程设计保存失败');
    } finally {
      savingDesign.value = false;
    }
  }

  async function publish(model: BpmModel) {
    await deployModel(model.id!);
    createMessage.success('流程发布成功');
    await loadModels();
  }

  async function toggleState(model: BpmModel) {
    const nextState = model.processDefinition?.suspensionState === 1 ? 2 : 1;
    await updateModelState(model.id!, nextState);
    createMessage.success(nextState === 1 ? '流程已激活' : '流程已挂起');
    await loadModels();
  }

  async function downloadModel(model: BpmModel) {
    const data = await exportModel(model.id!);
    const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${model.key}.json`;
    link.click();
    URL.revokeObjectURL(url);
  }

  function confirmClean(model: BpmModel) {
    createConfirm({
      iconType: 'warning',
      title: '清理流程实例',
      content: `将删除“${model.name}”的全部运行中及历史实例，确定继续？`,
      onOk: async () => {
        await cleanModel(model.id!);
        createMessage.success('流程实例已清理');
      },
    });
  }

  function confirmDelete(model: BpmModel) {
    createConfirm({
      iconType: 'warning',
      title: '删除流程模型',
      content: `确定删除“${model.name}”吗？已发布定义将被挂起。`,
      onOk: async () => {
        await deleteModel(model.id!);
        createMessage.success('流程模型已删除');
        await loadModels();
      },
    });
  }

  const handleImport: UploadProps['beforeUpload'] = async (file) => {
    if (!file.name.toLowerCase().endsWith('.json')) {
      createMessage.warning('只允许导入 JSON 文件');
      return false;
    }
    if (file.size > 1024 * 1024) {
      createMessage.warning('导入文件不能超过 1 MB');
      return false;
    }
    await importModel(file as File);
    createMessage.success('流程模型导入成功');
    await loadModels();
    return false;
  };

  onMounted(loadModels);
  onActivated(loadModels);
</script>

<style scoped lang="less">
  .bpm-model-page {
    min-height: calc(100vh - 112px);
    padding: 16px;
    background: #f5f7fa;
  }

  .model-page-card {
    border: 1px solid #e8ebef;
    border-radius: 12px;
    background: #fff;

    :deep(.ant-card-head) {
      min-height: 66px;
      padding: 0 20px;
      border-bottom: 1px solid #edf0f3;
    }

    :deep(.ant-card-head-title) {
      color: #344054;
      font-size: 18px;
      font-weight: 600;
    }

    :deep(.ant-card-body) {
      padding: 12px 20px 20px;
    }
  }

  .model-search {
    width: 300px;
  }

  .create-model-button {
    height: 40px;
    padding: 0 18px;
    border-radius: 8px;
    font-size: 15px;
  }

  .import-model-button,
  .settings-button {
    height: 40px;
    border-radius: 8px;
  }

  .settings-button {
    width: 40px;
    padding: 0;
    color: #667085;
  }

  .model-group-panel {
    margin-top: 14px;
    padding: 0 10px 10px;
    overflow: hidden;
    border: 1px solid #e5e9ef;
    border-radius: 12px;
    background: #fff;
  }

  .model-group-header {
    display: flex;
    min-height: 64px;
    align-items: center;
    justify-content: space-between;
    padding: 0 10px;
  }

  .group-title {
    display: inline-flex;
    align-items: center;
    gap: 12px;
    border: 0;
    background: transparent;
    color: #344054;
    cursor: pointer;
    font-size: 20px;
    font-weight: 600;
  }

  .group-title em {
    color: #98a2b3;
    font-size: 15px;
    font-style: normal;
    font-weight: 500;
  }

  .group-title :deep(.anticon) {
    color: #98a2b3;
    font-size: 18px;
  }

  .group-actions :deep(.ant-btn) {
    padding: 0 6px;
    color: #1677ff;
    font-size: 15px;
  }

  .model-table {
    :deep(.ant-table) {
      overflow: hidden;
      border: 1px solid #e5e9ef;
      border-radius: 8px;
    }

    :deep(.ant-table-thead > tr > th) {
      padding: 13px 14px;
      background: #f7f8fa;
      color: #667085;
      font-weight: 600;
      text-align: center;
    }

    :deep(.ant-table-tbody > tr > td) {
      padding: 11px 14px;
      color: #475467;
      text-align: center;
    }

    :deep(.ant-table-tbody > tr > td:first-child) {
      text-align: left;
    }
  }

  .model-name-cell {
    display: flex;
    align-items: center;
    gap: 12px;

    :deep(.ant-avatar) {
      flex: 0 0 auto;
      background: #2f88ff;
      color: #fff;
      font-size: 16px;
    }
  }

  .model-key {
    margin-top: 3px;
    color: #98a2b3;
    font-size: 12px;
  }

  .muted-text {
    color: #98a2b3;
  }

  .version-tag {
    margin-left: 10px;
    border-color: #d0d5dd;
    color: #667085;
  }

  .row-actions :deep(.ant-btn) {
    padding: 0;
  }

  .source-editor {
    height: 58vh;
    font-family: Consolas, Monaco, monospace;
    line-height: 1.5;
  }

  .model-editor-content {
    padding: 8px 24px 16px;
  }

  .model-editor-form {
    :deep(.ant-form-item) {
      margin-bottom: 16px;
    }

    :deep(.ant-form-item-label) {
      padding-bottom: 8px;
    }
  }

  .row-drag-handle {
    flex: 0 0 auto;
    color: #98a2b3;
    cursor: grab;
    font-size: 18px;

    &:active {
      cursor: grabbing;
    }
  }

  .model-table :deep(.drag-sort-row) {
    cursor: move;

    &:hover {
      box-shadow: inset 0 0 0 1px #91caff;
    }
  }
</style>
