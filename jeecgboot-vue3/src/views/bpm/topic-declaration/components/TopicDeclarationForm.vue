<template>
  <div class="topic-declaration-page">
    <a-card class="topic-header" :bordered="false">
      <div class="topic-header__top">
        <div>
          <div class="topic-title">课题在线申报填报系统</div>
          <div class="topic-subtitle">课题审批业务表单 · {{ readOnly ? '查看模式' : '填写模式' }}</div>
        </div>
        <a-tag color="blue">业务表单</a-tag>
      </div>
      <div class="topic-header__hint">
        <span>填写过程中可以随时保存草稿，提交后将进入配置好的审批流程。</span>
        <span>填报完整度：<strong>{{ completeness }}%</strong></span>
      </div>
      <a-progress :percent="completeness" :show-info="false" stroke-color="#34d399" />
    </a-card>

    <a-row :gutter="16" class="topic-content">
      <a-col :xs="24" :xl="18">
        <a-form ref="formRef" :model="form" layout="vertical" :disabled="readOnly" class="topic-form">
          <a-card title="01. 课题基本信息" class="topic-section" :bordered="false">
            <template #extra><a-tag color="blue">任务书基础信息</a-tag></template>
            <a-row :gutter="16">
              <a-col :xs="24" :md="16">
                <a-form-item label="对应课题指南" name="guideTitle" :rules="requiredRule">
                  <a-input v-model:value="form.guideTitle" placeholder="请输入或选择对应发布的课题指南" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="8">
                <a-form-item label="课题编号" name="code" :rules="requiredRule">
                  <a-input v-model:value="form.code" placeholder="如：2026CMA-ZP01" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="16">
                <a-form-item label="申报课题名称" name="projectName" :rules="requiredRule">
                  <a-input v-model:value="form.projectName" placeholder="请输入课题名称" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="8">
                <a-form-item label="课题密级" name="securityLevel">
                  <a-select v-model:value="form.securityLevel" placeholder="请选择">
                    <a-select-option value="公开级">公开级</a-select-option>
                    <a-select-option value="内部保密">内部保密</a-select-option>
                    <a-select-option value="商业秘密">商业秘密</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="8">
                <a-form-item label="指南方向 / 研究领域" name="category" :rules="requiredRule">
                  <a-select v-model:value="form.category" placeholder="请选择研究领域">
                    <a-select-option v-for="item in categories" :key="item" :value="item">{{ item }}</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="8">
                <a-form-item label="研究类型" name="researchType" :rules="requiredRule">
                  <a-select v-model:value="form.researchType" placeholder="请选择">
                    <a-select-option value="基础研究类">1. 基础研究类</a-select-option>
                    <a-select-option value="应用示范类">2. 应用示范类</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="8">
                <a-form-item label="课题活动类型" name="activityType" :rules="requiredRule">
                  <a-select v-model:value="form.activityType" placeholder="请选择">
                    <a-select-option value="定向课题">1. 定向课题</a-select-option>
                    <a-select-option value="非定向课题">2. 非定向课题</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="8">
                <a-form-item label="课题承担单位（乙方）" name="undertakingUnit" :rules="requiredRule">
                  <a-input v-model:value="form.undertakingUnit" placeholder="请输入承担单位" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="8">
                <a-form-item label="统一社会信用代码 / 组织机构代码" name="undertakingUnitCode">
                  <a-input v-model:value="form.undertakingUnitCode" placeholder="可留空" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="8">
                <a-form-item label="课题组织单位（甲方）" name="organizingUnit">
                  <a-input v-model:value="form.organizingUnit" placeholder="请输入组织单位" />
                </a-form-item>
              </a-col>
              <a-col :xs="24" :md="12">
                <a-form-item label="课题起止年限" name="duration" :rules="requiredRule">
                  <a-range-picker v-model:value="form.duration" picker="month" value-format="YYYY-MM" style="width: 100%" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-card>

          <a-card title="02. 课题研究目标与核心研究内容" class="topic-section" :bordered="false">
            <template #extra><a-tag color="purple">核心条款</a-tag></template>
            <a-form-item label="1. 课题研究目标" name="researchObjectives" :rules="requiredRule">
              <a-textarea v-model:value="form.researchObjectives" :rows="4" placeholder="明确本课题拟达到的业务与技术目标" />
            </a-form-item>
            <a-form-item label="2. 主要研究内容与研发方案" name="summary" :rules="requiredRule">
              <a-textarea v-model:value="form.summary" :rows="4" placeholder="阐述主要研究内容、技术方案与业务功能规划" />
            </a-form-item>
            <a-form-item label="3. 主要技术难点与解决途径" name="techDifficultiesAndSolutions">
              <a-textarea v-model:value="form.techDifficultiesAndSolutions" :rows="3" placeholder="阐述核心技术难题及解决途径" />
            </a-form-item>
            <a-form-item label="4. 技术路线与创新突破点" name="techRouteAndInnovations">
              <a-textarea v-model:value="form.techRouteAndInnovations" :rows="3" placeholder="阐述技术路线和关键创新点" />
            </a-form-item>
          </a-card>

          <a-card title="03. 科研团队配置与责任人信息" class="topic-section" :bordered="false">
            <template #extra>
              <a-button v-if="!readOnly" type="primary" ghost size="small" @click="addMember">添加团队成员</a-button>
            </template>
            <div class="member-summary">课题团队人员列表 <span>共 {{ form.members.length }} 人</span></div>
            <div v-for="(member, index) in form.members" :key="member.id" class="member-row">
              <a-avatar :size="36" class="member-avatar">{{ member.name?.slice(0, 1) || '新' }}</a-avatar>
              <div class="member-fields">
                <a-input v-model:value="member.name" placeholder="姓名" />
                <a-input v-model:value="member.role" placeholder="课题角色" />
                <a-input v-model:value="member.title" placeholder="职称 / 职务" />
                <a-input v-model:value="member.org" placeholder="所在单位" />
                <a-input v-model:value="member.researchField" placeholder="主要研究方向" />
              </div>
              <a-button v-if="!readOnly && form.members.length > 1" type="link" danger @click="removeMember(index)">移除</a-button>
            </div>
          </a-card>

          <a-card title="04. 预期成果与量化考核指标" class="topic-section" :bordered="false">
            <template #extra><a-tag color="green">成果指标</a-tag></template>
            <a-row :gutter="16">
              <a-col :xs="24" :md="12">
                <a-form-item label="落地产品 / 系统交付物" name="deliverableProducts" :rules="requiredRule">
                  <a-input v-model:value="form.deliverableProducts" placeholder="请输入交付物" />
                </a-form-item>
              </a-col>
              <a-col :xs="12" :md="6"><a-form-item label="预期发明专利（项）"><a-input-number v-model:value="form.patentsInventionCount" :min="0" style="width: 100%" /></a-form-item></a-col>
              <a-col :xs="12" :md="6"><a-form-item label="预期软件著作权（项）"><a-input-number v-model:value="form.softwareCopyrightsCount" :min="0" style="width: 100%" /></a-form-item></a-col>
              <a-col :xs="12" :md="6"><a-form-item label="预期技术标准（项）"><a-input-number v-model:value="form.techStandardsCount" :min="0" style="width: 100%" /></a-form-item></a-col>
              <a-col :xs="12" :md="6"><a-form-item label="预期实用新型专利（项）"><a-input-number v-model:value="form.patentsUtilityCount" :min="0" style="width: 100%" /></a-form-item></a-col>
              <a-col :xs="24" :md="12"><a-form-item label="预期经济效益与社会效益" name="economicSocialBenefits" :rules="requiredRule"><a-textarea v-model:value="form.economicSocialBenefits" :rows="3" placeholder="请输入预期效益" /></a-form-item></a-col>
              <a-col :xs="24" :md="12"><a-form-item label="申请经费（万元）" name="budget" :rules="requiredRule"><a-input-number v-model:value="form.budget" :min="0" :precision="2" style="width: 100%" /></a-form-item></a-col>
            </a-row>
          </a-card>

          <a-card title="05. 课题进度计划与阶段里程碑" class="topic-section" :bordered="false">
            <template #extra><a-button v-if="!readOnly" type="primary" ghost size="small" @click="addPhase">新增阶段</a-button></template>
            <div v-for="(phase, index) in form.schedulePhases" :key="phase.id" class="phase-row">
              <div class="phase-title">阶段 {{ index + 1 }}</div>
              <a-input v-model:value="phase.phaseName" placeholder="阶段名称" />
              <a-range-picker v-model:value="phase.period" picker="month" value-format="YYYY-MM" />
              <a-textarea v-model:value="phase.deliverable" :rows="1" placeholder="阶段目标 / 里程碑" />
              <a-button v-if="!readOnly && form.schedulePhases.length > 1" type="link" danger @click="removePhase(index)">删除</a-button>
            </div>
          </a-card>
        </a-form>
      </a-col>

      <a-col :xs="24" :xl="6">
        <a-card title="审批路径预览" :bordered="false" class="approval-card">
          <a-spin :spinning="previewLoading">
            <a-steps v-if="activityNodes.length" direction="vertical" size="small" :current="-1">
              <a-step v-for="node in activityNodes" :key="node.id" :title="node.name" :description="candidateDescription(node)" />
            </a-steps>
            <a-empty v-else description="暂无审批路径" />
            <template v-if="startUserSelectTasks.length && !readOnly">
              <a-divider orientation="left">选择审批人</a-divider>
              <a-form layout="vertical">
                <a-form-item v-for="node in startUserSelectTasks" :key="node.id" :label="node.name" required>
                  <JSelectUser v-model:value="startUserSelectAssignees[node.id]" row-key="id" label-key="realname" multiple="multiple" :placeholder="`请选择${node.name}的审批人`" />
                </a-form-item>
              </a-form>
            </template>
          </a-spin>
        </a-card>
      </a-col>
    </a-row>

    <div v-if="!readOnly" class="topic-actions">
      <a-button @click="resetForm">重置</a-button>
      <a-button type="primary" :loading="submitting" @click="submitForm">提交审批</a-button>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { computed, onMounted, reactive, ref, watch } from 'vue';
  import { useRoute, useRouter } from 'vue-router';
  import { useMessage } from '/@/hooks/web/useMessage';
  import JSelectUser from '/@/components/Form/src/jeecg/components/JSelectUser.vue';
  import { getApprovalDetail, startProcess, type WorkflowActivityNode } from '/@/views/workflow/workflow.api';

  interface Member {
    id: string;
    name: string;
    role: string;
    title: string;
    org: string;
    researchField: string;
  }

  interface Phase {
    id: string;
    phaseName: string;
    period: string[];
    deliverable: string;
  }

  interface TopicForm {
    guideTitle: string;
    code: string;
    projectName: string;
    securityLevel: string;
    category: string;
    researchType: string;
    activityType: string;
    undertakingUnit: string;
    undertakingUnitCode: string;
    organizingUnit: string;
    duration: string[];
    researchObjectives: string;
    summary: string;
    techDifficultiesAndSolutions: string;
    techRouteAndInnovations: string;
    members: Member[];
    deliverableProducts: string;
    patentsInventionCount: number;
    softwareCopyrightsCount: number;
    techStandardsCount: number;
    patentsUtilityCount: number;
    economicSocialBenefits: string;
    budget: number;
    schedulePhases: Phase[];
  }

  const props = withDefaults(defineProps<{ readOnly?: boolean; initialData?: Partial<TopicForm> }>(), { readOnly: false });
  const route = useRoute();
  const router = useRouter();
  const { createMessage } = useMessage();
  const formRef = ref();
  const submitting = ref(false);
  const previewLoading = ref(false);
  const activityNodes = ref<WorkflowActivityNode[]>([]);
  const startUserSelectTasks = ref<WorkflowActivityNode[]>([]);
  const startUserSelectAssignees = ref<Record<string, string[]>>({});
  const categories = ['数值预报与同化', '天气气候机理', '卫星遥感应用', '平台软件', '气象AI大模型与智能计算'];
  const requiredRule = [{ required: true, message: '该项不能为空' }];
  const createId = () => `${Date.now()}_${Math.random().toString(36).slice(2)}`;

  const emptyForm = (): TopicForm => ({
    guideTitle: '2026年度国家气象局全球数值天气预报模式核心算法重点课题指南',
    code: '',
    projectName: '',
    securityLevel: '公开级',
    category: '数值预报与同化',
    researchType: '基础研究类',
    activityType: '定向课题',
    undertakingUnit: '',
    undertakingUnitCode: '',
    organizingUnit: '',
    duration: [],
    researchObjectives: '',
    summary: '',
    techDifficultiesAndSolutions: '',
    techRouteAndInnovations: '',
    members: [{ id: createId(), name: '', role: '项目负责人', title: '', org: '', researchField: '' }],
    deliverableProducts: '',
    patentsInventionCount: 0,
    softwareCopyrightsCount: 0,
    techStandardsCount: 0,
    patentsUtilityCount: 0,
    economicSocialBenefits: '',
    budget: 0,
    schedulePhases: [{ id: createId(), phaseName: '立项与方案制定', period: [], deliverable: '' }],
  });

  const form = reactive<TopicForm>({ ...emptyForm(), ...(props.initialData || {}) });
  const processDefinitionId = computed(() => String(route.query.processDefinitionId || ''));
  const processDefinitionKey = computed(() => String(route.query.processDefinitionKey || ''));
  const completeness = computed(() => {
    const fields = [
      form.code,
      form.projectName,
      form.category,
      form.researchType,
      form.activityType,
      form.undertakingUnit,
      form.duration.length,
      form.researchObjectives,
      form.summary,
      form.deliverableProducts,
      form.economicSocialBenefits,
      form.budget,
    ];
    return Math.round((fields.filter((value) => value !== '' && value !== 0 && value !== undefined && (!Array.isArray(value) || value.length)).length / fields.length) * 100);
  });

  watch(
    () => props.initialData,
    (value) => {
      if (value && Object.keys(value).length) Object.assign(form, value);
    },
    { deep: true }
  );

  function addMember() {
    form.members.push({ id: createId(), name: '', role: '', title: '', org: '', researchField: '' });
  }
  function removeMember(index: number) {
    form.members.splice(index, 1);
  }
  function addPhase() {
    form.schedulePhases.push({ id: createId(), phaseName: '', period: [], deliverable: '' });
  }
  function removePhase(index: number) {
    form.schedulePhases.splice(index, 1);
  }
  function resetForm() {
    Object.assign(form, emptyForm());
  }
  function candidateDescription(node: WorkflowActivityNode) {
    if (node.nodeType === 10) return '当前登录用户';
    if (node.status === -2) return '满足跳过条件时自动跳过';
    if (node.candidateStrategy === 35) return '审批人由发起人选择';
    const names = (node.candidateUsers || []).map((user) => user.nickname || user.username).filter(Boolean);
    return names.length ? `预计审批人：${names.join('、')}` : '审批人将在流程运行时确定';
  }
  async function loadApprovalPreview() {
    if (!processDefinitionId.value || props.readOnly) return;
    previewLoading.value = true;
    try {
      const detail = await getApprovalDetail({
        processDefinitionId: processDefinitionId.value,
        activityId: 'StartUserNode',
        processVariablesStr: JSON.stringify({ ...form }),
      });
      activityNodes.value = detail.activityNodes || [];
      startUserSelectTasks.value = activityNodes.value.filter((node) => node.candidateStrategy === 35);
      startUserSelectAssignees.value = Object.fromEntries(startUserSelectTasks.value.map((node) => [node.id, startUserSelectAssignees.value[node.id] || []]));
    } catch (error) {
      console.warn('加载审批路径失败', error);
    } finally {
      previewLoading.value = false;
    }
  }
  async function submitForm() {
    if (!processDefinitionId.value && !processDefinitionKey.value) {
      createMessage.error('缺少流程定义参数，请从“发起流程”页面打开此表单');
      return;
    }
    try {
      await formRef.value?.validate();
      await loadApprovalPreview();
      for (const node of startUserSelectTasks.value) {
        if (!startUserSelectAssignees.value[node.id]?.length) {
          createMessage.warning(`请选择${node.name}的审批人`);
          return;
        }
      }
      submitting.value = true;
      await startProcess({
        processDefinitionId: processDefinitionId.value || undefined,
        processDefinitionKey: processDefinitionKey.value || undefined,
        name: form.projectName,
        variables: { ...form, topicApplication: { ...form } },
        startUserSelectAssignees: startUserSelectAssignees.value,
      });
      createMessage.success('课题审批流程发起成功');
      await router.push('/bpm/task/my');
    } catch (error) {
      console.error(error);
    } finally {
      submitting.value = false;
    }
  }

  onMounted(loadApprovalPreview);
</script>

<style lang="less" scoped>
  .topic-declaration-page { padding: 16px; background: #f5f7fb; min-height: 100%; }
  .topic-header { color: #fff; background: linear-gradient(120deg, #172554, #312e81 55%, #0f172a); border-radius: 8px; }
  .topic-header__top, .topic-header__hint { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
  .topic-header__hint { margin-top: 16px; color: #bfdbfe; font-size: 12px; }
  .topic-title { font-size: 20px; font-weight: 700; }
  .topic-subtitle { margin-top: 4px; color: #bfdbfe; font-size: 12px; }
  .topic-content { margin-top: 16px; }
  .topic-section, .approval-card { margin-bottom: 16px; border-radius: 8px; }
  .topic-section :deep(.ant-card-head-title) { font-weight: 700; }
  .topic-form :deep(.ant-form-item) { margin-bottom: 14px; }
  .member-summary { padding: 10px 12px; margin-bottom: 12px; color: #334155; background: #eff6ff; border: 1px solid #dbeafe; border-radius: 6px; font-size: 13px; font-weight: 600; }
  .member-summary span { float: right; color: #2563eb; }
  .member-row { display: flex; align-items: center; gap: 10px; padding: 12px; margin-bottom: 10px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; }
  .member-avatar { flex: none; color: #1e40af; background: #dbeafe; }
  .member-fields { display: grid; flex: 1; grid-template-columns: repeat(5, minmax(100px, 1fr)); gap: 8px; }
  .phase-row { display: grid; grid-template-columns: 70px 1fr 240px 1.4fr 48px; align-items: center; gap: 8px; padding: 10px 0; border-bottom: 1px solid #eef2f7; }
  .phase-title { color: #334155; font-weight: 600; }
  .topic-actions { display: flex; justify-content: center; gap: 12px; padding: 4px 0 20px; }
  @media (max-width: 900px) { .member-fields, .phase-row { grid-template-columns: 1fr; } .member-row { align-items: flex-start; } .topic-header__top, .topic-header__hint { align-items: flex-start; flex-direction: column; } }
</style>
