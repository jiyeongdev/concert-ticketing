package com.sdemo1.controller;

import java.math.BigInteger;
import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.config.SwaggerExamples;
import com.sdemo1.request.PaymentRequest;
import com.sdemo1.security.CustomUserDetails;
import com.sdemo1.service.PaymentCancelService;
import com.sdemo1.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/reservation")
@RequiredArgsConstructor
@Tag(name = "5. 예매/결제 관리", description = "결제 처리, 결제 취소 API")
public class ReservationController {

    private final PaymentService paymentService;
    private final PaymentCancelService paymentCancelService;

    /**
     * 결제 및 예매 확정
     */
    @PostMapping("/payment")
    @Operation(summary = "결제 처리", description = "좌석 예매를 위한 결제를 처리하고 예매를 확정합니다",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(value = SwaggerExamples.PAYMENT_REQUEST))))
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "결제 성공",
            content = @Content(examples = @ExampleObject(value = SwaggerExamples.PAYMENT_SUCCESS))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "결제 실패",
            content = @Content(examples = @ExampleObject(value = SwaggerExamples.PAYMENT_FAILURE)))
    })
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
     * 결제 취소
     */
    @DeleteMapping("/payment/{paymentId}")
    @Operation(summary = "결제 취소", description = "완료된 결제를 취소하고 예매를 해제합니다")
    public ResponseEntity<ApiResponse<?>> cancelPayment(@PathVariable("paymentId") BigInteger paymentId,
                                                      @RequestParam("reason") String reason) {
        try {
            BigInteger memberId = getCurrentMemberId();
            log.info("=== 결제 취소 API 호출: memberId={}, paymentId={}, reason={} ===", 
                memberId, paymentId, reason);
            
            // 결제 취소 처리
            PaymentCancelService.CancelResult result = paymentCancelService.cancelPayment(memberId, paymentId, reason);
            
            if (result.isSuccess()) {
                return ResponseEntity.ok()
                        .body(new ApiResponse<>(result.getMessage(), result, HttpStatus.OK));
            } else {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(result.getMessage(), result, HttpStatus.BAD_REQUEST));
            }
            
        } catch (Exception e) {
            log.error("결제 취소 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("결제 취소 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
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