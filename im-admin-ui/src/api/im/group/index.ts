import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { GroupVO, GroupBanDTO, GroupUnbanDTO, GroupQuery } from '@/api/im/group/types';

/**
 * 查询群列表
 * @param query
 * @returns {*}
 */

export const listGroup = (query?: GroupQuery): AxiosPromise<GroupVO[]> => {
  return request({
    url: '/im/group/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询群详细
 * @param id
 */
export const getGroup = (id: string | number): AxiosPromise<GroupVO> => {
  return request({
    url: '/im/group/' + id,
    method: 'get'
  });
};

/**
 * 封禁用户
 * @param data
 */
export const ban = (data: GroupBanDTO) => {
  return request({
    url: '/im/group/ban',
    method: 'put',
    data: data
  });
};


/**
 * 解封用户
 * @param data
 */
export const unban = (data: GroupUnbanDTO) => {
  return request({
    url: '/im/group/unban',
    method: 'put',
    data: data
  });
};



export const findGroupByName = (name?: String): AxiosPromise<GroupVO[]> => {
  return request({
    url: '/im/group/findByName?name='+name,
    method: 'get'
  });
};


/**
 * 获取总群组数量
 */
export const getTotalGroupCount = (): AxiosPromise<number> => {
  return request({
    url: '/im/group/totalCount',
    method: 'get'
  });
};

/**
 * 设置群聊人数(虚拟人数)
 * @param data
 */
export const updateCustomMemberCount = (data: { id: string | number; customMemberCount?: number }) => {
  return request({
    url: '/im/group/customMemberCount',
    method: 'put',
    data: data
  });
};

