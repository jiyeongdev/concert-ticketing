package com.sdemo1.service;

import java.math.BigInteger;
import java.util.Random;
import com.sdemo1.service.seat.hold.RedisSeatHoldService;
import com.sdemo1.service.seat.status.SeatStatusEventPublisher;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final RedisSeatHoldService redisSeatHoldService;
    private final SeatStatusEventPublisher eventPublisher;
    
    /**
     * 결제 처리 (모킹)
     */
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("결제 처리 시작: memberId={}, seatId={}, amount={}", 
            request.getMemberId(), request.getSeatId(), request.getAmount());
        
        try {
            // 결제 API 호출 (모킹)
            boolean paymentSuccess = callExternalPaymentAPI(request);
            
            if (paymentSuccess) {
                // 좌석 예매 확정
                boolean reservationConfirmed = redisSeatHoldService.confirmReservation(
                    request.getConcertId(), 
                    request.getSeatId(), 
                    request.getMemberId()
                );
                
                if (reservationConfirmed) {
                    // 예매 확정 이벤트 발행
                    eventPublisher.publishSeatBooked(
                        request.getConcertId(), 
                        request.getSeatId(), 
                        request.getMemberId()
                    );
                    
                    log.info("결제 및 예매 확정 성공: memberId={}, seatId={}", 
                        request.getMemberId(), request.getSeatId());
                    
                    return PaymentResult.builder()
                        .success(true)
                        .message("결제 및 예매가 완료되었습니다.")
                        .build();
                } else {
                    // 예매 확정 실패 시 결제 취소
                    cancelPayment(request);
                    
                    return PaymentResult.builder()
                        .success(false)
                        .message("예매 확정에 실패했습니다.")
                        .build();
                }
            } else {
                // 결제 실패 시 좌석 해제
                redisSeatHoldService.releaseSeat(
                    request.getConcertId(), 
                    request.getSeatId(), 
                    request.getMemberId()
                );
                
                eventPublisher.publishSeatRelease(request.getConcertId(), request.getSeatId());
                
                return PaymentResult.builder()
                    .success(false)
                    .message("결제에 실패했습니다.")
                    .build();
            }
            
        } catch (Exception e) {
            log.error("결제 처리 중 오류 발생", e);
            
            // 오류 발생 시 좌석 해제
            try {
                redisSeatHoldService.releaseSeat(
                    request.getConcertId(), 
                    request.getSeatId(), 
                    request.getMemberId()
                );
                eventPublisher.publishSeatRelease(request.getConcertId(), request.getSeatId());
            } catch (Exception releaseError) {
                log.error("좌석 해제 실패", releaseError);
            }
            
            return PaymentResult.builder()
                .success(false)
                .message("결제 처리 중 오류가 발생했습니다.")
                .build();
        }
    }
    
    /**
     * 외부 결제 API 호출 (모킹)
     */
    private boolean callExternalPaymentAPI(PaymentRequest request) {
        // 실제로는 PG사 API 호출
        // 여기서는 90% 성공률로 모킹
        Random random = new Random();
        return random.nextInt(100) < 90; // 90% 성공률
    }
    
    /**
     * 결제 취소
     */
    private void cancelPayment(PaymentRequest request) {
        log.info("결제 취소: memberId={}, seatId={}", request.getMemberId(), request.getSeatId());
        // 실제로는 PG사 취소 API 호출
    }
    
    /**
     * 결제 요청 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class PaymentRequest {
        private BigInteger memberId;
        private BigInteger concertId;
        private BigInteger seatId;
        private Integer amount;
        private String paymentMethod; // CARD, BANK_TRANSFER 등
    }
    
    /**
     * 결제 결과 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class PaymentResult {
        private boolean success;
        private String message;
        private String transactionId; // 결제 거래 ID
    }
} 