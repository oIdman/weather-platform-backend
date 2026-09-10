import { defHttp } from '/@/utils/http/axios';
import { BpmPage, toTablePage } from '../types';

export interface BpmUserGroup {
  id?: number;
  name: string;
  description?: string;
  userIds: string[];
  status: number;
  createTime?: string;
}

export const getUserGroupPage = (params) => defHttp.get<BpmPage<BpmUserGroup>>({ url: '/bpm/user-group/page', params }).then(toTablePage);
export const getUserGroup = (id: number) => defHttp.get<BpmUserGroup>({ url: '/bpm/user-group/get', params: { id } });
export const createUserGroup = (data: BpmUserGroup) => defHttp.post<number>({ url: '/bpm/user-group/create', data });
export const updateUserGroup = (data: BpmUserGroup) => defHttp.put<boolean>({ url: '/bpm/user-group/update', data });
export const deleteUserGroup = (id: number) => defHttp.delete<boolean>({ url: `/bpm/user-group/delete?id=${encodeURIComponent(id)}` });
export const getUserGroupSimpleList = () => defHttp.get<BpmUserGroup[]>({ url: '/bpm/user-group/simple-list' });
