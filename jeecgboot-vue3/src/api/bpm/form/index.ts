import { defHttp } from '/@/utils/http/axios';
import { BpmPage, toTablePage } from '../types';

export interface BpmForm {
  id?: number;
  name: string;
  status: number;
  conf: string;
  fields: string[];
  remark?: string;
  createTime?: string;
}

export const getFormPage = (params) => defHttp.get<BpmPage<BpmForm>>({ url: '/bpm/form/page', params }).then(toTablePage);
export const getForm = (id: number) => defHttp.get<BpmForm>({ url: '/bpm/form/get', params: { id } });
export const createForm = (data: BpmForm) => defHttp.post<number>({ url: '/bpm/form/create', data });
export const updateForm = (data: BpmForm) => defHttp.put<boolean>({ url: '/bpm/form/update', data });
export const deleteForm = (id: number) => defHttp.delete<boolean>({ url: `/bpm/form/delete?id=${encodeURIComponent(id)}` });
export const getFormSimpleList = () => defHttp.get<BpmForm[]>({ url: '/bpm/form/simple-list' });
