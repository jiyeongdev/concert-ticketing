package com.sdemo1.service;

import java.math.BigInteger;
import java.util.Optional;
import com.sdemo1.entity.Member;
import com.sdemo1.entity.Payment;
import com.sdemo1.entity.Reservation;
import com.sdemo1.entity.Seat;
import com.sdemo1.repository.MemberRepository;
import com.sdemo1.repository.PaymentRepository;
import com.sdemo1.repository.ReservationRepository;
import com.sdemo1.repository.SeatRepository;
import com.sdemo1.service.seat.hold.RedisSeatHoldService;
import com.sdemo1.service.seat.status.SeatStatusEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final RedisSeatHoldService redisSeatHoldService;
    private final SeatStatusEventPublisher eventPublisher;
    private final PaymentStatusService paymentStatusService;
    private final PaymentValidationService paymentValidationService;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final SeatRepository seatRepository;
    
    /**
     * 결제 처리 (개선된 버전)
     */
    @Transactional
    public PaymentResult processPayment(PaymentRequest request) {
        log.info("결제 처리 시작: memberId={}, seatId={}, amount={}", 
            request.getMemberId(), request.getSeatId(), request.getAmount());
        
        try {
            // 1. 결제 요청 검증
            PaymentValidationService.ValidationResult validationResult = 
                paymentValidationService.validatePaymentRequest(
                    request.getMemberId(), 
                    request.getConcertId(), 
                    request.getSeatId(), 
                    request.getAmount(), 
                    request.getPaymentMethod()
                );
            
            if (!validationResult.isValid()) {
                return PaymentResult.builder()
                    .success(false)
                    .message(validationResult.getMessage())
                    .build();
            }
            
            // 2. 예매 정보 생성 (중복 방지)
            Reservation reservation = getOrCreateReservation(request);
            
            // 3. 결제 정보 생성 (모든 시도 기록)
            Payment payment = createPaymentAttempt(request, reservation);
            
            // 4. 외부 결제 API 호출 (모킹)
            boolean paymentSuccess = callExternalPaymentAPI(request);
            
            if (paymentSuccess) {
                // 5. 좌석 예매 확정
                boolean reservationConfirmed = redisSeatHoldService.confirmReservation(
                    request.getConcertId(), 
                    request.getSeatId(), 
                    request.getMemberId()
                );
                
                if (reservationConfirmed) {
                    // 6. 결제 상태를 SUCCESS로 업데이트
                    String transactionId = paymentStatusService.generateTransactionId();
                    paymentStatusService.updatePaymentStatus(payment.getId(), Payment.PaymentStatus.SUCCESS, transactionId);
                    
                    // 7. 예매 확정 이벤트 발행
                    eventPublisher.publishSeatBooked(
                        request.getConcertId(), 
                        request.getSeatId(), 
                        request.getMemberId()
                    );
                    
                    log.info("결제 및 예매 확정 성공: memberId={}, seatId={}, transactionId={}", 
                        request.getMemberId(), request.getSeatId(), transactionId);
                    
                    return PaymentResult.builder()
                        .success(true)
                        .message("결제 및 예매가 완료되었습니다.")
                        .transactionId(transactionId)
                        .build();
                } else {
                    // 예매 확정 실패 시 결제 취소
                    handlePaymentFailure(payment, request, "예매 확정에 실패했습니다.");
                    return PaymentResult.builder()
                        .success(false)
                        .message("예매 확정에 실패했습니다.")
                        .build();
                }
            } else {
                // 결제 실패 처리
                handlePaymentFailure(payment, request, "결제에 실패했습니다.");
                return PaymentResult.builder()
                    .success(false)
                    .message("결제에 실패했습니다.")
                    .build();
            }
            
        } catch (Exception e) {
            log.error("결제 처리 중 오류 발생", e);
            return PaymentResult.builder()
                .success(false)
                .message("결제 처리 중 오류가 발생했습니다.")
                .build();
        }
    }
    
    /**
     * 예매 정보 생성 (중복 방지)
     */
    private Reservation getOrCreateReservation(PaymentRequest request) {
        // 기존 예매가 있는지 확인
        Optional<Reservation> existingReservation = reservationRepository.findByMemberIdAndSeatId(
            request.getMemberId(), request.getSeatId()
        );
        
        if (existingReservation.isPresent()) {
            log.info("기존 예매 정보 재사용: memberId={}, seatId={}, reservationId={}", 
                request.getMemberId(), request.getSeatId(), existingReservation.get().getId());
            return existingReservation.get();
        }
        
        // 새 예매 생성
        Member member = memberRepository.findById(request.getMemberId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        
        Seat seat = seatRepository.findById(request.getSeatId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));
        
        Reservation reservation = new Reservation();
        reservation.setMember(member);
        reservation.setSeat(seat);
        reservation.setPaymentStatus(Reservation.PaymentStatus.PENDING);
        
        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("새 예매 정보 생성: memberId={}, seatId={}, reservationId={}", 
            request.getMemberId(), request.getSeatId(), savedReservation.getId());
        
        return savedReservation;
    }
    
    /**
     * 결제 시도 정보 생성 (모든 시도 기록)
     */
    private Payment createPaymentAttempt(PaymentRequest request, Reservation reservation) {
        // 이번 시도 번호 계산
        int attemptNumber = paymentRepository.countByReservationId(reservation.getId()) + 1;
        
        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setAmount(request.getAmount());
        payment.setPaymentGateway(request.getPaymentMethod());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setAttemptNumber(attemptNumber); // 시도 번호 추가
        
        Payment savedPayment = paymentRepository.save(payment);
        
        log.info("결제 시도 기록 생성: reservationId={}, attemptNumber={}, amount={}", 
            reservation.getId(), attemptNumber, request.getAmount());
        
        return savedPayment;
    }
    
    /**
     * 결제 실패 처리
     */
    private void handlePaymentFailure(Payment payment, PaymentRequest request, String reason) {
        try {
            // 결제 상태를 FAIL로 업데이트
            paymentStatusService.updatePaymentStatus(payment.getId(), Payment.PaymentStatus.FAIL, null);
            
            // 좌석 해제
            redisSeatHoldService.releaseSeat(
                request.getConcertId(), 
                request.getSeatId(), 
                request.getMemberId()
            );
            
            // 좌석 해제 이벤트 발행
            eventPublisher.publishSeatRelease(request.getConcertId(), request.getSeatId());
            
            log.info("결제 실패 처리 완료: paymentId={}, reason={}", payment.getId(), reason);
            
        } catch (Exception e) {
            log.error("결제 실패 처리 중 오류 발생: paymentId={}", payment.getId(), e);
        }
    }
    
    /**
     * 외부 결제 API 호출 (모킹)
     */
    private boolean callExternalPaymentAPI(PaymentRequest request) {
        return true;

        // 실제로는 PG사 API 호출
        // 여기서는 90% 성공률로 모킹
        // Random random = new Random();
        // return random.nextInt(100) < 90; // 90% 성공률
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