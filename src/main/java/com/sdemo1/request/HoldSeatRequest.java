package com.sdemo1.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoldSeatRequest {
    @NotNull(message = "좌석 ID는 필수입니다.")
    private Long seatId;
} 