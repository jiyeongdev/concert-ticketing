package com.sdemo1.controller;

import com.sdemo1.common.utils.ThreadPoolMonitor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 스레드 풀 상태 모니터링 API
 * 시스템 리소스 사용량을 실시간으로 확인
 */
@Slf4j
@RestController
@RequestMapping("/thread-pool")
@RequiredArgsConstructor
public class ThreadPoolController {

    private final ThreadPoolMonitor threadPoolMonitor;

    /**
     * 스레드 풀 상태 조회
     */
    @GetMapping("/status")
    public ThreadPoolMonitor.ThreadPoolStatus getThreadPoolStatus() {
        log.info("스레드 풀 상태 조회 요청");
        return threadPoolMonitor.getThreadPoolStatus();
    }

    /**
     * 시스템 리소스 정보 조회
     */
    @GetMapping("/system-info")
    public ThreadPoolMonitor.SystemInfo getSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        ThreadPoolMonitor.SystemInfo systemInfo = new ThreadPoolMonitor.SystemInfo();
        
        systemInfo.totalMemory = runtime.totalMemory();
        systemInfo.freeMemory = runtime.freeMemory();
        systemInfo.maxMemory = runtime.maxMemory();
        systemInfo.availableProcessors = runtime.availableProcessors();
        
        log.info("시스템 정보 조회 - CPU: {}개, 메모리: {}/{} MB", 
                systemInfo.availableProcessors,
                (systemInfo.totalMemory - systemInfo.freeMemory) / (1024 * 1024),
                systemInfo.maxMemory / (1024 * 1024));
        
        return systemInfo;
    }
} 