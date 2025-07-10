package com.sdemo1.dto.seat;

import java.util.List;
import com.sdemo1.common.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 좌석 점유/해제 WebSocket 관련 DTO 클래스들
 */
public class SeatHoldWebSocketDto {

    /**
     * 좌석 상태 구독 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatStatusSubscriptionRequest {
        private Long concertId;
        private Long memberId;
    }

    /**
     * 좌석 점유 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatHoldRequest {
        private Long concertId;
        private Long seatId;
        private Long memberId;
    }

    /**
     * 좌석 해제 요청 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatReleaseRequest {
        private Long concertId;
        private Long seatId;
        private Long memberId;
    }

    /**
     * 좌석 점유/해제 응답 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeatHoldResponse {
        private Long concertId;
        private List<SeatStatusDto> seats;
        private String type; // INITIAL_STATUS, HOLD_SUCCESS, HOLD_FAILED, RELEASE_SUCCESS, RELEASE_FAILED, SUBSCRIPTION_DENIED, etc.
        private String message;
        
        // 사용자 상태 정보 (실패 시 원인 파악용)
        private UserStatus userStatus;
        private String statusDescription;
    }
} 