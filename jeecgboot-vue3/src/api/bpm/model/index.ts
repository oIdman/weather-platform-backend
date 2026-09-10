import { defHttp } from '/@/utils/http/axios';

export interface BpmModelDefinition {
  id: string;
  key: string;
  name: string;
  version: number;
  deploymentTime?: string;
  suspensionState: number;
  formType?: number;
  formCustomCreatePath?: string;
  formCustomViewPath?: string;
  formFields?: string[];
}

export interface BpmModel {
  id?: string;
  key: string;
  name: string;
  category?: string;
  categoryName?: string;
  icon?: string;
  description?: string;
  type: number;
  formType: number;
  formId?: number;
  formName?: string;
  formCustomCreatePath?: string;
  formCustomViewPath?: string;
  visible: boolean;
  startUserIds?: string[];
  startDeptIds?: string[];
  managerUserIds: string[];
  sort?: number;
  allowCancelRunningProcess?: boolean;
  allowWithdrawTask?: boolean;
  processIdRule?: Record<string, unknown>;
  autoApprovalType?: number;
  titleSetting?: Record<string, unknown>;
  summarySetting?: Record<string, unknown>;
  processBeforeTriggerSetting?: Record<string, unknown>;
  processAfterTriggerSetting?: Record<string, unknown>;
  taskBeforeTriggerSetting?: Record<string, unknown>;
  taskAfterTriggerSetting?: Record<string, unknown>;
  printTemplateSetting?: Record<string, unknown>;
  createTime?: string;
  updateTime?: string;
  bpmnXml?: string;
  simpleModel?: Record<string, unknown>;
  status?: number;
  processDefinition?: BpmModelDefinition;
}

export const getModelList = (name?: string) => defHttp.get<BpmModel[]>({ url: '/bpm/model/list', params: { name } });
export const getModel = (id: string) => defHttp.get<BpmModel>({ url: '/bpm/model/get', params: { id } });
export const createModel = (data: BpmModel) => defHttp.post<string>({ url: '/bpm/model/create', data });
export const updateModel = (data: BpmModel) => defHttp.put<boolean>({ url: '/bpm/model/update', data });
export const updateModelSortBatch = (ids: string[]) => {
  const query = ids.map((id) => `ids=${encodeURIComponent(id)}`).join('&');
  return defHttp.put<boolean>({ url: `/bpm/model/update-sort-batch?${query}` });
};
export const updateModelBpmn = (id: string, bpmnXml: string) => defHttp.put<boolean>({ url: '/bpm/model/update-bpmn', data: { id, bpmnXml } });
export const updateSimpleModel = (id: string, simpleModel: Record<string, unknown>) =>
  defHttp.post<boolean>({ url: '/bpm/model/simple/update', data: { id, simpleModel } });
export const deployModel = (id: string) => defHttp.post<boolean>({ url: `/bpm/model/deploy?id=${encodeURIComponent(id)}` });
export const updateModelState = (id: string, state: number) => defHttp.put<boolean>({ url: '/bpm/model/update-state', data: { id, state } });
export const deleteModel = (id: string) => defHttp.delete<boolean>({ url: `/bpm/model/delete?id=${encodeURIComponent(id)}` });
export const cleanModel = (id: string) => defHttp.delete<boolean>({ url: `/bpm/model/clean?id=${encodeURIComponent(id)}` });
export const exportModel = (id: string) => defHttp.get<BpmModel>({ url: '/bpm/model/export', params: { id } });

export function importModel(file: File, key?: string, name?: string) {
  const data = new FormData();
  data.append('file', file);
  return defHttp.post<string>({
    url: '/bpm/model/import',
    params: { key, name },
    data,
    headers: { 'Content-Type': 'multipart/form-data' },
  });
}
