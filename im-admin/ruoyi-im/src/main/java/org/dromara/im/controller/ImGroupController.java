package org.dromara.im.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.fhs.core.trans.anno.TransMethodResult;
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
import org.dromara.im.domain.bo.ImGroupBo;
import org.dromara.im.domain.bo.ImGroupMemberBo;
import org.dromara.im.domain.dto.ImGroupBanDto;
import org.dromara.im.domain.dto.ImGroupCustomCountDto;
import org.dromara.im.domain.dto.ImGroupUnbanDto;
import org.dromara.im.domain.vo.ImGroupMemberVo;
import org.dromara.im.domain.vo.ImGroupVo;
import org.dromara.im.service.IImGroupMemberService;
import org.dromara.im.service.IImGroupService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 群
 *
 * @author Blue
 * @date 2024-12-22
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/im/group")
public class ImGroupController extends BaseController {

    private final IImGroupService groupService;
    private final IImGroupMemberService groupMemberService;

    /**
     * 查询群列表
     */
    @TransMethodResult
    @SaCheckPermission("im:group:list")
    @GetMapping("/list")
    public TableDataInfo<ImGroupVo> list(ImGroupBo bo, PageQuery pageQuery) {
        return groupService.queryPageList(bo, pageQuery);
    }

    /**
     * 导出群列表
     */
    @SaCheckPermission("im:group:export")
    @Log(title = "群", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(ImGroupBo bo, HttpServletResponse response) {
        List<ImGroupVo> list = groupService.queryList(bo);
        ExcelUtil.exportExcel(list, "群", ImGroupVo.class, response);
    }

    /**
     * 获取群详细信息
     *
     * @param id 主键
     */
    @TransMethodResult
    @SaCheckPermission("im:group:query")
    @GetMapping("/{id}")
    public R<ImGroupVo> getInfo(@NotNull(message = "主键不能为空") @PathVariable Long id) {
        return R.ok(groupService.queryById(id));
    }


    @Operation(summary = "封禁")
    @PutMapping("/ban")
    @SaCheckPermission("im:group:ban")
    public void ban(@RequestBody @Valid ImGroupBanDto dto) {
        groupService.ban(dto);
    }

    @Operation(summary = "解封")
    @PutMapping("/unban")
    @SaCheckPermission("im:group:ban")
    public void unban(@RequestBody @Valid ImGroupUnbanDto dto) {
        groupService.unban(dto);
    }

    @Operation(summary = "设置群聊人数")
    @PutMapping("/customMemberCount")
    @SaCheckPermission("im:group:query")
    public R<Void> updateCustomMemberCount(@RequestBody @Valid ImGroupCustomCountDto dto) {
        groupService.updateCustomMemberCount(dto);
        return R.ok();
    }

    @GetMapping("/findByName")
    @Operation(summary = "根据名称查找群组",description = "用于下拉框选择群组")
    @SaCheckPermission("im:group:info")
    public R<List<ImGroupVo>> findByName(@RequestParam String name){
        List<ImGroupVo> vos = groupService.findByName(name);
        return R.ok(vos);
    }

    @GetMapping("/member/list")
    @Operation(summary = "分页查询群成员")
    @SaCheckPermission("im:group:member")
    public TableDataInfo<ImGroupMemberVo> findMembers( ImGroupMemberBo bo, PageQuery pageQuery) {
        return groupMemberService.queryPageList(bo,pageQuery);
    }

    /**
     * 获取总群组数量
     */
    @SaCheckPermission("im:group:list")
    @GetMapping("/totalCount")
    public R<Long> getTotalGroupCount() {
        return R.ok(groupService.getTotalGroupCount());
    }
}
