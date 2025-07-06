package com.sdemo1.service;

import com.sdemo1.request.JoinQueueRequest;
import com.sdemo1.response.QueueStatusResponse;

import java.math.BigInteger;
import java.util.List;

public interface WaitingQueueService {
    
    /**
     * 대기열 참가
     */
    QueueStatusResponse joinQueue(BigInteger memberId, JoinQueueRequest request);
    
    /**
     * 대기열 상태 조회
     */
    QueueStatusResponse getQueueStatus(BigInteger memberId, BigInteger concertId);
    
    /**
     * 대기열 입장 처리
     */
    boolean enterFromQueue(BigInteger memberId, BigInteger concertId);
    
    /**
     * 대기열에서 나가기
     */
    void leaveQueue(BigInteger memberId, BigInteger concertId);
    
    /**
     * 특정 콘서트의 대기열 조회 (관리자용)
     */
    List<QueueStatusResponse> getQueueByConcertId(BigInteger concertId);
    
    /**
     * 만료된 대기열 정리
     */
    void cleanupExpiredQueues();
} 