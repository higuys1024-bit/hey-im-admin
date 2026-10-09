package org.dromara.im.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 签到记录统计Mapper接口
 * 对应IM库的 im_checkin_record 表，仅用于后台数据统计（无实体CRUD需求）
 *
 * @author Blue
 */
public interface ImCheckinRecordMapper {

    /**
     * 今日签到用户数（同一用户当天多次签到只计一次，由唯一键保证一人一天一条）
     *
     * @return 今日签到用户数
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM im_checkin_record WHERE checkin_date = CURDATE()")
    Long getTodayCheckinUserCount();

    /**
     * 按天统计签到用户数
     *
     * @param days 统计天数
     * @return 每日签到用户数 [{date, count}]
     */
    @Select("SELECT checkin_date AS date, COUNT(DISTINCT user_id) AS count " +
        "FROM im_checkin_record " +
        "WHERE checkin_date >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
        "GROUP BY checkin_date " +
        "ORDER BY date ASC")
    List<Map<String, Object>> getDailyCheckinUserCount(@Param("days") Integer days);

}
