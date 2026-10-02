package com.roc.contract;

import java.util.Objects;

/**
 * 平台 API 通用响应包装。
 *
 * <p>与 api-client-sdk 中定义保持一致：成功码为 0，失败码为 -1。
 *
 * @author roc
 * @since 2026/10/2
 */
public class ApiResponse {

    public static final int SUCCESS_CODE = 0;
    public static final int FAIL_CODE = -1;

    private int code;
    private String message;
    private Object data;

    public ApiResponse() {}

    public ApiResponse(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static ApiResponse fail(String message) {
        return new ApiResponse(FAIL_CODE, message, null);
    }

    public static ApiResponse success(Object data) {
        return new ApiResponse(SUCCESS_CODE, "OK", data);
    }

    public boolean isSuccess() {
        return this.code == SUCCESS_CODE;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApiResponse that)) {
            return false;
        }
        return code == that.code
                && Objects.equals(message, that.message)
                && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, data);
    }

    @Override
    public String toString() {
        return "ApiResponse(code=" + code + ", message=" + message + ", data=" + data + ")";
    }
}
