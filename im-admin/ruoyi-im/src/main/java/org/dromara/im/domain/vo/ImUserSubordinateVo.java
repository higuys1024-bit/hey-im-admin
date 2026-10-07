package org.dromara.im.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * 下级用户视图对象
 *
 * @author Blue
 */
@Data
public class ImUserSubordinateVo {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 账号（用户名/手机号）
     */
    private String userName;

    /**
     * 姓名（昵称）
     */
    private String nickName;

    /**
     * 注册时间
     */
    private Date createdTime;

    /**
     * 该下级用户自身的下级人数
     */
    private Long subordinateCount;

}
