package com.sdemo1.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long memberId;
    
    @NotNull(message = "콘서트 ID는 필수입니다.")
    private Long concertId;
    
    @NotNull(message = "좌석 ID는 필수입니다.")
    private Long seatId;
    
    @NotNull(message = "결제 금액은 필수입니다.")
    @Positive(message = "결제 금액은 0보다 커야 합니다.")
    private Integer amount;
    
    @NotNull(message = "결제 방법은 필수입니다.")
    private String paymentMethod; // CARD, BANK_TRANSFER, KAKAO_PAY 등
} 