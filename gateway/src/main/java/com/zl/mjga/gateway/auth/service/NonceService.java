package com.zl.mjga.gateway.auth.service;

import com.zl.mjga.gateway.auth.constant.ApiSignConstant;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class NonceService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    private static final String NONCE_KEY_PREFIX = "api:nonce:";

    private static final String NONCE_LUA_SCRIPT =
            """
            local ttl = tonumber(ARGV[1])
            if not ttl or ttl <= 0 then
                return 0
            end
            if redis.call('exists', KEYS[1]) == 1 then
                return 0
            end
            redis.call('setnx', KEYS[1], '1')
            redis.call('expire', KEYS[1], ttl)
            return 1
            """;

    public Mono<Boolean> verifyAndRecordNonce(String nonce) {
        if (nonce == null || nonce.isEmpty()) {
            log.warn("Nonce is null or empty");
            return Mono.just(false);
        }

        String key = NONCE_KEY_PREFIX + nonce;
        long ttlSeconds = ApiSignConstant.REQUEST_VALID_MINUTES * 60;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(NONCE_LUA_SCRIPT, Long.class);

        return reactiveRedisTemplate
                .execute(script, Collections.singletonList(key), String.valueOf(ttlSeconds))
                .map(
                        result -> {
                            boolean valid = result == 1L;
                            if (!valid) {
                                log.warn("Duplicate request detected, nonce: {}", nonce);
                            }
                            return valid;
                        })
                .next();
    }
}
