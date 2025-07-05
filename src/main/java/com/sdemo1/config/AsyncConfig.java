package com.sdemo1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import lombok.extern.slf4j.Slf4j;
import com.sdemo1.util.ThreadPoolMonitor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    private final ThreadPoolMonitor threadPoolMonitor;
    private ThreadPoolConfig recipeConfig;
    private ThreadPoolConfig notificationConfig;

    public AsyncConfig(ThreadPoolMonitor threadPoolMonitor) {
        this.threadPoolMonitor = threadPoolMonitor;
    }

    // 레시피 추천 스레드 풀 설정
    @Value("${thread-pool.recipe-recommendation.core-pool-size:0}")
    private int recipeCorePoolSize;
    
    @Value("${thread-pool.recipe-recommendation.max-pool-size:0}")
    private int recipeMaxPoolSize;
    
    @Value("${thread-pool.recipe-recommendation.queue-capacity:0}")
    private int recipeQueueCapacity;
    
    @Value("${thread-pool.recipe-recommendation.keep-alive-seconds:60}")
    private int recipeKeepAliveSeconds;

    // 알림 스레드 풀 설정
    @Value("${thread-pool.notification.core-pool-size:0}")
    private int notificationCorePoolSize;
    
    @Value("${thread-pool.notification.max-pool-size:0}")
    private int notificationMaxPoolSize;
    
    @Value("${thread-pool.notification.queue-capacity:0}")
    private int notificationQueueCapacity;
    
    @Value("${thread-pool.notification.keep-alive-seconds:30}")
    private int notificationKeepAliveSeconds;

    /**
     * 시스템 리소스 기반 동적 스레드 풀 설정 (한 번만 계산)
     */
    private void initializeThreadPoolConfigs() {
        if (recipeConfig != null && notificationConfig != null) {
            return; // 이미 초기화됨
        }

        // 시스템 정보 조회 (한 번만)
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        long maxMemoryMB = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        
        log.info("=== 스레드 풀 동적 설정 초기화 ===");
        log.info("사용 가능한 CPU 코어: {}", availableProcessors);
        log.info("최대 메모리: {} MB", maxMemoryMB);
        
        // 레시피 추천 설정 계산
        recipeConfig = new ThreadPoolConfig();
        recipeConfig.corePoolSize = calculateCorePoolSize(recipeCorePoolSize, availableProcessors, "레시피 추천");
        recipeConfig.maxPoolSize = calculateMaxPoolSize(recipeMaxPoolSize, availableProcessors, "레시피 추천");
        recipeConfig.queueCapacity = calculateQueueCapacity(recipeQueueCapacity, availableProcessors, "레시피 추천");
        recipeConfig.keepAliveSeconds = recipeKeepAliveSeconds;
        recipeConfig.threadNamePrefix = "RecipeRecommend-";
        
        // 알림 설정 계산
        notificationConfig = new ThreadPoolConfig();
        notificationConfig.corePoolSize = calculateCorePoolSize(notificationCorePoolSize, availableProcessors, "알림");
        notificationConfig.maxPoolSize = calculateMaxPoolSize(notificationMaxPoolSize, availableProcessors, "알림");
        notificationConfig.queueCapacity = calculateQueueCapacity(notificationQueueCapacity, availableProcessors, "알림");
        notificationConfig.keepAliveSeconds = notificationKeepAliveSeconds;
        notificationConfig.threadNamePrefix = "Notification-";
        
        log.info("레시피 추천 최종 설정 - Core: {}, Max: {}, Queue: {}, KeepAlive: {}초", 
                recipeConfig.corePoolSize, recipeConfig.maxPoolSize, 
                recipeConfig.queueCapacity, recipeConfig.keepAliveSeconds);
        log.info("알림 최종 설정 - Core: {}, Max: {}, Queue: {}, KeepAlive: {}초", 
                notificationConfig.corePoolSize, notificationConfig.maxPoolSize, 
                notificationConfig.queueCapacity, notificationConfig.keepAliveSeconds);
    }

    /**
     * Core Pool Size 동적 계산
     */
    private int calculateCorePoolSize(int configuredValue, int availableProcessors, String poolType) {
        int calculatedValue;
        
        if (configuredValue <= 0) {
            // 설정값이 0 이하면 시스템 기반 계산
            if (poolType.contains("Recipe")) {
                calculatedValue = Math.max(2, availableProcessors / 2);
            } else {
                calculatedValue = Math.max(1, availableProcessors / 4);
            }
        } else {
            // 설정값이 있으면 시스템 리소스를 고려하여 조정
            calculatedValue = Math.min(configuredValue, availableProcessors);
        }
        
        log.info("{} Core Pool Size 계산 - 설정값: {}, 계산값: {}", poolType, configuredValue, calculatedValue);
        return calculatedValue;
    }

    /**
     * Max Pool Size 동적 계산
     */
    private int calculateMaxPoolSize(int configuredValue, int availableProcessors, String poolType) {
        int calculatedValue;
        
        if (configuredValue <= 0) {
            // 설정값이 0 이하면 시스템 기반 계산
            if (poolType.contains("Recipe")) {
                calculatedValue = Math.min(availableProcessors, 8);
            } else {
                calculatedValue = Math.min(availableProcessors / 2, 4);
            }
        } else {
            // 설정값이 있으면 시스템 리소스를 고려하여 조정
            calculatedValue = Math.min(configuredValue, availableProcessors * 2);
        }
        
        log.info("{} Max Pool Size 계산 - 설정값: {}, 계산값: {}", poolType, configuredValue, calculatedValue);
        return calculatedValue;
    }

    /**
     * Queue Capacity 동적 계산
     */
    private int calculateQueueCapacity(int configuredValue, int availableProcessors, String poolType) {
        int calculatedValue;
        
        if (configuredValue <= 0) {
            // 설정값이 0 이하면 시스템 기반 계산
            if (poolType.contains("Recipe")) {
                calculatedValue = Math.max(20, availableProcessors * 5);
            } else {
                calculatedValue = Math.max(10, availableProcessors * 2);
            }
        } else {
            // 설정값이 있으면 시스템 리소스를 고려하여 조정
            calculatedValue = Math.max(configuredValue, availableProcessors * 2);
        }
        
        log.info("{} Queue Capacity 계산 - 설정값: {}, 계산값: {}", poolType, configuredValue, calculatedValue);
        return calculatedValue;
    }

    /**
     * 레시피 추천 전용 스레드 풀 (동적 설정)
     */
    // @Bean(name = "recipeRecommendationExecutor")
    public Executor recipeRecommendationExecutor() {
        // 설정 초기화 (한 번만)
        initializeThreadPoolConfigs();
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(recipeConfig.corePoolSize);
        executor.setMaxPoolSize(recipeConfig.maxPoolSize);
        executor.setQueueCapacity(recipeConfig.queueCapacity);
        executor.setThreadNamePrefix(recipeConfig.threadNamePrefix);
        executor.setKeepAliveSeconds(recipeConfig.keepAliveSeconds);
        
        // 애플리케이션 종료 시 진행 중인 작업 완료 대기
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // 거부된 작업 처리 정책 (호출자 스레드에서 실행)
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        
        // 모니터링 등록
        threadPoolMonitor.registerRecipeExecutor(executor);
        
        log.info("레시피 추천 스레드 풀 초기화 완료");
        return executor;
    }

    /**
     * 알림 전송 전용 스레드 풀 (동적 설정)
     */
    // @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        // 설정 초기화 (한 번만)
        initializeThreadPoolConfigs();
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(notificationConfig.corePoolSize);
        executor.setMaxPoolSize(notificationConfig.maxPoolSize);
        executor.setQueueCapacity(notificationConfig.queueCapacity);
        executor.setThreadNamePrefix(notificationConfig.threadNamePrefix);
        executor.setKeepAliveSeconds(notificationConfig.keepAliveSeconds);
        
        // 애플리케이션 종료 시 진행 중인 작업 완료 대기
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(15);
        
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        
        // 모니터링 등록
        threadPoolMonitor.registerNotificationExecutor(executor);
        
        log.info("알림 전송 스레드 풀 초기화 완료");
        return executor;
    }

    /**
     * 스레드 풀 설정을 담는 내부 클래스
     */
    private static class ThreadPoolConfig {
        int corePoolSize;
        int maxPoolSize;
        int queueCapacity;
        int keepAliveSeconds;
        String threadNamePrefix;
    }
} 