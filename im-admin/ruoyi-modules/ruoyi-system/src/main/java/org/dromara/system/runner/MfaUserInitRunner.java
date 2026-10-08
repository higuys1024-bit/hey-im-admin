package org.dromara.system.runner;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.utils.mfa.MfaUtils;
import org.dromara.common.tenant.helper.TenantHelper;
import org.dromara.system.domain.SysUser;
import org.dromara.system.mapper.SysUserMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时为尚未配置 MFA 密钥的历史用户自动生成初始化密钥
 *
 * @author Lion Li
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MfaUserInitRunner implements ApplicationRunner {

    private final SysUserMapper userMapper;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // 忽略多租户行级拦截器，扫描全库所有租户的历史用户
            TenantHelper.ignore(() -> {
                List<SysUser> usersWithoutMfa = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
                    .and(w -> w.isNull(SysUser::getMfaSecret).or().eq(SysUser::getMfaSecret, "")));
                if (CollUtil.isNotEmpty(usersWithoutMfa)) {
                    log.info("【MFA初始化】检测到 {} 位用户未配置 MFA 密钥，开始自动生成并落库...", usersWithoutMfa.size());
                    int count = 0;
                    for (SysUser user : usersWithoutMfa) {
                        String secret = MfaUtils.generateSecret();
                        user.setMfaSecret(secret);
                        userMapper.updateById(user);
                        count++;
                        log.info("【MFA初始化】已为用户 [userId={}, userName={}] 生成 MFA 密钥: {}",
                            user.getUserId(), user.getUserName(), secret);
                    }
                    log.info("【MFA初始化】完成！共初始化 {} 位用户的 MFA 密钥。", count);
                } else {
                    log.info("【MFA初始化】所有用户均已具备 MFA 密钥，无需初始化。");
                }
            });
        } catch (Exception e) {
            log.error("【MFA初始化】初始化已有用户 MFA 密钥异常: {}", e.getMessage(), e);
        }
    }
}
