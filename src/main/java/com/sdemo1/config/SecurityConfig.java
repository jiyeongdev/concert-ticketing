package com.sdemo1.config;

import java.util.Arrays;
import com.sdemo1.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("BCryptPasswordEncoder 설정 완료");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        log.info("AuthenticationManager 설정 완료");
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("=== Security Filter Chain 설정 시작 ===");
        
        http
            .cors(cors -> {
                log.info("CORS 설정 적용");
                cors.configurationSource(corsConfigurationSource());
            })
            .csrf(csrf -> {
                log.info("CSRF 비활성화");
                csrf.disable();
            })
            .sessionManagement(session -> {
                log.info("세션 정책: STATELESS");
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            })
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> {
                log.info("인증 요구사항 설정");
                
                // SecurityConstants를 활용한 권한 설정
                // 공개 경로는 permitAll()
                SecurityConstants.PUBLIC_PATHS.forEach(path -> {
                    if (path.endsWith("/")) {
                        auth.requestMatchers(path + "**").permitAll();
                    } else {
                        auth.requestMatchers(path).permitAll();
                    }
                });
                
                // 관리자 전용 경로는 ADMIN 역할 필요
                SecurityConstants.ADMIN_PATHS.forEach(path -> {
                    if (path.endsWith("/")) {
                        auth.requestMatchers(path + "**").hasRole("ADMIN");
                    } else {
                        auth.requestMatchers(path + "**").hasRole("ADMIN"); // /concerts/** 모든 하위 경로 포함
                    }
                });
                
                // 인증된 사용자 경로는 authenticated()
                SecurityConstants.AUTHENTICATED_PATHS.forEach(path -> {
                    if (path.endsWith("/")) {
                        auth.requestMatchers(path + "**").authenticated();
                    } else {
                        auth.requestMatchers(path).authenticated();
                    }
                });
                
                // 나머지는 인증 필요
                auth.anyRequest().authenticated();
            })
            .exceptionHandling(exception -> {
                log.info("예외 처리 설정");
                exception.authenticationEntryPoint((request, response, authException) -> {
                    log.error("인증 실패: {}", authException.getMessage());
                    // JWT 필터에서 이미 응답을 보냈으면 추가 응답하지 않음
                    if (!response.isCommitted()) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json;charset=UTF-8");
                        String errorMessage = String.format(
                            "{\"error\": \"Unauthorized\", \"message\": \"%s\", \"path\": \"%s\"}",
                            authException.getMessage(),
                            request.getRequestURI()
                        );
                        response.getWriter().write(errorMessage);
                    }
                });
                exception.accessDeniedHandler((request, response, accessDeniedException) -> {
                    log.error("접근 거부: {} - 사용자: {}", accessDeniedException.getMessage(), request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous");
                    if (!response.isCommitted()) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json;charset=UTF-8");
                        String errorMessage = String.format(
                            "{\"error\": \"Forbidden\", \"message\": \"해당 리소스에 대한 접근 권한이 없습니다\", \"path\": \"%s\", \"requiredRole\": \"ADMIN\"}",
                            request.getRequestURI()
                        );
                        response.getWriter().write(errorMessage);
                    }
                });
            });

        log.info("=== Security Filter Chain 설정 완료 ===");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        log.info("CORS 설정 완료: 모든 origin 허용");
        return source;
    }
} 