import { defHttp } from '/@/utils/http/axios';
import { BpmPage, toTablePage } from '../types';

export interface BpmProcessExpression {
  id?: number;
  name: string;
  status: number;
  expression: string;
  createTime?: string;
}

export const getProcessExpressionPage = (params) =>
  defHttp.get<BpmPage<BpmProcessExpression>>({ url: '/bpm/process-expression/page', params }).then(toTablePage);
export const getProcessExpression = (id: number) => defHttp.get<BpmProcessExpression>({ url: '/bpm/process-expression/get', params: { id } });
export const createProcessExpression = (data: BpmProcessExpression) => defHttp.post<number>({ url: '/bpm/process-expression/create', data });
export const updateProcessExpression = (data: BpmProcessExpression) => defHttp.put<boolean>({ url: '/bpm/process-expression/update', data });
export const deleteProcessExpression = (id: number) => defHttp.delete<boolean>({ url: `/bpm/process-expression/delete?id=${encodeURIComponent(id)}` });
