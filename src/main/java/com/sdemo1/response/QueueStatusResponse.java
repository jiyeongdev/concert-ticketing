package com.sdemo1.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QueueStatusResponse {
    private Long concertId;
    private String concertTitle;
    private Integer queueNumber;
    private Integer totalWaitingCount;
    private Integer estimatedWaitTime; // 분 단위
    private String estimatedEnterTime; // 예상 입장 시간
    private String status; // WAITING, ENTERED, EXPIRED, BOOKING_CLOSED, NOT_IN_QUEUE
    private Boolean canEnter; // 입장 가능 여부
} 