package com.roc.contract;

import java.util.Objects;

/**
 * 模拟接口 /api/name 的请求体契约。
 *
 * @author roc
 * @since 2026/10/2
 */
public class User {

    private String username;

    public User() {}

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User that)) {
            return false;
        }
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return "User(username=" + username + ")";
    }
}
