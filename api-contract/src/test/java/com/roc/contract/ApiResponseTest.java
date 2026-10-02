package com.roc.contract;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author roc
 * @since 2026/10/2
 */
public class ApiResponseTest {

    @Test
    public void success_shouldCarryCodeZeroAndOkMessage() {
        ApiResponse response = ApiResponse.success("payload");

        assertTrue(response.isSuccess());
        assertEquals(0, response.getCode());
        assertEquals("OK", response.getMessage());
        assertEquals("payload", response.getData());
    }

    @Test
    public void fail_shouldCarryFailCodeAndMessage() {
        ApiResponse response = ApiResponse.fail("验签失败");

        assertFalse(response.isSuccess());
        assertEquals(-1, response.getCode());
        assertEquals("验签失败", response.getMessage());
        assertNull(response.getData());
    }
}
