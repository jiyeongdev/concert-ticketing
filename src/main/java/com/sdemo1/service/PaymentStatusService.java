package com.sdemo1.service;

import java.time.LocalDateTime;
import java.util.UUID;
import com.sdemo1.entity.Payment;
import com.sdemo1.entity.Reservation;
import com.sdemo1.repository.PaymentRepository;
import com.sdemo1.repository.ReservationRepository;
import com.sdemo1.service.seat.hold.RedisSeatHoldService;
import com.sdemo1.service.seat.status.SeatStatusEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentStatusService {
    
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final RedisSeatHoldService redisSeatHoldService;
    private final SeatStatusEventPublisher eventPublisher;
    
    /**
     * 결제 상태 업데이트
     */
    @Transactional
    public void updatePaymentStatus(Long paymentId, Payment.PaymentStatus status, String transactionId) {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다."));
        
        payment.setStatus(status);
        payment.setTransactionId(transactionId);
        
        if (status == Payment.PaymentStatus.SUCCESS) {
            payment.setPaidAt(LocalDateTime.now());
        }
        
        paymentRepository.save(payment);
        
        // 예매 상태도 함께 업데이트
        updateReservationStatus(payment.getReservation().getId(), status);
        
        log.info("결제 상태 업데이트: paymentId={}, status={}, transactionId={}", 
            paymentId, status, transactionId);
    }
    
    /**
     * 예매 상태 업데이트
     */
    @Transactional
    public void updateReservationStatus(Long reservationId, Payment.PaymentStatus paymentStatus) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예매입니다."));
        
        switch (paymentStatus) {
            case SUCCESS:
                reservation.setPaymentStatus(Reservation.PaymentStatus.PAID);
                break;
            case FAIL:
            case CANCELLED:
                reservation.setPaymentStatus(Reservation.PaymentStatus.FAILED);
                break;
            default:
                reservation.setPaymentStatus(Reservation.PaymentStatus.PENDING);
        }
        
        reservationRepository.save(reservation);
        log.info("예매 상태 업데이트: reservationId={}, status={}", reservationId, reservation.getPaymentStatus());
    }
    
    /**
     * 중복 결제 방지 체크
     */
    public boolean isPaymentInProgress(Long memberId, Long seatId) {
        return paymentRepository.findByReservationId(
            reservationRepository.findByMemberIdAndSeatId(memberId, seatId)
                .map(Reservation::getId)
                .orElse(null)
        ).isPresent();
    }
    
    /**
     * 거래 ID 생성
     */
    public String generateTransactionId() {
        return "TXN_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
} 