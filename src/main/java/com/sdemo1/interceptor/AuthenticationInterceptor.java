package com.sdemo1.interceptor;

import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.security.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    // 인증이 필요하지 않은 경로들
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/auth/",
        "/health-check",
        "/static/",
        "/error",
        "/favicon.ico"
    );

    // 관리자만 접근 가능한 경로들
    private static final List<String> ADMIN_PATHS = Arrays.asList(
        "/admin/",
        "/admin/"
    );

    // 읽기 전용 경로들 (GET 요청만 허용)
    private static final List<String> READ_ONLY_PATHS = Arrays.asList(
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        log.info("[인증 인터셉터] {} {}", method, requestURI);

        // 1. 공개 경로 체크
        if (isPublicPath(requestURI)) {
            log.info("[인증 인터셉터] 공개 경로 접근 허용: {}", requestURI);
            return true;
        }

        // 2. 읽기 전용 경로 체크 (GET 요청만 허용)
        if (isReadOnlyPath(requestURI) && "GET".equals(method)) {
            log.info("[인증 인터셉터] 읽기 전용 경로 접근 허용: {} {}", method, requestURI);
            return true;
        }

        // 3. JWT 토큰 추출
        String token = getJwtFromRequest(request);
        if (!StringUtils.hasText(token)) {
            log.warn("[인증 인터셉터] 토큰이 없음: {}", requestURI);
            sendUnauthorizedResponse(response, "인증 토큰이 필요합니다");
            return false;
        }

        try {
            // 3. JWT 토큰 검증
            Authentication authentication = jwtTokenProvider.validateAndGetAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.info("[인증 인터셉터] 인증 성공 - 사용자: {}", authentication.getName());

            // 4. 관리자 권한 체크
            if (isAdminPath(requestURI) && !hasAdminRole(authentication)) {
                log.warn("[인증 인터셉터] 관리자 권한 필요: {}", requestURI);
                sendForbiddenResponse(response, "관리자 권한이 필요합니다");
                return false;
            }

            return true;

        } catch (Exception e) {
            log.error("[인증 인터셉터] 토큰 검증 실패: {}", e.getMessage());
            sendUnauthorizedResponse(response, "유효하지 않은 토큰입니다");
            return false;
        }
    }

    /**
     * 공개 경로인지 확인
     */
    private boolean isPublicPath(String requestURI) {
        return PUBLIC_PATHS.stream()
                .anyMatch(requestURI::startsWith);
    }

    /**
     * 관리자 전용 경로인지 확인
     */
    private boolean isAdminPath(String requestURI) {
        return ADMIN_PATHS.stream()
                .anyMatch(requestURI::startsWith);
    }

    /**
     * 읽기 전용 경로인지 확인
     */
    private boolean isReadOnlyPath(String requestURI) {
        return READ_ONLY_PATHS.stream()
                .anyMatch(requestURI::startsWith);
    }

    /**
     * 관리자 권한이 있는지 확인
     */
    private boolean hasAdminRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }

    /**
     * JWT 토큰을 요청에서 추출
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 401 Unauthorized 응답 전송
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        ApiResponse<String> errorResponse = new ApiResponse<>(message, null, HttpStatus.UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * 403 Forbidden 응답 전송
     */
    private void sendForbiddenResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        ApiResponse<String> errorResponse = new ApiResponse<>(message, null, HttpStatus.FORBIDDEN);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
} 