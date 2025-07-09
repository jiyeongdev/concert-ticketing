package com.sdemo1.config;

import java.util.Arrays;
import java.util.List;

/**
 * 보안 관련 상수 관리 클래스
 * 
 * @author Concert Reservation Team
 * @version 1.0
 * @since 2024-01-01
 */
public class SecurityConstants {

    /**
     * 인증이 필요하지 않은 공개 경로들
     */
    public static final List<String> PUBLIC_PATHS = Arrays.asList(
        // 인증 관련
        "/auth/",
        "/api/auth/",
        
        // 헬스 체크
        "/health-check",
        "/actuator/health",
        
        // 정적 리소스
        "/index.html",
        "/websocket-reservation.html",
        "/static/",
        "/css/",
        "/js/",
        "/images/",
        "/favicon.ico",
        "/error",
        
        // Swagger UI
        "/swagger-ui/",
        "/swagger-ui.html",
        "/v3/api-docs/",
        "/v3/api-docs",
        "/swagger-resources/",
        "/webjars/",
        
        // WebSocket
        "/ws/"
    );

    /**
     * 관리자 권한이 필요한 경로들
     */
    public static final List<String> ADMIN_PATHS = Arrays.asList(
        "/admin/",
        "/seats/admin/", // POST, PUT, DELETE
        "/concerts/admin/", // 콘서트 관리 전용 (/concert/admin, /concert/admin/8, /concert/admin/delete 등)
        "/members/admin/",
        "/seat-grades/admin/"
    );

    /**
     * 인증이 필요한 경로들 (로그인 사용자)
     */
    public static final List<String> AUTHENTICATED_PATHS = Arrays.asList(
        "/reservations/",
        "/payments/",
        "/members/profile",
        "/queue/"
    );

    /**
     * 경로가 공개 경로인지 확인
     */
    public static boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream()    
                .anyMatch(publicPath -> {
                    if (publicPath.endsWith("/")) {
                        return path.startsWith(publicPath);
                    } else {
                        return path.equals(publicPath);
                    }
                });
    }

    /**
     * 경로가 관리자 권한이 필요한지 확인
     */
    public static boolean isAdminPath(String path) {
        return ADMIN_PATHS.stream()
                .anyMatch(adminPath -> path.startsWith(adminPath));
    }

    /**
     * 경로가 인증이 필요한지 확인
     */
    public static boolean isAuthenticatedPath(String path) {
        return AUTHENTICATED_PATHS.stream()
                .anyMatch(authPath -> path.startsWith(authPath));
    }

    /**
     * 경로가 보안 검사가 필요한지 확인
     */
    public static boolean requiresSecurityCheck(String path) {
        return !isPublicPath(path);
    }
} 