package com.sdemo1;

import com.sdemo1.service.seat.hold.RedisSeatHoldService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import lombok.RequiredArgsConstructor;

@SpringBootApplication
@RequiredArgsConstructor
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
