package com.sdemo1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("queueNameResolver")
public class QueueNameResolver {

    @Value("${queue.concert.waiting-prefix}")
    private String waitingPrefix;

    @Value("${queue.concert.processing-prefix}")
    private String processingPrefix;

    /**
     * 대기열 큐 이름 해결
     */
    public String resolveWaitingQueueName(String concertId) {
        return waitingPrefix + concertId;
    }

    /**
     * 처리 큐 이름 해결
     */
    public String resolveProcessingQueueName(String concertId) {
        return processingPrefix + concertId;
    }

    /**
     * 기본 대기열 큐 이름 (테스트용)
     */
    public String resolveDefaultWaitingQueueName() {
        return waitingPrefix + "default";
    }

    /**
     * 기본 처리 큐 이름 (테스트용)
     */
    public String resolveDefaultProcessingQueueName() {
        return processingPrefix + "default";
    }
} 