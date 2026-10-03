package org.dromara.common.core.utils.ip;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.net.NetUtil;
import cn.hutool.http.HtmlUtil;
import org.dromara.common.core.utils.StringUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 获取地址类
 *
 * @author Lion Li
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressUtils {

    // 未知地址
    public static final String UNKNOWN = "XX XX";

    public static String getRealAddressByIP(String ip) {
        if (StringUtils.isBlank(ip)) {
            return UNKNOWN;
        }
        // 内网不查询
        ip = StringUtils.contains(ip, "0:0:0:0:0:0:0:1") ? "127.0.0.1" : HtmlUtil.cleanHtmlTag(ip);
        // 非合法 IPv4（公网 IPv6、多级代理拼接的 XFF、unknown 等）不做内网判断与离线定位，
        // 避免 Hutool 的 NetUtil.isInnerIP 抛出 "Invalid IPv4 address!" 导致登录等流程中断
        if (!Validator.isIpv4(ip)) {
            return UNKNOWN;
        }
        if (NetUtil.isInnerIP(ip)) {
            return "内网IP";
        }
        return RegionUtils.getCityInfo(ip);
    }
}
