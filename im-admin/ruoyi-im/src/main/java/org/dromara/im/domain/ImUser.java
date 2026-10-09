package org.dromara.im.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fhs.core.trans.vo.TransPojo;
import lombok.Data;

import java.util.Date;

/**
 * 用户对象 im_user
 *
 * @author Blue
 * @date 2024-12-22
 */
@Data
@TableName("im_user")
public class ImUser implements TransPojo {

    /**
     * id
     */
    @TableId
    private Long id;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户昵称
     */
    private String nickName;

    /**
     * 用户头像
     */
    private String headImage;

    /**
     * 用户头像缩略图
     */
    private String headImageThumb;

    /**
     * 密码(明文)
     */
    private String password;

    /**
     * 性别 0:男 1::女
     */
    private Long sex;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 最后登录IP（用于解析用户所在地址）
     */
    private String lastLoginIp;

    /**
     * 最后登录地区
     */
    private String lastLoginRegion;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 
     */
    private Long type;

    /**
     * 是否被封禁 0:否 1:是
     */
    private Boolean isBanned;

    /**
     * 被封禁原因
     */
    private String reason;

    /**
     * 邀请码
     */
    private String inviteCode;

    /**
     * 邀请人id（注册时使用的邀请码所属用户）
     */
    private Long inviterId;

}
