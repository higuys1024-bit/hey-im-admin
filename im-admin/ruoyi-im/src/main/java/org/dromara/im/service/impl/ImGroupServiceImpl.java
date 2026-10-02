package org.dromara.im.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fhs.core.trans.anno.TransMethodResult;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.im.config.ImCacheConfig;
import org.dromara.im.constant.ImConstant;
import org.dromara.im.constant.ImRedisKey;
import org.dromara.im.domain.ImGroup;
import org.dromara.im.domain.bo.ImGroupBo;
import org.dromara.im.domain.dto.ImGroupBanDto;
import org.dromara.im.domain.dto.ImGroupCustomCountDto;
import org.dromara.im.domain.dto.ImGroupUnbanDto;
import org.dromara.im.domain.vo.ImGroupVo;
import org.dromara.im.mapper.ImGroupMapper;
import org.dromara.im.mq.ImRedisMQTemplate;
import org.dromara.im.service.IImGroupMemberService;
import org.dromara.im.service.IImGroupService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 群Service业务层处理
 *
 * @author Blue
 */
@DS(ImConstant.DS_IM_PLATFORM)
@CacheConfig(cacheManager = ImCacheConfig.REDIS_CACHE_MANAGER ,cacheNames = ImRedisKey.IM_CACHE_GROUP)
@RequiredArgsConstructor
@Service
public class ImGroupServiceImpl implements IImGroupService {

    private final ImGroupMapper baseMapper;

    private final ImRedisMQTemplate redisMQTemplate;


    private final IImGroupMemberService groupMemberService;
    /**
     * 查询群
     *
     * @param id 主键
     * @return 群
     */
    @Override
    public ImGroupVo queryById(Long id){
        return baseMapper.selectVoById(id);
    }

    /**
     * 分页查询群列表
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 群分页列表
     */
    @Override
    public TableDataInfo<ImGroupVo> queryPageList(ImGroupBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ImGroup> wrapper = buildQueryWrapper(bo);
        Page<ImGroupVo> result = baseMapper.selectVoPage(pageQuery.build(), wrapper);
        // 填充成员数量
        result.getRecords().forEach(vo -> vo.setMemberCount(groupMemberService.findCountByGroupId(vo.getId())));
        return TableDataInfo.build(result);
    }

    /**
     * 查询符合条件的群列表
     *
     * @param bo 查询条件
     * @return 群列表
     */
    @TransMethodResult
    @Override
    public List<ImGroupVo> queryList(ImGroupBo bo) {
        LambdaQueryWrapper<ImGroup> wrapper = buildQueryWrapper(bo);
        return baseMapper.selectVoList(wrapper);
    }


    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(key = "#dto.getId()")
    @Override
    public void ban(ImGroupBanDto dto) {
        LambdaUpdateWrapper<ImGroup> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(ImGroup::getId, dto.getId());
        wrapper.set(ImGroup::getIsBanned, true);
        wrapper.set(ImGroup::getReason, dto.getReason());
        baseMapper.update(wrapper);
        // 推送到处理队列，等待im-platfrom处理
        redisMQTemplate.opsForList().rightPush(ImRedisKey.IM_QUEUE_GROUP_BANNED, dto);
    }

    @CacheEvict(key = "#dto.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unban(ImGroupUnbanDto dto) {
        LambdaUpdateWrapper<ImGroup> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(ImGroup::getId, dto.getId());
        wrapper.set(ImGroup::getIsBanned, false);
        wrapper.set(ImGroup::getReason, Strings.EMPTY);
        baseMapper.update(wrapper);
        // 推送到处理队列，等待im-platfrom处理
        redisMQTemplate.opsForList().rightPush(ImRedisKey.IM_QUEUE_GROUP_UNBAN, dto);
    }


    @Override
    public List<ImGroupVo> findByName(String name) {
        // 取出用户名或昵称匹配的前10条
        LambdaQueryWrapper<ImGroup> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(StrUtil.isNotEmpty(name), ImGroup::getName, name);
        queryWrapper.select(ImGroup::getId, ImGroup::getName);
        queryWrapper.orderByDesc(ImGroup::getId);
        queryWrapper.last("limit 10");
        return  baseMapper.selectVoList(queryWrapper);
    }

    /**
     * 获取总群组数量
     * @return 总群组数量
     */
    @Override
    public Long getTotalGroupCount() {
        return baseMapper.selectCount(null);
    }

    @CacheEvict(key = "#dto.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCustomMemberCount(ImGroupCustomCountDto dto) {
        LambdaUpdateWrapper<ImGroup> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(ImGroup::getId, dto.getId());
        wrapper.set(ImGroup::getCustomMemberCount, dto.getCustomMemberCount() == null ? 0 : dto.getCustomMemberCount());
        baseMapper.update(wrapper);
    }

    private LambdaQueryWrapper<ImGroup> buildQueryWrapper(ImGroupBo bo) {
        Map<String, Object> params =  bo.getParams();
        LambdaQueryWrapper<ImGroup> wrapper = Wrappers.lambdaQuery();
        wrapper.like(StringUtils.isNotBlank(bo.getName()), ImGroup::getName, bo.getName());
        wrapper.eq(bo.getOwnerId() != null, ImGroup::getOwnerId, bo.getOwnerId());
        wrapper.eq(bo.getDissolve() != null, ImGroup::getDissolve, bo.getDissolve());
        wrapper.eq(bo.getCreatedTime() != null, ImGroup::getCreatedTime, bo.getCreatedTime());
        wrapper.eq(bo.getIsBanned() != null, ImGroup::getIsBanned, bo.getIsBanned());
        wrapper.eq(StringUtils.isNotBlank(bo.getReason()), ImGroup::getReason, bo.getReason());
        wrapper.between(params.get("beginTime") != null && params.get("endTime") != null,
            ImGroup::getCreatedTime, params.get("beginTime"), params.get("endTime"));
        wrapper.orderByDesc(ImGroup::getCreatedTime);
        return wrapper;
    }

}
