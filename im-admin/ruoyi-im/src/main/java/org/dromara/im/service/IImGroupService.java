package org.dromara.im.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.im.domain.bo.ImGroupBo;
import org.dromara.im.domain.dto.ImGroupBanDto;
import org.dromara.im.domain.dto.ImGroupCustomCountDto;
import org.dromara.im.domain.dto.ImGroupUnbanDto;
import org.dromara.im.domain.vo.ImGroupVo;

import java.util.List;

/**
 * 群Service接口
 *
 * @author Blue
 * @date 2024-12-22
 */
public interface IImGroupService {

    /**
     * 查询群
     *
     * @param id 主键
     * @return 群
     */
    ImGroupVo queryById(Long id);

    /**
     * 分页查询群列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 群分页列表
     */
    TableDataInfo<ImGroupVo> queryPageList(ImGroupBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的群列表
     *
     * @param bo 查询条件
     * @return 群列表
     */
    List<ImGroupVo> queryList(ImGroupBo bo);

    /**
     * 群聊封禁
     *
     * @param dto dto
     */
    void ban(ImGroupBanDto dto);

    /**
     * 群聊解封
     *
     * @param dto dto
     */
    void unban(ImGroupUnbanDto dto);


    /**
     * 根据用户名查找
     *
     * @param name 用户名
     */
    List<ImGroupVo> findByName(String name);


    /**
     * 获取总群组数量
     * @return 总群组数量
     */
    Long getTotalGroupCount();

    /**
     * 设置群聊人数(虚拟人数)
     *
     * @param dto dto
     */
    void updateCustomMemberCount(ImGroupCustomCountDto dto);

}
