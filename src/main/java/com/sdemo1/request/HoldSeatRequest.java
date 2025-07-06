package com.sdemo1.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoldSeatRequest {
    @NotNull(message = "좌석 ID는 필수입니다.")
    private BigInteger seatId;
} 