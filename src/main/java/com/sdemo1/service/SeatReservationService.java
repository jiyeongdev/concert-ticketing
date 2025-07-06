package com.sdemo1.service;

import java.math.BigInteger;
import java.util.List;
import com.sdemo1.dto.SeatStatusDto;
import com.sdemo1.request.HoldSeatRequest;

public interface SeatReservationService {
    
    /**
     * 콘서트의 모든 좌석 상태 조회
     */
    List<SeatStatusDto> getSeatStatusByConcertId(BigInteger concertId);
    
    /**
     * 좌석 점유 (10분 타이머)
     */
    SeatStatusDto holdSeat(BigInteger memberId, HoldSeatRequest request);
    
    /**
     * 좌석 점유 해제
     */
    void releaseSeat(BigInteger memberId, BigInteger seatId);
    
    /**
     * 사용자의 점유 좌석 조회
     */
    List<SeatStatusDto> getUserHeldSeats(BigInteger memberId);
    
    /**
     * 만료된 좌석 점유 정리
     */
    void cleanupExpiredHolds();
    
    /**
     * 좌석 예매 확정 (결제 완료 후)
     */
    boolean confirmReservation(BigInteger memberId, BigInteger seatId);
} 