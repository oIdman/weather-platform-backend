<template>
  <div class="condition-groups">
    <div class="condition-toolbar">
      <span>条件组之间</span>
      <a-radio-group :value="groups.and !== false" size="small" button-style="solid" @change="setGroupJoin">
        <a-radio-button :value="true">且</a-radio-button>
        <a-radio-button :value="false">或</a-radio-button>
      </a-radio-group>
      <a-button type="link" size="small" @click="addGroup">添加条件组</a-button>
    </div>

    <a-card v-for="(group, groupIndex) in groups.conditions" :key="groupIndex" size="small" class="condition-card">
      <template #title>
        <div class="condition-title">
          <span>条件组 {{ groupIndex + 1 }}</span>
          <a-button v-if="groups.conditions.length > 1" type="text" danger size="small" @click="removeGroup(groupIndex)">删除</a-button>
        </div>
      </template>
      <div class="rule-toolbar">
        <span>组内规则关系</span>
        <a-radio-group :value="group.and !== false" size="small" button-style="solid" @change="setRuleJoin(groupIndex, $event)">
          <a-radio-button :value="true">且</a-radio-button>
          <a-radio-button :value="false">或</a-radio-button>
        </a-radio-group>
      </div>
      <div v-for="(rule, ruleIndex) in group.rules" :key="ruleIndex" class="rule-row">
        <a-select
          :value="rule.leftSide || undefined"
          :options="fieldOptions"
          show-search
          allow-clear
          placeholder="请选择表单字段"
          @change="updateRule(groupIndex, ruleIndex, 'leftSide', $event)"
        />
        <a-select
          :value="rule.opCode || '=='"
          :options="operatorOptions"
          @change="updateRule(groupIndex, ruleIndex, 'opCode', $event)"
        />
        <a-input
          :value="rule.rightSide ?? ''"
          allow-clear
          placeholder="比较值"
          @update:value="updateRule(groupIndex, ruleIndex, 'rightSide', $event)"
        />
        <a-button type="text" danger size="small" @click="removeRule(groupIndex, ruleIndex)">删除</a-button>
      </div>
      <a-button type="link" size="small" @click="addRule(groupIndex)">添加规则</a-button>
    </a-card>

    <a-collapse v-if="showAdvanced" ghost class="advanced-condition">
      <a-collapse-panel key="json" header="高级 JSON">
        <a-textarea
          :value="advancedDraft"
          :rows="5"
          spellcheck="false"
          placeholder="可直接编辑 conditionGroups JSON"
          @update:value="updateAdvanced"
        />
        <div class="condition-hint">高级 JSON 仅在内容为有效条件组对象时写回流程配置。</div>
      </a-collapse-panel>
    </a-collapse>
  </div>
</template>

<script lang="ts" setup>
  import { computed, ref, watch } from 'vue';

  type FormField = { label: string; value: string };
  type Rule = Record<string, any>;
  type ConditionGroup = { and?: boolean; rules: Rule[] };
  type Groups = { and?: boolean; conditions: ConditionGroup[] };

  const props = withDefaults(
    defineProps<{
      modelValue?: Groups;
      formFields?: FormField[];
      showAdvanced?: boolean;
    }>(),
    { modelValue: undefined, formFields: () => [], showAdvanced: true }
  );
  const emit = defineEmits<{ 'update:modelValue': [value: Groups] }>();

  const operatorOptions = [
    { label: '等于', value: '==' },
    { label: '不等于', value: '!=' },
    { label: '大于', value: '>' },
    { label: '大于等于', value: '>=' },
    { label: '小于', value: '<' },
    { label: '小于等于', value: '<=' },
    { label: '包含', value: 'contain' },
    { label: '不包含', value: '!contain' },
  ];
  const fieldOptions = computed(() => props.formFields.map((field) => ({ label: field.label, value: field.value })));
  const defaultGroups = (): Groups => ({
    and: true,
    conditions: [{ and: true, rules: [{ opCode: '==', leftSide: '', rightSide: '' }] }],
  });
  const groups = computed<Groups>(() => normalizeGroups(props.modelValue));
  const advancedDraft = ref('');

  watch(
    () => props.modelValue,
    (value) => {
      advancedDraft.value = JSON.stringify(normalizeGroups(value), null, 2);
    },
    { deep: true, immediate: true }
  );

  function normalizeGroups(value?: Groups): Groups {
    const source = value && typeof value === 'object' ? value : defaultGroups();
    const conditions = Array.isArray(source.conditions) && source.conditions.length
      ? source.conditions.map((condition) => ({
          ...condition,
          rules: Array.isArray(condition?.rules) && condition.rules.length
            ? condition.rules.map((rule) => ({ opCode: '==', leftSide: '', rightSide: '', ...rule }))
            : [{ opCode: '==', leftSide: '', rightSide: '' }],
        }))
      : defaultGroups().conditions;
    return { ...source, and: source.and !== false, conditions };
  }

  function cloneGroups() {
    return JSON.parse(JSON.stringify(groups.value)) as Groups;
  }

  function updateGroups(updater: (value: Groups) => void) {
    const next = cloneGroups();
    updater(next);
    emit('update:modelValue', next);
  }

  function setGroupJoin(event: { target?: { value?: boolean } }) {
    updateGroups((value) => { value.and = event?.target?.value !== false; });
  }

  function addGroup() {
    updateGroups((value) => value.conditions.push({ and: true, rules: [{ opCode: '==', leftSide: '', rightSide: '' }] }));
  }

  function removeGroup(index: number) {
    updateGroups((value) => { value.conditions.splice(index, 1); });
  }

  function setRuleJoin(groupIndex: number, event: { target?: { value?: boolean } }) {
    updateGroups((value) => { value.conditions[groupIndex].and = event?.target?.value !== false; });
  }

  function addRule(groupIndex: number) {
    updateGroups((value) => value.conditions[groupIndex].rules.push({ opCode: '==', leftSide: '', rightSide: '' }));
  }

  function removeRule(groupIndex: number, ruleIndex: number) {
    updateGroups((value) => {
      const rules = value.conditions[groupIndex].rules;
      if (rules.length > 1) rules.splice(ruleIndex, 1);
    });
  }

  function updateRule(groupIndex: number, ruleIndex: number, key: string, value: unknown) {
    updateGroups((groupsValue) => {
      groupsValue.conditions[groupIndex].rules[ruleIndex][key] = value == null ? '' : String(value);
    });
  }

  function updateAdvanced(value: string) {
    advancedDraft.value = value;
    try {
      const parsed = JSON.parse(value);
      if (parsed && typeof parsed === 'object' && !Array.isArray(parsed) && Array.isArray(parsed.conditions)) {
        emit('update:modelValue', normalizeGroups(parsed));
      }
    } catch {
      // 保留草稿文本，等待 JSON 完整后再写回。
    }
  }
</script>

<style scoped lang="less">
  .condition-groups { margin-top: 4px; }
  .condition-toolbar, .rule-toolbar, .condition-title { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
  .condition-card { margin-bottom: 10px; }
  .rule-toolbar { justify-content: flex-start; margin-bottom: 8px; color: #595959; font-size: 12px; }
  .rule-row { display: grid; grid-template-columns: minmax(120px, 1fr) 120px minmax(120px, 1fr) auto; gap: 8px; align-items: center; margin-bottom: 8px; }
  .advanced-condition { margin-top: 8px; }
  .condition-hint { margin-top: 4px; color: #8c8c8c; font-size: 12px; }
  @media (max-width: 720px) {
    .rule-row { grid-template-columns: 1fr; }
  }
</style>
