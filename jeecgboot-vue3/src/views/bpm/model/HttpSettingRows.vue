<template>
  <div class="http-rows">
    <div class="http-heading">{{ label }} <a-button type="link" size="small" @click="add">添加</a-button></div>
    <div v-for="(row, index) in modelValue || []" :key="index" class="http-row">
      <a-input :value="row.key" placeholder="参数名" @update:value="update(index, 'key', $event)" />
      <a-select :value="row.type" :options="[{ label: '固定值', value: 1 }, { label: '表单字段', value: 2 }]" @update:value="update(index, 'type', $event)" />
      <a-input :value="row.value" placeholder="参数值 / 字段标识" @update:value="update(index, 'value', $event)" />
      <a-button danger @click="remove(index)">删除</a-button>
    </div>
  </div>
</template>
<script setup lang="ts">
  const props = defineProps<{ modelValue?: Record<string, any>[]; label: string }>();
  const emit = defineEmits<{ 'update:modelValue': [value: Record<string, any>[]] }>();
  function add() { emit('update:modelValue', [...(props.modelValue || []), { key: '', value: '', type: 1 }]); }
  function remove(index: number) { emit('update:modelValue', (props.modelValue || []).filter((_, position) => position !== index)); }
  function update(index: number, key: string, value: unknown) { emit('update:modelValue', (props.modelValue || []).map((row, position) => position === index ? { ...row, [key]: value } : row)); }
</script>
<style scoped lang="less">
  .http-rows { margin-top: 12px; }
  .http-heading { margin-bottom: 6px; }
  .http-row { display: grid; grid-template-columns: 1fr 120px 1fr auto; gap: 8px; margin-bottom: 8px; }
</style>
