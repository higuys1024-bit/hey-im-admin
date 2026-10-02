package org.dromara.im.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 设置群聊人数DTO
 */
@Data
@Schema(description = "设置群聊人数")
public class ImGroupCustomCountDto {

    @NotNull(message = "群组id不可为空")
    @Schema(description = "群组id")
    private Long id;

    @Schema(description = "自定义群聊人数(虚拟人数), 为0或null表示不设置")
    private Integer customMemberCount;
}
