package com.zl.mjga.unit;

import com.roc.apiclientsdk.client.ApiClient;
import com.roc.apiclientsdk.module.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author roc
 * @since 2026/2/25 13:49
 */
public class SdkUnitTest {

    @Test
    public void test() {
        User user = new User();
        user.setUsername("username");
        ApiClient apiClient = new ApiClient("123", "456");
        Assertions.assertEquals("username", apiClient.getName(user));
    }
}
