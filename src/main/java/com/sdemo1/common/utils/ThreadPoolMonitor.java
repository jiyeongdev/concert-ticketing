package com.sdemo1.common.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * 스레드 풀 상태 모니터링 유틸리티
 * 시스템 리소스 사용량을 실시간으로 추적
 */
@Slf4j
@Component
public class ThreadPoolMonitor {

    private ThreadPoolExecutor recipeExecutor;
    private ThreadPoolExecutor notificationExecutor;

    /**
     * 스레드 풀 등록
     */
    public void registerRecipeExecutor(Executor executor) {
        if (executor instanceof ThreadPoolExecutor) {
            this.recipeExecutor = (ThreadPoolExecutor) executor;
            log.info("레시피 추천 스레드 풀 모니터링 등록 완료");
        }
    }

    public void registerNotificationExecutor(Executor executor) {
        if (executor instanceof ThreadPoolExecutor) {
            this.notificationExecutor = (ThreadPoolExecutor) executor;
            log.info("알림 스레드 풀 모니터링 등록 완료");
        }
    }

    /**
     * 스레드 풀 상태 모니터링 (5분마다 실행)
     */
    @Scheduled(fixedRate = 300000) // 5분 = 300초
    public void monitorThreadPools() {
        if (recipeExecutor != null) {
            logThreadPoolStatus("레시피 추천", recipeExecutor);
        }
        
        if (notificationExecutor != null) {
            logThreadPoolStatus("알림 전송", notificationExecutor);
        }
    }

    /**
     * 스레드 풀 상태 로깅
     */
    private void logThreadPoolStatus(String poolName, ThreadPoolExecutor executor) {
        int activeCount = executor.getActiveCount();
        int poolSize = executor.getPoolSize();
        int corePoolSize = executor.getCorePoolSize();
        int maximumPoolSize = executor.getMaximumPoolSize();
        long completedTaskCount = executor.getCompletedTaskCount();
        int queueSize = executor.getQueue().size();
        
        // 시스템 리소스 정보
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;
        
        log.info("=== {} 스레드 풀 상태 ===", poolName);
        log.info("활성 스레드: {}/{} (최대: {})", activeCount, poolSize, maximumPoolSize);
        log.info("완료된 작업: {}", completedTaskCount);
        log.info("대기 중인 작업: {}", queueSize);
        log.info("메모리 사용량: {}/{} MB ({:.1f}%)", 
                usedMemory / (1024 * 1024), 
                maxMemory / (1024 * 1024), 
                memoryUsagePercent);
        
        // 경고 조건 체크
        if (activeCount >= maximumPoolSize * 0.8) {
            log.warn("{} 스레드 풀 사용률이 높습니다: {}/{}", poolName, activeCount, maximumPoolSize);
        }
        
        if (memoryUsagePercent > 80) {
            log.warn("메모리 사용률이 높습니다: {:.1f}%", memoryUsagePercent);
        }
        
        if (queueSize > executor.getQueue().remainingCapacity() * 0.8) {
            log.warn("{} 작업 큐가 거의 가득 찼습니다: {}/{}", poolName, queueSize, executor.getQueue().remainingCapacity());
        }
    }

    /**
     * 현재 스레드 풀 상태 조회 (API용)
     */
    public ThreadPoolStatus getThreadPoolStatus() {
        ThreadPoolStatus status = new ThreadPoolStatus();
        
        if (recipeExecutor != null) {
            status.recipeRecommendation = getExecutorStatus(recipeExecutor);
        }
        
        if (notificationExecutor != null) {
            status.notification = getExecutorStatus(notificationExecutor);
        }
        
        // 시스템 정보 추가
        Runtime runtime = Runtime.getRuntime();
        status.systemInfo = new SystemInfo();
        status.systemInfo.totalMemory = runtime.totalMemory();
        status.systemInfo.freeMemory = runtime.freeMemory();
        status.systemInfo.maxMemory = runtime.maxMemory();
        status.systemInfo.availableProcessors = runtime.availableProcessors();
        
        return status;
    }

    private ExecutorStatus getExecutorStatus(ThreadPoolExecutor executor) {
        ExecutorStatus status = new ExecutorStatus();
        status.activeCount = executor.getActiveCount();
        status.poolSize = executor.getPoolSize();
        status.corePoolSize = executor.getCorePoolSize();
        status.maximumPoolSize = executor.getMaximumPoolSize();
        status.completedTaskCount = executor.getCompletedTaskCount();
        status.queueSize = executor.getQueue().size();
        status.queueCapacity = executor.getQueue().remainingCapacity() + executor.getQueue().size();
        
        return status;
    }

    /**
     * 스레드 풀 상태 정보 클래스
     */
    public static class ThreadPoolStatus {
        public ExecutorStatus recipeRecommendation;
        public ExecutorStatus notification;
        public SystemInfo systemInfo;
    }

    public static class ExecutorStatus {
        public int activeCount;
        public int poolSize;
        public int corePoolSize;
        public int maximumPoolSize;
        public long completedTaskCount;
        public int queueSize;
        public int queueCapacity;
    }

    public static class SystemInfo {
        public long totalMemory;
        public long freeMemory;
        public long maxMemory;
        public int availableProcessors;
    }
} 