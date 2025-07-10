package com.sdemo1.service.queue;

import com.sdemo1.dto.QueueEntryMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${queue.concert.entry-interval-seconds:30}")
    private int entryIntervalSeconds;

    @Value("${queue.concert.exchanges.processing}")
    private String processingExchangeName;

    @Value("${queue.concert.routing-keys.processing}")
    private String processingRoutingKey;

    /**
     * 큐 입장 처리 메시지 전송
     * Redis에서 상위 N명을 추출한 후 이 메서드를 호출하여 순차 처리
     */
    public void sendToProcessingQueue(Long memberId, Long concertId, String concertTitle) {
        QueueEntryMessage message = QueueEntryMessage.builder()
                .memberId(memberId)
                .concertId(concertId)
                .concertTitle(concertTitle)
                .build();

        // 설정 파일의 exchange와 라우팅 키 사용
        rabbitTemplate.convertAndSend(processingExchangeName, processingRoutingKey, message);
        
        log.info("큐 입장 메시지 전송: exchange={}, routingKey={}, memberId={}, concertId={}, concertTitle={}", 
                processingExchangeName, processingRoutingKey, memberId, concertId, concertTitle);
    }
} 