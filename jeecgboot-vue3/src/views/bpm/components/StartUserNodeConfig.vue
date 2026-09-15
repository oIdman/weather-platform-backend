<template>
  <div class="start-user-config">
    <a-alert
      message="发起权限在流程基本信息中维护"
      description="此处展示当前模型的可发起范围；如需修改，请返回“基本信息”步骤调整。"
      show-icon
      type="info"
    />
    <a-descriptions class="permission-summary" :column="1" bordered size="small">
      <a-descriptions-item label="可发起人员">{{ startScopeText }}</a-descriptions-item>
    </a-descriptions>
    <a-divider orientation="left">表单字段权限</a-divider>
    <a-empty v-if="!node.fieldsPermission.length" description="请先在“表单设计”中选择流程表单" />
    <div v-else class="permission-table">
      <div class="permission-row permission-header">
        <span>字段名称</span>
        <span class="permission-actions">
          <a @click="setAllPermissions('READ')">只读</a>
          <a @click="setAllPermissions('WRITE')">可编辑</a>
          <a @click="setAllPermissions('NONE')">隐藏</a>
        </span>
      </div>
      <div v-for="item in node.fieldsPermission" :key="item.field" class="permission-row">
        <span class="field-name" :title="item.field">{{ item.title || item.field }}</span>
        <a-radio-group v-model:value="item.permission" class="permission-radios">
          <a-radio value="READ">只读</a-radio>
          <a-radio value="WRITE">可编辑</a-radio>
          <a-radio value="NONE">隐藏</a-radio>
        </a-radio-group>
      </div>
    </div>
    <a-divider orientation="left">操作按钮设置</a-divider>
    <a-checkbox checked disabled>提交</a-checkbox>
    <div class="button-hint">发起人节点固定显示“提交”，其他办理按钮不适用于发起人。</div>
  </div>
</template>

<script setup lang="ts">
  import { computed, watch } from 'vue';

  type SimpleNode = Record<string, any>;
  type FormField = { label: string; value: string };

  const props = withDefaults(
    defineProps<{
      node: SimpleNode;
      formFields?: FormField[];
      startUserIds?: string[];
      startDeptIds?: string[];
    }>(),
    { formFields: () => [], startUserIds: () => [], startDeptIds: () => [] }
  );

  const startScopeText = computed(() => {
    const users = props.startUserIds.length;
    const departments = props.startDeptIds.length;
    if (!users && !departments) return '全部成员';
    if (users && departments) return `指定 ${users} 名人员及 ${departments} 个部门`;
    if (users) return `指定 ${users} 名人员`;
    return `指定 ${departments} 个部门`;
  });

  function ensureFieldPermissions() {
    const current = Array.isArray(props.node.fieldsPermission) ? props.node.fieldsPermission : [];
    const existing = new Map<string, any>(current.map((item: any) => [String(item.field), item]));
    // eslint-disable-next-line vue/no-mutating-props
    props.node.fieldsPermission = props.formFields.map((field) => ({
      field: field.value,
      title: field.label,
      permission: ['READ', 'WRITE', 'NONE'].includes(existing.get(field.value)?.permission) ? existing.get(field.value).permission : 'WRITE',
    }));
  }

  function setAllPermissions(permission: string) {
    props.node.fieldsPermission.forEach((item: any) => { item.permission = permission; });
  }

  watch(() => props.formFields, ensureFieldPermissions, { deep: true, immediate: true });
</script>

<style scoped lang="less">
  .start-user-config { padding-bottom: 16px; }
  .permission-summary { margin-top: 16px; }
  .permission-table { overflow: hidden; border: 1px solid #e5e7eb; border-radius: 6px; }
  .permission-row { display: grid; grid-template-columns: minmax(130px, 1fr) minmax(280px, 2fr); align-items: center; min-height: 48px; padding: 8px 12px; border-top: 1px solid #eef0f3; gap: 12px; }
  .permission-row:first-child { border-top: 0; }
  .permission-header { min-height: 42px; border-top: 0; background: #fafafa; font-weight: 600; }
  .permission-actions, .permission-radios { display: grid; grid-template-columns: repeat(3, 1fr); align-items: center; text-align: center; }
  .permission-radios :deep(.ant-radio-wrapper) { justify-content: center; margin-inline-end: 0; }
  .field-name { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
  .button-hint { margin-top: 8px; color: #98a2b3; font-size: 12px; }
</style>
