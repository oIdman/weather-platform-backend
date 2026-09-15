<template>
  <div class="response-rows">
    <div class="response-heading">
      <span>{{ label }}</span>
      <a-button type="link" size="small" @click="add">添加</a-button>
    </div>
    <div v-for="(row, index) in modelValue || []" :key="index" class="response-row">
      <a-input :value="row.key" placeholder="流程变量名" @update:value="update(index, 'key', $event)" />
      <span class="response-arrow">←</span>
      <a-input :value="row.value" placeholder="响应字段名，例如 data.id" @update:value="update(index, 'value', $event)" />
      <a-button danger @click="remove(index)">删除</a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
  type ResponseRow = Record<string, string>;
  const props = defineProps<{ modelValue?: ResponseRow[]; label: string }>();
  const emit = defineEmits<{ 'update:modelValue': [value: ResponseRow[]] }>();

  function add() {
    emit('update:modelValue', [...(props.modelValue || []), { key: '', value: '' }]);
  }

  function remove(index: number) {
    emit('update:modelValue', (props.modelValue || []).filter((_, position) => position !== index));
  }

  function update(index: number, key: string, value: unknown) {
    emit('update:modelValue', (props.modelValue || []).map((row, position) => position === index ? { ...row, [key]: String(value ?? '') } : row));
  }
</script>

<style scoped lang="less">
  .response-rows { margin-top: 12px; }
  .response-heading { display: flex; align-items: center; justify-content: space-between; margin-bottom: 6px; }
  .response-row { display: grid; grid-template-columns: 1fr 24px 1fr auto; gap: 8px; align-items: center; margin-bottom: 8px; }
  .response-arrow { color: #8c8c8c; text-align: center; }
</style>
