package com.zl.mjga.unit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zl.mjga.exception.BusinessException;
import com.zl.mjga.repository.PermissionRepository;
import com.zl.mjga.repository.RolePermissionMapRepository;
import com.zl.mjga.repository.RoleRepository;
import com.zl.mjga.repository.UserRepository;
import com.zl.mjga.repository.UserRoleMapRepository;
import com.zl.mjga.service.UserRolePermissionService;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.jooq.generated.api_gateway.tables.pojos.Role;
import org.jooq.generated.api_gateway.tables.pojos.UserRoleMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 管理员申请/审批状态机测试：申请绑定 PENDING_ADMIN，审批置换为 ADMIN，驳回移除，全程保留已有角色
 *
 * @author roc
 * @since 2026/10/3
 */
@ExtendWith(MockitoExtension.class)
class AdminApplicationFlowTest {

    private static final Long USER_ID = 1L;
    private static final Role ADMIN_ROLE = role(1L, "ADMIN");
    private static final Role GENERAL_ROLE = role(2L, "GENERAL");
    private static final Role PENDING_ROLE = role(3L, "PENDING_ADMIN");

    @Mock private UserRepository userRepository;

    @Mock private RoleRepository roleRepository;

    @Mock private UserRoleMapRepository userRoleMapRepository;

    @Mock private PermissionRepository permissionRepository;

    @Mock private RolePermissionMapRepository rolePermissionMapRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks @Spy private UserRolePermissionService userRolePermissionService;

    @Test
    void applyForAdmin_givenGeneralUser_shouldBindPendingAdminRole() {
        stubUserWithRoles(GENERAL_ROLE);
        stubRoleDirectory(GENERAL_ROLE, PENDING_ROLE, ADMIN_ROLE);
        when(roleRepository.fetchOneByCode("PENDING_ADMIN")).thenReturn(PENDING_ROLE);

        userRolePermissionService.applyForAdmin(USER_ID);

        verify(userRolePermissionService).bindRoleToUser(USER_ID, List.of(2L, 3L));
    }

    @Test
    void applyForAdmin_givenAdmin_shouldThrow() {
        stubUserWithRoles(ADMIN_ROLE, GENERAL_ROLE);

        assertThatThrownBy(() -> userRolePermissionService.applyForAdmin(USER_ID))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void applyForAdmin_givenPendingUser_shouldThrow() {
        stubUserWithRoles(GENERAL_ROLE, PENDING_ROLE);

        assertThatThrownBy(() -> userRolePermissionService.applyForAdmin(USER_ID))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void approveAdminApplication_givenPendingUser_shouldReplaceWithAdmin() {
        stubUserWithRoles(GENERAL_ROLE, PENDING_ROLE);
        stubRoleDirectory(GENERAL_ROLE, PENDING_ROLE, ADMIN_ROLE);
        when(roleRepository.fetchOneByCode("ADMIN")).thenReturn(ADMIN_ROLE);

        userRolePermissionService.approveAdminApplication(USER_ID);

        verify(userRolePermissionService).bindRoleToUser(USER_ID, List.of(2L, 1L));
    }

    @Test
    void approveAdminApplication_givenNoPendingRole_shouldThrow() {
        stubUserWithRoles(GENERAL_ROLE);

        assertThatThrownBy(() -> userRolePermissionService.approveAdminApplication(USER_ID))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void rejectAdminApplication_givenPendingUser_shouldRemovePendingRole() {
        stubUserWithRoles(GENERAL_ROLE, PENDING_ROLE);
        stubRoleDirectory(GENERAL_ROLE, PENDING_ROLE, ADMIN_ROLE);

        userRolePermissionService.rejectAdminApplication(USER_ID);

        verify(userRolePermissionService).bindRoleToUser(USER_ID, List.of(2L));
    }

    @Test
    void applyForAdmin_givenUnknownUser_shouldThrow() {
        when(userRepository.fetchOneById(USER_ID)).thenReturn(null);

        assertThatThrownBy(() -> userRolePermissionService.applyForAdmin(USER_ID))
                .isInstanceOf(BusinessException.class);
    }

    private void stubUserWithRoles(Role... roles) {
        org.jooq.generated.api_gateway.tables.pojos.User user =
                new org.jooq.generated.api_gateway.tables.pojos.User();
        user.setId(USER_ID);
        user.setUsername("tester");
        when(userRepository.fetchOneById(USER_ID)).thenReturn(user);
        List<UserRoleMap> maps =
                Arrays.stream(roles)
                        .map(
                                role -> {
                                    UserRoleMap map = new UserRoleMap();
                                    map.setUserId(USER_ID);
                                    map.setRoleId(role.getId());
                                    return map;
                                })
                        .collect(Collectors.toList());
        when(userRoleMapRepository.fetchByUserId(USER_ID)).thenReturn(maps);
    }

    /** 角色目录 stub：按请求的 id 列表返回已知角色（覆盖查询与全量回绑两类调用） */
    private void stubRoleDirectory(Role... knownRoles) {
        List<Role> directory = Arrays.asList(knownRoles);
        when(roleRepository.selectByRoleIdIn(anyList()))
                .thenAnswer(
                        invocation -> {
                            List<Long> requestedIds = invocation.getArgument(0);
                            return directory.stream()
                                    .filter(role -> requestedIds.contains(role.getId()))
                                    .collect(Collectors.toList());
                        });
    }

    private static Role role(Long id, String code) {
        Role role = new Role();
        role.setId(id);
        role.setCode(code);
        role.setName(code);
        return role;
    }
}
