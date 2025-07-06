package com.sdemo1.service;

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
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQService {

    private final RabbitTemplate rabbitTemplate;
    private final AmqpAdmin amqpAdmin;
    private final RabbitMQConfig rabbitMQConfig;

    @Value("${queue.concert.waiting-prefix}")
    private String waitingPrefix;

    @Value("${queue.concert.processing-prefix}")
    private String processingPrefix;

    // 메모리 기반 큐 상태 관리 (실제 운영에서는 Redis 등 사용 권장)
    private final ConcurrentHashMap<String, ConcurrentHashMap<BigInteger, QueueMessage>> queueState = new ConcurrentHashMap<>();

    /**
     * 대기열에 메시지 전송 (GUI에서 확인 가능하도록)
     */
    public void sendToWaitingQueue(BigInteger memberId, BigInteger concertId, String memberName, String concertTitle, Integer queueNumber, Integer estimatedWaitTime) {
        String queueName = waitingPrefix + concertId; // 콘서트별 큐
        
        // 큐가 존재하지 않으면 생성
        ensureQueueExists(concertId.toString());
        
        QueueMessage message = QueueMessage.builder()
                .memberId(memberId)
                .concertId(concertId)
                .memberName(memberName)
                .concertTitle(concertTitle)
                .queueNumber(queueNumber)
                .timestamp(LocalDateTime.now())
                .messageType("JOIN")
                .estimatedWaitTime(estimatedWaitTime)
                .build();

        // RabbitMQ에 실제 메시지 전송 (콘서트별 라우팅 키 사용)
        String routingKey = "waiting." + concertId;
        rabbitTemplate.convertAndSend("concert.waiting.exchange", routingKey, message);
        
        // 메모리 상태 업데이트
        queueState.computeIfAbsent(queueName, k -> new ConcurrentHashMap<>()).put(memberId, message);
        
        log.info("대기열 메시지 전송: queue={}, routingKey={}, memberId={}, queueNumber={}, memberName={}", 
                queueName, routingKey, memberId, queueNumber, memberName);
    }

    /**
     * 처리 큐에 메시지 전송 (입장 처리)
     */
    public void sendToProcessingQueue(BigInteger memberId, BigInteger concertId, String memberName, String concertTitle) {
        String queueName = processingPrefix + concertId; // 콘서트별 큐
        
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
        log.info("처리 큐 메시지 전송: queue={}, routingKey={}, memberId={}", queueName, routingKey, memberId);
    }

    /**
     * 대기열에서 메시지 제거
     */
    public void removeFromWaitingQueue(BigInteger memberId, BigInteger concertId) {
        String queueName = waitingPrefix + concertId; // 콘서트별 큐
        
        QueueMessage message = QueueMessage.builder()
                .memberId(memberId)
                .concertId(concertId)
                .timestamp(LocalDateTime.now())
                .messageType("LEAVE")
                .build();

        // 콘서트별 라우팅 키 사용
        String routingKey = "waiting.remove." + concertId;
        rabbitTemplate.convertAndSend("concert.waiting.exchange", routingKey, message);
        
        // 메모리 상태에서 제거
        ConcurrentHashMap<BigInteger, QueueMessage> concertQueue = queueState.get(queueName);
        if (concertQueue != null) {
            concertQueue.remove(memberId);
        }
        
        log.info("대기열에서 제거: queue={}, routingKey={}, memberId={}", queueName, routingKey, memberId);
    }

    /**
     * 만료된 메시지 처리
     */
    public void sendToDeadLetter(BigInteger memberId, BigInteger concertId, String reason) {
        QueueMessage message = QueueMessage.builder()
                .memberId(memberId)
                .concertId(concertId)
                .timestamp(LocalDateTime.now())
                .messageType("EXPIRE")
                .build();

        rabbitTemplate.convertAndSend("concert.dead.letter.exchange", "dead.letter", message);
        log.info("데드레터 전송: memberId={}, reason={}", memberId, reason);
    }

    /**
     * 특정 콘서트의 대기열 메시지 수 조회
     */
    public Long getWaitingQueueSize(BigInteger concertId) {
        String queueName = waitingPrefix + concertId; // 콘서트별 큐
        ConcurrentHashMap<BigInteger, QueueMessage> concertQueue = queueState.get(queueName);
        return concertQueue != null ? (long) concertQueue.size() : 0L;
    }

    /**
     * 특정 사용자가 대기열에 있는지 확인
     */
    public boolean isUserInQueue(BigInteger memberId, BigInteger concertId) {
        String queueName = waitingPrefix + concertId; // 콘서트별 큐
        ConcurrentHashMap<BigInteger, QueueMessage> concertQueue = queueState.get(queueName);
        return concertQueue != null && concertQueue.containsKey(memberId);
    }

    /**
     * 특정 콘서트의 대기열 전체 조회
     */
    public java.util.List<QueueMessage> getQueueMessages(BigInteger concertId) {
        String queueName = waitingPrefix + concertId; // 콘서트별 큐
        ConcurrentHashMap<BigInteger, QueueMessage> concertQueue = queueState.get(queueName);
        if (concertQueue != null) {
            return java.util.List.copyOf(concertQueue.values());
        }
        return java.util.List.of();
    }

    /**
     * 특정 사용자의 대기열 정보 조회
     */
    public QueueMessage getUserQueueMessage(BigInteger memberId, BigInteger concertId) {
        String queueName = waitingPrefix + concertId; // 콘서트별 큐
        ConcurrentHashMap<BigInteger, QueueMessage> concertQueue = queueState.get(queueName);
        return concertQueue != null ? concertQueue.get(memberId) : null;
    }

    /**
     * 큐가 존재하는지 확인하고 없으면 생성
     */
    private void ensureQueueExists(String concertId) {
        String waitingQueueName = waitingPrefix + concertId;
        String processingQueueName = processingPrefix + concertId;

        // 대기열 큐 확인 및 생성
        if (!queueExists(waitingQueueName)) {
            try {
                Queue waitingQueue = rabbitMQConfig.createWaitingQueue(concertId);
                Binding waitingBinding = rabbitMQConfig.createWaitingBinding(concertId);
                
                amqpAdmin.declareQueue(waitingQueue);
                amqpAdmin.declareBinding(waitingBinding);
                
                log.info("대기열 큐 생성: {}", waitingQueueName);
            } catch (Exception e) {
                log.warn("대기열 큐 생성 실패 (이미 존재할 수 있음): {}, 에러: {}", waitingQueueName, e.getMessage());
            }
        }

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