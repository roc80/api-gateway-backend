package com.zl.mjga.service;

import com.zl.mjga.dto.sign.SignInDto;
import com.zl.mjga.dto.sign.SignUpDto;
import com.zl.mjga.exception.BusinessException;
import com.zl.mjga.model.urp.ERole;
import com.zl.mjga.repository.UserRepository;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.generated.api_gateway.tables.pojos.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final UserRolePermissionService userRolePermissionService;

    public Long signIn(SignInDto signInDto) {
        User user = userRepository.fetchOneByUsername(signInDto.getUsername());
        if (user == null) {
            throw new BusinessException(
                    String.format("%s user not found", signInDto.getUsername()));
        }
        if (!user.getEnable()) {
            throw new BusinessException("user disabled");
        }
        if (!passwordEncoder.matches(signInDto.getPassword(), user.getPassword())) {
            throw new BusinessException("password invalid");
        }
        return user.getId();
    }

    @Transactional(rollbackFor = Throwable.class)
    public void signUp(SignUpDto signUpDto) {
        if (userRolePermissionService.isUsernameDuplicate(signUpDto.getUsername())) {
            throw new BusinessException(
                    String.format("username %s already exist", signUpDto.getUsername()));
        }
        User user = new User();
        user.setUsername(signUpDto.getUsername());
        user.setPassword(passwordEncoder.encode(signUpDto.getPassword()));
        genAkSk(user);
        userRepository.insert(user);
        User insertUser = userRepository.fetchOneByUsername(signUpDto.getUsername());
        userRolePermissionService.bindRoleModuleToUser(insertUser.getId(), List.of(ERole.GENERAL));
    }

    private void genAkSk(User user) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[20];
        secureRandom.nextBytes(bytes);
        String accessKey = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        user.setAccessKey(accessKey);
        bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String secretKey = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        user.setSecretKey(secretKey);
    }
}
