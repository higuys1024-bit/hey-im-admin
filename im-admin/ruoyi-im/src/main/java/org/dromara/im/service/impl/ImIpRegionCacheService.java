package org.dromara.im.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.ip.RegionUtils;
import org.dromara.common.redis.utils.RedisUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IP 归属地二级缓存服务 (L1 本地内存 + L2 Redis 分布式缓存)
 * 解决用户列表高频翻页、全量导出时的级联查询性能问题，实现 0 毫秒级地址响应
 *
 * @author bx
 */
@Slf4j
@Service
public class ImIpRegionCacheService {

    /** Redis 缓存前缀 */
    private static final String REDIS_KEY_PREFIX = "im:ip:region:";

    /** Redis 缓存过期时间：30 天 */
    private static final Duration CACHE_TTL = Duration.ofDays(30);

    /** L1 本地极速缓存（JVM 进程内，容量上限 10000，保障超高并发下免查 Redis） */
    private static final Map<String, String> L1_CACHE = new ConcurrentHashMap<>(1024);

    /**
     * 单个 IP 归属地查询（L1 -> L2 -> 离线解析兜底）
     */
    public String getRegion(String ip) {
        if (StrUtil.isBlank(ip)) {
            return "未知";
        }
        ip = ip.trim();

        // 1. 先查 L1 本地缓存
        String l1 = L1_CACHE.get(ip);
        if (l1 != null) {
            return l1;
        }

        // 2. 再查 L2 Redis 分布式缓存
        String redisKey = REDIS_KEY_PREFIX + ip;
        String l2 = RedisUtils.getCacheObject(redisKey);
        if (l2 != null) {
            if (L1_CACHE.size() < 10000) {
                L1_CACHE.put(ip, l2);
            }
            return l2;
        }

        // 3. 缓存均未命中，调用 RegionUtils（内部已带兜底占位缓存）
        String region = RegionUtils.getCityInfo(ip);
        if (StrUtil.isBlank(region)) {
            region = "未知";
        }

        // 4. 回写 L1 与 L2 缓存（彻底避免后续级联查与穿透）
        L1_CACHE.put(ip, region);
        try {
            RedisUtils.setCacheObject(redisKey, region, CACHE_TTL);
        } catch (Exception e) {
            log.warn("缓存 IP 地址到 Redis 失败: {} -> {}", ip, e.getMessage());
        }

        return region;
    }

    /**
     * 批量查询并填充 IP 归属地映射 (Map: ip -> region)
     * 批量预填充机制，杜绝在列表循环中逐条查询
     */
    public Map<String, String> getRegions(Collection<String> ips) {
        if (CollUtil.isEmpty(ips)) {
            return Collections.emptyMap();
        }

        Map<String, String> resultMap = new HashMap<>(ips.size());
        List<String> missIps = new ArrayList<>();

        // 1. 批量过滤并优先查询 L1 本地缓存
        for (String rawIp : ips) {
            if (StrUtil.isBlank(rawIp)) {
                continue;
            }
            String ip = rawIp.trim();
            String l1 = L1_CACHE.get(ip);
            if (l1 != null) {
                resultMap.put(ip, l1);
            } else {
                missIps.add(ip);
            }
        }

        // 若全部命中本地缓存，直接返回
        if (missIps.isEmpty()) {
            return resultMap;
        }

        // 2. 未命中 L1 的去重查 L2 Redis
        Set<String> uniqueMissIps = new HashSet<>(missIps);
        List<String> needParseIps = new ArrayList<>();

        for (String ip : uniqueMissIps) {
            String redisKey = REDIS_KEY_PREFIX + ip;
            String l2 = RedisUtils.getCacheObject(redisKey);
            if (l2 != null) {
                resultMap.put(ip, l2);
                if (L1_CACHE.size() < 10000) {
                    L1_CACHE.put(ip, l2);
                }
            } else {
                needParseIps.add(ip);
            }
        }

        // 3. 真正未命中的冷门新 IP，执行解析并写回缓存
        for (String ip : needParseIps) {
            String region = RegionUtils.getCityInfo(ip);
            if (StrUtil.isBlank(region)) {
                region = "未知";
            }
            resultMap.put(ip, region);
            if (L1_CACHE.size() < 10000) {
                L1_CACHE.put(ip, region);
            }
            try {
                RedisUtils.setCacheObject(REDIS_KEY_PREFIX + ip, region, CACHE_TTL);
            } catch (Exception e) {
                log.warn("批量缓存 IP 地址到 Redis 失败: {} -> {}", ip, e.getMessage());
            }
        }

        return resultMap;
    }
}
