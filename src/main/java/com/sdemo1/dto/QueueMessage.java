package com.sdemo1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueMessage {
    private BigInteger memberId;
    private BigInteger concertId;
    private String memberName;
    private String concertTitle;
    private Integer queueNumber;
    private LocalDateTime timestamp;
    private String messageType; // JOIN, ENTER, LEAVE, EXPIRE
    private Integer estimatedWaitTime;
} 