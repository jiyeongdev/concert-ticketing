package com.sdemo1.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueMessage {
    private Long memberId;
    private Long concertId;
    private String memberName;
    private String concertTitle;
    private Integer queueNumber;
    private LocalDateTime timestamp;
    private String messageType; // JOIN, ENTER, LEAVE, EXPIRE
    private Integer estimatedWaitTime;
} 