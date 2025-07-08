package com.sdemo1.controller;

import java.math.BigInteger;
import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.request.PaymentRequest;
import com.sdemo1.security.CustomUserDetails;
import com.sdemo1.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final PaymentService paymentService;

    /**
     * 결제 및 예매 확정
     */
    @PostMapping("/payment")
    public ResponseEntity<ApiResponse<?>> processPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            BigInteger memberId = getCurrentMemberId();
            log.info("=== 결제 처리 API 호출: memberId={}, seatId={}, amount={} ===", 
                memberId, request.getSeatId(), request.getAmount());
            
            // 요청자와 토큰의 사용자 일치 확인
            if (!memberId.equals(request.getMemberId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse<>("본인의 결제만 처리할 수 있습니다.", null, HttpStatus.FORBIDDEN));
            }
            
            // PaymentService.PaymentRequest로 변환
            PaymentService.PaymentRequest paymentRequest = PaymentService.PaymentRequest.builder()
                .memberId(request.getMemberId())
                .concertId(request.getConcertId())
                .seatId(request.getSeatId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .build();
            
            // 결제 처리
            PaymentService.PaymentResult result = paymentService.processPayment(paymentRequest);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok()
                        .body(new ApiResponse<>(result.getMessage(), result, HttpStatus.OK));
            } else {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(result.getMessage(), result, HttpStatus.BAD_REQUEST));
            }
            
        } catch (Exception e) {
            log.error("결제 처리 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("결제 처리 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 현재 인증된 사용자의 ID 가져오기
     */
    private BigInteger getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getMemberId();
        }
        throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다.");
    }
} 