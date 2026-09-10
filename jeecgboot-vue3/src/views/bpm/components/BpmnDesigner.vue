<template>
  <div class="bpmn-designer">
    <div class="designer-toolbar">
      <a-space>
        <a-button size="small" @click="zoom(-0.1)">缩小</a-button>
        <a-button size="small" @click="resetZoom">适应画布</a-button>
        <a-button size="small" @click="zoom(0.1)">放大</a-button>
        <a-button size="small" :disabled="!canUndo" @click="undo">撤销</a-button>
        <a-button size="small" :disabled="!canRedo" @click="redo">重做</a-button>
      </a-space>
      <span class="toolbar-tip">从左侧拖入节点，点击节点后在右侧配置</span>
    </div>
    <div class="designer-body">
      <div ref="canvasRef" class="designer-canvas"></div>
      <aside class="property-panel">
        <template v-if="selectedElement">
          <h3>节点属性</h3>
          <a-form layout="vertical" size="small">
            <a-form-item label="节点类型"><a-input :value="selectedType" disabled /></a-form-item>
            <a-form-item label="节点编号"><a-input :value="selectedElement.id" disabled /></a-form-item>
            <a-form-item label="节点名称">
              <a-input v-model:value="properties.name" @change="updateProperty('name', properties.name)" />
            </a-form-item>
            <template v-if="isUserTask">
              <a-form-item label="指定审批人">
                <JSelectUser
                  v-model:value="properties.assignee"
                  row-key="id"
                  label-key="realname"
                  @change="updateProperty('flowable:assignee', normalizeValue(properties.assignee))"
                />
              </a-form-item>
              <a-form-item label="候选用户">
                <JSelectUser
                  v-model:value="properties.candidateUsers"
                  row-key="id"
                  label-key="realname"
                  :multiple="true"
                  @change="updateProperty('flowable:candidateUsers', normalizeValue(properties.candidateUsers))"
                />
              </a-form-item>
              <a-form-item label="候选角色编码">
                <a-input
                  v-model:value="properties.candidateGroups"
                  placeholder="多个角色用英文逗号分隔"
                  @change="updateProperty('flowable:candidateGroups', properties.candidateGroups)"
                />
              </a-form-item>
              <a-form-item label="表单标识">
                <a-input v-model:value="properties.formKey" @change="updateProperty('flowable:formKey', properties.formKey)" />
              </a-form-item>
            </template>
            <template v-if="isServiceTask">
              <a-form-item label="委托表达式">
                <a-input
                  v-model:value="properties.delegateExpression"
                  placeholder="例如 ${workflowService}"
                  @change="updateProperty('flowable:delegateExpression', properties.delegateExpression)"
                />
              </a-form-item>
            </template>
            <a-form-item v-if="isSequenceFlow" label="流转条件">
              <a-textarea
                v-model:value="properties.conditionExpression"
                :rows="3"
                placeholder="例如 ${amount > 1000}"
                @change="updateConditionExpression"
              />
            </a-form-item>
          </a-form>
        </template>
        <a-empty v-else description="请选择流程节点" :image-style="{ height: '56px' }" />
      </aside>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
  import JSelectUser from '/@/components/Form/src/jeecg/components/JSelectUser.vue';
  import flowableDescriptor from './flowableDescriptor';

  const props = defineProps<{ modelValue?: string }>();
  const emit = defineEmits<{ (event: 'update:modelValue', value: string): void }>();

  const canvasRef = ref<HTMLElement>();
  const modeler = ref<any>();
  const selectedElement = ref<any>();
  const selectedType = computed(() => selectedElement.value?.businessObject?.$type || '-');
  const isUserTask = computed(() => selectedType.value === 'bpmn:UserTask');
  const isServiceTask = computed(() => selectedType.value === 'bpmn:ServiceTask');
  const isSequenceFlow = computed(() => selectedType.value === 'bpmn:SequenceFlow');
  const canUndo = ref(false);
  const canRedo = ref(false);
  const properties = reactive({
    name: '',
    assignee: '' as string | string[],
    candidateUsers: [] as string | string[],
    candidateGroups: '',
    formKey: '',
    delegateExpression: '',
    conditionExpression: '',
  });
  let lastXml = '';
  let changeTimer: ReturnType<typeof setTimeout> | undefined;

  function service<T = any>(name: string): T {
    return modeler.value?.get(name) as T;
  }

  async function importXml(xml?: string) {
    if (!modeler.value || !xml?.trim() || xml === lastXml) return;
    await modeler.value.importXML(xml);
    lastXml = xml;
    resetZoom();
  }

  async function emitXml() {
    if (!modeler.value) return;
    const result = await modeler.value.saveXML({ format: true });
    lastXml = result.xml || '';
    emit('update:modelValue', lastXml);
  }

  function scheduleEmit() {
    if (changeTimer) clearTimeout(changeTimer);
    changeTimer = setTimeout(emitXml, 180);
    const commandStack = service<any>('commandStack');
    canUndo.value = Boolean(commandStack?.canUndo());
    canRedo.value = Boolean(commandStack?.canRedo());
  }

  function loadProperties(element?: any) {
    selectedElement.value = element;
    const businessObject = element?.businessObject;
    properties.name = businessObject?.name || '';
    properties.assignee = businessObject?.assignee || '';
    properties.candidateUsers = businessObject?.candidateUsers ? String(businessObject.candidateUsers).split(',').filter(Boolean) : [];
    properties.candidateGroups = businessObject?.candidateGroups || '';
    properties.formKey = businessObject?.formKey || '';
    properties.delegateExpression = businessObject?.delegateExpression || '';
    properties.conditionExpression = businessObject?.conditionExpression?.body || businessObject?.conditionExpression || '';
  }

  function normalizeValue(value: string | string[]) {
    return Array.isArray(value) ? value.join(',') : value;
  }

  function updateProperty(name: string, value: unknown) {
    if (!selectedElement.value) return;
    service<any>('modeling').updateProperties(selectedElement.value, { [name]: value || undefined });
  }

  function updateConditionExpression() {
    if (!selectedElement.value) return;
    const expression = properties.conditionExpression.trim()
      ? service<any>('bpmnFactory').create('bpmn:FormalExpression', { body: properties.conditionExpression.trim() })
      : undefined;
    service<any>('modeling').updateProperties(selectedElement.value, { conditionExpression: expression });
  }

  function zoom(delta: number) {
    const canvas = service<any>('canvas');
    const current = Number(canvas.zoom()) || 1;
    canvas.zoom(Math.max(0.2, Math.min(4, current + delta)));
  }

  function resetZoom() {
    nextTick(() => service<any>('canvas')?.zoom('fit-viewport'));
  }

  function undo() {
    service<any>('commandStack')?.undo();
  }

  function redo() {
    service<any>('commandStack')?.redo();
  }

  onMounted(async () => {
    const { default: BpmnModeler } = await import('bpmn-js/lib/Modeler');
    modeler.value = new BpmnModeler({
      container: canvasRef.value,
      moddleExtensions: { flowable: flowableDescriptor },
    });
    const eventBus = service<any>('eventBus');
    eventBus.on('selection.changed', (event: any) => loadProperties(event.newSelection?.[0]));
    eventBus.on('element.changed', (event: any) => {
      if (event.element?.id === selectedElement.value?.id) loadProperties(event.element);
    });
    eventBus.on('commandStack.changed', scheduleEmit);
    await importXml(props.modelValue);
  });

  watch(
    () => props.modelValue,
    (value) => importXml(value)
  );

  onBeforeUnmount(() => {
    if (changeTimer) clearTimeout(changeTimer);
    modeler.value?.destroy();
  });
</script>

<style lang="less" scoped>
  @import 'bpmn-js/dist/assets/diagram-js.css';
  @import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css';

  .bpmn-designer {
    overflow: hidden;
    border: 1px solid #d9d9d9;
    border-radius: 6px;
    background: #fff;
  }

  .designer-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 12px;
    border-bottom: 1px solid #f0f0f0;
  }

  .toolbar-tip {
    color: #8c8c8c;
    font-size: 12px;
  }

  .designer-body {
    display: flex;
    height: 58vh;
    min-height: 520px;
  }

  .designer-canvas {
    min-width: 0;
    flex: 1;
    background-color: #fafafa;
    background-image: linear-gradient(#eee 1px, transparent 1px), linear-gradient(90deg, #eee 1px, transparent 1px);
    background-size: 20px 20px;
  }

  .property-panel {
    width: 310px;
    overflow-y: auto;
    padding: 14px;
    border-left: 1px solid #f0f0f0;

    h3 {
      margin-bottom: 14px;
    }
  }

  :deep(.djs-palette) {
    background: #fff;
  }
</style>
