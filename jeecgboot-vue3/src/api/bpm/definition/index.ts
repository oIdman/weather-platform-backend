import { defHttp } from '/@/utils/http/axios';
import type { BpmPage } from '../types';
import { toTablePage } from '../types';

export interface BpmProcessDefinition {
  id: string;
  key: string;
  name: string;
  version: number;
  category?: string;
  categoryName?: string;
  description?: string;
  deploymentTime?: string;
  suspensionState: number;
  suspended: boolean;
  modelType?: number;
  modelId?: string;
  formType?: number;
  formId?: number;
  formName?: string;
  formConf?: string;
  formFields?: string[];
  formCustomCreatePath?: string;
  formCustomViewPath?: string;
  icon?: string;
  visible?: boolean;
  sort?: number;
  bpmnXml?: string;
  simpleModel?: string;
}

export const getProcessDefinition = (id?: string, key?: string) =>
  defHttp.get<BpmProcessDefinition>({ url: '/bpm/process-definition/get', params: { id, key } });

export const getProcessDefinitionPage = (params) =>
  defHttp.get<BpmPage<BpmProcessDefinition>>({ url: '/bpm/process-definition/page', params }).then(toTablePage);

export const getProcessDefinitionList = (params: { suspensionState: number }) =>
  defHttp.get<BpmProcessDefinition[]>({ url: '/bpm/process-definition/list', params });

export const getSimpleProcessDefinitionList = () => defHttp.get<BpmProcessDefinition[]>({ url: '/bpm/process-definition/simple-list' });
