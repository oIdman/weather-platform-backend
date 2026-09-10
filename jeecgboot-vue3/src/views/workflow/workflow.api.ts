import { defHttp } from '/@/utils/http/axios';
import qs from 'qs';

export interface WorkflowPage<T> {
  list: T[];
  total: number;
}

export interface WorkflowDefinition {
  id: string;
  key: string;
  name: string;
  version: number;
  category?: string;
  deploymentTime?: string;
  suspended: boolean;
  formType?: number;
  formId?: number;
  formName?: string;
  formConf?: string;
  formFields?: string[];
  formCustomCreatePath?: string;
  formCustomViewPath?: string;
  icon?: string;
  description?: string;
  visible?: boolean;
}

export interface WorkflowTask {
  id: string;
  name: string;
  processInstanceName?: string;
  processInstanceStatus?: string;
  businessKey?: string;
  category?: string;
  categoryName?: string;
  startUserId?: string;
  startUserName?: string;
  assignee?: string;
  assigneeName?: string;
  owner?: string;
  ownerName?: string;
  parentTaskId?: string;
  createTime?: string;
  endTime?: string;
  status: string;
  resultStatus?: number;
  reason?: string;
  signPicUrl?: string;
  attachments?: string[];
  children?: WorkflowTask[];
}

export interface WorkflowProcessCopy {
  id: number;
  userId: string;
  userName?: string;
  startUserId: string;
  startUserName?: string;
  processInstanceId: string;
  processInstanceName: string;
  processInstanceStartTime?: string;
  processDefinitionId: string;
  category?: string;
  categoryName?: string;
  activityId?: string;
  activityName?: string;
  taskId?: string;
  reason?: string;
  createTime?: string;
}

export interface WorkflowInstance {
  id: string;
  name: string;
  businessKey: string;
  processDefinitionName?: string;
  category?: string;
  categoryName?: string;
  startUserId?: string;
  startUserName?: string;
  startTime?: string;
  endTime?: string;
  status: string;
  resultStatus?: number;
  reason?: string;
  currentTasks?: string[];
  formVariables?: Record<string, unknown>;
  tasks?: WorkflowTask[];
}

export interface WorkflowComment {
  id: string;
  taskId?: string;
  processInstanceId: string;
  type: string;
  message: string;
  createTime?: string;
  task?: { id: string; name: string; taskDefinitionKey: string };
  user?: { id: string; nickname: string; avatar?: string };
}

export interface WorkflowBpmnView {
  bpmnXml: string;
  currentActivityIds: string[];
  completedActivityIds: string[];
}

export interface WorkflowApprovalDetail {
  status?: number;
  activityNodes?: WorkflowActivityNode[];
  formFieldsPermission?: Record<string, string>;
  enabledButtons?: number[];
  buttonDisplayNames?: Record<number, string>;
  signEnable?: boolean;
  reasonRequire?: boolean;
  processInstance?: WorkflowInstance;
  tasks: WorkflowTask[];
  todoTask?: WorkflowTask;
  processDefinition?: WorkflowDefinition;
  bpmnModelView?: WorkflowBpmnView;
}

export interface WorkflowApprovalDetailRequest {
  processDefinitionId?: string;
  processVariables?: Record<string, unknown>;
  processVariablesStr?: string;
  processInstanceId?: string;
  activityId?: string;
  taskId?: string;
}

export interface WorkflowUserSimple {
  id: string;
  username?: string;
  nickname?: string;
  avatar?: string;
  status?: number;
}

export interface WorkflowActivityNode {
  id: string;
  name: string;
  nodeType: number;
  status: number;
  startTime?: string;
  endTime?: string;
  tasks?: WorkflowTask[];
  candidateStrategy?: number;
  candidateUsers?: WorkflowUserSimple[];
  processInstanceId?: string;
}

const toTablePage = <T>(page: WorkflowPage<T>) => ({ records: page.list, total: page.total });
const repeatArrayParams = (params) => qs.stringify(params, { arrayFormat: 'repeat' });

export const getDefinitionPage = (params) =>
  defHttp.get<WorkflowPage<WorkflowDefinition>>({ url: '/bpm/process-definition/page', params }).then(toTablePage);

export const deployDefinition = (data) => defHttp.post({ url: '/bpm/process-definition/deploy', data });

export const getTodoPage = (params) =>
  defHttp.get<WorkflowPage<WorkflowTask>>({ url: '/bpm/task/todo-page', params, paramsSerializer: repeatArrayParams }).then(toTablePage);

export const getDonePage = (params) =>
  defHttp.get<WorkflowPage<WorkflowTask>>({ url: '/bpm/task/done-page', params, paramsSerializer: repeatArrayParams }).then(toTablePage);

export const getManagerTaskPage = (params) =>
  defHttp.get<WorkflowPage<WorkflowTask>>({ url: '/bpm/task/manager-page', params, paramsSerializer: repeatArrayParams }).then(toTablePage);

export const getProcessCopyPage = (params) =>
  defHttp.get<WorkflowPage<WorkflowProcessCopy>>({ url: '/bpm/process-instance/copy/page', params }).then(toTablePage);

export const approveTask = (data) => defHttp.put({ url: '/bpm/task/approve', data });

export const rejectTask = (data) => defHttp.put({ url: '/bpm/task/reject', data });

export const copyTask = (data) => defHttp.put({ url: '/bpm/task/copy', data });

export const getTaskListByReturn = (id: string) => defHttp.get<WorkflowTask[]>({ url: '/bpm/task/list-by-return', params: { id } });
export const returnTask = (data) => defHttp.put({ url: '/bpm/task/return', data });
export const delegateTask = (data) => defHttp.put({ url: '/bpm/task/delegate', data });
export const transferTask = (data) => defHttp.put({ url: '/bpm/task/transfer', data });
export const createSignTask = (data) => defHttp.put({ url: '/bpm/task/create-sign', data });
export const deleteSignTask = (data) => defHttp.delete({ url: '/bpm/task/delete-sign', data });
export const getChildTaskList = (parentTaskId: string) =>
  defHttp.get<WorkflowTask[]>({ url: '/bpm/task/list-by-parent-task-id', params: { parentTaskId } });
export const withdrawTask = (taskId: string) => defHttp.put({ url: '/bpm/task/withdraw', params: { taskId } });

export const getMyProcessPage = (params) =>
  defHttp
    .get<WorkflowPage<WorkflowInstance>>({ url: '/bpm/process-instance/my-page', params, paramsSerializer: repeatArrayParams })
    .then(toTablePage);

export const getManagerProcessPage = (params) =>
  defHttp
    .get<WorkflowPage<WorkflowInstance>>({ url: '/bpm/process-instance/manager-page', params, paramsSerializer: repeatArrayParams })
    .then(toTablePage);

export const startProcess = (data) => defHttp.post({ url: '/bpm/process-instance/create', data });

export const getProcessInstance = (id: string) => defHttp.get<WorkflowInstance>({ url: '/bpm/process-instance/get', params: { id } });

export const getApprovalDetail = (params: WorkflowApprovalDetailRequest) =>
  defHttp.get<WorkflowApprovalDetail>({ url: '/bpm/process-instance/get-approval-detail', params });

export const getNextApprovalNodes = (params: { processInstanceId: string; taskId: string; processVariablesStr?: string }) =>
  defHttp.get<WorkflowActivityNode[]>({ url: '/bpm/process-instance/get-next-approval-nodes', params });

export const getBpmnModelView = (id: string) => defHttp.get<WorkflowBpmnView>({ url: '/bpm/process-instance/get-bpmn-model-view', params: { id } });

export const getPrintData = (processInstanceId: string) =>
  defHttp.get({ url: '/bpm/process-instance/get-print-data', params: { processInstanceId } });

export const getTaskListByProcessInstanceId = (processInstanceId: string) =>
  defHttp.get<WorkflowTask[]>({ url: '/bpm/task/list-by-process-instance-id', params: { processInstanceId } });

export const getCommentListByProcessInstanceId = (processInstanceId: string) =>
  defHttp.get<WorkflowComment[]>({ url: '/bpm/comment/list-by-process-instance-id', params: { processInstanceId } });

export const createComment = (taskId: string, message: string) => defHttp.post({ url: '/bpm/comment/create', data: { taskId, message } });

export const cancelProcess = (data, manager = false) =>
  defHttp.delete({ url: manager ? '/bpm/process-instance/cancel-by-admin' : '/bpm/process-instance/cancel-by-start-user', data });

export const deleteProcessInstance = (id: string) => defHttp.delete({ url: '/bpm/process-instance/delete', params: { id } });
