package com.zl.mjga.service;

import com.roc.api.dto.UserAuthInfo;
import com.roc.api.service.UserAuthService;
import com.zl.mjga.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.jooq.generated.api_gateway.tables.pojos.User;

/**
 * 网关侧调用方认证查询 RPC 实现：按 accessKey 提供 secretKey 与启用状态，供网关验签
 *
 * @author roc
 * @since 2026/10/2
 */
@DubboService
public class UserAuthServiceImpl implements UserAuthService {

    private final UserRepository userRepository;

    public UserAuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserAuthInfo getAuthByAccessKey(String accessKey) {
        if (StringUtils.isBlank(accessKey)) {
            return null;
        }
        User user = userRepository.fetchOneByAccessKey(accessKey);
        if (user == null) {
            return null;
        }
        return new UserAuthInfo(user.getUsername(), user.getSecretKey(), user.getEnable());
    }
}
