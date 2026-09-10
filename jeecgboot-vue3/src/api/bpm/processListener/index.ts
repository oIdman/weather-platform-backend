import { defHttp } from '/@/utils/http/axios';
import { BpmPage, toTablePage } from '../types';

export interface BpmProcessListener {
  id?: number;
  name: string;
  type: string;
  status: number;
  event: string;
  valueType: string;
  value: string;
  createTime?: string;
}

export const getProcessListenerPage = (params) =>
  defHttp.get<BpmPage<BpmProcessListener>>({ url: '/bpm/process-listener/page', params }).then(toTablePage);
export const getProcessListener = (id: number) => defHttp.get<BpmProcessListener>({ url: '/bpm/process-listener/get', params: { id } });
export const createProcessListener = (data: BpmProcessListener) => defHttp.post<number>({ url: '/bpm/process-listener/create', data });
export const updateProcessListener = (data: BpmProcessListener) => defHttp.put<boolean>({ url: '/bpm/process-listener/update', data });
export const deleteProcessListener = (id: number) => defHttp.delete<boolean>({ url: `/bpm/process-listener/delete?id=${encodeURIComponent(id)}` });
