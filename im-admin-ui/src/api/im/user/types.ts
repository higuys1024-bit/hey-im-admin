export interface UserVO {
  /**
   * id
   */
  id: string | number;

  /**
   * 用户名
   */
  userName: string;

  /**
   * 用户昵称
   */
  nickName: string;

  /**
   * 用户头像
   */
  headImage: string;

  /**
   * 用户头像Url
   */
  headImageUrl: string;
  /**
   * 用户头像缩略图
   */
  headImageThumb: string;

  /**
   * 密码(明文)
   */
  password: string;

  /**
   * 性别 0:男 1::女
   */
  sex: number;

  /**
   * 个性签名
   */
  signature: string;

  /**
   * 最后登录时间
   */
  lastLoginTime: string;

  /**
   * 最后登录IP
   */
  lastLoginIp: string;

  /**
   * 地址（由最后登录IP离线解析得到）
   */
  location: string;

  /**
   * 创建时间
   */
  createdTime: string;

  /**
   *
   */
  type: number;

  /**
   * 是否被封禁 0:否 1:是
   */
  isBanned: number;

  /**
   * 被封禁原因
   */
  reason: string;

  /**
   * 邀请码
   */
  inviteCode: string;

  /**
   * 邀请人id
   */
  inviterId: string | number;

}

export interface UserForm extends BaseEntity {
  /**
   * id
   */
  id?: string | number;

  /**
   * 用户名
   */
  userName?: string;

  /**
   * 用户昵称
   */
  nickName?: string;

  /**
   * 用户头像
   */
  headImage?: string;

  /**
   * 用户头像缩略图
   */
  headImageThumb?: string;

  /**
   * 密码(明文)
   */
  password?: string;

  /**
   * 性别 0:男 1::女
   */
  sex?: number;

  /**
   * 个性签名
   */
  signature?: string;

  /**
   * 最后登录时间
   */
  lastLoginTime?: string;

  /**
   * 地址（由最后登录IP离线解析得到）
   */
  location?: string;

  /**
   * 创建时间
   */
  createdTime?: string;

  /**
   *
   */
  type?: number;

  /**
   * 是否被封禁 0:否 1:是
   */
  isBanned?: number;

  /**
   * 被封禁原因
   */
  reason?: string;

  /**
   * 邀请码
   */
  inviteCode?: string;

  /**
   * 邀请人id
   */
  inviterId?: string | number;

}

export interface UserQuery extends PageQuery {

  /**
   * 用户名
   */
  userName?: string;

  /**
   * 用户昵称
   */
  nickName?: string;
  /**
   * 创建时间
   */
  createdTime?: string;
  /**
  * 日期范围参数
  */
  params?: any;
}

export interface UserBanDTO {

  /**
   * id
   */
  id?: string | number;

  /**
   * 被封禁原因
   */
  reason?: string;
}


export interface UserUnbanDTO {

  /**
   * id
   */
  id?: string | number;
}


export interface UserResetPwdDTO {

  /**
   * id
   */
  id?: string | number;

  /**
   * 新登录密码
   */
  password?: string;
}


