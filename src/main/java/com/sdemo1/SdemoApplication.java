package com.sdemo1;

import com.sdemo1.service.seat.hold.RedisSeatHoldService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import lombok.RequiredArgsConstructor;

@SpringBootApplication
@EntityScan(basePackages = "com.sdemo1.entity")
@EnableJpaRepositories(basePackages = "com.sdemo1.repository")
@RequiredArgsConstructor
@EnableScheduling
public class SdemoApplication {

    private final RedisSeatHoldService redisSeatHoldService;

    public static void main(String[] args) {
        SpringApplication.run(SdemoApplication.class, args);
    }

    /**
     * 애플리케이션 시작 완료 후 Redis Key Events 리스너 초기화
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeRedisKeyEvents() {
        redisSeatHoldService.initializeKeyExpirationListener();
    }
}
