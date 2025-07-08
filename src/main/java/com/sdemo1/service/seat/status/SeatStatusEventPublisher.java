package com.sdemo1.service.seat.status;

import java.math.BigInteger;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdemo1.service.seat.websocket.WebSocketMessageService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatStatusEventPublisher {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final WebSocketMessageService webSocketMessageService;
    private final ObjectMapper objectMapper;
    
    /**
     * 좌석 상태 변경 이벤트 발행 (Redis Pub/Sub + WebSocket)
     */
    public void publishSeatStatusChange(SeatStatusEvent event) {
        try {
            String channel = "seat:status:" + event.getConcertId();
            String destination = "";
            if(event.getStatus().equals("HELD")){ //점유 브로드캐스트 이벤트
                destination = "/topic/" + event.getConcertId().toString() + "/hold";
            }else if(event.getStatus().equals("AVAILABLE")){ //해제 브로드캐스트 이벤트
                destination = "/topic/" + event.getConcertId().toString() + "/release";
            }else if(event.getStatus().equals("BOOKED")){ //예매 확정 브로드캐스트 이벤트 
                destination = "/topic/" + event.getConcertId().toString() + "/booked";
            }
            // 구조화된 메시지 생성
            SeatStatusBroadcastMessage broadcastMessage = SeatStatusBroadcastMessage.builder()
                .channel(channel)
                .eventType(getEventType(event.getStatus()))
                .timestamp(System.currentTimeMillis())
                .data(event)
                .build();

            log.info("broadcastMessage: {}", broadcastMessage);
            log.info("destination: {}", destination);
            
            // 1. Redis Pub/Sub으로 이벤트 발행
            try {
                String message = objectMapper.writeValueAsString(broadcastMessage);
                redisTemplate.convertAndSend(channel, message);
                log.info("Redis 이벤트 발행 성공: channel={}, eventType={}", channel, getEventType(event.getStatus()));
            } catch (Exception e) {
                log.error("Redis 이벤트 발행 실패: channel={}", channel, e);
            }
            
            // 2. WebSocket으로 실시간 브로드캐스트
            if (!destination.isEmpty()) {
                webSocketMessageService.broadcastMessage(destination, broadcastMessage);
            }
                
        } catch (Exception e) {
            log.error("좌석 상태 이벤트 발행 실패: concertId={}, seatId={}", 
                event.getConcertId(), event.getSeatId(), e);
        }
    }
    
    /**
     * 상태에 따른 이벤트 타입 반환
     */
    private String getEventType(String status) {
        switch (status) {
            case "HELD":
                return "SEAT_HOLD";
            case "AVAILABLE":
                return "SEAT_RELEASE";
            case "BOOKED":
                return "SEAT_BOOKED";
            default:
                return "SEAT_STATUS_CHANGE";
        }
    }
    
    /**
     * 좌석 점유 이벤트 발행
     */
    public void publishSeatHold(BigInteger concertId, BigInteger seatId, BigInteger memberId, Long remainingTime) {
        SeatStatusEvent event = SeatStatusEvent.builder()
            .concertId(concertId)
            .seatId(seatId)
            .status("HELD")
            .memberId(memberId)
            .remainingHoldTime(remainingTime)
            .timestamp(LocalDateTime.now())
            .build();
            
        publishSeatStatusChange(event);
    }
    
    /**
     * 좌석 해제 이벤트 발행
     */
    public void publishSeatRelease(BigInteger concertId, BigInteger seatId) {
        SeatStatusEvent event = SeatStatusEvent.builder()
            .concertId(concertId)
            .seatId(seatId)
            .status("AVAILABLE")
            .memberId(null)
            .remainingHoldTime(null)
            .timestamp(LocalDateTime.now())
            .build();
            
        publishSeatStatusChange(event);
    }
    
    /**
     * 좌석 예매 확정 이벤트 발행
     */
    public void publishSeatBooked(BigInteger concertId, BigInteger seatId, BigInteger memberId) {
        SeatStatusEvent event = SeatStatusEvent.builder()
            .concertId(concertId)
            .seatId(seatId)
            .status("BOOKED")
            .memberId(memberId)
            .remainingHoldTime(null)
            .timestamp(LocalDateTime.now())
            .build();
            
        publishSeatStatusChange(event);
    }
    
    /**
     * 전체 좌석 상태 이벤트 발행
     */
    public void publishAllSeatsStatus(BigInteger concertId, Object seatsData) {
        String destination = "/topic/" + concertId.toString() + "/status";
        webSocketMessageService.broadcastMessage(destination, seatsData);
        log.info("전체 좌석 상태 이벤트 발행: concertId={}", concertId);
    }
    

    

    
    /**
     * 좌석 상태 이벤트 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class SeatStatusEvent {
        private BigInteger concertId;
        private BigInteger seatId;
        private String status; // AVAILABLE, HELD, BOOKED
        private BigInteger memberId;
        private Long remainingHoldTime; // 남은 점유 시간 (초)
        private LocalDateTime timestamp;
    }
    
    /**
     * 좌석 상태 브로드캐스트 메시지 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class SeatStatusBroadcastMessage {
        private String channel;           // Redis 채널명
        private String eventType;         // 이벤트 타입 (SEAT_HOLD, SEAT_RELEASE, SEAT_BOOKED)
        private Long timestamp;           // 브로드캐스트 시간
        private SeatStatusEvent data;     // 실제 좌석 상태 데이터
    }
} 