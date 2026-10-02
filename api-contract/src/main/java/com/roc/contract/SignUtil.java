package com.roc.contract;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * API 网关签名协议，服务端（网关验签）与调用端共用的权威实现。
 *
 * <p>与第三方 api-client-sdk（https://github.com/roc80/api-client-sdk）的签名算法逐位一致， 兼容性由 {@code
 * SignUtilTest} 中的固定向量保证，修改算法前请与 SDK 同步。
 *
 * @author roc
 * @since 2026/10/2
 */
public final class SignUtil {

    private SignUtil() {}

    /**
     * 生成请求签名
     *
     * @param accessKey 用户身份凭证
     * @param nonce 随机数，防重放
     * @param timestamp 时间戳，和随机数配合使用，防重放
     * @param secretKey 用户身份密钥
     * @param bodyJson 请求体JSON字符串
     * @return 对上述防篡改部分的SHA-512哈希值
     */
    public static byte[] genSignBySha512(
            String accessKey, String nonce, String timestamp, String secretKey, String bodyJson) {
        String data = accessKey + nonce + timestamp + secretKey + bodyJson;
        try {
            return MessageDigest.getInstance("SHA-512")
                    .digest(data.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("JVM does not provide SHA-512 digest", e);
        }
    }

    /**
     * 生成请求头的传输格式签名（sign 头的取值）
     *
     * <p>沿用 api-client-sdk 的 {@code Arrays.toString(byte[])} 格式，保证与存量调用方兼容。
     *
     * @param accessKey 用户身份凭证
     * @param nonce 随机数，防重放
     * @param timestamp 时间戳
     * @param secretKey 用户身份密钥
     * @param bodyJson 请求体JSON字符串
     * @return 签名字符串
     */
    public static String genSignString(
            String accessKey, String nonce, String timestamp, String secretKey, String bodyJson) {
        return Arrays.toString(genSignBySha512(accessKey, nonce, timestamp, secretKey, bodyJson));
    }
}
