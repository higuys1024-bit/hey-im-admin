package org.dromara.im.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.im.domain.ImUser;

import java.util.Date;

/**
 * 用户业务对象 im_user
 *
 * @author Blue
 * @date 2024-12-22
 */
@Data
@AutoMapper(target = ImUser.class, reverseConvertGenerate = false)
public class ImUserBo {

    /**
     * id
     */
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
     * 创建时间
     */
    private Date createdTime;

    /**
     *
     */
    private Long type;

    /**
     * 是否被封禁
     */
    private Boolean isBanned;

    /**
     * 被封禁原因
     */
    private String reason;

    /**
     * 邀请码（用于按邀请码搜索用户）
     */
    private String inviteCode;

}
