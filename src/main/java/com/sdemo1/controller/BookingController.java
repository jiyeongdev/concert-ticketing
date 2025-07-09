package com.sdemo1.controller;

import java.math.BigInteger;
import java.util.List;
import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.common.utils.BookingEligibilityChecker;
import com.sdemo1.config.SwaggerExamples;
import com.sdemo1.dto.seat.SeatHoldResult;
import com.sdemo1.dto.seat.SeatStatusDto;
import com.sdemo1.response.QueueStatusResponse;
import com.sdemo1.security.CustomUserDetails;
import com.sdemo1.service.queue.EnhancedWaitingQueueService;
import com.sdemo1.service.seat.hold.SeatHoldService;
import com.sdemo1.service.seat.status.SeatStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 예매 관련 REST API 컨트롤러
 * 좌석 점유/해제 및 예매 처리를 위한 REST API 제공
 */
@Slf4j
@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
@Tag(name = "7. 예매 관리", description = "예매 토큰 발급, 좌석 점유/해제, 실시간 좌석 상태 조회 API")
public class BookingController {

    private final SeatStatusService seatStatusService;
    private final SeatHoldService seatHoldService;
    private final EnhancedWaitingQueueService enhancedWaitingQueueService;
    private final BookingEligibilityChecker eligibilityChecker;

    /**
     * 예매 토큰 발급 (예매 페이지 입장)
     */
    @PostMapping("/token/{concertId}")
    @Operation(summary = "예매 토큰 발급", description = "대기열 입장 후 예매 자격을 확인하고 예매 토큰을 발급합니다")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "예매 토큰 발급 성공",
            content = @Content(examples = @ExampleObject(value = SwaggerExamples.BOOKING_TOKEN_RESPONSE))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "예매 자격 없음",
            content = @Content(examples = @ExampleObject(value = SwaggerExamples.ERROR_FORBIDDEN)))
    })
    public ResponseEntity<ApiResponse<?>> getBookingToken(@PathVariable("concertId") BigInteger concertId) {
        try {
            BigInteger memberId = getCurrentMemberId();
            log.info("=== 예매 토큰 발급 API 호출: memberId={}, concertId={} ===", memberId, concertId);
            
            // 예매 자격 확인 및 토큰 발급
            QueueStatusResponse result = enhancedWaitingQueueService.getBookingToken(memberId, concertId);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("예매 토큰 발급 성공", result, HttpStatus.OK));
        } catch (IllegalStateException e) {
            log.error("예매 토큰 발급 실패 - 상태 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(e.getMessage(), null, HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("예매 토큰 발급 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("예매 토큰 발급 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 초기 로딩 시 실시간 좌석 상태 조회 (REST API)
     */
    @GetMapping("/{concertId}/seats")
    @Operation(summary = "좌석 상태 조회", description = "콘서트의 모든 좌석 상태를 조회합니다")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좌석 상태 조회 성공",
            content = @Content(examples = @ExampleObject(value = SwaggerExamples.SEAT_STATUS_RESPONSE)))
    })
    public ResponseEntity<?> getSeatStatus(@PathVariable("concertId") BigInteger concertId) {
        BigInteger memberId = getCurrentMemberId();
        log.info("실시간 좌석 상태 조회 (REST): concertId={}, memberId={}", concertId, memberId);
        
        // 예매 자격 확인 (ENTERED 상태만 허용)
        if (!eligibilityChecker.checkEntered(memberId, concertId)) {
            return ResponseEntity.badRequest()
                .body("예매 자격이 없습니다. 대기열에 입장 후 예매 시간을 기다려주세요.");
        }
        
        try {
            // 공통 서비스를 통한 현재 좌석 상태 조회
            List<SeatStatusDto> seats = seatStatusService.getSeatStatusFromRedis(concertId);
            
            return ResponseEntity.ok(seats);
        } catch (Exception e) {
            log.error("좌석 상태 조회 실패 (REST)", e);
            return ResponseEntity.internalServerError()
                .body("좌석 상태 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 좌석 점유 요청 (REST API)
     */
    @PostMapping("/{concertId}/seats/{seatId}/hold")
    @Operation(summary = "좌석 점유", description = "특정 좌석을 점유합니다")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좌석 점유 성공",
            content = @Content(examples = @ExampleObject(value = SwaggerExamples.SEAT_HOLD_SUCCESS))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "좌석 점유 실패",
            content = @Content(examples = @ExampleObject(value = SwaggerExamples.SEAT_HOLD_FAILURE)))
    })
    public ResponseEntity<?> holdSeat(@PathVariable("concertId") BigInteger concertId, 
                                     @PathVariable("seatId") BigInteger seatId) {
        BigInteger memberId = getCurrentMemberId();
        log.info("좌석 점유 요청 (REST): concertId={}, seatId={}, memberId={}", 
            concertId, seatId, memberId);
        
        try {
            // 공통 서비스를 통한 좌석 점유 처리 (REST API는 sessionId가 없으므로 null 전달)
            SeatHoldResult result = seatHoldService.holdSeat(concertId, seatId, memberId, null);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("좌석 점유 실패 (REST)", e);
            return ResponseEntity.internalServerError()
                .body("좌석 점유 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 좌석 해제 요청 (REST API)
     */
    @DeleteMapping("/{concertId}/seats/{seatId}/hold")
    @Operation(summary = "좌석 해제", description = "점유한 좌석을 해제합니다")
    public ResponseEntity<?> releaseSeat(@PathVariable("concertId") BigInteger concertId,
                                        @PathVariable("seatId") BigInteger seatId) {
        BigInteger memberId = getCurrentMemberId();
        log.info("좌석 해제 요청 (REST): concertId={}, seatId={}, memberId={}", 
            concertId, seatId, memberId);
        
        // 예매 자격 확인 (READY 또는 ENTERED 상태만 허용)
        if (!eligibilityChecker.checkEntered(memberId, concertId)) {
            return ResponseEntity.badRequest()
                .body("예매 자격이 없습니다. 대기열에 입장 후 예매 시간을 기다려주세요.");
        }
        
        try {
            // 공통 서비스를 통한 좌석 해제 처리 (REST API는 sessionId가 없으므로 null 전달)
            SeatHoldResult result = seatHoldService.releaseSeat(concertId, seatId, memberId, null);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            log.error("좌석 해제 실패 (REST)", e);
            return ResponseEntity.internalServerError()
                .body("좌석 해제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 현재 인증된 사용자의 ID 가져오기
     */
    private BigInteger getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getMemberId();
        }
        throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다.");
    }

    /**
     * 실시간 좌석 상태 응답 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class RealtimeSeatStatusResponse {
        private List<SeatStatusDto> seats;
        private String websocketUrl;
        private String subscribeTopic;
    }

} 