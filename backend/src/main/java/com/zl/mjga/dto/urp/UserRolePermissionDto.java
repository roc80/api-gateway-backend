package com.zl.mjga.dto.urp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserRolePermissionDto {
    private Long id;
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private Boolean enable;
    private List<RoleDto> roles = new LinkedList<>();

    private OffsetDateTime createTime;

    public Set<PermissionDto> getPermissions() {
        return roles.stream()
                .flatMap((roleDto) -> roleDto.getPermissions().stream())
                .collect(Collectors.toSet());
    }
}
