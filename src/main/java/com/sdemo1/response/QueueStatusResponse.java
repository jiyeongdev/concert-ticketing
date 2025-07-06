package com.sdemo1.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusResponse {
    private BigInteger concertId;
    private String concertTitle;
    private Integer queueNumber;
    private Integer totalWaitingCount;
    private Integer estimatedWaitTime; // 분 단위
    private String estimatedEnterTime; // 예상 입장 시간
    private String status; // WAITING, ENTERED, EXPIRED, BOOKING_CLOSED, NOT_IN_QUEUE
    private Boolean canEnter; // 입장 가능 여부
} 