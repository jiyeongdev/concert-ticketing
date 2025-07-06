package com.sdemo1.service;

import com.sdemo1.service.impl.SeatReservationServiceImpl;
import com.sdemo1.service.impl.WaitingQueueServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupService {

    private final WaitingQueueServiceImpl waitingQueueService;
    private final SeatReservationServiceImpl seatReservationService;

    /**
     * 매 5분마다 만료된 대기열 정리
     */
    @Scheduled(fixedRate = 300000) // 5분 = 300,000ms
    public void cleanupExpiredQueues() {
        try {
            log.info("=== 만료된 대기열 정리 스케줄러 실행 ===");
            waitingQueueService.cleanupExpiredQueues();
        } catch (Exception e) {
            log.error("만료된 대기열 정리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 매 1분마다 만료된 좌석 점유 정리
     */
    @Scheduled(fixedRate = 60000) // 1분 = 60,000ms
    public void cleanupExpiredHolds() {
        try {
            log.info("=== 만료된 좌석 점유 정리 스케줄러 실행 ===");
            seatReservationService.cleanupExpiredHolds();
        } catch (Exception e) {
            log.error("만료된 좌석 점유 정리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
} 