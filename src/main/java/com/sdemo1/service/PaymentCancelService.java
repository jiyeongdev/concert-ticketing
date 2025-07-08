package com.sdemo1.service;

import java.math.BigInteger;
import com.sdemo1.entity.Payment;
import com.sdemo1.entity.Reservation;
import com.sdemo1.repository.PaymentRepository;
import com.sdemo1.service.seat.hold.RedisSeatHoldService;
import com.sdemo1.service.seat.status.SeatStatusEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCancelService {
    
    private final PaymentRepository paymentRepository;
    private final PaymentStatusService paymentStatusService;
    private final RedisSeatHoldService redisSeatHoldService;
    private final SeatStatusEventPublisher eventPublisher;
    
    /**
     * 결제 취소 처리
     */
    @Transactional
    public CancelResult cancelPayment(BigInteger memberId, BigInteger paymentId, String reason) {
        try {
            log.info("결제 취소 요청: memberId={}, paymentId={}, reason={}", memberId, paymentId, reason);
            
            // 1. 결제 정보 조회
            Payment payment = paymentRepository.findById(paymentId)
                .orElse(null);
            
            if (payment == null) {
                return CancelResult.failure("존재하지 않는 결제입니다.");
            }
            
            // 2. 결제 소유자 확인
            if (!payment.getReservation().getMember().getMemberId().equals(memberId)) {
                return CancelResult.failure("본인의 결제만 취소할 수 있습니다.");
            }
            
            // 3. 결제 상태 확인
            if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
                return CancelResult.failure("완료된 결제만 취소할 수 있습니다.");
            }
            
            // 4. 외부 결제 취소 API 호출 (모킹)
            boolean cancelSuccess = callExternalCancelAPI(payment);
            
            if (cancelSuccess) {
                // 5. 결제 상태를 CANCELLED로 업데이트
                paymentStatusService.updatePaymentStatus(payment.getId(), Payment.PaymentStatus.CANCELLED, null);
                
                // 6. 좌석 해제
                Reservation reservation = payment.getReservation();
                redisSeatHoldService.releaseSeat(
                    reservation.getSeat().getConcert().getId(),
                    reservation.getSeat().getId(),
                    reservation.getMember().getMemberId()
                );
                
                // 7. 좌석 해제 이벤트 발행
                eventPublisher.publishSeatRelease(
                    reservation.getSeat().getConcert().getId(),
                    reservation.getSeat().getId()
                );
                
                log.info("결제 취소 성공: paymentId={}, memberId={}", paymentId, memberId);
                
                return CancelResult.success("결제가 취소되었습니다.");
            } else {
                return CancelResult.failure("결제 취소에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("결제 취소 처리 중 오류 발생: paymentId={}, memberId={}", paymentId, memberId, e);
            return CancelResult.failure("결제 취소 처리 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 외부 결제 취소 API 호출 (모킹)
     */
    private boolean callExternalCancelAPI(Payment payment) {
        // 실제로는 PG사 취소 API 호출
        // 여기서는 95% 성공률로 모킹
        java.util.Random random = new java.util.Random();
        return random.nextInt(100) < 95; // 95% 성공률
    }
    
    /**
     * 취소 결과 DTO
     */
    public static class CancelResult {
        private final boolean success;
        private final String message;
        
        private CancelResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public static CancelResult success(String message) {
            return new CancelResult(true, message);
        }
        
        public static CancelResult failure(String message) {
            return new CancelResult(false, message);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public String getMessage() {
            return message;
        }
    }
} 