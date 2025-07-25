package com.sdemo1.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.sdemo1.entity.SeatHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {

    /**
     * 특정 좌석의 현재 점유 정보 조회
     */
    @Query("SELECT sh FROM SeatHold sh WHERE sh.seat.id = :seatId AND sh.holdExpireAt > :now")
    Optional<SeatHold> findBySeatIdAndNotExpired(@Param("seatId") Long seatId, @Param("now") LocalDateTime now);

    /**
     * 특정 사용자의 모든 점유 좌석 조회
     */
    @Query("SELECT sh FROM SeatHold sh WHERE sh.member.memberId = :memberId AND sh.holdExpireAt > :now")
    List<SeatHold> findByMemberIdAndNotExpired(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);

    /**
     * 만료된 점유 정보 조회
     */
    @Query("SELECT sh FROM SeatHold sh WHERE sh.holdExpireAt <= :now")
    List<SeatHold> findExpiredHolds(@Param("now") LocalDateTime now);

    /**
     * 특정 콘서트의 모든 점유 좌석 조회
     */
    @Query("SELECT sh FROM SeatHold sh WHERE sh.seat.concert.id = :concertId AND sh.holdExpireAt > :now")
    List<SeatHold> findByConcertIdAndNotExpired(@Param("concertId") Long concertId, @Param("now") LocalDateTime now);

    /**
     * 특정 좌석의 점유 정보 삭제
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM SeatHold sh WHERE sh.seat.id = :seatId")
    void deleteBySeatId(@Param("seatId") Long seatId);

    /**
     * 특정 좌석의 만료된 점유 정보 조회
     */
    @Query("SELECT sh FROM SeatHold sh WHERE sh.seat.id = :seatId AND sh.holdExpireAt <= :now")
    List<SeatHold> findBySeatIdAndExpired(@Param("seatId") Long seatId, @Param("now") LocalDateTime now);
} 