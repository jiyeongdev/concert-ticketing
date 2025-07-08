package com.sdemo1.repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import com.sdemo1.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, BigInteger> {
    
    /**
     * 사용자별 예매 내역 조회
     */
    @Query("SELECT r FROM Reservation r WHERE r.member.id = :memberId ORDER BY r.reservationTime DESC")
    List<Reservation> findByMemberId(@Param("memberId") BigInteger memberId);
    
    /**
     * 콘서트별 예매 내역 조회
     */
    @Query("SELECT r FROM Reservation r WHERE r.seat.concert.id = :concertId ORDER BY r.reservationTime DESC")
    List<Reservation> findByConcertId(@Param("concertId") BigInteger concertId);
    
    /**
     * 좌석별 예매 조회
     */
    Optional<Reservation> findBySeatId(BigInteger seatId);
    
    /**
     * 예매 상태별 조회
     */
    List<Reservation> findByPaymentStatus(Reservation.PaymentStatus status);
    
    /**
     * 사용자와 좌석으로 예매 조회
     */
    @Query("SELECT r FROM Reservation r WHERE r.member.id = :memberId AND r.seat.id = :seatId")
    Optional<Reservation> findByMemberIdAndSeatId(@Param("memberId") BigInteger memberId, @Param("seatId") BigInteger seatId);
    
    /**
     * 진행 중인 예매 조회 (결제 대기 중)
     */
    @Query("SELECT r FROM Reservation r WHERE r.paymentStatus = 'PENDING'")
    List<Reservation> findPendingReservations();
} 