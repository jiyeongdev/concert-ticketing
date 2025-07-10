package com.sdemo1.dto.seat;

import io.micrometer.common.lang.Nullable;

/**
 * 좌석등급 정보 DTO
 * 인터파크 공연의 실제 좌석등급과 유사하게 구성
 */
public record SeatGradeDto(
    @Nullable
    Long id,
    
    Long concertId,
    String gradeName,  // VIP, R, S, A, B 등
    Integer price
) {
    // Record는 자동으로 equals, hashCode, toString 메서드를 생성
    // 생성자도 자동으로 생성됨
} 