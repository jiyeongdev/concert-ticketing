package com.sdemo1.dto.seat;

import com.sdemo1.common.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 좌석 점유 결과 DTO
 * 
 * 사용자에게 제공할 상세한 결과 정보를 포함
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatHoldResult {
    private boolean success;
    private String message;
    private SeatStatusDto seatInfo;
    
    // 사용자 상태 정보
    private UserStatus userStatus;
} 