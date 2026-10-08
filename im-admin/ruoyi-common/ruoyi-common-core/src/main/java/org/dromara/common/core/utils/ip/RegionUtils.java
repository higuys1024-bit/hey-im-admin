package org.dromara.common.core.utils.ip;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.service.Config;
import org.lionsoul.ip2region.service.Ip2Region;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 离线+在线三层兜底 IP 地址解析工具类
 * <p>
 * 查询链：
 * 1. ip2region 离线库（毫秒级，IPv4+IPv6）
 * 2. 若省市为空 → 异步 ip-api.com 在线补充（免费，无需 Key，支持中文，限 45req/min）
 * 3. 若在线查询失败 → 返回 ISP 兜底（如"中国·移动"）
 * <p>
 * 所需 resources 文件：
 *   ip2region_v4.xdb / ip2region.xdb
 *   ip2region_v6.xdb (可选，IPv6 离线解析)
 *
 * @author Lion Li / bx
 */
@Slf4j
public class RegionUtils {

    /** ip-api.com 免费接口，45次/分钟，无需 Key */
    private static final String ONLINE_API = "http://ip-api.com/json/%s?lang=zh-CN&fields=status,country,regionName,city";

    /** 进程内本地缓存：key=ip，value=地区名（规避频繁 HTTP 请求，TTL 由 JVM 生命周期管理） */
    private static final ConcurrentHashMap<String, String> LOCAL_CACHE = new ConcurrentHashMap<>(512);

    /** 单线程池，异步执行在线补充查询，避免阻塞业务主线程 */
    private static final Executor ASYNC_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "admin-ip-region-fallback");
        t.setDaemon(true);
        return t;
    });

    /** ip2region 全局服务实例（并发安全） */
    private static volatile Ip2Region ip2Region;

    static {
        try {
            Config v4Config = buildV4Config();
            Config v6Config = buildV6Config();
            if (v4Config != null) {
                ip2Region = Ip2Region.create(v4Config, v6Config);
                log.info("RegionUtils: ip2region 服务初始化成功（IPv4={}, IPv6={}）", v4Config != null, v6Config != null);
            } else {
                log.warn("RegionUtils: ip2region_v4.xdb 或 ip2region.xdb 未找到，请确保 resources 中存在该文件");
            }
        } catch (Exception e) {
            log.error("RegionUtils: 初始化 ip2region 服务失败: {}", e.getMessage(), e);
        }
    }

    // ==================== 初始化 ====================

    private static Config buildV4Config() throws Exception {
        ClassPathResource res = new ClassPathResource("ip2region_v4.xdb");
        if (!res.exists()) {
            res = new ClassPathResource("ip2region.xdb");
        }
        if (!res.exists()) {
            return null;
        }
        return Config.custom().setCachePolicy(Config.BufferCache)
                .setXdbFile(toTempFile(res.getInputStream(), "ip2r_v4_")).asV4();
    }

    private static Config buildV6Config() {
        try {
            ClassPathResource res = new ClassPathResource("ip2region_v6.xdb");
            if (!res.exists()) {
                log.warn("RegionUtils: ip2region_v6.xdb 不存在，IPv6 将降级为在线查询");
                return null;
            }
            return Config.custom().setCachePolicy(Config.BufferCache)
                    .setXdbFile(toTempFile(res.getInputStream(), "ip2r_v6_")).asV6();
        } catch (Exception e) {
            log.warn("RegionUtils: 初始化 v6 config 失败: {}", e.getMessage());
            return null;
        }
    }

    private static File toTempFile(InputStream in, String prefix) throws Exception {
        File f = File.createTempFile(prefix, ".xdb");
        f.deleteOnExit();
        try (InputStream is = in) {
            Files.copy(is, f.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return f;
    }

    // ==================== 公共查询接口 ====================

    /**
     * 同步查询 IP 归属地（优先离线，省市缺失时触发异步在线补充）
     *
     * @param ip IPv4 或 IPv6 字符串
     * @return 地区名，如"广东广州"/"北京"/"中国·移动"/"内网IP"/"未知"
     */
    public static String getCityInfo(String ip) {
        if (StrUtil.isBlank(ip)) {
            return "未知";
        }
        ip = ip.trim();

        // 1. 内网/回环地址快速返回
        if (isPrivateIp(ip)) {
            return "内网IP";
        }

        // 2. 先查进程内缓存（含之前在线查询结果）
        String cached = LOCAL_CACHE.get(ip);
        if (cached != null) {
            return cached;
        }

        // 3. 离线 ip2region 查询
        String offlineResult = queryOffline(ip);

        // 4. 判断离线结果是否已有省市信息
        if (isDetailed(offlineResult)) {
            LOCAL_CACHE.put(ip, offlineResult);
            return offlineResult;
        }

        // 5. 省市信息缺失 → 先将离线兜底结果写入本地缓存（防止高并发/列表遍历重复穿透），再异步尝试在线补充
        LOCAL_CACHE.put(ip, StrUtil.isNotBlank(offlineResult) ? offlineResult : "未知");
        final String ipFinal = ip;
        final String fallback = offlineResult;
        CompletableFuture.runAsync(() -> {
            String online = queryOnline(ipFinal);
            if (isDetailed(online) || (StrUtil.isNotBlank(online) && !"未知".equals(online))) {
                LOCAL_CACHE.put(ipFinal, online);
                log.debug("RegionUtils: 在线补充 IP 归属地成功: {} => {}", ipFinal, online);
            } else if (StrUtil.isNotBlank(fallback) && !"未知".equals(fallback)) {
                LOCAL_CACHE.put(ipFinal, fallback);
            }
        }, ASYNC_EXECUTOR);

        return offlineResult;
    }

    /**
     * 同步强制在线补充查询（适用于后台等需要立即展示精准地址的场景）
     */
    public static String getCityInfoSync(String ip) {
        if (StrUtil.isBlank(ip)) {
            return "未知";
        }
        ip = ip.trim();
        if (isPrivateIp(ip)) {
            return "内网IP";
        }

        String cached = LOCAL_CACHE.get(ip);
        if (cached != null && isDetailed(cached)) {
            return cached;
        }

        String offline = queryOffline(ip);
        if (isDetailed(offline)) {
            LOCAL_CACHE.put(ip, offline);
            return offline;
        }

        String online = queryOnline(ip);
        String result = (isDetailed(online) || (StrUtil.isNotBlank(online) && !"未知".equals(online))) ? online : offline;
        if (StrUtil.isNotBlank(result) && !"未知".equals(result)) {
            LOCAL_CACHE.put(ip, result);
        }
        return result;
    }

    // ==================== 私有方法 ====================

    /** 离线查询 ip2region */
    private static String queryOffline(String ip) {
        if (ip2Region == null) {
            return "未知";
        }
        try {
            String region = ip2Region.search(ip);
            return StrUtil.isBlank(region) ? "未知" : parseOfflineRegion(region);
        } catch (Exception e) {
            log.debug("RegionUtils: 离线 IP 查询失败: {} - {}", ip, e.getMessage());
            return "未知";
        }
    }

    /**
     * 在线查询 ip-api.com（中文，免费，45次/分钟）
     */
    private static String queryOnline(String ip) {
        try {
            String url = String.format(ONLINE_API, ip);
            try (HttpResponse resp = HttpRequest.get(url).timeout(3000).execute()) {
                if (!resp.isOk()) {
                    return "未知";
                }
                JSONObject json = JSONUtil.parseObj(resp.body());
                if (!"success".equals(json.getStr("status"))) {
                    return "未知";
                }

                String country  = StrUtil.nullToEmpty(json.getStr("country"));
                String province = StrUtil.nullToEmpty(json.getStr("regionName"));
                String city     = StrUtil.nullToEmpty(json.getStr("city"));

                StringBuilder sb = new StringBuilder();
                if (StrUtil.isNotBlank(country) && !"中国".equals(country)) {
                    sb.append(country).append("·");
                }
                if (StrUtil.isNotBlank(province)) {
                    sb.append(province);
                }
                if (StrUtil.isNotBlank(city) && !city.equals(province)) {
                    sb.append(city);
                }
                String result = sb.toString();
                return StrUtil.isNotBlank(result) ? result : (StrUtil.isNotBlank(country) ? country : "未知");
            }
        } catch (Exception e) {
            log.debug("RegionUtils: 在线 IP 查询失败: {} - {}", ip, e.getMessage());
            return "未知";
        }
    }

    /**
     * 解析 ip2region 离线返回的原始字符串 "国家|区域|省份|城市|ISP"
     * 当省市为空时，保留 ISP 作为兜底描述（如"中国·移动"）
     */
    private static String parseOfflineRegion(String region) {
        String[] parts = region.split("\\|");
        String country  = get(parts, 0);
        String province = get(parts, 2);
        String city     = get(parts, 3);
        String isp      = get(parts, 4);

        StringBuilder sb = new StringBuilder();
        boolean isChina = "中国".equals(country);

        if (StrUtil.isNotBlank(country) && !isChina) {
            sb.append(country);
        }
        if (StrUtil.isNotBlank(province)) {
            sb.append(province);
        }
        if (StrUtil.isNotBlank(city) && !city.equals(province)) {
            sb.append(city);
        }

        String result = sb.toString();
        if (StrUtil.isNotBlank(result)) {
            return result;
        }

        // 省市为空时，用 国家+ISP 兜底
        if (isChina && StrUtil.isNotBlank(isp)) {
            return "中国·" + isp;
        }
        return StrUtil.isNotBlank(country) ? country : "未知";
    }

    /** 判断查询结果是否含有省级以上的精确信息 */
    private static boolean isDetailed(String result) {
        if (StrUtil.isBlank(result) || "未知".equals(result)) {
            return false;
        }
        return result.length() > 2 && !result.equals("中国") && !result.startsWith("中国·");
    }

    /** 判断是否为内网/回环地址 */
    private static boolean isPrivateIp(String ip) {
        return "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)
                || ip.startsWith("192.168.") || ip.startsWith("10.")
                || ip.startsWith("172.16.") || ip.startsWith("172.17.")
                || ip.startsWith("172.18.") || ip.startsWith("172.19.")
                || ip.startsWith("172.2") || ip.startsWith("172.30.") || ip.startsWith("172.31.")
                || ip.startsWith("fe80:") || ip.startsWith("fc00:") || ip.startsWith("fd");
    }

    /** 安全获取数组元素，"0" 转空串 */
    private static String get(String[] arr, int idx) {
        if (arr.length <= idx) {
            return "";
        }
        String v = arr[idx].trim();
        return "0".equals(v) ? "" : v;
    }
}
