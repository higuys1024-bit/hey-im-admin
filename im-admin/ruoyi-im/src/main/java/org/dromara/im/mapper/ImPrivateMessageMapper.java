package org.dromara.im.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.dromara.common.mybatis.core.mapper.BaseMapperPlus;
import org.dromara.im.domain.ImPrivateMessage;
import org.dromara.im.domain.vo.ImPrivateMessageVo;

import java.util.List;
import java.util.Map;

/**
 * 私聊消息Mapper接口
 *
 * @author Blue
 * @date 2024-12-22
 */
public interface ImPrivateMessageMapper extends BaseMapperPlus<ImPrivateMessage, ImPrivateMessageVo> {

    /**
     * 按天统计私聊消息量
     * @param days 统计天数
     * @return 统计结果
     */
    @Select("SELECT DATE(send_time) as date, COUNT(*) as count " +
        "FROM im_private_message " +
        "WHERE send_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
        "GROUP BY DATE(send_time) " +
        "ORDER BY date ASC")
    List<Map<String, Object>> getDailyMessageCount(@Param("days") Integer days);

    /**
     * 今日发送消息的用户数（私聊+群聊合并去重，同一用户当天发多条只计一次，排除系统提示消息type=21）
     *
     * @return 今日发消息用户数
     */
    @Select("SELECT COUNT(DISTINCT send_id) FROM ( " +
        "  SELECT send_id FROM im_private_message WHERE send_time >= CURDATE() AND type != 21 " +
        "  UNION ALL " +
        "  SELECT send_id FROM im_group_message WHERE send_time >= CURDATE() AND type != 21 " +
        ") t")
    Long getTodayMessageUserCount();

    /**
     * 按天统计发送消息的用户数（私聊+群聊合并，按天对发送人去重）
     *
     * @param days 统计天数
     * @return 每日发消息用户数 [{date, count}]
     */
    @Select("SELECT t.date AS date, COUNT(DISTINCT t.send_id) AS count FROM ( " +
        "  SELECT DATE(send_time) AS date, send_id FROM im_private_message " +
        "    WHERE send_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) AND type != 21 " +
        "  UNION ALL " +
        "  SELECT DATE(send_time) AS date, send_id FROM im_group_message " +
        "    WHERE send_time >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) AND type != 21 " +
        ") t " +
        "GROUP BY t.date " +
        "ORDER BY t.date ASC")
    List<Map<String, Object>> getDailyMessageUserCount(@Param("days") Integer days);
}
