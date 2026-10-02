package com.roc.contract;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * 签名协议测试。
 *
 * <p>EXPECTED_SIGN_HEX 由 api-client-sdk 0.0.1 的 SignUtil（hutool SHA-512）实际输出生成， 用于锁死本模块与第三方 SDK
 * 的逐位兼容性；任何一方变更算法都会在此失败。
 *
 * @author roc
 * @since 2026/10/2
 */
public class SignUtilTest {

    private static final String ACCESS_KEY = "ak-user-01";
    private static final String NONCE = "9f3k2";
    private static final String TIMESTAMP = "1760000000000";
    private static final String SECRET_KEY = "sk-secret-01";
    private static final String BODY_JSON = "{\"username\":\"dave\"}";

    private static final String EXPECTED_SIGN_HEX =
            "4b81b750f800ac0ba09dafae66aec34a331e718e52cc3488881ccf9a2105c8fa"
                    + "426262705b44fbdd9f33719124e57241651eb7a13b7c1029e622327a514ea782";

    @Test
    public void genSignBySha512_shouldMatchApiClientSdkVector() {
        byte[] sign = SignUtil.genSignBySha512(ACCESS_KEY, NONCE, TIMESTAMP, SECRET_KEY, BODY_JSON);

        assertEquals(EXPECTED_SIGN_HEX, toHex(sign));
    }

    @Test
    public void genSignBySha512_shouldBeDeterministic() {
        byte[] first =
                SignUtil.genSignBySha512(ACCESS_KEY, NONCE, TIMESTAMP, SECRET_KEY, BODY_JSON);
        byte[] second =
                SignUtil.genSignBySha512(ACCESS_KEY, NONCE, TIMESTAMP, SECRET_KEY, BODY_JSON);

        assertArrayEquals(first, second);
    }

    @Test
    public void genSignBySha512_shouldCoverEverySignedPart() throws Exception {
        byte[] expected =
                MessageDigest.getInstance("SHA-512")
                        .digest(
                                (ACCESS_KEY + NONCE + TIMESTAMP + SECRET_KEY + BODY_JSON)
                                        .getBytes(StandardCharsets.UTF_8));

        assertArrayEquals(
                expected,
                SignUtil.genSignBySha512(ACCESS_KEY, NONCE, TIMESTAMP, SECRET_KEY, BODY_JSON));
    }

    @Test
    public void genSignString_shouldUseArraysToStringWireFormat() {
        String signString =
                SignUtil.genSignString(ACCESS_KEY, NONCE, TIMESTAMP, SECRET_KEY, BODY_JSON);

        assertEquals(
                Arrays.toString(
                        SignUtil.genSignBySha512(
                                ACCESS_KEY, NONCE, TIMESTAMP, SECRET_KEY, BODY_JSON)),
                signString);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
