package com.sdemo1.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinQueueRequest {
    @NotNull(message = "콘서트 ID는 필수입니다.")
    private Long concertId;
} 