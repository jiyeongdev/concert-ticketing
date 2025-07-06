package com.sdemo1.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

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
            .authorizeHttpRequests(auth -> {
                log.info("인증 요구사항 설정");
                // Interceptor에서 인증을 처리하므로 모든 요청 허용
                auth.anyRequest().permitAll();
            })
            .exceptionHandling(exception -> {
                log.info("예외 처리 설정");
                exception.authenticationEntryPoint((request, response, authException) -> {
                    log.error("인증 실패: {}", authException.getMessage());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    String errorMessage = String.format(
                        "{\"error\": \"Unauthorized\", \"message\": \"%s\", \"path\": \"%s\"}",
                        authException.getMessage(),
                        request.getRequestURI()
                    );
                    response.getWriter().write(errorMessage);
                });
            });
            // Interceptor에서 인증을 처리하므로 JWT Filter 비활성화
            // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

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