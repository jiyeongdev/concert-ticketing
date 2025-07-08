package com.sdemo1.common.utils;

import java.math.BigInteger;
import com.sdemo1.common.UserStatus;
import com.sdemo1.service.queue.RedisQueueService;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 예매 자격 확인 유틸리티
 * 사용자의 예매 자격을 확인하는 공통 로직을 제공
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEligibilityChecker {
    
    private final RedisQueueService redisQueueService;
    
    /**
     * READY 또는 ENTERED 상태인지 확인
     */
    public boolean checkReadyOrEntered(BigInteger memberId, BigInteger concertId) {
        String statusStr = redisQueueService.getUserStatus(memberId, concertId);
        UserStatus status = UserStatus.fromString(statusStr);
        boolean isEligible = status == UserStatus.READY || status == UserStatus.ENTERED;
        
        log.debug("예매 자격 확인 (READY/ENTERED): memberId={}, concertId={}, status={}, eligible={}", 
            memberId, concertId, status, isEligible);
            
        return isEligible;
    }
    
    /**
     * READY 상태인지 확인
     */
    public boolean checkReady(BigInteger memberId, BigInteger concertId) {
        String statusStr = redisQueueService.getUserStatus(memberId, concertId);
        UserStatus status = UserStatus.fromString(statusStr);
        boolean isEligible = status == UserStatus.READY;
        
        log.debug("예매 자격 확인 (READY): memberId={}, concertId={}, status={}, eligible={}", 
            memberId, concertId, status, isEligible);
            
        return isEligible;
    }
    
    /**
     * ENTERED 상태인지 확인
     */
    public boolean checkEntered(BigInteger memberId, BigInteger concertId) {
        String statusStr = redisQueueService.getUserStatus(memberId, concertId);
        UserStatus status = UserStatus.fromString(statusStr);
        boolean isEligible = status == UserStatus.ENTERED;
        
        log.debug("예매 자격 확인 (ENTERED): memberId={}, concertId={}, status={}, eligible={}", 
            memberId, concertId, status, isEligible);
            
        return isEligible;
    }
    
    /**
     * WAITING 상태인지 확인
     */
    public boolean checkWaiting(BigInteger memberId, BigInteger concertId) {
        String statusStr = redisQueueService.getUserStatus(memberId, concertId);
        UserStatus status = UserStatus.fromString(statusStr);
        boolean isWaiting = status == UserStatus.WAITING;
        
        log.debug("대기 상태 확인 (WAITING): memberId={}, concertId={}, status={}, waiting={}", 
            memberId, concertId, status, isWaiting);
            
        return isWaiting;
    }
    
    /**
     * 예매 가능한 상태인지 확인 (READY 또는 ENTERED)
     */
    public boolean isBookingEligible(BigInteger memberId, BigInteger concertId) {
        return checkReadyOrEntered(memberId, concertId);
    }
    
    /**
     * 실시간 좌석 상태 조회 가능한 상태인지 확인 (ENTERED)
     */
    public boolean isSeatStatusViewable(BigInteger memberId, BigInteger concertId) {
        return checkEntered(memberId, concertId);
    }
} 