package com.sdemo1.repository;

import com.sdemo1.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, BigInteger> {
    
    /**
     * 콘서트 ID로 좌석 조회
     */
    List<Seat> findByConcertIdOrderBySeatRowAscSeatNumberAsc(BigInteger concertId);
    
    /**
     * 콘서트 ID와 좌석등급 ID로 좌석 조회
     */
    List<Seat> findByConcertIdAndSeatGradeIdOrderBySeatRowAscSeatNumberAsc(BigInteger concertId, BigInteger seatGradeId);
    
    /**
     * 콘서트 ID와 좌석 상태로 조회
     */
    List<Seat> findByConcertIdAndStatusOrderBySeatRowAscSeatNumberAsc(BigInteger concertId, Seat.SeatStatus status);
    
    /**
     * 콘서트 ID로 좌석 존재 여부 확인
     */
    boolean existsByConcertId(BigInteger concertId);
    
    /**
     * 콘서트 ID, 행, 번호로 특정 좌석 조회
     */
    Seat findByConcertIdAndSeatRowAndSeatNumber(BigInteger concertId, String seatRow, String seatNumber);
    
    /**
     * 콘서트 ID로 사용 가능한 좌석 수 조회
     */
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.concert.id = :concertId AND s.status = 'AVAILABLE'")
    long countAvailableSeatsByConcertId(@Param("concertId") BigInteger concertId);
    
    /**
     * 콘서트 ID로 모든 좌석 삭제
     */
    void deleteByConcertId(BigInteger concertId);
} 