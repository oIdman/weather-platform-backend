import { defHttp } from '/@/utils/http/axios';
import { BpmPage, toTablePage } from '../types';

export interface BpmCategory {
  id?: number;
  name: string;
  code: string;
  description?: string;
  status: number;
  sort: number;
  createTime?: string;
}

export const getCategoryPage = (params) => defHttp.get<BpmPage<BpmCategory>>({ url: '/bpm/category/page', params }).then(toTablePage);
export const getCategory = (id: number) => defHttp.get<BpmCategory>({ url: '/bpm/category/get', params: { id } });
export const createCategory = (data: BpmCategory) => defHttp.post<number>({ url: '/bpm/category/create', data });
export const updateCategory = (data: BpmCategory) => defHttp.put<boolean>({ url: '/bpm/category/update', data });
export const deleteCategory = (id: number) => defHttp.delete<boolean>({ url: `/bpm/category/delete?id=${encodeURIComponent(id)}` });
export const getCategorySimpleList = () => defHttp.get<BpmCategory[]>({ url: '/bpm/category/simple-list' });
export const updateCategorySortBatch = (ids: number[]) => {
  const query = ids.map((id) => `ids=${encodeURIComponent(id)}`).join('&');
  return defHttp.put<boolean>({ url: `/bpm/category/update-sort-batch?${query}` });
};
