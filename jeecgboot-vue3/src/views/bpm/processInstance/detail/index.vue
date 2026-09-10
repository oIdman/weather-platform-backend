<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="process-detail-page">
    <a-spin :spinning="loading">
      <a-page-header title="流程详情" :sub-title="detail?.processInstance?.name" @back="router.back()">
        <template #extra><a-button @click="printPage">打印</a-button></template>
      </a-page-header>
      <a-row v-if="detail?.processInstance" :gutter="16">
        <a-col :xs="24" :xl="17">
          <a-card title="申请信息" :bordered="false">
            <a-descriptions :column="2" bordered size="small">
              <a-descriptions-item label="流程名称">{{ detail.processInstance.processDefinitionName || '-' }}</a-descriptions-item>
              <a-descriptions-item label="实例状态"
                ><a-tag :color="statusColor(detail.processInstance.status)">{{
                  statusText(detail.processInstance.status)
                }}</a-tag></a-descriptions-item
              >
              <a-descriptions-item label="实例编号">{{ detail.processInstance.id }}</a-descriptions-item>
              <a-descriptions-item label="业务标识">{{ detail.processInstance.businessKey || '-' }}</a-descriptions-item>
              <a-descriptions-item label="发起人">{{ detail.processInstance.startUserName || detail.processInstance.startUserId || '-' }}</a-descriptions-item>
              <a-descriptions-item label="发起时间">{{ formatDate(detail.processInstance.startTime) }}</a-descriptions-item>
            </a-descriptions>
          </a-card>

          <a-card title="表单数据" :bordered="false" class="section-card">
            <div v-if="formRules.length" class="readonly-form">
              <FormCreate v-model="detailFormValue" :option="formOption" :rule="formRules" />
            </div>
            <a-descriptions v-else-if="variableEntries.length" :column="2" bordered size="small">
              <a-descriptions-item v-for="entry in variableEntries" :key="entry[0]" :label="entry[0]">{{
                displayValue(entry[1])
              }}</a-descriptions-item>
            </a-descriptions>
            <a-empty v-else description="该流程未提交表单变量" />
          </a-card>

          <a-card title="审批进度" :bordered="false" class="section-card">
            <a-timeline v-if="detail.activityNodes?.length">
              <a-timeline-item v-for="node in detail.activityNodes" :key="node.id" :color="nodeStatusColor(node.status)">
                <div class="timeline-title">
                  <span>{{ node.name }}</span>
                  <a-tag :color="nodeStatusTagColor(node.status)">{{ nodeStatusText(node.status) }}</a-tag>
                </div>
                <div v-if="node.startTime" class="timeline-meta">
                  {{ formatDate(node.startTime) }}<template v-if="node.endTime"> 至 {{ formatDate(node.endTime) }}</template>
                </div>
                <div v-if="!node.tasks?.length && node.candidateUsers?.length" class="timeline-meta">
                  候选人：{{ node.candidateUsers.map((user) => user.nickname || user.username).join('、') }}
                </div>
                <div v-for="task in node.tasks" :key="task.id" class="activity-task">
                  <div class="timeline-meta">
                    处理人：{{ task.assigneeName || task.assignee || task.ownerName || task.owner || '待认领' }} · {{
                      taskStatusText(task.status, task.resultStatus)
                    }}
                  </div>
                  <div v-if="task.reason" class="timeline-meta">处理意见：{{ task.reason }}</div>
                  <a-image
                    v-if="task.signPicUrl && safeEvidenceUrl(task.signPicUrl)"
                    class="task-signature"
                    :width="120"
                    :src="safeEvidenceUrl(task.signPicUrl)"
                    alt="审批签名"
                  />
                  <div v-if="task.attachments?.length" class="task-attachments">
                    <span>附件：</span>
                    <a
                      v-for="(attachment, index) in task.attachments"
                      v-show="safeEvidenceUrl(attachment)"
                      :key="`${task.id}-attachment-${index}`"
                      :href="safeEvidenceUrl(attachment)"
                      target="_blank"
                      rel="noopener noreferrer"
                      >附件 {{ index + 1 }}</a
                    >
                  </div>
                </div>
              </a-timeline-item>
            </a-timeline>
            <a-empty v-else description="流程尚未产生审批任务" />
          </a-card>

          <a-card title="流程图数据" :bordered="false" class="section-card">
            <div v-if="detail.activityNodes?.length" class="node-strip">
              <template v-for="(node, index) in detail.activityNodes" :key="`node-${node.id}`">
                <div v-if="index" class="node-arrow">→</div>
                <div class="node-box" :class="nodeStatusClass(node.status)">{{ node.name }}</div>
              </template>
            </div>
            <a-collapse ghost class="source-collapse">
              <a-collapse-panel key="bpmn" header="查看 BPMN XML">
                <a-textarea :value="detail.bpmnModelView?.bpmnXml" readonly :rows="16" class="code-area" />
              </a-collapse-panel>
            </a-collapse>
          </a-card>
        </a-col>

        <a-col :xs="24" :xl="7">
          <a-card
            v-if="detail.todoTask && detail.todoTask.resultStatus !== 7 && detail.todoTask.resultStatus !== 0"
            title="审批操作"
            :bordered="false"
            class="operation-card"
          >
            <a-space wrap>
              <a-button v-if="canAction(1)" type="primary" @click="openAction('approve')">{{ buttonLabel(1, '通过') }}</a-button>
              <a-button v-if="canAction(2)" danger @click="openAction('reject')">{{ buttonLabel(2, '拒绝') }}</a-button>
              <a-button v-if="canAction(6) && !detail.todoTask.parentTaskId" @click="openAction('return')">{{ buttonLabel(6, '退回') }}</a-button>
              <a-button v-if="canAction(3)" @click="openAction('transfer')">{{ buttonLabel(3, '转办') }}</a-button>
              <a-button v-if="canAction(4)" @click="openAction('delegate')">{{ buttonLabel(4, '委派') }}</a-button>
              <a-button v-if="canAction(5)" @click="openAction('sign')">{{ buttonLabel(5, '加签') }}</a-button>
              <a-button v-if="canAction(7)" @click="openAction('copy')">抄送</a-button>
            </a-space>
          </a-card>

          <a-alert
            v-if="detail.todoTask?.resultStatus === 7"
            class="section-card"
            type="info"
            show-icon
            message="原审批任务已通过，正在等待后加签任务全部完成"
          />
          <a-card v-if="detail.todoTask?.children?.length" title="加签任务" :bordered="false" class="section-card">
            <a-list :data-source="detail.todoTask.children" size="small">
              <template #renderItem="{ item }">
                <a-list-item>
                  <template #actions>
                    <a-button danger type="link" @click="openDeleteSign(item.id)">减签</a-button>
                  </template>
                  <a-list-item-meta
                    :title="item.name"
                    :description="`${item.assigneeName || item.assignee || item.ownerName || item.owner || '待分配'} · ${taskStatusText(item.status, item.resultStatus)}`"
                  />
                </a-list-item>
              </template>
            </a-list>
          </a-card>
          <a-card
            title="流程评论"
            :bordered="false"
            :class="['section-card', { 'operation-card': !detail.todoTask || detail.todoTask.resultStatus === 7 }]"
          >
            <a-list :data-source="comments" size="small" class="comment-list">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta :description="`${commentTypeText(item.type)} · ${formatDate(item.createTime)}`">
                    <template #title
                      >{{ item.user?.nickname || '流程操作' }}<span v-if="item.task?.name">（{{ item.task.name }}）</span></template
                    >
                  </a-list-item-meta>
                  <div class="comment-message">{{ item.message || '-' }}</div>
                </a-list-item>
              </template>
            </a-list>
            <a-empty v-if="!comments.length" description="暂无评论" :image-style="{ height: '48px' }" />
            <template v-if="detail.todoTask && detail.todoTask.resultStatus !== 0 && detail.todoTask.resultStatus !== 7">
              <a-divider />
              <a-textarea v-model:value="commentMessage" :rows="3" :maxlength="500" placeholder="输入评论内容" />
              <a-button class="comment-button" type="primary" :loading="commenting" @click="submitComment">发表评论</a-button>
            </template>
          </a-card>
        </a-col>
      </a-row>
    </a-spin>

    <a-modal v-model:open="actionVisible" :title="actionTitle" :confirm-loading="submitting" width="620px" @ok="submitAction">
      <a-form layout="vertical">
        <a-form-item v-if="actionType === 'return'" label="退回节点" required>
          <a-select v-model:value="targetTaskDefinitionKey" :options="returnOptions" placeholder="请选择历史节点" />
        </a-form-item>
        <a-form-item
          v-if="actionType === 'delegate' || actionType === 'transfer' || actionType === 'sign' || actionType === 'copy'"
          :label="userFieldLabel"
          required
        >
          <JSelectUser
            v-model:value="selectedUsers"
            row-key="id"
            label-key="realname"
            :multiple="actionType === 'sign' || actionType === 'copy' ? 'multiple' : ''"
          />
        </a-form-item>
        <a-form-item v-if="actionType === 'sign'" label="加签方式" required>
          <a-radio-group v-model:value="signType"><a-radio value="before">前加签</a-radio><a-radio value="after">后加签</a-radio></a-radio-group>
        </a-form-item>
        <template v-if="actionType === 'approve'">
          <a-form-item v-for="node in selectableNextNodes" :key="node.id" :label="`${node.name}审批人`" required>
            <JSelectUser v-model:value="nextAssignees[node.id]" row-key="id" label-key="realname" multiple="multiple" />
          </a-form-item>
        </template>
        <a-form-item label="处理意见" :required="actionType !== 'approve'">
          <a-textarea v-model:value="actionReason" :rows="4" :maxlength="500" placeholder="请输入处理意见" />
        </a-form-item>
        <template v-if="(actionType === 'approve' || actionType === 'reject') && detail?.signEnable">
          <a-form-item label="审批签名" required>
            <JImageUpload v-model:value="signPicUrl" biz-path="workflow/signature" :file-max="1" text="上传签名" />
          </a-form-item>
          <a-form-item label="审批附件">
            <JUpload v-model:value="attachmentUrls" biz-path="workflow/attachment" :max-count="10" text="上传附件" />
          </a-form-item>
        </template>
      </a-form>
    </a-modal>
  </div>
</template>

<script lang="ts" name="bpm-process-instance-detail" setup>
  import { computed, ref, watch } from 'vue';
  import formCreate, { type Rule } from '@form-create/ant-design-vue';
  import { useRoute, useRouter } from 'vue-router';
  import JSelectUser from '/@/components/Form/src/jeecg/components/JSelectUser.vue';
  import JImageUpload from '/@/components/Form/src/jeecg/components/JImageUpload.vue';
  import JUpload from '/@/components/Form/src/jeecg/components/JUpload/JUpload.vue';
  import { useMessage } from '/@/hooks/web/useMessage';
  import { getFileAccessHttpUrl } from '/@/utils/common/compUtils';
  import {
    approveTask,
    copyTask,
    createComment,
    createSignTask,
    deleteSignTask,
    delegateTask,
    getApprovalDetail,
    getCommentListByProcessInstanceId,
    getNextApprovalNodes,
    getTaskListByReturn,
    rejectTask,
    returnTask,
    transferTask,
    type WorkflowApprovalDetail,
    type WorkflowComment,
    type WorkflowActivityNode,
  } from '/@/views/workflow/workflow.api';

  type ActionType = 'approve' | 'reject' | 'return' | 'delegate' | 'transfer' | 'sign' | 'deleteSign' | 'copy';

  const route = useRoute();
  const router = useRouter();
  const { createMessage } = useMessage();
  const loading = ref(false);
  const submitting = ref(false);
  const commenting = ref(false);
  const detail = ref<WorkflowApprovalDetail>();
  const comments = ref<WorkflowComment[]>([]);
  const actionVisible = ref(false);
  const actionType = ref<ActionType>('approve');
  const actionReason = ref('');
  const signPicUrl = ref('');
  const attachmentUrls = ref('');
  const selectedUsers = ref<string[] | string>([]);
  const signType = ref<'before' | 'after'>('before');
  const signChildTaskId = ref('');
  const targetTaskDefinitionKey = ref('');
  const returnOptions = ref<{ label: string; value: string }[]>([]);
  const commentMessage = ref('');
  const formRules = ref<Rule[]>([]);
  const formOption = ref<Record<string, unknown>>({});
  const detailFormValue = ref<Record<string, unknown>>({});
  const nextApprovalNodes = ref<WorkflowActivityNode[]>([]);
  const nextAssignees = ref<Record<string, string[] | string>>({});

  const processInstanceId = computed(() => String(route.query.id || ''));
  const taskId = computed(() => (route.query.taskId ? String(route.query.taskId) : undefined));
  const variableEntries = computed(() => Object.entries(detail.value?.processInstance?.formVariables || {}));
  const actionTitleMap: Record<ActionType, string> = {
    approve: '通过任务',
    reject: '拒绝任务',
    return: '退回任务',
    delegate: '委派任务',
    transfer: '转办任务',
    sign: '加签任务',
    deleteSign: '减签任务',
    copy: '抄送任务',
  };
  const actionTitle = computed(() => actionTitleMap[actionType.value]);
  const userFieldLabel = computed(
    () => ({ delegate: '被委派人', transfer: '新审批人', sign: '加签用户', copy: '抄送用户' })[actionType.value] || '用户'
  );
  const selectableNextNodes = computed(() => nextApprovalNodes.value.filter((node) => node.candidateStrategy === 34 && !node.candidateUsers?.length));
  function canAction(buttonCode: number) {
    const enabled = detail.value?.enabledButtons;
    return !enabled || !enabled.length || enabled.includes(buttonCode);
  }
  function buttonLabel(buttonCode: number, fallback: string) {
    return detail.value?.buttonDisplayNames?.[buttonCode] || fallback;
  }

  async function loadDetail() {
    if (!processInstanceId.value) return;
    loading.value = true;
    try {
      const [approvalDetail, commentList] = await Promise.all([
        getApprovalDetail({ processInstanceId: processInstanceId.value, taskId: taskId.value }),
        getCommentListByProcessInstanceId(processInstanceId.value),
      ]);
      if (!approvalDetail.processInstance) {
        createMessage.error('审批详情缺少流程实例信息');
        return;
      }
      detail.value = approvalDetail;
      comments.value = commentList;
      detailFormValue.value = { ...(approvalDetail.processInstance.formVariables || {}) };
      const fieldPermissions = approvalDetail.formFieldsPermission || {};
      const rawRules = (approvalDetail.processDefinition?.formFields || []).map((item) => formCreate.parseJson(item));
      formRules.value = rawRules
        .filter((rule: any) => (fieldPermissions[String(rule.field || rule.title)] || 'READ') !== 'NONE')
        .map((rule: any) => {
          const permission = fieldPermissions[String(rule.field || rule.title)] || 'READ';
          return { ...rule, props: { ...(rule.props || {}), disabled: permission !== 'WRITE' } };
        });
      const option = approvalDetail.processDefinition?.formConf ? formCreate.parseJson(approvalDetail.processDefinition.formConf) : {};
      const hasWritableField = Object.values(fieldPermissions).includes('WRITE');
      formOption.value = { ...option, submitBtn: false, resetBtn: false, form: { ...(option.form || {}), disabled: !hasWritableField } };
    } finally {
      loading.value = false;
    }
  }

  async function openAction(type: ActionType) {
    actionType.value = type;
    actionReason.value = '';
    signPicUrl.value = '';
    attachmentUrls.value = '';
    selectedUsers.value = [];
    signType.value = 'before';
    signChildTaskId.value = '';
    targetTaskDefinitionKey.value = '';
    returnOptions.value = [];
    nextApprovalNodes.value = [];
    nextAssignees.value = {};
    if (type === 'return' && detail.value?.todoTask) {
      const tasks = await getTaskListByReturn(detail.value.todoTask.id);
      returnOptions.value = tasks.map((task) => ({ label: task.name, value: task.taskDefinitionKey! }));
      if (!returnOptions.value.length) {
        createMessage.warning('当前任务没有可退回的历史节点');
        return;
      }
    }
    if (type === 'approve' && detail.value?.todoTask) {
      nextApprovalNodes.value = await getNextApprovalNodes({
        processInstanceId: processInstanceId.value,
        taskId: detail.value.todoTask.id,
        processVariablesStr: JSON.stringify(detailFormValue.value),
      });
      selectableNextNodes.value.forEach((node) => {
        nextAssignees.value[node.id] = [];
      });
    }
    actionVisible.value = true;
  }

  async function openDeleteSign(taskId: string) {
    await openAction('deleteSign');
    signChildTaskId.value = taskId;
  }

  function normalizedUsers() {
    return (Array.isArray(selectedUsers.value) ? selectedUsers.value : [selectedUsers.value]).filter(Boolean);
  }

  async function submitAction() {
    const todo = detail.value?.todoTask;
    if (!todo) return;
    const users = normalizedUsers();
    if (['delegate', 'transfer', 'sign', 'copy'].includes(actionType.value) && !users.length) {
      createMessage.warning('请选择用户');
      return;
    }
    if (actionType.value !== 'approve' && !actionReason.value.trim()) {
      createMessage.warning('请输入处理意见');
      return;
    }
    if (actionType.value === 'approve' && detail.value?.reasonRequire && !actionReason.value.trim()) {
      createMessage.warning('该节点必须填写审批意见');
      return;
    }
    if (
      (actionType.value === 'approve' || actionType.value === 'reject') &&
      detail.value?.signEnable &&
      !signPicUrl.value
    ) {
      createMessage.warning('该节点必须上传审批签名');
      return;
    }
    if (actionType.value === 'return' && !targetTaskDefinitionKey.value) {
      createMessage.warning('请选择退回节点');
      return;
    }
    if (actionType.value === 'deleteSign' && !signChildTaskId.value) {
      createMessage.warning('请选择需要减签的任务');
      return;
    }
    if (
      actionType.value === 'approve' &&
      selectableNextNodes.value.some((node) => {
        const value = nextAssignees.value[node.id];
        return !value || (Array.isArray(value) && !value.length);
      })
    ) {
      createMessage.warning('请选择下一节点审批人');
      return;
    }
    submitting.value = true;
    try {
      const base = {
        id: todo.id,
        reason: actionReason.value.trim(),
        signPicUrl: signPicUrl.value || undefined,
        attachments: splitUploadUrls(attachmentUrls.value),
      };
      if (actionType.value === 'approve') {
        const selected = Object.fromEntries(
          Object.entries(nextAssignees.value).map(([nodeId, value]) => [nodeId, Array.isArray(value) ? value : [value].filter(Boolean)])
        );
        await approveTask({ ...base, variables: detailFormValue.value, nextAssignees: selected });
      }
      if (actionType.value === 'reject') await rejectTask(base);
      if (actionType.value === 'return') await returnTask({ ...base, targetTaskDefinitionKey: targetTaskDefinitionKey.value });
      if (actionType.value === 'delegate') await delegateTask({ ...base, delegateUserId: users[0] });
      if (actionType.value === 'transfer') await transferTask({ ...base, assigneeUserId: users[0] });
      if (actionType.value === 'sign') await createSignTask({ ...base, userIds: users, type: signType.value });
      if (actionType.value === 'deleteSign') await deleteSignTask({ id: signChildTaskId.value, reason: actionReason.value.trim() });
      if (actionType.value === 'copy') await copyTask({ ...base, copyUserIds: users });
      createMessage.success('操作成功');
      actionVisible.value = false;
      await loadDetail();
    } finally {
      submitting.value = false;
    }
  }

  async function submitComment() {
    if (!commentMessage.value.trim() || !detail.value?.todoTask) {
      createMessage.warning('请输入评论内容');
      return;
    }
    commenting.value = true;
    try {
      await createComment(detail.value.todoTask.id, commentMessage.value.trim());
      commentMessage.value = '';
      comments.value = await getCommentListByProcessInstanceId(processInstanceId.value);
    } finally {
      commenting.value = false;
    }
  }

  function displayValue(value: unknown) {
    return typeof value === 'object' ? JSON.stringify(value) : String(value ?? '-');
  }

  function splitUploadUrls(value: string) {
    return value
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean);
  }

  function safeEvidenceUrl(value: string) {
    const url = value.trim();
    if (url.startsWith('/') && !url.startsWith('//')) return url;
    try {
      const parsed = new URL(url);
      if (parsed.protocol === 'http:' || parsed.protocol === 'https:') return url;
    } catch {
      const hasUnsafeSegment = url.split('/').some((segment) => segment === '..');
      if (url && !url.includes(':') && !url.includes('\\') && !url.startsWith('//') && !hasUnsafeSegment) {
        return getFileAccessHttpUrl(url);
      }
    }
    return '';
  }

  function formatDate(value?: string | Date) {
    return value ? new Date(value).toLocaleString() : '-';
  }

  function statusText(status?: string) {
    return { RUNNING: '审批中', COMPLETED: '已完成', CANCELED: '已取消' }[status || ''] || status || '-';
  }

  function statusColor(status?: string) {
    return { RUNNING: 'processing', COMPLETED: 'success', CANCELED: 'error' }[status || ''] || 'default';
  }

  function taskStatusText(status?: string, resultStatus?: number) {
    const resultText = ({ 0: '等待中', 2: '已通过', 3: '未通过', 4: '已取消', 5: '已退回', 7: '通过中' } as Record<number, string>)[resultStatus ?? -1];
    const statusText = ({
      WAIT: '等待中',
      TODO: '处理中',
      DONE: '已完成',
      REJECTED: '未通过',
      CANCELED: '已取消',
      RETURNED: '已退回',
      APPROVING: '通过中',
    } as Record<string, string>)[status || ''];
    return resultText || statusText || status || '-';
  }

  function nodeStatusText(status?: number) {
    return (
      (
        { '-2': '已跳过', '-1': '未开始', 0: '等待中', 1: '审批中', 2: '已通过', 3: '未通过', 4: '已取消', 5: '已退回', 7: '通过中' } as Record<
          string,
          string
        >
      )[String(status)] || '-'
    );
  }

  function nodeStatusColor(status?: number) {
    if (status === 1 || status === 7) return 'blue';
    if (status === 2) return 'green';
    if (status === 3 || status === 4 || status === 5) return 'red';
    return 'gray';
  }

  function nodeStatusTagColor(status?: number) {
    if (status === 1 || status === 7) return 'processing';
    if (status === 2) return 'success';
    if (status === 3 || status === 4 || status === 5) return 'error';
    return 'default';
  }

  function nodeStatusClass(status?: number) {
    if (status === 1 || status === 7) return 'todo';
    if (status === 2) return 'completed';
    if (status === 3 || status === 4 || status === 5) return 'canceled';
    return '';
  }

  function commentTypeText(type?: string) {
    return (
      {
        approve: '通过',
        reject: '拒绝',
        return: '退回',
        delegate: '委派',
        transfer: '转办',
        addSign: '加签',
        deleteSign: '减签',
        copy: '抄送',
        withdraw: '撤回',
        cancel: '取消',
        comment: '评论',
      }[type || ''] || '流程记录'
    );
  }

  function printPage() {
    window.print();
  }

  watch([processInstanceId, taskId], loadDetail, { immediate: true });
</script>

<style lang="less" scoped>
  .process-detail-page {
    min-height: 100%;
    padding: 0 16px 16px;
    background: @background-color-light;

    .section-card {
      margin-top: 16px;
    }

    .operation-card {
      margin-top: 0;
    }

    .timeline-title {
      display: flex;
      align-items: center;
      justify-content: space-between;
      font-weight: 500;
    }

    .timeline-meta {
      margin-top: 4px;
      color: @text-color-secondary;
      font-size: 12px;
    }

    .activity-task {
      margin-top: 8px;
      padding: 8px 10px;
      border-radius: 4px;
      background: @background-color-light;
    }

    .node-strip {
      display: flex;
      overflow-x: auto;
      align-items: center;
      padding: 16px 4px;
    }

    .node-box {
      min-width: 96px;
      padding: 10px 14px;
      border: 1px solid @border-color-base;
      border-radius: 6px;
      background: #fff;
      text-align: center;

      &.completed,
      &.done {
        border-color: @success-color;
        background: fade(@success-color, 8%);
        color: @success-color;
      }

      &.todo {
        border-color: @primary-color;
        background: fade(@primary-color, 8%);
        color: @primary-color;
      }

      &.canceled {
        border-color: @error-color;
        color: @error-color;
      }
    }

    .node-arrow {
      margin: 0 8px;
      color: @text-color-secondary;
    }

    .source-collapse {
      margin-top: 8px;
    }

    .code-area {
      font-family: Consolas, Monaco, monospace;
      font-size: 12px;
    }

    .readonly-form {
      pointer-events: none;
    }

    .comment-list :deep(.ant-list-item) {
      display: block;
    }

    .comment-message {
      padding: 4px 0 8px;
      white-space: pre-wrap;
    }

    .comment-button {
      width: 100%;
      margin-top: 10px;
    }

    .task-signature {
      margin-top: 8px;
    }

    .task-attachments {
      display: flex;
      gap: 8px;
      margin-top: 6px;
      color: @text-color-secondary;
    }
  }

  @media print {
    .operation-card,
    .comment-button,
    .source-collapse,
    :deep(.ant-page-header-back),
    :deep(.ant-page-header-heading-extra) {
      display: none !important;
    }
  }
</style>
