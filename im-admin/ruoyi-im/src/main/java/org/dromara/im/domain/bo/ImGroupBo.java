package org.dromara.im.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.im.domain.ImGroup;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 群业务对象 im_group
 *
 * @author Blue
 * @date 2024-12-22
 */
@Data
@AutoMapper(target = ImGroup.class, reverseConvertGenerate = false)
public class ImGroupBo {

    /**
     * id
     */
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
     * 自定义群聊人数(虚拟人数)
     */
    private Integer customMemberCount;

    /**
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();


}
