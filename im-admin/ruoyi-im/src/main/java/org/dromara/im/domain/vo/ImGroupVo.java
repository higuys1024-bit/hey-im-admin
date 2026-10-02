package org.dromara.im.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import com.fhs.core.trans.vo.TransPojo;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import org.dromara.im.constant.ImConstant;
import org.dromara.im.domain.ImGroup;
import org.dromara.im.domain.ImUser;

import java.util.Date;

/**
 * 群视图对象 im_group
 *
 * @author Blue
 * @date 2024-12-22
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ImGroup.class)
public class ImGroupVo implements TransPojo {

    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 群名字
     */
    @ExcelProperty(value = "群名字")
    private String name;

    /**
     * 群主id
     */
    @Trans(type = TransType.SIMPLE, dataSource = ImConstant.DS_IM_PLATFORM, target = ImUser.class, fields = "userName",
        ref = "ownerUserName")
    private Long ownerId;

    /**
     * 群主名
     */
    @ExcelProperty(value = "群主")
    private String ownerUserName;

    /**
     * 群头像
     */
    @ExcelProperty(value = "群头像")
    private String headImage;

    /**
     * 群头像
     */
    @ExcelProperty(value = "群头像缩略图")
    private String headImageThumb;

    /**
     * 群公告
     */
    private String notice;

    /**
     * 是否已解散
     */
    @ExcelProperty(value = "是否已解散", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "false=否,true=是")
    private Boolean dissolve;

    /**
     * 是否被封禁 0:否 1:是
     */
    @ExcelProperty(value = "是否被封禁", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "false=否,true=是")
    private Boolean isBanned;

    /**
     * 被封禁原因
     */
    @ExcelProperty(value = "被封禁原因")
    private String reason;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createdTime;


    @Schema(description = "成员数量")
    private Long memberCount;

    @Schema(description = "自定义群聊人数(虚拟人数)")
    private Integer customMemberCount;
}
