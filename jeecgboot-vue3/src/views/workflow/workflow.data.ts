import { FormSchema } from '/@/components/Table';
import { getCategorySimpleList } from '/@/api/bpm/category';
import { getSimpleProcessDefinitionList } from '/@/api/bpm/definition';

const processStatusOptions = [
  { label: '审批中', value: 1 },
  { label: '审批通过', value: 2 },
  { label: '审批不通过', value: 3 },
  { label: '已取消', value: 4 },
];

const taskStatusOptions = [
  { label: '审批通过', value: 2 },
  { label: '审批不通过', value: 3 },
  { label: '已取消', value: 4 },
  { label: '已退回', value: 5 },
  { label: '审批通过中', value: 7 },
];

const dateTimeRangeProps = {
  showTime: true,
  valueFormat: 'YYYY-MM-DD HH:mm:ss',
  placeholder: ['开始时间', '结束时间'],
};

const processDefinitionSelectProps = {
  api: getSimpleProcessDefinitionList,
  labelField: 'name',
  valueField: 'key',
  immediate: true,
};

const categorySelectProps = {
  api: getCategorySimpleList,
  labelField: 'name',
  valueField: 'code',
  immediate: true,
};

export const definitionColumns = [
  { title: '流程名称', dataIndex: 'name', align: 'left' },
  { title: '流程标识', dataIndex: 'key', align: 'left' },
  { title: '版本', dataIndex: 'version', width: 80, align: 'center' },
  { title: '分类', dataIndex: 'category', width: 140, align: 'center' },
  { title: '部署时间', dataIndex: 'deploymentTime', width: 180 },
  { title: '状态', dataIndex: 'suspended', width: 100, customRender: ({ text }) => (text ? '已挂起' : '已激活') },
];

export const definitionSearch: FormSchema[] = [{ field: 'name', label: '流程名称', component: 'Input', colProps: { span: 6 } }];

export const processSearch: FormSchema[] = [
  { field: 'name', label: '流程名称', component: 'Input', colProps: { span: 6 } },
  { field: 'processDefinitionKey', label: '所属流程', component: 'ApiSelect', componentProps: processDefinitionSelectProps, colProps: { span: 6 } },
  { field: 'category', label: '流程分类', component: 'ApiSelect', componentProps: categorySelectProps, colProps: { span: 6 } },
  { field: 'status', label: '流程状态', component: 'Select', componentProps: { options: processStatusOptions }, colProps: { span: 6 } },
  { field: 'createTime', label: '发起时间', component: 'RangePicker', componentProps: dateTimeRangeProps, colProps: { span: 8 } },
];

export const processManagerSearch: FormSchema[] = [
  {
    field: 'startUserId',
    label: '发起人',
    component: 'JSelectUser',
    componentProps: { isRadioSelection: true, rowKey: 'id', labelKey: 'realname' },
    colProps: { span: 6 },
  },
  ...processSearch,
];

export const todoTaskSearch: FormSchema[] = [
  { field: 'name', label: '任务名称', component: 'Input', colProps: { span: 6 } },
  { field: 'processDefinitionKey', label: '所属流程', component: 'ApiSelect', componentProps: processDefinitionSelectProps, colProps: { span: 6 } },
  { field: 'category', label: '流程分类', component: 'ApiSelect', componentProps: categorySelectProps, colProps: { span: 6 } },
  { field: 'status', label: '流程状态', component: 'Select', componentProps: { options: processStatusOptions }, colProps: { span: 6 } },
  { field: 'createTime', label: '任务时间', component: 'RangePicker', componentProps: dateTimeRangeProps, colProps: { span: 8 } },
];

export const doneTaskSearch: FormSchema[] = [
  { field: 'name', label: '任务名称', component: 'Input', colProps: { span: 6 } },
  { field: 'processDefinitionKey', label: '所属流程', component: 'ApiSelect', componentProps: processDefinitionSelectProps, colProps: { span: 6 } },
  { field: 'category', label: '流程分类', component: 'ApiSelect', componentProps: categorySelectProps, colProps: { span: 6 } },
  { field: 'status', label: '审批状态', component: 'Select', componentProps: { options: taskStatusOptions }, colProps: { span: 6 } },
  { field: 'createTime', label: '任务时间', component: 'RangePicker', componentProps: dateTimeRangeProps, colProps: { span: 8 } },
];

export const managerTaskSearch: FormSchema[] = [
  { field: 'name', label: '任务名称', component: 'Input', colProps: { span: 6 } },
  { field: 'processDefinitionKey', label: '所属流程', component: 'ApiSelect', componentProps: processDefinitionSelectProps, colProps: { span: 6 } },
  { field: 'category', label: '流程分类', component: 'ApiSelect', componentProps: categorySelectProps, colProps: { span: 6 } },
  { field: 'status', label: '审批状态', component: 'Select', componentProps: { options: taskStatusOptions }, colProps: { span: 6 } },
  { field: 'createTime', label: '创建时间', component: 'RangePicker', componentProps: dateTimeRangeProps, colProps: { span: 8 } },
];

export const taskColumns = [
  { title: '任务名称', dataIndex: 'name', align: 'left' },
  { title: '流程实例', dataIndex: 'processInstanceName', align: 'left' },
  { title: '业务标识', dataIndex: 'businessKey', align: 'left' },
  { title: '发起人', dataIndex: 'startUserName', width: 150, align: 'center' },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '完成时间', dataIndex: 'endTime', width: 180 },
  {
    title: '审批状态',
    dataIndex: 'resultStatus',
    width: 120,
    align: 'center',
    customRender: ({ text }) =>
      ({ 0: '待审批', 1: '审批中', 2: '审批通过', 3: '审批不通过', 4: '已取消', 5: '已退回', 7: '审批通过中' })[text] || '审批中',
  },
  { title: '审批意见', dataIndex: 'reason', width: 180, align: 'left' },
];

export const todoTaskColumns = [
  { title: '流程分类', dataIndex: 'categoryName', align: 'left', customRender: ({ text, record }) => text || record.category || '-' },
  ...taskColumns.slice(1),
];

export const processColumns = [
  { title: '实例名称', dataIndex: 'name', align: 'left' },
  { title: '流程定义', dataIndex: 'processDefinitionName', align: 'left' },
  { title: '业务标识', dataIndex: 'businessKey', align: 'left' },
  { title: '发起人', dataIndex: 'startUserName', width: 150, align: 'center' },
  { title: '当前节点', dataIndex: 'currentTasks', align: 'left', customRender: ({ text }) => (text || []).join('、') },
  {
    title: '状态',
    dataIndex: 'status',
    width: 120,
    align: 'center',
    customRender: ({ text }) => ({ RUNNING: '审批中', COMPLETED: '审批通过', REJECTED: '审批不通过', CANCELED: '已取消' })[text] || text,
  },
  { title: '发起时间', dataIndex: 'startTime', width: 180 },
  { title: '结束时间', dataIndex: 'endTime', width: 180 },
];
