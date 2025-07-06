package com.sdemo1.service;

import com.sdemo1.dto.SeatStatusDto;
import com.sdemo1.request.HoldSeatRequest;

import java.math.BigInteger;
import java.util.List;

public interface SeatReservationService {
    
    /**
     * 콘서트의 모든 좌석 상태 조회
     */
    List<SeatStatusDto> getSeatStatusByConcertId(BigInteger concertId);
    
    /**
     * 좌석 점유 (10분 타이머)
     */
    SeatStatusDto holdSeat(BigInteger userId, HoldSeatRequest request);
    
    /**
     * 좌석 점유 해제
     */
    void releaseSeat(BigInteger userId, BigInteger seatId);
    
    /**
     * 사용자의 점유 좌석 조회
     */
    List<SeatStatusDto> getUserHeldSeats(BigInteger userId);
    
    /**
     * 만료된 좌석 점유 정리
     */
    void cleanupExpiredHolds();
    
    /**
     * 좌석 예매 확정 (결제 완료 후)
     */
    boolean confirmReservation(BigInteger userId, BigInteger seatId);
} 