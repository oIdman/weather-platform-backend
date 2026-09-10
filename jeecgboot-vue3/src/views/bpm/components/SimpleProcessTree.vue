<template>
  <div v-if="node" class="simple-tree">
    <div
      class="node-card"
      :class="[{ selected: selectedId === node.id }, `node-type-${node.type}`]"
      role="button"
      tabindex="0"
      @click.stop="$emit('select', node)"
      @keydown.enter.stop="$emit('select', node)"
    >
      <div class="node-title">
        <span>{{ node.name || nodeTypeName(node.type) }}</span>
        <a-tag :color="nodeTypeColor(node.type)">{{ nodeTypeName(node.type) }}</a-tag>
      </div>
      <div class="node-summary">{{ summary(node) }}</div>
    </div>

    <div v-if="isBranch(node.type)" class="branch-grid">
      <div v-for="(branch, index) in node.conditionNodes || []" :key="branch.id" class="branch-column">
        <button class="branch-title" :class="{ selected: selectedId === branch.id }" type="button" @click.stop="$emit('select', branch)">
          {{ branch.name || `分支 ${index + 1}` }}
        </button>
        <SimpleNodeAdd @add="$emit('add', branch, $event)" />
        <SimpleProcessTree v-if="branch.childNode" :node="branch.childNode" :selected-id="selectedId" @select="$emit('select', $event)" @add="(target, type) => $emit('add', target, type)" />
      </div>
    </div>

    <SimpleNodeAdd v-if="node.type !== 1" @add="$emit('add', node, $event)" />
    <SimpleProcessTree v-if="node.childNode" :node="node.childNode" :selected-id="selectedId" @select="$emit('select', $event)" @add="(target, type) => $emit('add', target, type)" />
  </div>
</template>

<script lang="ts" name="SimpleProcessTree" setup>
  import type { PropType } from 'vue';
  import SimpleNodeAdd from './SimpleNodeAdd.vue';

  type SimpleNode = Record<string, any>;

  defineProps({
    node: { type: Object as PropType<SimpleNode>, required: true },
    selectedId: { type: String, default: '' },
  });

  defineEmits<{ select: [node: SimpleNode]; add: [node: SimpleNode, type: number] }>();

  const typeNames: Record<number, string> = {
    0: '开始',
    1: '结束',
    10: '发起人',
    11: '审批人',
    12: '抄送人',
    13: '办理人',
    14: '延迟器',
    15: '触发器',
    20: '子流程',
    50: '条件',
    51: '条件分支',
    52: '并行分支',
    53: '包容分支',
    54: '路由分支',
  };

  function nodeTypeName(type: number) {
    return typeNames[type] || `节点 ${type}`;
  }

  function nodeTypeColor(type: number) {
    if ([11, 13].includes(type)) return 'blue';
    if ([51, 52, 53, 54].includes(type)) return 'purple';
    if (type === 12) return 'cyan';
    if (type === 14) return 'orange';
    if (type === 1) return 'default';
    return 'green';
  }

  function isBranch(type: number) {
    return [51, 52, 53, 54].includes(type);
  }

  function summary(value: SimpleNode) {
    if (value.showText) return value.showText;
    if ([11, 12, 13].includes(value.type)) {
      if (value.assignee) return `指定用户：${value.assignee}`;
      if (value.candidateParam) return `候选范围：${value.candidateParam}`;
      return '请配置人员';
    }
    if (value.type === 14) return value.delaySetting?.duration || value.delaySetting?.delayTime || '请配置等待时间';
    if (value.type === 20) return value.childProcessSetting?.calledElement || value.childProcessSetting?.calledProcessDefinitionKey || '请配置子流程';
    if (value.type === 15) return value.triggerSetting?.httpRequestSetting?.url || '请配置触发器';
    if (isBranch(value.type)) return `${value.conditionNodes?.length || 0} 个分支`;
    if (value.type === 10) return '设置发起权限和表单权限';
    if (value.type === 1) return '流程结束';
    return '点击配置节点';
  }
</script>

<style scoped lang="less">
  .simple-tree {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
  }

  .node-card {
    width: 260px;
    padding: 12px 14px;
    border: 2px solid transparent;
    border-radius: 8px;
    background: #fff;
    box-shadow: 0 2px 10px rgb(0 0 0 / 10%);
    cursor: pointer;

    &.selected {
      border-color: #1677ff;
      box-shadow: 0 0 0 3px rgb(22 119 255 / 12%);
    }
  }

  .node-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-weight: 600;
    gap: 8px;
  }

  .node-summary {
    margin-top: 8px;
    overflow: hidden;
    padding: 8px;
    border-radius: 4px;
    background: #f7f8fa;
    color: #475467;
    font-size: 12px;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .node-type-1 { width: 106px; border-radius: 28px; text-align: center; }
  .node-type-1 .node-title { justify-content: center; }
  .node-type-1 .node-summary, .node-type-1 :deep(.ant-tag) { display: none; }

  .connector {
    width: 2px;
    height: 30px;
    background: #bfbfbf;
  }

  .branch-grid {
    display: flex;
    width: max-content;
    min-width: 560px;
    max-width: 100%;
    margin: 26px 0;
    padding: 20px;
    border: 1px dashed #91caff;
    border-radius: 10px;
    background: #f0f7ff;
    gap: 24px;
    overflow-x: auto;
  }

  .branch-column {
    display: flex;
    flex: 1 0 260px;
    flex-direction: column;
    align-items: center;
  }

  .branch-title {
    width: 100%;
    margin-bottom: 18px;
    padding: 8px 12px;
    border: 1px solid #d9d9d9;
    border-radius: 6px;
    background: #fff;
    cursor: pointer;

    &.selected {
      border-color: #1677ff;
      color: #1677ff;
    }
  }

  .empty-branch {
    padding: 20px;
    border: 1px dashed #bfbfbf;
    border-radius: 6px;
    color: #8c8c8c;
    cursor: pointer;
  }
</style>
