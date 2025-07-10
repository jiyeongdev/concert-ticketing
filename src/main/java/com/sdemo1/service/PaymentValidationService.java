package com.sdemo1.service;

import com.sdemo1.entity.Seat;
import com.sdemo1.entity.SeatGrade;
import com.sdemo1.repository.SeatGradeRepository;
import com.sdemo1.repository.SeatRepository;
import com.sdemo1.service.seat.hold.RedisSeatHoldService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentValidationService {
    
    private final SeatRepository seatRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final RedisSeatHoldService redisSeatHoldService;
    private final PaymentStatusService paymentStatusService;
    
    /**
     * 결제 요청 검증
     */
    public ValidationResult validatePaymentRequest(Long memberId, Long concertId, 
                                                Long seatId, Integer amount, String paymentMethod) {
        try {
            // 1. 좌석 존재 여부 확인
            Seat seat = seatRepository.findById(seatId)
                .orElse(null);
            if (seat == null) {
                return ValidationResult.failure("존재하지 않는 좌석입니다.");
            }
            
            // 2. 좌석이 해당 콘서트에 속하는지 확인
            if (!seat.getConcert().getId().equals(concertId)) {
                return ValidationResult.failure("좌석이 해당 콘서트에 속하지 않습니다.");
            }
            
            // 3. 좌석 점유 상태 확인
            String seatStatus = redisSeatHoldService.getSeatStatus(concertId, seatId);
            if (!"HELD".equals(seatStatus)) {
                return ValidationResult.failure("점유되지 않은 좌석입니다.");
            }
            
            // 4. 점유자가 요청자와 일치하는지 확인
            RedisSeatHoldService.SeatHoldInfo holdInfo = redisSeatHoldService.getSeatHoldInfo(concertId, seatId);
            if (holdInfo == null || !holdInfo.getMemberId().equals(memberId)) {
                return ValidationResult.failure("본인이 점유한 좌석이 아닙니다.");
            }
            
            // 5. 좌석 가격과 결제 금액 일치 확인
            SeatGrade seatGrade = seat.getSeatGrade();
            if (seatGrade != null && !seatGrade.getPrice().equals(amount)) {
                return ValidationResult.failure("좌석 가격과 결제 금액이 일치하지 않습니다.");
            }
            
            // 6. 결제 방법 유효성 확인
            if (!isValidPaymentMethod(paymentMethod)) {
                return ValidationResult.failure("지원하지 않는 결제 방법입니다.");
            }
            
            // 7. 중복 결제 방지
            if (paymentStatusService.isPaymentInProgress(memberId, seatId)) {
                return ValidationResult.failure("이미 결제가 진행 중인 좌석입니다.");
            }
            
            // 8. 점유 시간 확인 (10분 이내)
            if (holdInfo.getRemainingSeconds() <= 0) {
                return ValidationResult.failure("좌석 점유 시간이 만료되었습니다.");
            }
            
            log.info("결제 요청 검증 성공: memberId={}, seatId={}, amount={}", memberId, seatId, amount);
            return ValidationResult.success();
            
        } catch (Exception e) {
            log.error("결제 요청 검증 중 오류 발생: memberId={}, seatId={}", memberId, seatId, e);
            return ValidationResult.failure("결제 검증 중 오류가 발생했습니다.");
        }
    }
    
    /**
     * 결제 방법 유효성 확인
     */
    private boolean isValidPaymentMethod(String paymentMethod) {
        return paymentMethod != null && (
            "CARD".equals(paymentMethod) ||
            "BANK_TRANSFER".equals(paymentMethod) ||
            "KAKAO_PAY".equals(paymentMethod) ||
            "NAVER_PAY".equals(paymentMethod) ||
            "PAYPAL".equals(paymentMethod)
        );
    }
    
    /**
     * 검증 결과 DTO
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, "검증 성공");
        }
        
        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
} 