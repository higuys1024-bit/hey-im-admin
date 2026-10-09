package org.dromara.im.service;

import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.im.domain.bo.ImUserBo;
import org.dromara.im.domain.dto.ImUserBanDto;
import org.dromara.im.domain.dto.ImUserResetPwdDto;
import org.dromara.im.domain.dto.ImUserUnbanDto;
import org.dromara.im.domain.vo.ImUserVo;

import java.util.List;
import java.util.Map;

/**
 * 用户Service接口
 *
 * @author Blue
 * @date 2024-12-22
 */
public interface IImUserService {

    /**
     * 查询用户
     *
     * @param id 主键
     * @return 用户
     */
    ImUserVo queryById(Long id);

    /**
     * 分页查询用户列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户分页列表
     */
    TableDataInfo<ImUserVo> queryPageList(ImUserBo bo, PageQuery pageQuery);

    /**
     * 查询符合条件的用户列表
     *
     * @param bo 查询条件
     * @return 用户列表
     */
    List<ImUserVo> queryList(ImUserBo bo);

    /**
     *  封禁用户
     *
     * @param dto dto
     */
    void ban(ImUserBanDto dto);

    /**
     *  解封用户
     *
     * @param dto dto
     */
    void unban(ImUserUnbanDto dto);

    /**
     * 重置用户登录密码
     *
     * @param dto dto
     */
    void resetPassword(ImUserResetPwdDto dto);

    /**
     * 查询指定用户的直接下级用户列表（含每个下级自身的下级人数）
     *
     * @param userId 上级用户id
     * @return 下级用户列表
     */
    List<org.dromara.im.domain.vo.ImUserSubordinateVo> querySubordinates(Long userId);

    /**
     * 根据用户名查找
     *
     * @param name 用户名
     */
    List<ImUserVo> findByName(String name);

    /**
     * 根据用户id查找
     *
     * @param ids 用户id
     */
    List<ImUserVo> findByIds(List<Long> ids);

    /**
     * 按天统计用户注册数量
     *
     * @param days 统计天数
     * @return 统计结果
     */
    List<Map<String, Object>> getDailyRegistrationCount(Integer days);

    /**
     * 获取总用户数量
     * @return 总用户数量
     */
    Long getTotalUserCount();

    /**
     * 获取日活跃用户数量（最近1天）
     * @return 日活跃用户数量
     */
    Long getDailyActiveUserCount();

    /**
     * 获取周活跃用户数量（最近7天）
     * @return 周活跃用户数量
     */
    Long getWeeklyActiveUserCount();

    /**
     * 获取月活跃用户数量（最近30天）
     * @return 月活跃用户数量
     */
    Long getMonthlyActiveUserCount();

    /**
     * 今日签到用户数
     * @return 今日签到用户数
     */
    Long getTodayCheckinUserCount();

    /**
     * 按天统计签到用户数
     * @param days 统计天数
     * @return 每日签到用户数
     */
    List<Map<String, Object>> getDailyCheckinUserCount(Integer days);

    /**
     * 今日发送消息的用户数（私聊+群聊去重）
     * @return 今日发消息用户数
     */
    Long getTodayMessageUserCount();

    /**
     * 按天统计发送消息的用户数（私聊+群聊去重）
     * @param days 统计天数
     * @return 每日发消息用户数
     */
    List<Map<String, Object>> getDailyMessageUserCount(Integer days);

}
