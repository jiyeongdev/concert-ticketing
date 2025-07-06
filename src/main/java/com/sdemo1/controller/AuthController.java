package com.sdemo1.controller;

import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.entity.Member;
import com.sdemo1.repository.MemberRepository;
import com.sdemo1.request.LoginRequest;
import com.sdemo1.request.SignupRequest;
import com.sdemo1.security.JwtTokenProvider;
import com.sdemo1.config.RefreshTokenCookieConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ck/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenCookieConfig refreshTokenCookieConfig;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signup(@Valid @RequestBody SignupRequest request) {
        try {
            log.info("=== 회원가입 시작 ===");
            
            // 이메일 중복 확인
            if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>("이미 존재하는 이메일입니다.", null, HttpStatus.BAD_REQUEST));
            }

            // 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            log.info("비밀번호 인코딩 완료: 원본 길이={}, 인코딩 길이={}", 
                    request.getPassword().length(), encodedPassword.length());

            // 회원 생성
            Member member = new Member();
            member.setEmail(request.getEmail());
            member.setPassword(encodedPassword);
            member.setName(request.getName());
            member.setPhone(request.getPhone());
            
            // role 설정 (기본값: USER)
            Member.Role role = Member.Role.USER;
            if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
                try {
                    role = Member.Role.valueOf(request.getRole().toUpperCase());
                    log.info("회원가입 role 설정: {}", role);
                } catch (IllegalArgumentException e) {
                    log.warn("잘못된 role 값: '{}'. 존재하지 않는 role입니다. 기본값 USER로 설정합니다.", request.getRole());
                    role = Member.Role.USER;
                }
            } else {
                log.info("role이 지정되지 않아 기본값 USER로 설정합니다.");
            }
            member.setRole(role);

            Member savedMember = memberRepository.save(member);
            log.info("회원가입 완료: {}", savedMember.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("memberId", savedMember.getMemberId());
            response.put("email", savedMember.getEmail());
            response.put("name", savedMember.getName());
            response.put("role", savedMember.getRole().name());

            return ResponseEntity.ok()
                    .body(new ApiResponse<>("회원가입이 완료되었습니다.", response, HttpStatus.OK));

        } catch (Exception e) {
            log.error("회원가입 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("회원가입 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("=== 로그인 시작 ===");
            log.info("로그인 시도: email={}, password 길이={}", request.getEmail(), request.getPassword().length());
            
            // 사용자 존재 여부 먼저 확인
            Member member = memberRepository.findByEmail(request.getEmail())
                    .orElse(null);
            
            if (member == null) {
                log.error("사용자를 찾을 수 없습니다: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>("이메일 또는 비밀번호가 올바르지 않습니다.", null, HttpStatus.UNAUTHORIZED));
            }
            
            log.info("사용자 발견: memberId={}, 저장된 비밀번호 길이={}", 
                    member.getMemberId(), member.getPassword().length());
            
            // 인증 처리
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // JWT 토큰 생성
            String accessToken = jwtTokenProvider.createAccessToken(member);
            String refreshToken = jwtTokenProvider.createRefreshToken(member);

            // Refresh Token 쿠키 생성
            ResponseCookie refreshTokenCookie = refreshTokenCookieConfig.createCookie(refreshToken);

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("memberId", member.getMemberId());
            response.put("email", member.getEmail());
            response.put("name", member.getName());
            response.put("role", member.getRole().name());
            response.put("accessToken", accessToken);

            log.info("로그인 성공: {}", member.getEmail());

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(new ApiResponse<>("로그인 성공", response, HttpStatus.OK));

        } catch (Exception e) {
            log.error("로그인 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>("이메일 또는 비밀번호가 올바르지 않습니다.", null, HttpStatus.UNAUTHORIZED));
        }
    }

    /**
     * 토큰 갱신
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<?>> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        try {
            log.info("=== 토큰 재발급 시작 ===");
            
            if (refreshToken == null) {
                log.error("Refresh Token이 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>("Refresh Token이 없습니다.", null, HttpStatus.UNAUTHORIZED));
            }

            // Refresh Token 검증 및 만료 체크
            try {
                jwtTokenProvider.validateAndGetAuthentication(refreshToken);
            } catch (JwtTokenProvider.TokenValidationException e) {
                log.error("Refresh Token 검증 실패: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>(e.getMessage(), null, HttpStatus.UNAUTHORIZED));
            }

            // DB에서 사용자 정보 조회
            String memberId = jwtTokenProvider.getUserInfoFromToken(refreshToken);
            if (memberId == null) {
                log.error("사용자 정보를 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse<>("사용자 정보를 찾을 수 없습니다.", null, HttpStatus.UNAUTHORIZED));
            }
            
            log.info("user: {}", memberId);

            // DB에서 사용자 정보 조회
            Member member = memberRepository.findById(new java.math.BigInteger(memberId))
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 사용자 정보로 AccessToken 생성
            String newAccessToken = jwtTokenProvider.createAccessToken(member);

            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", newAccessToken);

            return ResponseEntity.ok()
                    .body(new ApiResponse<>("토큰 재발급 성공", response, HttpStatus.OK));

        } catch (Exception e) {
            log.error("토큰 재발급 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("토큰 재발급 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout() {
        try {
            log.info("=== 로그아웃 시작 ===");
            
            // Refresh Token 쿠키 삭제
            ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                    .path("/")
                    .maxAge(0)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                    .body(new ApiResponse<>("로그아웃 성공", null, HttpStatus.OK));

        } catch (Exception e) {
            log.error("로그아웃 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("로그아웃 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
}