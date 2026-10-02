package org.dromara.im.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fhs.core.trans.vo.TransPojo;
import lombok.Data;

import java.util.Date;

/**
 * 群对象 im_group
 *
 * @author Blue
 * @date 2024-12-22
 */
@Data
@TableName("im_group")
public class ImGroup implements TransPojo {


    /**
     * id
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 群名字
     */
    private String name;

    /**
     * 群主id
     */
    private Long ownerId;

    /**
     * 群头像
     */
    private String headImage;

    /**
     * 群头像缩略图
     */
    private String headImageThumb;

    /**
     * 群公告
     */
    private String notice;


    /**
     * 是否已解散
     */
    private Boolean dissolve;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 是否被封禁 0:否 1:是
     */
    private Boolean isBanned;

    /**
     * 被封禁原因
     */
    private String reason;

    /**
     * 自定义群聊人数(虚拟人数),大于0时生效
     */
    private Integer customMemberCount;


}
