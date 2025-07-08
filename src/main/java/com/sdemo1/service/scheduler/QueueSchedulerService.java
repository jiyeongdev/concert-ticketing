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
    @Value("${queue.concert.entry-interval-seconds:30}")
    private int entryIntervalSeconds;

    /**
     * 매 30초마다 큐 오픈(입장) 처리
     */
    @Scheduled(fixedRate = 30000999)
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
     * 큐 종료 정리 - TTL 기반 자동 정리로 대체됨
     * 
     * Redis TTL 설정:
     * - 대기열 대기 상태: 240분 (4시간)
     * - 예매 입장 준비 상태: 60분
     * - 예매 페이지 입장 완료 상태: 60분
     * 
     * TTL이 만료되면 Redis에서 자동으로 삭제되므로 별도 정리 로직 불필요
     */
    // @Scheduled(fixedRate = 60000) // 주석 처리 - TTL 기반 자동 정리
    // public void cleanupClosedQueues() {
    //     // TTL이 자동으로 처리하므로 불필요
    // }
} 