package org.dromara.im.runner;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.ip.RegionUtils;
import org.dromara.im.constant.ImConstant;
import org.dromara.im.domain.ImUser;
import org.dromara.im.mapper.ImUserMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时为尚未填充 last_login_region 的历史用户自动解析并回填落库
 * 杜绝运行时动态解析与缓存维护，实现完全落库固化
 *
 * @author bx
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DS(ImConstant.DS_IM_PLATFORM)
public class ImUserRegionInitRunner implements ApplicationRunner {

    private final ImUserMapper userMapper;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // 查询 last_login_ip 不为空且 last_login_region 为空的历史用户
            List<ImUser> uninitializedUsers = userMapper.selectList(new LambdaQueryWrapper<ImUser>()
                .isNotNull(ImUser::getLastLoginIp)
                .ne(ImUser::getLastLoginIp, "")
                .and(w -> w.isNull(ImUser::getLastLoginRegion).or().eq(ImUser::getLastLoginRegion, "")));

            if (CollUtil.isNotEmpty(uninitializedUsers)) {
                log.info("【用户归属地初始化】检测到 {} 位历史用户未填充 last_login_region，开始自动解析并回填落库...", uninitializedUsers.size());
                int count = 0;
                for (ImUser user : uninitializedUsers) {
                    String region = RegionUtils.getCityInfo(user.getLastLoginIp());
                    if (StrUtil.isNotBlank(region)) {
                        userMapper.update(null, new LambdaUpdateWrapper<ImUser>()
                            .eq(ImUser::getId, user.getId())
                            .set(ImUser::getLastLoginRegion, region));
                        count++;
                    }
                }
                log.info("【用户归属地初始化】完成！共成功回填 {} 位历史用户的归属地到数据库。", count);
            } else {
                log.info("【用户归属地初始化】所有历史用户的归属地均已持久化，无需回填。");
            }
        } catch (Exception e) {
            log.error("【用户归属地初始化】执行异常: {}", e.getMessage(), e);
        }
    }
}
