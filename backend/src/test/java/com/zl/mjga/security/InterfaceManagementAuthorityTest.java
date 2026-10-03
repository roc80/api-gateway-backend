package com.zl.mjga.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zl.mjga.client.GatewayApiClient;
import com.zl.mjga.config.security.HttpFireWallConfig;
import com.zl.mjga.config.security.Jwt;
import com.zl.mjga.config.security.UserDetailsServiceImpl;
import com.zl.mjga.config.security.WebSecurityConfig;
import com.zl.mjga.controller.InterfaceController;
import com.zl.mjga.controller.UserRolePermissionController;
import com.zl.mjga.repository.RoleRepository;
import com.zl.mjga.repository.UserRepository;
import com.zl.mjga.service.InterfaceCallLogService;
import com.zl.mjga.service.InterfaceService;
import com.zl.mjga.service.UserRolePermissionService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 接口管理写权限与管理员申请端点的鉴权测试
 *
 * @author roc
 * @since 2026/10/3
 */
@WebMvcTest(value = {InterfaceController.class, UserRolePermissionController.class})
@Import({WebSecurityConfig.class, HttpFireWallConfig.class})
class InterfaceManagementAuthorityTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private InterfaceService interfaceService;

    @MockBean private InterfaceCallLogService interfaceCallLogService;

    @MockBean private GatewayApiClient gatewayApiClient;

    @MockBean private UserRepository userRepository;

    @MockBean private RoleRepository roleRepository;

    @MockBean private UserRolePermissionService userRolePermissionService;

    @MockBean private Jwt jwt;

    @MockBean private UserDetailsServiceImpl userDetailsService;

    private static final String CREATE_INTERFACE_BODY =
            "{\"name\":\"测试接口\", \"code\":\"test-code\", \"owner\":\"roc\"}";

    @Test
    void givenNoToken_whenCreateInterface_shouldReturn401() throws Exception {
        mockMvc.perform(
                        post("/interfaces")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(CREATE_INTERFACE_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenUserWithoutWriteInterface_whenCreateInterface_shouldReturn403() throws Exception {
        stubAuthorities();

        mockMvc.perform(
                        post("/interfaces")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(CREATE_INTERFACE_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenUserWithWriteInterface_whenCreateInterface_shouldReturn200() throws Exception {
        stubAuthorities("WRITE_INTERFACE");
        when(interfaceService.createInterface(any())).thenReturn(null);

        mockMvc.perform(
                        post("/interfaces")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(CREATE_INTERFACE_BODY))
                .andExpect(status().isOk());
    }

    @Test
    void givenAuthenticatedUser_whenApplyAdmin_shouldReturn200() throws Exception {
        stubAuthorities();
        org.jooq.generated.api_gateway.tables.pojos.User me =
                new org.jooq.generated.api_gateway.tables.pojos.User();
        me.setId(1L);
        me.setUsername("tester");
        when(userRepository.fetchOneByUsername("tester")).thenReturn(me);

        mockMvc.perform(post("/urp/me/apply-admin").with(csrf())).andExpect(status().isOk());
    }

    @Test
    void givenUserWithoutUrpWrite_whenApproveAdminApplication_shouldReturn403() throws Exception {
        stubAuthorities();

        mockMvc.perform(post("/urp/pending-admins/approve").param("userId", "2").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void givenUserWithUrpWrite_whenApproveAdminApplication_shouldReturn200() throws Exception {
        stubAuthorities("WRITE_USER_ROLE_PERMISSION");

        mockMvc.perform(post("/urp/pending-admins/approve").param("userId", "2").with(csrf()))
                .andExpect(status().isOk());
    }

    private void stubAuthorities(String... authorities) {
        List<SimpleGrantedAuthority> grantedAuthorities =
                Arrays.stream(authorities).map(SimpleGrantedAuthority::new).toList();
        User stubUser = new User("tester", "test_password", grantedAuthorities);
        when(jwt.extract(any(HttpServletRequest.class))).thenReturn("stub-token");
        when(jwt.getSubject(any(String.class))).thenReturn("1");
        when(jwt.verify(any(String.class))).thenReturn(Boolean.TRUE);
        when(userDetailsService.loadUserByUsername(any(String.class))).thenReturn(stubUser);
    }
}
