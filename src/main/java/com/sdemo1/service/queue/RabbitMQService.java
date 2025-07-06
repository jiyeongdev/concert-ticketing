package com.sdemo1.service.queue;

import com.sdemo1.config.RabbitMQConfig;
import com.sdemo1.dto.QueueMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQService {

    private final RabbitTemplate rabbitTemplate;
    private final AmqpAdmin amqpAdmin;
    private final RabbitMQConfig rabbitMQConfig;

    @Value("${queue.concert.processing-prefix}")
    private String processingPrefix;

    /**
     * 예매 입장 처리 큐에 메시지 전송
     * Redis에서 상위 N명을 추출한 후 이 메서드를 호출하여 순차 처리
     */
    public void sendToProcessingQueue(BigInteger memberId, BigInteger concertId, String memberName, String concertTitle) {
        String queueName = processingPrefix + concertId;
        
        // 큐가 존재하지 않으면 생성
        ensureQueueExists(concertId.toString());
        
        QueueMessage message = QueueMessage.builder()
                .memberId(memberId)
                .concertId(concertId)
                .memberName(memberName)
                .concertTitle(concertTitle)
                .timestamp(LocalDateTime.now())
                .messageType("ENTER")
                .build();

        // 콘서트별 라우팅 키 사용
        String routingKey = "processing." + concertId;
        rabbitTemplate.convertAndSend("concert.processing.exchange", routingKey, message);
        
        log.info("예매 입장 메시지 전송: queue={}, routingKey={}, memberId={}, memberName={}", 
                queueName, routingKey, memberId, memberName);
    }

    /**
     * 큐가 존재하는지 확인하고 없으면 생성
     */
    private void ensureQueueExists(String concertId) {
        String processingQueueName = processingPrefix + concertId;

        // 처리 큐 확인 및 생성
        if (!queueExists(processingQueueName)) {
            try {
                Queue processingQueue = rabbitMQConfig.createProcessingQueue(concertId);
                Binding processingBinding = rabbitMQConfig.createProcessingBinding(concertId);
                
                amqpAdmin.declareQueue(processingQueue);
                amqpAdmin.declareBinding(processingBinding);
                
                log.info("처리 큐 생성: {}", processingQueueName);
            } catch (Exception e) {
                log.warn("처리 큐 생성 실패 (이미 존재할 수 있음): {}, 에러: {}", processingQueueName, e.getMessage());
            }
        }
    }

    /**
     * 큐 존재 여부 확인
     */
    private boolean queueExists(String queueName) {
        try {
            // 큐 정보를 조회하여 존재 여부 확인
            Object queueInfo = amqpAdmin.getQueueInfo(queueName);
            return queueInfo != null;
        } catch (Exception e) {
            // 큐가 존재하지 않으면 예외 발생
            return false;
        }
    }
} 