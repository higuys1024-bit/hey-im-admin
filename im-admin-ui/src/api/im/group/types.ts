export interface GroupVO {
  /**
   * id
   */
  id: string | number;

  /**
   * 群名字
   */
  name: string;

  /**
   * 群主id
   */
  ownerId: string | number;

  /**
   * 群头像
   */
  headImage: string;

  /**
   * 群头像Url
   */
  headImageUrl: string;
  /**
   * 群头像缩略图
   */
  headImageThumb: string;

  /**
   * 群公告
   */
  notice: string;
  /**
   * 是否已解散
   */
  dissolve: number;

  /**
   * 创建时间
   */
  createdTime: string;

  /**
   * 是否被封禁 0:否 1:是
   */
  isBanned: number;

  /**
   * 被封禁原因
   */
  reason: string;

  /**
   * 成员数量
   */
  memberCount: number;

  /**
   * 自定义群聊人数(虚拟人数)
   */
  customMemberCount?: number;

}

export interface GroupForm extends BaseEntity {
  /**
   * id
   */
  id?: string | number;

  /**
   * 群名字
   */
  name?: string;

  /**
   * 群主id
   */
  ownerId?: string | number;

    /**
   * 群主
   */
  ownerUserName: string,
  /**
   * 群头像
   */
  headImage?: string;

  /**
   * 群头像缩略图
   */
  headImageThumb?: string;

  /**
   * 群公告
   */
  notice?: string;

  /**
   * 是否已解散
   */
  dissolve?: number;

  /**
   * 创建时间
   */
  createdTime?: string;

  /**
   * 是否被封禁 0:否 1:是
   */
  isBanned?: number;

  /**
   * 被封禁原因
   */
  reason?: string;

  /**
   * 自定义群聊人数(虚拟人数)
   */
  customMemberCount?: number;

}

export interface GroupQuery extends PageQuery {

  /**
   * 群名字
   */
  name?: string;

  /**
   * 群主id
   */
  ownerId?: number;

  /**
   * 群头像
   */
  headImage?: string;

  /**
   * 群头像缩略图
   */
  headImageThumb?: string;

  /**
   * 群公告
   */
  notice?: string;

  /**
   * 是否已解散
   */
  dissolve?: number;

  /**
   * 创建时间
   */
  createdTime?: string;

  /**
   * 是否被封禁 0:否 1:是
   */
  isBanned?: number;

  /**
   * 被封禁原因
   */
  reason?: string;

    /**
     * 日期范围参数
     */
    params?: any;
}

export interface GroupBanDTO {

  /**
   * id
   */
  id?: string | number;

  /**
   * 被封禁原因
   */
  reason?: string;
}


export interface GroupUnbanDTO {

  /**
   * id
   */
  id?: string | number;
}

