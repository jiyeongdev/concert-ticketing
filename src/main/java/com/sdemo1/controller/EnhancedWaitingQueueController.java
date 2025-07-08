package com.sdemo1.controller;

import java.math.BigInteger;
import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.request.JoinQueueRequest;
import com.sdemo1.response.QueueStatusResponse;
import com.sdemo1.security.CustomUserDetails;
import com.sdemo1.service.queue.EnhancedWaitingQueueService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/waiting-room")
@RequiredArgsConstructor
public class EnhancedWaitingQueueController {

    private final EnhancedWaitingQueueService enhancedWaitingQueueService;

    @Value("${queue.concert.group-size:10}")
    private int groupSize;

    /**
     * 대기열 입장 요청 (10분 전부터 가능)
     * POST /v2/waiting-room/enter
     */
    @PostMapping("/enter")
    public ResponseEntity<ApiResponse<?>> enterWaitingRoom(@Valid @RequestBody JoinQueueRequest request) {
        try {
            BigInteger memberId = getCurrentMemberId();
            log.info("=== 대기열 입장 API 호출: memberId={}, concertId={} ===", memberId, request.getConcertId());
            
            QueueStatusResponse result = enhancedWaitingQueueService.enterWaitingRoom(memberId, request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("대기열 입장 성공", result, HttpStatus.CREATED));
        } catch (IllegalArgumentException e) {
            log.error("대기열 입장 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null, HttpStatus.BAD_REQUEST));
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
     * 대기열 상태 조회
     * GET /v2/waiting-room/status/{concertId}
     */
    @GetMapping("/status/{concertId}")
    public ResponseEntity<ApiResponse<?>> getWaitingRoomStatus(@PathVariable("concertId") BigInteger concertId) {
        try {
            BigInteger memberId = getCurrentMemberId();
            log.info("=== 대기열 상태 조회 API 호출: memberId={}, concertId={} ===", memberId, concertId);
            
            QueueStatusResponse result = enhancedWaitingQueueService.getWaitingRoomStatus(memberId, concertId);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("대기열 상태 조회 성공", result, HttpStatus.OK));
        } catch (IllegalArgumentException e) {
            log.error("대기열 상태 조회 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(e.getMessage(), null, HttpStatus.BAD_REQUEST));
        } catch (IllegalStateException e) {
            log.error("대기열 상태 조회 실패 - 상태 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(e.getMessage(), null, HttpStatus.CONFLICT));
        } catch (Exception e) {
            log.error("대기열 상태 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("대기열 상태 조회 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 대기열에서 나가기
     * POST /v2/waiting-room/exit/{concertId}
     */
    @PostMapping("/exit/{concertId}")
    public ResponseEntity<ApiResponse<?>> leaveWaitingRoom(@PathVariable("concertId") BigInteger concertId) {
        try {
            BigInteger memberId = getCurrentMemberId();
            log.info("=== 대기열 나가기 API 호출: memberId={}, concertId={} ===", memberId, concertId);
            
            enhancedWaitingQueueService.leaveWaitingRoom(memberId, concertId);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("대기열 나가기 성공", null, HttpStatus.OK));
        } catch (Exception e) {
            log.error("대기열 나가기 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("대기열 나가기 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }



    // ========== Private Helper Methods ==========

    /**
     * 현재 로그인한 사용자의 ID 조회
     */
    private BigInteger getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getMemberId();
        }
        throw new IllegalStateException("로그인이 필요합니다.");
    }
} 