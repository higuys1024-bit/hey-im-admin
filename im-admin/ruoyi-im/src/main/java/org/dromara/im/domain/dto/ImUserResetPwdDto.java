package org.dromara.im.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置用户登录密码
 *
 * @author: Blue
 * @version: 1.0
 */
@Data
@Schema(description = "重置用户登录密码")
public class ImUserResetPwdDto {

    @NotNull(message = "用户id不可为空")
    @Schema(description = "用户id")
    private Long id;

    @NotBlank(message = "新密码不可为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    @Schema(description = "新登录密码")
    private String password;

}
