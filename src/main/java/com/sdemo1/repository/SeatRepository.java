package com.sdemo1.repository;

import java.util.List;
import com.sdemo1.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    /**
     * 콘서트 ID로 좌석 조회
     */
    List<Seat> findByConcertIdOrderBySeatRowAscSeatNumberAsc(Long concertId);
    
    /**
     * 콘서트 ID와 좌석등급 ID로 좌석 조회
     */
    List<Seat> findByConcertIdAndSeatGradeIdOrderBySeatRowAscSeatNumberAsc(Long concertId, Long seatGradeId);
    
    /**
     * 콘서트 ID와 좌석 상태로 조회
     */
    List<Seat> findByConcertIdAndStatusOrderBySeatRowAscSeatNumberAsc(Long concertId, Seat.SeatStatus status);
    
    /**
     * 콘서트 ID로 좌석 존재 여부 확인
     */
    boolean existsByConcertId(Long concertId);
    
    /**
     * 콘서트 ID로 모든 좌석 조회
     */
    List<Seat> findByConcertId(Long concertId);
    
    /**
     * 콘서트 ID, 행, 번호로 특정 좌석 조회
     */
    Seat findByConcertIdAndSeatRowAndSeatNumber(Long concertId, String seatRow, String seatNumber);
    
    /**
     * 콘서트 ID로 사용 가능한 좌석 수 조회
     */
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.concert.id = :concertId AND s.status = 'AVAILABLE'")
    long countAvailableSeatsByConcertId(@Param("concertId") Long concertId);
    
    /**
     * 콘서트 ID로 모든 좌석 삭제
     */
    void deleteByConcertId(Long concertId);
} 