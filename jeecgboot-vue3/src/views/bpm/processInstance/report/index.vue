<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="bpm-process-report-page">
    <a-alert
      v-if="definition"
      class="report-summary"
      :message="`${definition.name} · 数据报表`"
      :description="`流程标识：${definition.key}，当前按该流程的全部历史版本查询实例。`"
      type="info"
      show-icon
    />
    <BasicTable @register="registerTable">
      <template #action="{ record }"><TableAction :actions="actions(record)" /></template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="BpmProcessInstanceReport" setup>
  import formCreate from '@form-create/ant-design-vue';
  import { onMounted, ref } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import type { BasicColumn, FormSchema } from '/@/components/Table';
  import { BasicTable, TableAction, useTable } from '/@/components/Table';
  import { getProcessDefinition, type BpmProcessDefinition } from '/@/api/bpm/definition';
  import { useMessage } from '/@/hooks/web/useMessage';
  import {
    cancelProcess,
    getManagerProcessPage,
    type WorkflowInstance,
  } from '/@/views/workflow/workflow.api';

  interface ReportField {
    field: string;
    title: string;
    type: string;
  }

  const route = useRoute();
  const router = useRouter();
  const { createMessage } = useMessage();
  const definition = ref<BpmProcessDefinition>();
  const reportFields = ref<ReportField[]>([]);
  const formFieldPrefix = 'formField__';
  const processDefinitionId = String(route.query.processDefinitionId || '');
  const processDefinitionKey = String(route.query.processDefinitionKey || '');

  const statusOptions = [
    { label: '审批中', value: 1 },
    { label: '审批通过', value: 2 },
    { label: '审批不通过', value: 3 },
    { label: '已取消', value: 4 },
  ];
  const dateTimeRangeProps = {
    showTime: true,
    valueFormat: 'YYYY-MM-DD HH:mm:ss',
    placeholder: ['开始时间', '结束时间'],
  };

  const [registerTable, { getForm, reload, setColumns }] = useTable({
    title: '流程实例列表',
    api: loadReportPage,
    columns: createColumns(),
    formConfig: { schemas: createSearchSchemas() },
    rowKey: 'id',
    useSearchForm: true,
    immediate: false,
    actionColumn: { width: 130 },
  });

  async function loadReportPage(params: Record<string, unknown>) {
    const queryParams: Record<string, unknown> = {};
    const formFieldsParams: Record<string, unknown> = {};
    Object.entries(params || {}).forEach(([key, value]) => {
      if (key.startsWith(formFieldPrefix)) {
        if (value !== undefined && value !== null && value !== '') {
          formFieldsParams[key.slice(formFieldPrefix.length)] = value;
        }
        return;
      }
      queryParams[key] = value;
    });
    return getManagerProcessPage({
      ...queryParams,
      processDefinitionKey,
      formFieldsParams: JSON.stringify(formFieldsParams),
    });
  }

  function createSearchSchemas(fields: ReportField[] = []): FormSchema[] {
    const baseSchemas: FormSchema[] = [
      {
        field: 'startUserId',
        label: '发起人',
        component: 'JSelectUser',
        componentProps: { isRadioSelection: true, rowKey: 'id', labelKey: 'realname' },
        colProps: { span: 6 },
      },
      { field: 'name', label: '流程名称', component: 'Input', colProps: { span: 6 } },
      {
        field: 'status',
        label: '流程状态',
        component: 'Select',
        componentProps: { options: statusOptions, allowClear: true },
        colProps: { span: 6 },
      },
      {
        field: 'createTime',
        label: '发起时间',
        component: 'RangePicker',
        componentProps: dateTimeRangeProps,
        colProps: { span: 8 },
      },
      {
        field: 'endTime',
        label: '结束时间',
        component: 'RangePicker',
        componentProps: dateTimeRangeProps,
        colProps: { span: 8 },
      },
    ];
    const dynamicSchemas = fields
      .filter((field) => ['input', 'textarea'].includes(field.type.toLowerCase()))
      .map<FormSchema>((field) => ({
        field: `${formFieldPrefix}${field.field}`,
        label: field.title,
        component: 'Input',
        componentProps: { allowClear: true, placeholder: `请输入${field.title}` },
        colProps: { span: 6 },
      }));
    return [...baseSchemas, ...dynamicSchemas];
  }

  function createColumns(fields: ReportField[] = []): BasicColumn[] {
    const columns: BasicColumn[] = [
      { title: '流程名称', dataIndex: 'name', align: 'left', width: 220 },
      { title: '流程发起人', dataIndex: 'startUserName', width: 150, align: 'center' },
      {
        title: '流程状态',
        dataIndex: 'status',
        width: 120,
        align: 'center',
        customRender: ({ text }) =>
          ({ RUNNING: '审批中', COMPLETED: '审批通过', REJECTED: '审批不通过', CANCELED: '已取消' })[text] || text,
      },
      { title: '发起时间', dataIndex: 'startTime', width: 180 },
      { title: '结束时间', dataIndex: 'endTime', width: 180 },
    ];
    const dynamicColumns = fields.map<BasicColumn>((field) => ({
      title: field.title,
      dataIndex: `formVariables.${field.field}`,
      width: 150,
      customRender: ({ record }) => formatValue(record.formVariables?.[field.field]),
    }));
    return [...columns, ...dynamicColumns];
  }

  function parseReportFields(fields: string[] = []) {
    const result: ReportField[] = [];
    const seen = new Set<string>();
    const visit = (value: unknown) => {
      if (Array.isArray(value)) {
        value.forEach(visit);
        return;
      }
      if (!value || typeof value !== 'object') return;
      const rule = value as Record<string, unknown>;
      const field = typeof rule.field === 'string' ? rule.field : '';
      const title = typeof rule.title === 'string' ? rule.title : field;
      const type = typeof rule.type === 'string' ? rule.type : '';
      if (field && title && type && !seen.has(field)) {
        seen.add(field);
        result.push({ field, title, type });
      }
      Object.values(rule).forEach(visit);
    };
    fields.forEach((field) => {
      try {
        visit(formCreate.parseJson(field));
      } catch (error) {
        console.warn('解析流程表单字段失败', error);
      }
    });
    return result;
  }

  function formatValue(value: unknown) {
    if (value === undefined || value === null || value === '') return '-';
    if (Array.isArray(value)) return value.map(formatValue).join('、');
    if (typeof value === 'object') return JSON.stringify(value);
    if (typeof value === 'boolean') return value ? '是' : '否';
    return String(value);
  }

  function actions(record: WorkflowInstance) {
    return [
      {
        label: '详情',
        auth: 'bpm:process-instance:manager-query',
        onClick: () => router.push({ path: '/bpm/process-instance/detail', query: { id: record.id } }),
      },
      {
        label: '取消',
        auth: 'bpm:process-instance:cancel-by-admin',
        color: 'error',
        ifShow: record.status === 'RUNNING',
        popConfirm: {
          title: '确认取消该流程？',
          confirm: async () => {
            await cancelProcess({ id: record.id, reason: '管理员在流程报表中取消' }, true);
            createMessage.success('流程已取消');
            reload();
          },
        },
      },
    ];
  }

  onMounted(async () => {
    if (!processDefinitionId && !processDefinitionKey) {
      createMessage.error('缺少流程定义参数，无法加载数据报表');
      return;
    }
    definition.value = await getProcessDefinition(processDefinitionId || undefined, processDefinitionKey || undefined);
    if (!definition.value) {
      createMessage.error('流程定义不存在或已被删除');
      return;
    }
    reportFields.value = parseReportFields(definition.value.formFields || []);
    setColumns(createColumns(reportFields.value));
    await getForm().resetSchema(createSearchSchemas(reportFields.value));
    await reload();
  });
</script>

<style scoped lang="less">
  .bpm-process-report-page {
    padding: 16px;
  }

  .report-summary {
    margin-bottom: 16px;
  }
</style>
