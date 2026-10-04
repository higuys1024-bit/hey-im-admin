package org.dromara.im.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import org.dromara.common.excel.annotation.ExcelDictFormat;
import org.dromara.common.excel.convert.ExcelDictConvert;
import org.dromara.im.domain.ImUser;

import java.util.Date;



/**
 * 用户视图对象 im_user
 *
 * @author Blue
 * @date 2024-12-22
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = ImUser.class)
public class ImUserVo {


    /**
     * id
     */
    @ExcelProperty(value = "id")
    private Long id;

    /**
     * 用户名
     */
    @ExcelProperty(value = "用户名")
    private String userName;

    /**
     * 用户昵称
     */
    @ExcelProperty(value = "用户昵称")
    private String nickName;

    /**
     * 用户头像
     */
    @ExcelProperty(value = "用户头像")
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
    @ExcelProperty(value = "性别", converter = ExcelDictConvert.class)
    @ExcelDictFormat(readConverterExp = "0=男,1=女")
    private Long sex;

    /**
     * 个性签名
     */
    private String signature;

    /**
     * 最后登录时间
     */
    @ExcelProperty(value = "最后登录时间")
    private Date lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 地址（由最后登录IP离线解析得到，不对应数据库字段）
     */
    @ExcelProperty(value = "地址")
    private String location;

    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    private Date createdTime;

    /**
     *  类型
     */
    private Long type;

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
     * 邀请码
     */
    @ExcelProperty(value = "邀请码")
    private String inviteCode;

    /**
     * 邀请人id（注册时使用的邀请码所属用户）
     */
    private Long inviterId;

}
