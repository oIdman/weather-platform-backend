<template>
  <div class="trigger-form-settings">
    <div class="settings-heading">
      <span>{{ mode === 'update' ? '修改表单设置' : '删除表单设置' }}</span>
      <a-button type="link" size="small" @click="addSetting">添加设置</a-button>
    </div>

    <a-card v-for="(setting, index) in settings" :key="index" size="small" class="setting-card">
      <template #title>
        <div class="setting-title">
          <span>{{ mode === 'update' ? '修改表单设置' : '删除表单设置' }} {{ index + 1 }}</span>
          <a-button v-if="settings.length > 1" type="text" danger size="small" @click="removeSetting(index)">删除</a-button>
        </div>
      </template>

      <a-form-item label="执行条件">
        <a-select
          :value="conditionType(setting)"
          :options="conditionOptions"
          placeholder="无条件（始终执行）"
          @change="setConditionType(index, $event)"
        />
      </a-form-item>
      <a-form-item v-if="conditionType(setting) === 1" label="条件表达式">
        <a-input
          :value="setting.conditionExpression || ''"
          allow-clear
          placeholder="例如 amount > 1000"
          @update:value="updateConditionExpression(index, $event)"
        />
      </a-form-item>
      <a-form-item v-if="conditionType(setting) === 2" label="条件组">
        <TriggerConditionGroups
          :model-value="setting.conditionGroups"
          :form-fields="formFields"
          @update:model-value="updateConditionGroups(index, $event)"
        />
      </a-form-item>

      <template v-if="mode === 'update'">
        <a-divider orientation="left">修改表单字段</a-divider>
        <div v-for="key in updateFieldKeys(setting)" :key="`${index}-${key}`" class="update-field-row">
          <a-select
            :value="key || undefined"
            :options="updateFieldOptions(setting, key)"
            placeholder="请选择表单字段"
            @change="updateFieldKey(index, key, $event)"
          />
          <span class="field-operator">的值设置为</span>
          <a-input
            :value="setting.updateFormFields?.[key] ?? ''"
            :disabled="!key"
            allow-clear
            placeholder="请输入值"
            @update:value="updateFieldValue(index, key, $event)"
          />
          <a-button type="text" danger size="small" @click="removeUpdateField(index, key)">删除</a-button>
        </div>
        <a-button type="link" size="small" @click="addUpdateField(index)">添加修改字段</a-button>
      </template>

      <template v-else>
        <a-divider orientation="left">删除表单字段</a-divider>
        <a-select
          mode="multiple"
          :value="Array.isArray(setting.deleteFields) ? setting.deleteFields : []"
          :options="fieldOptions"
          class="delete-fields"
          placeholder="请选择要删除的字段"
          @change="updateDeleteFields(index, $event)"
        />
      </template>
    </a-card>

    <a-alert v-if="!settings.length" type="info" show-icon message="尚未配置表单触发设置" />
  </div>
</template>

<script lang="ts" setup>
  import { computed } from 'vue';
  import TriggerConditionGroups from './TriggerConditionGroups.vue';

  type FormField = { label: string; value: string };
  type FormSetting = Record<string, any>;

  const props = withDefaults(
    defineProps<{
      modelValue?: FormSetting[];
      mode: 'update' | 'delete';
      formFields?: FormField[];
    }>(),
    { modelValue: () => [], formFields: () => [] }
  );
  const emit = defineEmits<{ 'update:modelValue': [value: FormSetting[]] }>();

  const conditionOptions = [
    { label: '无条件（始终执行）', value: 0 },
    { label: '表达式条件', value: 1 },
    { label: '条件组', value: 2 },
  ];
  const settings = computed(() => Array.isArray(props.modelValue) ? props.modelValue : []);
  const fieldOptions = computed(() => props.formFields.map((field) => ({ label: field.label, value: field.value })));

  function emitSettings(next: FormSetting[]) {
    emit('update:modelValue', next);
  }

  function createSetting(): FormSetting {
    return props.mode === 'update' ? { updateFormFields: {} } : { deleteFields: [] };
  }

  function addSetting() {
    emitSettings([...settings.value, createSetting()]);
  }

  function removeSetting(index: number) {
    emitSettings(settings.value.filter((_, position) => position !== index));
  }

  function conditionType(setting: FormSetting) {
    const value = Number(setting.conditionType);
    return value === 1 || value === 2 ? value : 0;
  }

  function setConditionType(index: number, value: unknown) {
    const type = Number(value);
    const next = settings.value.map((setting, position) => {
      if (position !== index) return setting;
      const copy = { ...setting };
      if (type === 1) {
        copy.conditionType = 1;
        delete copy.conditionGroups;
      } else if (type === 2) {
        copy.conditionType = 2;
        copy.conditionGroups ||= {
          and: true,
          conditions: [{ and: true, rules: [{ opCode: '==', leftSide: '', rightSide: '' }] }],
        };
        delete copy.conditionExpression;
      } else {
        delete copy.conditionType;
        delete copy.conditionExpression;
        delete copy.conditionGroups;
      }
      return copy;
    });
    emitSettings(next);
  }

  function updateConditionExpression(index: number, value: unknown) {
    updateSetting(index, (setting) => ({ ...setting, conditionExpression: String(value ?? '') }));
  }

  function updateConditionGroups(index: number, value: unknown) {
    if (!value || typeof value !== 'object' || Array.isArray(value)) return;
    updateSetting(index, (setting) => ({ ...setting, conditionGroups: value }));
  }

  function updateSetting(index: number, updater: (setting: FormSetting) => FormSetting) {
    emitSettings(settings.value.map((setting, position) => position === index ? updater(setting) : setting));
  }

  function updateFieldKeys(setting: FormSetting) {
    const fields = setting.updateFormFields;
    return fields && typeof fields === 'object' && !Array.isArray(fields) ? Object.keys(fields) : [];
  }

  function updateFieldOptions(setting: FormSetting, currentKey: string) {
    const used = new Set(updateFieldKeys(setting).filter((key) => key && key !== currentKey));
    return fieldOptions.value.map((field) => ({ ...field, disabled: used.has(field.value) }));
  }

  function addUpdateField(index: number) {
    updateSetting(index, (setting) => {
      const fields = { ...(setting.updateFormFields || {}) };
      if (!Object.prototype.hasOwnProperty.call(fields, '')) fields[''] = '';
      return { ...setting, updateFormFields: fields };
    });
  }

  function updateFieldKey(index: number, oldKey: string, value: unknown) {
    const newKey = String(value ?? '');
    if (!newKey || newKey === oldKey) return;
    updateSetting(index, (setting) => {
      const fields = { ...(setting.updateFormFields || {}) };
      const fieldValue = fields[oldKey];
      delete fields[oldKey];
      fields[newKey] = fieldValue ?? '';
      return { ...setting, updateFormFields: fields };
    });
  }

  function updateFieldValue(index: number, key: string, value: unknown) {
    if (!key) return;
    updateSetting(index, (setting) => ({
      ...setting,
      updateFormFields: { ...(setting.updateFormFields || {}), [key]: String(value ?? '') },
    }));
  }

  function removeUpdateField(index: number, key: string) {
    updateSetting(index, (setting) => {
      const fields = { ...(setting.updateFormFields || {}) };
      delete fields[key];
      return { ...setting, updateFormFields: fields };
    });
  }

  function updateDeleteFields(index: number, value: unknown) {
    const values = Array.isArray(value) ? value.map((item) => String(item)) : [];
    updateSetting(index, (setting) => ({ ...setting, deleteFields: values }));
  }
</script>

<style scoped lang="less">
  .trigger-form-settings { margin-top: 8px; }
  .settings-heading, .setting-title { display: flex; align-items: center; justify-content: space-between; }
  .setting-card { margin-bottom: 12px; }
  .update-field-row { display: grid; grid-template-columns: minmax(130px, 1fr) auto minmax(130px, 1fr) auto; gap: 8px; align-items: center; margin-bottom: 8px; }
  .field-operator { color: #8c8c8c; white-space: nowrap; }
  .delete-fields { width: 100%; }
  @media (max-width: 720px) {
    .update-field-row { grid-template-columns: 1fr; }
    .field-operator { display: none; }
  }
</style>
