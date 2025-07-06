package com.sdemo1.service.queue;

import java.math.BigInteger;
import com.sdemo1.dto.QueueMessage;
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
     * 예매 입장 처리 메시지 리스너
     * RabbitMQ에서 메시지를 받아서 Redis에 사용자 상태를 READY로 변경
     */
    @RabbitListener(queues = "#{@defaultProcessingQueue}")
    public void handleReservationEntry(QueueMessage message) {
        try {
            log.info("예매 입장 메시지 처리 시작: memberId={}, concertId={}", 
                    message.getMemberId(), message.getConcertId());

            BigInteger memberId = message.getMemberId();
            BigInteger concertId = message.getConcertId();
        
            // Redis에 사용자 상태를 READY로 변경 (예매 입장 허용)
            boolean success = redisQueueService.setUserReady(memberId, concertId);
            
            if (success) {
                log.info("예매 입장 처리 완료: memberId={}, concertId={}", memberId, concertId);
            } else {
                log.error("예매 입장 처리 실패: memberId={}, concertId={}", memberId, concertId);
            }

        } catch (Exception e) {
            log.error("예매 입장 메시지 처리 중 오류 발생: message={}", message, e);
        }
    }


} 