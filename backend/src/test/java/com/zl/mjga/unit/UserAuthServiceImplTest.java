package com.zl.mjga.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.roc.api.dto.UserAuthInfo;
import com.zl.mjga.repository.UserRepository;
import com.zl.mjga.service.UserAuthServiceImpl;
import org.jooq.generated.api_gateway.tables.pojos.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * @author roc
 * @since 2026/10/2
 */
@ExtendWith(MockitoExtension.class)
class UserAuthServiceImplTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private UserAuthServiceImpl userAuthService;

    @Test
    void getAuthByAccessKey_givenExistingUser_shouldReturnAuthInfo() {
        User user = new User();
        user.setUsername("dave");
        user.setSecretKey("sk-secret-01");
        user.setEnable(true);
        when(userRepository.fetchOneByAccessKey("ak-user-01")).thenReturn(user);

        UserAuthInfo authInfo = userAuthService.getAuthByAccessKey("ak-user-01");

        assertThat(authInfo)
                .extracting(
                        UserAuthInfo::getUsername,
                        UserAuthInfo::getSecretKey,
                        UserAuthInfo::getEnabled)
                .containsExactly("dave", "sk-secret-01", true);
    }

    @Test
    void getAuthByAccessKey_givenUnknownAccessKey_shouldReturnNull() {
        when(userRepository.fetchOneByAccessKey("unknown-ak")).thenReturn(null);

        assertThat(userAuthService.getAuthByAccessKey("unknown-ak")).isNull();
    }

    @Test
    void getAuthByAccessKey_givenBlankAccessKey_shouldReturnNullWithoutQuery() {
        assertThat(userAuthService.getAuthByAccessKey("  ")).isNull();

        verifyNoInteractions(userRepository);
    }
}
