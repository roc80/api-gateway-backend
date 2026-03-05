package com.zl.mjga.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

@Slf4j
@Component
@Getter
public class Jwt {

    private final String secret;

    private final int expirationMin;

    private final String cookieName;

    private final JWTVerifier verifier;

    public Jwt(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-min}") int expirationMin,
            @Value("${jwt.cookie-name}") String cookieName) {
        this.verifier = JWT.require(Algorithm.HMAC256(secret)).build();
        this.secret = secret;
        this.expirationMin = expirationMin;
        this.cookieName = cookieName;
    }

    public String getSubject(String token) {
        return JWT.decode(token).getSubject();
    }

    public Boolean verify(String token) {
        try {
            verifier.verify(token);
            return Boolean.TRUE;
        } catch (JWTVerificationException e) {
            return Boolean.FALSE;
        }
    }

    public String extract(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, cookieName);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
    }

    public String create(String userIdentify) {
        return JWT.create()
                .withSubject(String.valueOf(userIdentify))
                .withIssuedAt(new Date())
                .withExpiresAt(
                        Date.from(
                                LocalDateTime.now()
                                        .plusMinutes(expirationMin)
                                        .atZone(ZoneId.systemDefault())
                                        .toInstant()))
                .sign(Algorithm.HMAC256(secret));
    }

    private Cookie buildJwtCookiePojo(HttpServletRequest request, String userIdentify) {
        String contextPath = request.getContextPath();
        String cookiePath = StringUtils.isNotEmpty(contextPath) ? contextPath : "/";
        Cookie cookie = new Cookie(cookieName, create(userIdentify));
        cookie.setPath(cookiePath);
        cookie.setMaxAge(expirationMin * 60);
        cookie.setSecure(request.isSecure());
        cookie.setHttpOnly(true);
        return cookie;
    }

    public void makeToken(
            HttpServletRequest request, HttpServletResponse response, String userIdentify) {
        // todo@lp 只用于联调阶段允许cors
        String contextPath = request.getContextPath();
        String cookiePath = StringUtils.isNotEmpty(contextPath) ? contextPath : "/";
        String token = create(userIdentify);

        // 手动构建 Set-Cookie 响应头以支持 SameSite=None
        // SameSite=None 必须配合 Secure 使用
        String setCookieHeader = String.format(
                "%s=%s; Path=%s; Max-Age=%d; %s; %s; SameSite=None",
                cookieName,
                token,
                cookiePath,
                expirationMin * 60,
                "Secure",
                "HttpOnly");
        response.addHeader("Set-Cookie", setCookieHeader);
    }

    public void removeToken(HttpServletRequest request, HttpServletResponse response) {
        // todo@lp 只用于联调阶段允许cors
        String contextPath = request.getContextPath();
        String cookiePath = StringUtils.isNotEmpty(contextPath) ? contextPath : "/";

        // 手动构建 Set-Cookie 响应头以支持 SameSite=None
        String setCookieHeader = String.format(
                "%s=; Path=%s; Max-Age=0; %s; %s; SameSite=None",
                cookieName,
                cookiePath,
                "Secure",
                "HttpOnly");
        response.addHeader("Set-Cookie", setCookieHeader);
    }
}
