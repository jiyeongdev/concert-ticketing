package com.sdemo1.service.queue;

import com.sdemo1.dto.QueueEntryMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueMessageListener {

    private final RedisQueueService redisQueueService;

    /**
     * 큐 입장 처리 메시지 리스너
     * RabbitMQ에서 메시지를 받아서 Redis에 사용자 상태를 READY로 변경
     */
    @RabbitListener(queues = "#{@defaultProcessingQueue}")
    public void handleQueueEntry(QueueEntryMessage message) {
        try {
            log.info("콘서트별 큐 입장 메시지 처리 시작: memberId={}, concertId={}, concertTitle={}", 
                    message.getMemberId(), message.getConcertId(), message.getConcertTitle());

            Long memberId = message.getMemberId();
            Long concertId = message.getConcertId();
        
            // Redis에 사용자 상태를 READY로 변경 (예매 입장 허용)
            boolean success = redisQueueService.setUserReady(memberId, concertId);
            
            if (success) {
                log.info("콘서트별 큐 입장 처리 완료: memberId={}, concertId={}, concertTitle={}", 
                        memberId, concertId, message.getConcertTitle());
            } else {
                log.error("콘서트별 큐 입장 처리 실패: memberId={}, concertId={}, concertTitle={}", 
                        memberId, concertId, message.getConcertTitle());
            }

        } catch (Exception e) {
            log.error("콘서트별 큐 입장 메시지 처리 중 오류 발생: message={}", message, e);
        }
    }
} 