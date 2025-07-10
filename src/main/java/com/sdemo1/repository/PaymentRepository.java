package com.sdemo1.repository;

import java.util.List;
import java.util.Optional;
import com.sdemo1.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    /**
     * 사용자별 결제 내역 조회
     */
    @Query("SELECT p FROM Payment p WHERE p.reservation.member.id = :memberId ORDER BY p.paidAt DESC")
    List<Payment> findByMemberId(@Param("memberId") Long memberId);
    
    /**
     * 콘서트별 결제 내역 조회
     */
    @Query("SELECT p FROM Payment p WHERE p.reservation.seat.concert.id = :concertId ORDER BY p.paidAt DESC")
    List<Payment> findByConcertId(@Param("concertId") Long concertId);
    
    /**
     * 결제 상태별 조회
     */
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    /**
     * 거래 ID로 결제 조회
     */
    Optional<Payment> findByTransactionId(String transactionId);
    
    /**
     * 예매 ID로 결제 조회
     */
    Optional<Payment> findByReservationId(Long reservationId);
    
    /**
     * 예매 ID로 결제 시도 횟수 조회
     */
    @Query("SELECT COUNT(p) FROM Payment p WHERE p.reservation.id = :reservationId")
    int countByReservationId(@Param("reservationId") Long reservationId);
} 