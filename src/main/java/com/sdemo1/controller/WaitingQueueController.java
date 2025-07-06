package com.sdemo1.controller;

import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.entity.Member;
import com.sdemo1.request.JoinQueueRequest;
import com.sdemo1.response.QueueStatusResponse;
import com.sdemo1.security.CustomUserDetails;
import com.sdemo1.service.RabbitMQService;
import com.sdemo1.service.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigInteger;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ck/queue")
@RequiredArgsConstructor
public class WaitingQueueController {

    private final WaitingQueueService waitingQueueService;
    private final RabbitMQService rabbitMQService;

    /**
     * 대기열 참가
     */
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<?>> joinQueue(@Valid @RequestBody JoinQueueRequest request) {
        try {
            BigInteger memberId = getCurrentMemberId();
            log.info("=== 대기열 참가 API 호출: memberId={}, concertId={} ===", memberId, request.getConcertId());
            
            QueueStatusResponse result = waitingQueueService.joinQueue(memberId, request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("대기열 참가 성공", result, HttpStatus.CREATED));
        } catch (IllegalArgumentException e) {
            log.error("대기열 참가 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null, HttpStatus.BAD_REQUEST));
        } catch (IllegalStateException e) {
            log.error("대기열 참가 실패 - 상태 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(e.getMessage(), null, HttpStatus.CONFLICT));
        } catch (Exception e) {
            log.error("대기열 참가 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("대기열 참가 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 대기열 상태 조회
     */
    @GetMapping("/status/{concertId}")
    public ResponseEntity<ApiResponse<?>> getQueueStatus(@PathVariable BigInteger concertId) {
        try {
            BigInteger memberId = getCurrentMemberId();
            log.info("=== 대기열 상태 조회 API 호출: memberId={}, concertId={} ===", memberId, concertId);
            
            QueueStatusResponse result = waitingQueueService.getQueueStatus(memberId, concertId);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("대기열 상태 조회 성공", result, HttpStatus.OK));
        } catch (Exception e) {
            log.error("대기열 상태 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("대기열 상태 조회 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 대기열 입장 처리
     */
    @PostMapping("/enter/{concertId}")
    public ResponseEntity<ApiResponse<?>> enterFromQueue(@PathVariable BigInteger concertId) {
        try {
            BigInteger memberId = getCurrentMemberId();
            log.info("=== 대기열 입장 처리 API 호출: memberId={}, concertId={} ===", memberId, concertId);
            
            boolean result = waitingQueueService.enterFromQueue(memberId, concertId);
            
            if (result) {
                return ResponseEntity.ok()
                        .body(new ApiResponse<>("대기열 입장 성공", null, HttpStatus.OK));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse<>("대기열 입장 실패", null, HttpStatus.BAD_REQUEST));
            }
        } catch (IllegalStateException e) {
            log.error("대기열 입장 실패 - 상태 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(e.getMessage(), null, HttpStatus.CONFLICT));
        } catch (Exception e) {
            log.error("대기열 입장 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("대기열 입장 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 대기열 나가기
     */
    @DeleteMapping("/leave/{concertId}")
    public ResponseEntity<ApiResponse<?>> leaveQueue(@PathVariable BigInteger concertId) {
        try {
            BigInteger memberId = getCurrentMemberId();
            log.info("=== 대기열 나가기 API 호출: memberId={}, concertId={} ===", memberId, concertId);
            
            waitingQueueService.leaveQueue(memberId, concertId);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("대기열 나가기 성공", null, HttpStatus.OK));
        } catch (Exception e) {
            log.error("대기열 나가기 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("대기열 나가기 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 콘서트 대기열 조회 (관리자용)
     */
    @GetMapping("/admin/{concertId}")
    public ResponseEntity<ApiResponse<?>> getQueueByConcertId(@PathVariable BigInteger concertId) {
        try {
            checkAdminRole();
            log.info("=== 콘서트 대기열 조회 API 호출 (관리자): concertId={} ===", concertId);
            
            List<QueueStatusResponse> result = waitingQueueService.getQueueByConcertId(concertId);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("콘서트 대기열 조회 성공", result, HttpStatus.OK));
        } catch (AccessDeniedException e) {
            log.error("권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("ADMIN 권한이 필요합니다.", null, HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("콘서트 대기열 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("콘서트 대기열 조회 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 만료된 대기열 정리 (관리자용)
     */
    @PostMapping("/admin/cleanup")
    public ResponseEntity<ApiResponse<?>> cleanupExpiredQueues() {
        try {
            checkAdminRole();
            log.info("=== 만료된 대기열 정리 API 호출 (관리자) ===");
            
            waitingQueueService.cleanupExpiredQueues();
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("만료된 대기열 정리 완료", null, HttpStatus.OK));
        } catch (AccessDeniedException e) {
            log.error("권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("ADMIN 권한이 필요합니다.", null, HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("만료된 대기열 정리 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("만료된 대기열 정리 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 현재 인증된 사용자의 ID 가져오기
     */
    private BigInteger getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
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
        
        throw new AccessDeniedException("사용자 정보를 찾을 수 없습니다. Principal type: " + 
                authentication.getPrincipal().getClass().getSimpleName());
    }

    /**
     * 관리자 권한 확인
     */
    private void checkAdminRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }
        
        boolean hasAdminRole = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        
        if (!hasAdminRole) {
            throw new AccessDeniedException("ADMIN 권한이 필요합니다.");
        }
    }

    /**
     * RabbitMQ GUI용: 대기열 정보 조회 (텍스트 형태)
     */
    @GetMapping("/info/{concertId}")
    public ResponseEntity<String> getQueueInfoForGUI(@PathVariable BigInteger concertId) {
        
        log.info("GUI용 대기열 정보 조회: concertId={}", concertId);
        
        try {
            // 간단한 대기열 정보 조회
            Long queueSize = rabbitMQService.getWaitingQueueSize(concertId);
            String queueInfo = String.format("콘서트 ID %s의 대기열 정보:\n총 대기자 수: %d명", concertId, queueSize);
            return ResponseEntity.ok(queueInfo);
        } catch (Exception e) {
            log.error("GUI용 대기열 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("대기열 정보 조회에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * RabbitMQ GUI용: 모든 콘서트의 대기열 요약 정보
     */
    @GetMapping("/summary")
    public ResponseEntity<String> getQueueSummary() {
        
        log.info("GUI용 대기열 요약 정보 조회");
        
        try {
            StringBuilder summary = new StringBuilder();
            summary.append("=== 대기열 요약 정보 ===\n\n");
            
            // 콘서트 ID 1~5까지 확인 (실제로는 동적으로 확인해야 함)
            for (int i = 1; i <= 5; i++) {
                BigInteger concertId = BigInteger.valueOf(i);
                Long queueSize = rabbitMQService.getWaitingQueueSize(concertId);
                if (queueSize > 0) {
                    summary.append(String.format("콘서트 ID %d: %d명 대기 중\n", i, queueSize));
                }
            }
            
            if (summary.toString().equals("=== 대기열 요약 정보 ===\n\n")) {
                summary.append("현재 활성화된 대기열이 없습니다.");
            }
            
            return ResponseEntity.ok(summary.toString());
        } catch (Exception e) {
            log.error("GUI용 대기열 요약 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body("대기열 요약 정보 조회에 실패했습니다: " + e.getMessage());
        }
    }
} 