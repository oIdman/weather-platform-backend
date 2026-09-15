<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <a-modal v-model:open="open" title="打印流程" width="900px" :footer="null" :destroy-on-close="true" @cancel="handleClose">
    <a-spin :spinning="loading">
      <div v-if="printData" id="bpmPrintContainer" class="bpm-print-container">
        <!-- eslint-disable-next-line vue/no-v-html  打印模板由流程模型配置，渲染前已做去脚本处理 -->
        <div v-if="printData.printTemplateEnable" v-html="customTemplateHtml"></div>
        <template v-else>
          <h2 class="print-title">{{ printData.processInstance?.name || '-' }}</h2>
          <div class="print-meta">
            <span>流程编号：{{ printData.processInstance?.businessKey || printData.processInstance?.id || '-' }}</span>
            <span>打印人员：{{ currentUserName }}</span>
          </div>
          <table class="print-table">
            <tbody>
              <tr>
                <td class="print-label">发起人</td>
                <td>{{ printData.processInstance?.startUserName || '-' }}</td>
                <td class="print-label">发起时间</td>
                <td>{{ formatDate(printData.processInstance?.startTime) }}</td>
              </tr>
              <tr>
                <td class="print-label">所属部门</td>
                <td>{{ printData.processInstance?.startUserDeptName || '-' }}</td>
                <td class="print-label">流程状态</td>
                <td>{{ statusText(printData.processInstance?.status) }}</td>
              </tr>
              <tr>
                <td class="print-section" colspan="4">表单内容</td>
              </tr>
              <tr v-for="field in formFields" :key="field.label">
                <td class="print-label">{{ field.label }}</td>
                <td colspan="3">{{ field.value }}</td>
              </tr>
              <tr v-if="!formFields.length">
                <td class="print-section" colspan="4">该流程未提交表单变量</td>
              </tr>
            </tbody>
          </table>
          <table class="print-table">
            <tbody>
              <tr>
                <td class="print-section" colspan="4">流程记录</td>
              </tr>
              <tr v-for="task in printTasks" :key="task.id">
                <td class="print-label">{{ task.name }}</td>
                <td colspan="3">
                  <div>{{ task.description }}</div>
                  <img v-if="task.signPicUrl" class="print-sign" :src="task.signPicUrl" alt="审批签名" />
                </td>
              </tr>
              <tr v-if="!printTasks.length">
                <td class="print-section" colspan="4">暂无流程记录</td>
              </tr>
            </tbody>
          </table>
        </template>
      </div>
      <a-empty v-else-if="!loading" description="未获取到打印数据" />
    </a-spin>
    <div class="print-footer">
      <a-button @click="handleClose">取消</a-button>
      <a-button type="primary" :disabled="!printData" @click="handlePrint">打印</a-button>
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
  import { computed, ref, watch } from 'vue';
  import { getProcessDefinition, type BpmProcessDefinition } from '/@/api/bpm/definition';
  import { useUserStore } from '/@/store/modules/user';
  import { printNb } from '/@/hooks/web/usePrintJS';
  import { getPrintData } from '/@/views/workflow/workflow.api';

  interface PrintTask {
    id: string;
    name: string;
    description: string;
    signPicUrl?: string;
  }

  interface PrintData {
    printTemplateEnable?: boolean;
    printTemplateHtml?: string;
    processInstance?: {
      id?: string;
      name?: string;
      businessKey?: string;
      processDefinitionId?: string;
      startUserName?: string;
      startUserDeptName?: string;
      startTime?: string;
      endTime?: string;
      status?: string;
      formVariables?: Record<string, unknown>;
    };
    tasks?: any[];
  }

  const props = defineProps<{ open: boolean; processInstanceId?: string }>();
  const emit = defineEmits(['update:open']);

  const userStore = useUserStore();
  const loading = ref(false);
  const printData = ref<PrintData>();
  const definition = ref<BpmProcessDefinition>();

  const open = computed({
    get: () => props.open,
    set: (value: boolean) => emit('update:open', value),
  });
  const currentUserName = computed(() => userStore.userInfo?.realname || userStore.userInfo?.username || '-');

  // 表单字段按流程模型设计顺序解析，保证打印出的申请单顺序与设计器一致
  const configuredFields = computed(() => {
    const fields: { field: string; title: string }[] = [];
    (definition.value?.formFields || []).forEach((item) => {
      try {
        const rule = JSON.parse(item);
        if (rule?.field) fields.push({ field: String(rule.field), title: String(rule.title || rule.field) });
      } catch {
        // 忽略无法解析的表单字段定义，退回使用字段标识展示
      }
    });
    return fields;
  });

  const fieldLabels = computed(() => {
    const labels: Record<string, string> = {};
    configuredFields.value.forEach((item) => {
      labels[item.field] = item.title;
    });
    return labels;
  });

  const formFields = computed(() => {
    const variables = printData.value?.processInstance?.formVariables || {};
    const rows: { label: string; value: string }[] = [];
    const seen = new Set<string>();
    configuredFields.value.forEach((item) => {
      if (seen.has(item.field)) return;
      seen.add(item.field);
      const value = variables[item.field];
      if (value === undefined || value === null || value === '') return;
      rows.push({ label: item.title, value: formatValue(value) });
    });
    Object.entries(variables).forEach(([key, value]) => {
      if (seen.has(key) || value === undefined || value === null || value === '') return;
      rows.push({ label: fieldLabels.value[key] || key, value: formatValue(value) });
    });
    return rows;
  });

  const printTasks = computed<PrintTask[]>(() =>
    (printData.value?.tasks || []).map((task) => ({
      id: String(task.id),
      name: task.name || '-',
      description: [
        task.assigneeName || task.assignee || '待认领',
        task.name,
        task.endTime ? formatDate(task.endTime) : '处理中',
        taskStatusText(task.status, task.resultStatus),
        task.reason || '',
      ]
        .filter((item) => item !== undefined && item !== null && item !== '')
        .join(' / '),
      signPicUrl: task.signPicUrl,
    }))
  );

  const customTemplateHtml = computed(() => renderCustomTemplate(printData.value?.printTemplateHtml || ''));

  function renderCustomTemplate(template: string) {
    if (!template) return '';
    const replacements: Record<string, string> = {
      processName: printData.value?.processInstance?.name || '',
      processNum: printData.value?.processInstance?.businessKey || printData.value?.processInstance?.id || '',
      startUser: printData.value?.processInstance?.startUserName || '',
      startUserDept: printData.value?.processInstance?.startUserDeptName || '',
      startTime: formatDate(printData.value?.processInstance?.startTime),
      endTime: formatDate(printData.value?.processInstance?.endTime),
      processStatus: statusText(printData.value?.processInstance?.status),
      printUser: currentUserName.value,
      printTime: formatDate(new Date()),
    };

    // 芋道打印模板由 WangEditor mention 节点保存，先替换 mention，再处理兼容的 ${field} 占位符。
    const parser = new DOMParser();
    const doc = parser.parseFromString(sanitizeTemplate(template), 'text/html');
    doc.querySelectorAll('[data-w-e-type="mention"]').forEach((element) => {
      const htmlElement = element as HTMLElement;
      let key = '';
      try {
        key = JSON.parse(decodeURIComponent(htmlElement.dataset.info || '')).id || '';
      } catch {
        key = htmlElement.dataset.value || '';
      }
      htmlElement.textContent = resolveTemplateValue(key, replacements, '', false);
    });
    doc.querySelectorAll('[data-w-e-type="process-record"]').forEach((element) => {
      element.innerHTML = processRecordHtml();
    });

    return doc.body.innerHTML.replace(/\$\{\s*([\w.]+)\s*\}/g, (match, key: string) => {
      return resolveTemplateValue(key, replacements, match);
    });
  }

  function resolveTemplateValue(key: string, replacements: Record<string, string>, fallback = '', escape = true) {
    const aliases: Record<string, string> = {
      流程名称: 'processName',
      流程编号: 'processNum',
      发起人: 'startUser',
      发起人部门: 'startUserDept',
      发起时间: 'startTime',
      结束时间: 'endTime',
      流程状态: 'processStatus',
      打印人员: 'printUser',
      打印时间: 'printTime',
    };
    const normalizedKey = aliases[key] || key;
    if (normalizedKey in replacements) return escape ? escapeHtml(replacements[normalizedKey]) : replacements[normalizedKey];
    const formValue = printData.value?.processInstance?.formVariables?.[normalizedKey];
    if (formValue !== undefined && formValue !== null && formValue !== '') {
      const formatted = formatValue(formValue);
      return escape ? escapeHtml(formatted) : formatted;
    }
    return fallback;
  }

  function processRecordHtml() {
    const rows = printTasks.value
      .map((task) => `<tr><td class="border border-black p-1.5">${escapeHtml(task.name)}</td><td class="border border-black p-1.5">${escapeHtml(task.description)}</td></tr>`)
      .join('');
    return `<table class="w-full border-collapse"><tbody><tr><td colspan="2" class="border border-black p-1.5 text-center">流程记录</td></tr>${rows}</tbody></table>`;
  }

  function sanitizeTemplate(template: string) {
    return template
      .replace(/<\s*(script|iframe|object|embed|link|style)\b[^>]*>[\s\S]*?<\s*\/\s*\1\s*>/gi, '')
      .replace(/<\s*(script|iframe|object|embed|link|style)\b[^>]*\/?>/gi, '')
      .replace(/\son[a-z]+\s*=\s*("[^"]*"|'[^']*'|[^\s>]+)/gi, '')
      .replace(/(href|src)\s*=\s*("|')\s*javascript:[^"']*\2/gi, '$1=$2#$2');
  }

  function escapeHtml(value: string) {
    return value
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function formatValue(value: unknown) {
    if (Array.isArray(value)) return value.map((item) => formatValue(item)).join('、');
    if (typeof value === 'boolean') return value ? '是' : '否';
    if (value && typeof value === 'object') return JSON.stringify(value);
    return String(value ?? '-');
  }

  function formatDate(value?: string | Date | null) {
    if (!value) return '-';
    const date = value instanceof Date ? value : new Date(value);
    if (Number.isNaN(date.getTime())) return String(value);
    const pad = (input: number) => String(input).padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(
      date.getMinutes()
    )}:${pad(date.getSeconds())}`;
  }

  function statusText(status?: string) {
    return ({ RUNNING: '审批中', COMPLETED: '审批通过', REJECTED: '审批不通过', CANCELED: '已取消' } as Record<string, string>)[
      status || ''
    ] || status || '-';
  }

  function taskStatusText(status?: string, resultStatus?: number) {
    return (
      ({ WAIT: '待处理', APPROVING: '审批中', APPROVED: '已通过', REJECTED: '已拒绝', CANCELED: '已取消' } as Record<string, string>)[
        status || ''
      ] ||
      ({ 1: '待处理', 2: '已通过', 3: '已拒绝', 7: '审批中' } as Record<number, string>)[resultStatus || 0] ||
      '-'
    );
  }

  async function loadPrintData() {
    if (!props.processInstanceId) return;
    loading.value = true;
    try {
      printData.value = (await getPrintData(props.processInstanceId)) as PrintData;
      const processDefinitionId = printData.value?.processInstance?.processDefinitionId;
      definition.value = processDefinitionId ? await getProcessDefinition(String(processDefinitionId)) : undefined;
    } finally {
      loading.value = false;
    }
  }

  function handleClose() {
    open.value = false;
  }

  function handlePrint() {
    printNb('bpmPrintContainer');
  }

  watch(
    () => [props.open, props.processInstanceId],
    async ([visible]) => {
      if (!visible) return;
      printData.value = undefined;
      definition.value = undefined;
      await loadPrintData();
    },
    { immediate: true }
  );
</script>

<style scoped lang="less">
  .bpm-print-container {
    color: @text-color;

    .print-title {
      margin-bottom: 12px;
      font-size: 18px;
      font-weight: 600;
      text-align: center;
    }

    .print-meta {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      font-size: 12px;
    }

    .print-table {
      width: 100%;
      margin-top: 12px;
      border-collapse: collapse;
      table-layout: fixed;

      td {
        padding: 6px 8px;
        word-break: break-all;
        border: 1px solid @border-color-base;
      }

      .print-label {
        width: 20%;
        font-weight: 500;
        background: @background-color-light;
      }

      .print-section {
        font-weight: 600;
        text-align: center;
      }

      .print-sign {
        height: 40px;
        margin-top: 4px;
      }
    }
  }

  .print-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    margin-top: 16px;
  }
</style>
