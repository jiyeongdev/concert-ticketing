package com.sdemo1.service;

import com.sdemo1.dto.QueueMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueMessageListener {

    /**
     * 대기열 큐 메시지 처리 (콘서트별 큐)
     */
    @RabbitListener(queues = "#{@queueNameResolver.resolveDefaultWaitingQueueName()}")
    public void handleWaitingQueueMessage(QueueMessage message) {
        log.info("대기열 메시지 수신: memberId={}, concertId={}, type={}", 
                message.getMemberId(), message.getConcertId(), message.getMessageType());
        
        switch (message.getMessageType()) {
            case "JOIN":
                handleJoinMessage(message);
                break;
            case "LEAVE":
                handleLeaveMessage(message);
                break;
            case "EXPIRE":
                handleExpireMessage(message);
                break;
            default:
                log.warn("알 수 없는 메시지 타입: {}", message.getMessageType());
        }
    }

    /**
     * 처리 큐 메시지 처리 (콘서트별 큐)
     */
    @RabbitListener(queues = "#{@queueNameResolver.resolveDefaultProcessingQueueName()}")
    public void handleProcessingQueueMessage(QueueMessage message) {
        log.info("처리 큐 메시지 수신: memberId={}, concertId={}, type={}", 
                message.getMemberId(), message.getConcertId(), message.getMessageType());
        
        if ("ENTER".equals(message.getMessageType())) {
            handleEnterMessage(message);
        } else {
            log.warn("알 수 없는 처리 메시지 타입: {}", message.getMessageType());
        }
    }

    /**
     * 데드레터 큐 메시지 처리
     */
    @RabbitListener(queues = "concert.dead.letter")
    public void handleDeadLetterMessage(QueueMessage message) {
        log.info("데드레터 메시지 수신: memberId={}, concertId={}, type={}", 
                message.getMemberId(), message.getConcertId(), message.getMessageType());
        
        // 만료된 메시지 처리 로직
        handleExpireMessage(message);
    }

    /**
     * 대기열 참가 메시지 처리
     */
    private void handleJoinMessage(QueueMessage message) {
        log.info("대기열 참가 처리: memberId={}, concertId={}, queueNumber={}", 
                message.getMemberId(), message.getConcertId(), message.getQueueNumber());
        
        // 추가 처리 로직 (알림 발송, 로그 기록 등)
        // TODO: 필요한 비즈니스 로직 구현
    }

    /**
     * 대기열 나가기 메시지 처리
     */
    private void handleLeaveMessage(QueueMessage message) {
        log.info("대기열 나가기 처리: memberId={}, concertId={}", 
                message.getMemberId(), message.getConcertId());
        
        // 추가 처리 로직 (알림 발송, 로그 기록 등)
        // TODO: 필요한 비즈니스 로직 구현
    }

    /**
     * 입장 처리 메시지 처리
     */
    private void handleEnterMessage(QueueMessage message) {
        log.info("입장 처리: memberId={}, concertId={}", 
                message.getMemberId(), message.getConcertId());
        
        // 실제 예매 페이지로 이동하는 로직
        // TODO: 예매 시스템 연동 로직 구현
    }

    /**
     * 만료 메시지 처리
     */
    private void handleExpireMessage(QueueMessage message) {
        log.info("메시지 만료 처리: memberId={}, concertId={}", 
                message.getMemberId(), message.getConcertId());
        
        // 만료된 대기열 정리 로직
        // TODO: 필요한 정리 작업 구현
    }
} 