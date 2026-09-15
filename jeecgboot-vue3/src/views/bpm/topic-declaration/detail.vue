<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <TopicDeclarationForm :read-only="true" :initial-data="initialData" />
</template>

<script lang="ts" setup>
  import { onMounted, reactive } from 'vue';
  import { useRoute } from 'vue-router';
  import TopicDeclarationForm from './components/TopicDeclarationForm.vue';
  import { getProcessInstance } from '/@/views/workflow/workflow.api';

  const route = useRoute();
  const initialData = reactive<any>({});

  onMounted(async () => {
    const id = String(route.query.id || route.query.processInstanceId || '');
    if (!id) return;
    try {
      const instance = await getProcessInstance(id);
      const formVariables = instance.formVariables || {};
      const nested = formVariables['topicApplication'];
      Object.assign(initialData, nested && typeof nested === 'object' ? nested : formVariables);
    } catch (error) {
      console.warn('加载课题表单数据失败', error);
    }
  });
</script>
