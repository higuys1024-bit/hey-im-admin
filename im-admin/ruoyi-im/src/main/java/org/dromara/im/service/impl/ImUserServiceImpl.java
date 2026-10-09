package org.dromara.im.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.util.Strings;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.core.utils.ip.RegionUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.im.constant.ImConstant;
import org.dromara.im.constant.ImRedisKey;
import org.dromara.im.domain.ImUser;
import org.dromara.im.domain.bo.ImUserBo;
import org.dromara.im.domain.dto.ImUserBanDto;
import org.dromara.im.domain.dto.ImUserResetPwdDto;
import org.dromara.im.domain.dto.ImUserUnbanDto;
import org.dromara.im.domain.vo.ImUserSubordinateVo;
import org.dromara.im.domain.vo.ImUserVo;
import org.dromara.im.mapper.ImUserMapper;
import org.dromara.im.mq.ImRedisMQTemplate;
import org.dromara.im.service.IImUserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户Service业务层处理
 *
 * @author Blue
 * @date 2024-12-22
 */
@DS(ImConstant.DS_IM_PLATFORM)
@RequiredArgsConstructor
@Service
public class ImUserServiceImpl implements IImUserService {

    private final ImRedisMQTemplate redisMQTemplate;
    private final ImUserMapper baseMapper;

    /**
     * 查询用户
     *
     * @param id 主键
     * @return 用户
     */
    @Override
    public ImUserVo queryById(Long id){
        ImUserVo vo = baseMapper.selectVoById(id);
        fillLocation(vo);
        fillInviter(vo);
        return vo;
    }

    /**
     * 根据inviterId查询上级（邀请人）的账号与姓名，填充到VO
     */
    private void fillInviter(ImUserVo vo) {
        if (vo == null || vo.getInviterId() == null) {
            return;
        }
        ImUser inviter = baseMapper.selectById(vo.getInviterId());
        if (inviter != null) {
            vo.setInviterUserName(inviter.getUserName());
            vo.setInviterNickName(inviter.getNickName());
        }
    }

    /**
     * 根据数据库最后登录地区字段填充 location（0ms 读取，历史未填充数据离线解析兜底）
     */
    private void fillLocation(ImUserVo vo) {
        if (vo == null) {
            return;
        }
        if (StringUtils.isNotBlank(vo.getLastLoginRegion())) {
            vo.setLocation(vo.getLastLoginRegion());
        } else if (StringUtils.isNotBlank(vo.getLastLoginIp())) {
            vo.setLocation(RegionUtils.getCityInfo(vo.getLastLoginIp()));
        } else {
            vo.setLocation("未知");
        }
    }

    /**
     * 批量填充列表中所有用户的地址（直接读取数据库 last_login_region 字段，彻底免去 Redis 缓存与级联开销）
     */
    private void batchFillLocation(List<ImUserVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (ImUserVo vo : records) {
            fillLocation(vo);
        }
    }

    /**
     * 分页查询用户列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 用户分页列表
     */
    @Override
    public TableDataInfo<ImUserVo> queryPageList(ImUserBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ImUser> wrapper = buildQueryWrapper(bo);
        Page<ImUserVo> result = baseMapper.selectVoPage(pageQuery.build(), wrapper);
        if (result.getRecords() != null) {
            batchFillLocation(result.getRecords());
        }
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的用户列表
     *
     * @param bo 查询条件
     * @return 用户列表
     */
    @Override
    public List<ImUserVo> queryList(ImUserBo bo) {
        LambdaQueryWrapper<ImUser> wrapper = buildQueryWrapper(bo);
        List<ImUserVo> list = baseMapper.selectVoList(wrapper);
        batchFillLocation(list);
        return list;
    }


    @Override
    public void ban(ImUserBanDto dto) {
        LambdaUpdateWrapper<ImUser> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(ImUser::getId, dto.getId());
        wrapper.set(ImUser::getIsBanned, true);
        wrapper.set(ImUser::getReason, dto.getReason());
        baseMapper.update(wrapper);
        // 推送到处理队列，等待im-platfrom处理
        redisMQTemplate.opsForList().rightPush(ImRedisKey.IM_QUEUE_USER_BANNED, dto);
    }

    @Override
    public void unban(ImUserUnbanDto dto) {
        LambdaUpdateWrapper<ImUser> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(ImUser::getId, dto.getId());
        wrapper.set(ImUser::getIsBanned, false);
        wrapper.set(ImUser::getReason, Strings.EMPTY);
        baseMapper.update(wrapper);
    }

    @Override
    public void resetPassword(ImUserResetPwdDto dto) {
        // im_user中密码采用BCrypt存储（与im-platform的BCryptPasswordEncoder一致），这里使用相同算法加密
        String encrypted = BCrypt.hashpw(dto.getPassword());
        LambdaUpdateWrapper<ImUser> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(ImUser::getId, dto.getId());
        wrapper.set(ImUser::getPassword, encrypted);
        baseMapper.update(wrapper);
    }

    private LambdaQueryWrapper<ImUser> buildQueryWrapper(ImUserBo bo) {
        LambdaQueryWrapper<ImUser> wrapper = Wrappers.lambdaQuery();
        wrapper.like(StringUtils.isNotBlank(bo.getUserName()), ImUser::getUserName, bo.getUserName());
        wrapper.like(StringUtils.isNotBlank(bo.getNickName()), ImUser::getNickName, bo.getNickName());
        wrapper.eq(StringUtils.isNotBlank(bo.getInviteCode()), ImUser::getInviteCode, bo.getInviteCode());
        wrapper.orderByDesc(ImUser::getId);
        return wrapper;
    }

    @Override
    public List<ImUserSubordinateVo> querySubordinates(Long userId) {
        // 1.查出直接下级（inviter_id = userId）
        LambdaQueryWrapper<ImUser> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(ImUser::getInviterId, userId);
        wrapper.orderByDesc(ImUser::getCreatedTime);
        List<ImUser> subs = baseMapper.selectList(wrapper);
        if (subs.isEmpty()) {
            return new ArrayList<>();
        }
        // 2.一次性统计这些下级各自的下级人数（inviter_id in subIds group by inviter_id），避免N+1
        List<Long> subIds = subs.stream().map(ImUser::getId).collect(Collectors.toList());
        QueryWrapper<ImUser> countWrapper = Wrappers.query();
        countWrapper.select("inviter_id as inviterId, count(*) as cnt");
        countWrapper.in("inviter_id", subIds);
        countWrapper.groupBy("inviter_id");
        Map<Long, Long> childCountMap = new HashMap<>();
        for (Map<String, Object> row : baseMapper.selectMaps(countWrapper)) {
            Long inviterId = ((Number) row.get("inviterId")).longValue();
            Long cnt = ((Number) row.get("cnt")).longValue();
            childCountMap.put(inviterId, cnt);
        }
        // 3.组装VO
        return subs.stream().map(u -> {
            ImUserSubordinateVo vo = new ImUserSubordinateVo();
            vo.setId(u.getId());
            vo.setUserName(u.getUserName());
            vo.setNickName(u.getNickName());
            vo.setCreatedTime(u.getCreatedTime());
            vo.setSubordinateCount(childCountMap.getOrDefault(u.getId(), 0L));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ImUserVo> findByName(String name) {
        // 取出用户名或昵称匹配的前10条
        LambdaQueryWrapper<ImUser> queryWrapper = Wrappers.lambdaQuery();
        if(StrUtil.isNotEmpty(name)){
            queryWrapper.like(ImUser::getUserName, name);
        }
        queryWrapper.select(ImUser::getId, ImUser::getUserName);
        queryWrapper.orderByDesc(ImUser::getId);
        queryWrapper.last("limit 10");
        return baseMapper.selectVoList(queryWrapper);
    }

    @Override
    public List<ImUserVo> findByIds(List<Long> ids) {
        // 取出用户名或昵称匹配的前10条
        LambdaQueryWrapper<ImUser> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(ImUser::getId, ids);
        return baseMapper.selectVoList(queryWrapper);
    }

    /**
     * 按天统计用户注册数量
     *
     * @param days 统计天数
     * @return 统计结果
     */
    @Override
    public List<Map<String, Object>> getDailyRegistrationCount(Integer days) {
        if (days == null || days <= 0) {
            days = 7; // 默认统计最近7天
        }
        return baseMapper.getDailyRegistrationCount(days);
    }

    /**
     * 获取总用户数量
     *
     * @return 总用户数量
     */
    @Override
    public Long getTotalUserCount() {
        return baseMapper.selectCount(null);
    }


    /**
     * 获取日活跃用户数量（最近1天）
     *
     * @return 日活跃用户数量
     */
    @Override
    public Long getDailyActiveUserCount() {
        LambdaQueryWrapper<ImUser> wrapper = Wrappers.lambdaQuery();
        wrapper.ge(ImUser::getLastLoginTime, DateUtils.addDays(new Date(), -1));
        return baseMapper.selectCount(wrapper);
    }

    /**
     * 获取周活跃用户数量（最近7天）
     *
     * @return 周活跃用户数量
     */
    @Override
    public Long getWeeklyActiveUserCount() {
        LambdaQueryWrapper<ImUser> wrapper = Wrappers.lambdaQuery();
        wrapper.ge(ImUser::getLastLoginTime, DateUtils.addDays(new Date(), -7));
        return baseMapper.selectCount(wrapper);
    }

    /**
     * 获取月活跃用户数量（
     *
     * @return 月活跃用户数量
     */
    @Override
    public Long getMonthlyActiveUserCount() {
        LambdaQueryWrapper<ImUser> wrapper = Wrappers.lambdaQuery();
        wrapper.ge(ImUser::getLastLoginTime, DateUtils.addDays(new Date(), -30));
        return baseMapper.selectCount(wrapper);
    }
}
