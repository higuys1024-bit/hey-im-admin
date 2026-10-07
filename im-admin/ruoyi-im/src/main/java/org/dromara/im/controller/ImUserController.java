package org.dromara.im.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.excel.utils.ExcelUtil;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.im.domain.bo.ImUserBo;
import org.dromara.im.domain.dto.ImUserBanDto;
import org.dromara.im.domain.dto.ImUserResetPwdDto;
import org.dromara.im.domain.dto.ImUserUnbanDto;
import org.dromara.im.domain.vo.ImUserSubordinateVo;
import org.dromara.im.domain.vo.ImUserVo;
import org.dromara.im.service.IImUserService;
import org.dromara.im.util.CommaTextUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户
 *
 * @author Blue
 * @date 2024-12-22
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/im/user")
public class ImUserController extends BaseController {

    private final IImUserService userService;

    /**
     * 查询用户列表
     */
    @SaCheckPermission("im:user:list")
    @GetMapping("/list")
    public TableDataInfo<ImUserVo> list(ImUserBo bo, PageQuery pageQuery) {
        return userService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出用户列表
     */
    @SaCheckPermission("im:user:export")
    @Log(title = "用户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ImUserBo bo, HttpServletResponse response) {
        List<ImUserVo> list = userService.queryList(bo);
        ExcelUtil.exportExcel(list, "用户", ImUserVo.class, response);
    }

    /**
     * 获取用户详细信息
     *
     * @param id 主键
     */
    @SaCheckPermission("im:user:query")
    @GetMapping("/{id}")
    public R<ImUserVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(userService.queryById(id));
    }

    @Operation(summary = "账号封禁")
    @PutMapping("/ban")
    @SaCheckPermission("im:user:ban")
    public void ban(@RequestBody @Valid ImUserBanDto dto){
        userService.ban(dto);
    }


    @Operation(summary = "账号解封")
    @PutMapping("/unban")
    @SaCheckPermission("im:user:ban")
    public void unban(@RequestBody @Valid ImUserUnbanDto dto){
        userService.unban(dto);
    }

    @Operation(summary = "重置用户登录密码")
    @Log(title = "重置用户登录密码", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    @SaCheckPermission("im:user:resetPwd")
    public R<Void> resetPwd(@RequestBody @Valid ImUserResetPwdDto dto){
        userService.resetPassword(dto);
        return R.ok();
    }

    @Operation(summary = "查询用户的下级列表", description = "返回指定用户的所有直接下级用户：账号+姓名+注册时间+下级人数")
    @GetMapping("/subordinates")
    @SaCheckPermission("im:user:query")
    public R<List<ImUserSubordinateVo>> subordinates(@NotNull(message = "用户id不能为空") @RequestParam Long userId){
        return R.ok(userService.querySubordinates(userId));
    }

    @GetMapping("/findByName")
    @Operation(summary = "根据用户名查找用户",description = "用于下拉框选择用户")
    @SaCheckPermission("im:user:list")
    public R<List<ImUserVo>> findByName(@RequestParam String name){
        List<ImUserVo> vos = userService.findByName(name);
        return R.ok(vos);
    }

    @GetMapping("/findByIds")
    @Operation(summary = "根据id列表查找用户")
    @SaCheckPermission("im:user:list")
    public R<List<ImUserVo>> findByIds(@RequestParam String ids){
        List<Long> arrIds = CommaTextUtils.asList(ids).stream().map(Long::parseLong).collect(Collectors.toList());
        List<ImUserVo> vos = userService.findByIds(arrIds);
        return R.ok(vos);
    }

    /**
     * 按天统计用户注册数量
     *
     * @param days 统计天数，默认7天
     */
    @SaCheckPermission("im:user:list")
    @GetMapping("/dailyRegistrationCount")
    public R<List<Map<String, Object>>> getDailyRegistrationCount(@RequestParam(value = "days", defaultValue = "7") Integer days) {
        return R.ok(userService.getDailyRegistrationCount(days));
    }

    /**
     * 获取总用户数量
     */
    @SaCheckPermission("im:user:list")
    @GetMapping("/totalCount")
    public R<Long> getTotalUserCount() {
        return R.ok(userService.getTotalUserCount());
    }

    /**
     * 获取活跃用户统计（日活、周活、月活）
     */
    @SaCheckPermission("im:user:list")
    @GetMapping("/activeStats")
    public R<Map<String, Long>> getActiveUserStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("dailyActive", userService.getDailyActiveUserCount());
        stats.put("weeklyActive", userService.getWeeklyActiveUserCount());
        stats.put("monthlyActive", userService.getMonthlyActiveUserCount());
        return R.ok(stats);
    }
}
