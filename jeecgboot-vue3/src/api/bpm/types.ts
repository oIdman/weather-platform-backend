export interface BpmPage<T> {
  list: T[];
  total: number;
}

export const toTablePage = <T>(page: BpmPage<T>) => ({ records: page.list, total: page.total });
