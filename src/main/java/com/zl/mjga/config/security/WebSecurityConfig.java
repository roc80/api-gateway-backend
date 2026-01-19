package com.zl.mjga.config.security;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    private final Jwt jwt;

    private final CorsConfigurationSource corsConfigurationSource;

    private final Environment env;

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        RestfulAuthenticationEntryPointHandler restfulAuthenticationEntryPointHandler =
                new RestfulAuthenticationEntryPointHandler();

        List<String> activeProfiles = Arrays.stream(env.getActiveProfiles()).toList();

        http.cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth -> {
                            // 公共接口
                            auth.requestMatchers("/auth/sign-in", "/auth/sign-up", "/error")
                                    .permitAll();

                            // todo@lp 开发阶段部署时开放接口文档
                            if (true
                                    || activeProfiles.contains("dev")
                                    || activeProfiles.contains("test")) {
                                auth.requestMatchers(
                                                "/v3/api-docs/**",
                                                "/swagger-ui.html",
                                                "/swagger-ui/**",
                                                "/doc.html",
                                                "/webjars/**")
                                        .permitAll();
                            }

                            auth.anyRequest().authenticated();
                        })
                .exceptionHandling(
                        ex ->
                                ex.accessDeniedHandler(restfulAuthenticationEntryPointHandler)
                                        .authenticationEntryPoint(
                                                restfulAuthenticationEntryPointHandler))
                .sessionManagement(
                        sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterAt(
                        new JwtAuthenticationFilter(jwt, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
