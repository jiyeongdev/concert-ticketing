package com.sdemo1.service.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import com.sdemo1.entity.Concert;
import com.sdemo1.repository.ConcertRepository;
import com.sdemo1.service.queue.EnhancedWaitingQueueService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueSchedulerService {

    private final EnhancedWaitingQueueService enhancedWaitingQueueService;
    private final ConcertRepository concertRepository;

    @Value("${queue.concert.group-size:10}")
    private int groupSize;

    /**
     * 매 30초마다 큐 오픈(입장) 처리
     */
    @Scheduled(fixedRate = 30000)
    public void openQueuesAutomatically() {
        LocalDateTime now = LocalDateTime.now();
        List<Concert> openTargets = concertRepository.findByOpenTimeLessThanEqualAndCloseTimeAfter(now, now);
        for (Concert concert : openTargets) {
            try {
                log.info("[스케줄러] 콘서트 {} 큐 오픈 자동 처리", concert.getId());
                enhancedWaitingQueueService.startBooking(concert.getId(), groupSize);
            } catch (Exception e) {
                log.error("[스케줄러] 콘서트 {} 큐 오픈 처리 실패: {}", concert.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * 매 1분마다 큐 종료 정리
     */
    @Scheduled(fixedRate = 60000)
    public void cleanupClosedQueues() {
        LocalDateTime now = LocalDateTime.now();
        List<Concert> closedTargets = concertRepository.findByCloseTimeLessThanEqual(now);
        for (Concert concert : closedTargets) {
            try {
                log.info("[스케줄러] 콘서트 {} 큐 종료 정리", concert.getId());
                enhancedWaitingQueueService.cleanupExpiredQueues();
            } catch (Exception e) {
                log.error("[스케줄러] 콘서트 {} 큐 종료 정리 실패: {}", concert.getId(), e.getMessage(), e);
            }
        }
    }
} 