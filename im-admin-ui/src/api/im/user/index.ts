import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { UserVO, UserBanDTO, UserUnbanDTO, UserResetPwdDTO, UserQuery, SubordinateVO } from '@/api/im/user/types';

/**
 * 查询用户列表
 * @param query
 * @returns {*}
 */

export const listUser = (query?: UserQuery): AxiosPromise<UserVO[]> => {
  return request({
    url: '/im/user/list',
    method: 'get',
    params: query
  });
};

/**
 * 查询用户详细
 * @param id
 */
export const getUser = (id: string | number): AxiosPromise<UserVO> => {
  return request({
    url: '/im/user/' + id,
    method: 'get'
  });
};


/**
 * 封禁用户
 * @param data
 */
export const ban = (data: UserBanDTO) => {
  return request({
    url: '/im/user/ban',
    method: 'put',
    data: data
  });
};


/**
 * 解封用户
 * @param data
 */
export const unban = (data: UserUnbanDTO) => {
  return request({
    url: '/im/user/unban',
    method: 'put',
    data: data
  });
};


/**
 * 重置用户登录密码
 * @param data
 */
export const resetUserPwd = (data: UserResetPwdDTO) => {
  return request({
    url: '/im/user/resetPwd',
    method: 'put',
    data: data
  });
};


/**
 * 查询用户的下级列表
 * @param userId 上级用户id
 */
export const getSubordinates = (userId: string | number): AxiosPromise<SubordinateVO[]> => {
  return request({
    url: '/im/user/subordinates',
    method: 'get',
    params: { userId }
  });
};


export const findUserByName = (name?: String): AxiosPromise<UserVO[]> => {
  return request({
    url: '/im/user/findByName?name=' + name,
    method: 'get'
  });
};

/**
 * 根据id列表查找用户（后端入参为逗号分隔的id字符串）
 * @param ids 用户id数组
 */
export const findUserByIds = (ids: Array<string | number>): AxiosPromise<UserVO[]> => {
  return request({
    url: '/im/user/findByIds',
    method: 'get',
    params: { ids: ids.join(',') }
  });
};

/**
 * 按天统计用户注册数量
 * @param days 统计天数
 */
export const getDailyRegistrationCount = (days?: number): AxiosPromise<any[]> => {
  return request({
    url: '/im/user/dailyRegistrationCount',
    method: 'get',
    params: { days }
  });
};

/**
 * 获取总用户数量
 */
export const getTotalUserCount = (): AxiosPromise<number> => {
  return request({
    url: '/im/user/totalCount',
    method: 'get'
  });
};

/**
 * 获取活跃用户统计（日活、周活、月活）
 */
export const getActiveUserStats = (): AxiosPromise<any> => {
  return request({
    url: '/im/user/activeStats',
    method: 'get'
  });
};

/**
 * 今日签到用户数
 */
export const getTodayCheckinUserCount = (): AxiosPromise<number> => {
  return request({
    url: '/im/user/todayCheckinUserCount',
    method: 'get'
  });
};

/**
 * 按天统计签到用户数
 * @param days 统计天数
 */
export const getDailyCheckinUserCount = (days?: number): AxiosPromise<any[]> => {
  return request({
    url: '/im/user/dailyCheckinUserCount',
    method: 'get',
    params: { days }
  });
};

/**
 * 今日发送消息的用户数（私聊+群聊去重）
 */
export const getTodayMessageUserCount = (): AxiosPromise<number> => {
  return request({
    url: '/im/user/todayMessageUserCount',
    method: 'get'
  });
};

/**
 * 按天统计发送消息的用户数（私聊+群聊去重）
 * @param days 统计天数
 */
export const getDailyMessageUserCount = (days?: number): AxiosPromise<any[]> => {
  return request({
    url: '/im/user/dailyMessageUserCount',
    method: 'get',
    params: { days }
  });
};
