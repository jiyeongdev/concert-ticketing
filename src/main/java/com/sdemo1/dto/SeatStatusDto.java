package com.sdemo1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusDto {
    private BigInteger id;
    private BigInteger concertId;
    private BigInteger seatGradeId;
    private String seatRow;
    private String seatNumber;
    private Integer positionX;
    private Integer positionY;
    private String status; // AVAILABLE, HELD, BOOKED
    private String gradeName;
    private Integer price;
    private String heldBy; // 점유한 사용자 이름 (점유 중인 경우)
    private Long remainingHoldTime; // 남은 점유 시간 (초)
} 