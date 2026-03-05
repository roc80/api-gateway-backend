package com.zl.mjga.dto.urp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author roc
 * @since 2026/1/19
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserUpsertDto {
    @Positive private Long id;
    @NotBlank private String username;
    private String password;
    @NotNull private Boolean enable;
}
