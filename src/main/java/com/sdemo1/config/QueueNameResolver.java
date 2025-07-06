package com.sdemo1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("queueNameResolver")
public class QueueNameResolver {

    @Value("${queue.concert.queues.waiting-default}")
    private String waitingDefaultQueueName;

    @Value("${queue.concert.queues.processing-default}")
    private String processingDefaultQueueName;

    /**
     * 대기열 큐 이름 해결 (현재는 기본 큐만 사용)
     */
    public String resolveWaitingQueueName(String concertId) {
        // 현재는 콘서트별 큐 대신 기본 큐 사용
        return waitingDefaultQueueName;
    }

    /**
     * 처리 큐 이름 해결 (현재는 기본 큐만 사용)
     */
    public String resolveProcessingQueueName(String concertId) {
        // 현재는 콘서트별 큐 대신 기본 큐 사용
        return processingDefaultQueueName;
    }

    /**
     * 기본 대기열 큐 이름
     */
    public String resolveDefaultWaitingQueueName() {
        return waitingDefaultQueueName;
    }

    /**
     * 기본 처리 큐 이름
     */
    public String resolveDefaultProcessingQueueName() {
        return processingDefaultQueueName;
    }
} 