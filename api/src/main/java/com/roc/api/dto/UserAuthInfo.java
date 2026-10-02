package com.roc.api.dto;

import java.io.Serializable;

/**
 * 网关调用方认证信息（RPC 传输对象）
 *
 * @author roc
 * @since 2026/10/2
 */
public class UserAuthInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String secretKey;
    private Boolean enabled;

    public UserAuthInfo() {}

    public UserAuthInfo(String username, String secretKey, Boolean enabled) {
        this.username = username;
        this.secretKey = secretKey;
        this.enabled = enabled;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        // 不输出 secretKey，避免敏感信息进入日志
        return "UserAuthInfo(username=" + username + ", secretKey=***, enabled=" + enabled + ")";
    }
}
