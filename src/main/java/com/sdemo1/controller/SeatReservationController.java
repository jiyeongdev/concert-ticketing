package com.sdemo1.controller;

import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.dto.SeatStatusDto;
import com.sdemo1.entity.Member;
import com.sdemo1.request.HoldSeatRequest;
import com.sdemo1.security.CustomUserDetails;
import com.sdemo1.service.SeatReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigInteger;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ck/seat-reservation")
@RequiredArgsConstructor
public class SeatReservationController {

    private final SeatReservationService seatReservationService;

    /**
     * 콘서트의 모든 좌석 상태 조회
     */
    @GetMapping("/concert/{concertId}")
    public ResponseEntity<ApiResponse<?>> getSeatStatusByConcertId(@PathVariable BigInteger concertId) {
        try {
            log.info("=== 콘서트 좌석 상태 조회 API 호출: concertId={} ===", concertId);
            
            List<SeatStatusDto> result = seatReservationService.getSeatStatusByConcertId(concertId);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("좌석 상태 조회 성공", result, HttpStatus.OK));
        } catch (Exception e) {
            log.error("좌석 상태 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("좌석 상태 조회 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 좌석 점유
     */
    @PostMapping("/hold")
    public ResponseEntity<ApiResponse<?>> holdSeat(@Valid @RequestBody HoldSeatRequest request) {
        try {
            BigInteger userId = getCurrentUserId();
            log.info("=== 좌석 점유 API 호출: userId={}, seatId={} ===", userId, request.getSeatId());
            
            SeatStatusDto result = seatReservationService.holdSeat(userId, request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("좌석 점유 성공", result, HttpStatus.CREATED));
        } catch (IllegalArgumentException e) {
            log.error("좌석 점유 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null, HttpStatus.BAD_REQUEST));
        } catch (IllegalStateException e) {
            log.error("좌석 점유 실패 - 상태 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(e.getMessage(), null, HttpStatus.CONFLICT));
        } catch (Exception e) {
            log.error("좌석 점유 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("좌석 점유 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 좌석 점유 해제
     */
    @DeleteMapping("/release/{seatId}")
    public ResponseEntity<ApiResponse<?>> releaseSeat(@PathVariable BigInteger seatId) {
        try {
            BigInteger userId = getCurrentUserId();
            log.info("=== 좌석 점유 해제 API 호출: userId={}, seatId={} ===", userId, seatId);
            
            seatReservationService.releaseSeat(userId, seatId);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("좌석 점유 해제 성공", null, HttpStatus.OK));
        } catch (IllegalStateException e) {
            log.error("좌석 점유 해제 실패 - 상태 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(e.getMessage(), null, HttpStatus.CONFLICT));
        } catch (Exception e) {
            log.error("좌석 점유 해제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("좌석 점유 해제 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 사용자의 점유 좌석 조회
     */
    @GetMapping("/my-holds")
    public ResponseEntity<ApiResponse<?>> getUserHeldSeats() {
        try {
            BigInteger userId = getCurrentUserId();
            log.info("=== 사용자 점유 좌석 조회 API 호출: userId={} ===", userId);
            
            List<SeatStatusDto> result = seatReservationService.getUserHeldSeats(userId);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("점유 좌석 조회 성공", result, HttpStatus.OK));
        } catch (Exception e) {
            log.error("점유 좌석 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("점유 좌석 조회 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 좌석 예매 확정 (결제 완료 후)
     */
    @PostMapping("/confirm/{seatId}")
    public ResponseEntity<ApiResponse<?>> confirmReservation(@PathVariable BigInteger seatId) {
        try {
            BigInteger userId = getCurrentUserId();
            log.info("=== 좌석 예매 확정 API 호출: userId={}, seatId={} ===", userId, seatId);
            
            boolean result = seatReservationService.confirmReservation(userId, seatId);
            
            if (result) {
                return ResponseEntity.ok()
                        .body(new ApiResponse<>("좌석 예매 확정 성공", null, HttpStatus.OK));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>("좌석 예매 확정 실패", null, HttpStatus.BAD_REQUEST));
            }
        } catch (IllegalStateException e) {
            log.error("좌석 예매 확정 실패 - 상태 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(e.getMessage(), null, HttpStatus.CONFLICT));
        } catch (Exception e) {
            log.error("좌석 예매 확정 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("좌석 예매 확정 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 만료된 좌석 점유 정리 (관리자용)
     */
    @PostMapping("/admin/cleanup")
    public ResponseEntity<ApiResponse<?>> cleanupExpiredHolds() {
        try {
            checkAdminRole();
            log.info("=== 만료된 좌석 점유 정리 API 호출 (관리자) ===");
            
            seatReservationService.cleanupExpiredHolds();
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("만료된 좌석 점유 정리 완료", null, HttpStatus.OK));
        } catch (Exception e) {
            log.error("만료된 좌석 점유 정리 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("만료된 좌석 점유 정리 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 현재 인증된 사용자의 ID 가져오기
     */
    private BigInteger getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }
        
        // Principal이 CustomUserDetails인 경우
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getMemberId();
        }
        
        // Principal이 String (memberId)인 경우 (기존 방식)
        if (authentication.getPrincipal() instanceof String) {
            return new BigInteger((String) authentication.getPrincipal());
        }
        
        // Principal이 Member 객체인 경우 (기존 방식)
        if (authentication.getPrincipal() instanceof Member) {
            return ((Member) authentication.getPrincipal()).getMemberId();
        }
        
        throw new RuntimeException("사용자 정보를 찾을 수 없습니다. Principal type: " + 
                (authentication.getPrincipal() != null ? authentication.getPrincipal().getClass().getSimpleName() : "null"));
    }

    /**
     * ADMIN 권한 확인
     */
    private void checkAdminRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증되지 않은 사용자입니다.");
        }
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));
        
        if (!isAdmin) {
            throw new RuntimeException("ADMIN 권한이 필요합니다.");
        }
    }
} 