package com.sdemo1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RabbitMQ 큐 입장 메시지 DTO
 * 대기열에서 처리 큐로 이동하는 메시지
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueEntryMessage {
    private Long memberId;
    private Long concertId;
    private String concertTitle;
} 