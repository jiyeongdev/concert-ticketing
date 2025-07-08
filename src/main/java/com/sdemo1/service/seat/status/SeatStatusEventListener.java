package com.sdemo1.service.seat.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdemo1.service.seat.status.SeatStatusEventPublisher.SeatStatusEvent;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis Pub/Sub 좌석 상태 이벤트 리스너
 * 
 * 역할:
 * 1. 다른 서버의 좌석 상태 변경 감지
 * 2. 백그라운드 작업 처리 (통계, 로그, 알림)
 * 3. 데이터 일관성 보장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeatStatusEventListener implements MessageListener {
    
    private final ObjectMapper objectMapper;
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String payload = new String(message.getBody());
            
            log.info("Redis 이벤트 수신: channel={}, payload={}", channel, payload);
            
            // 좌석 상태 이벤트 채널인지 확인
            if (channel.startsWith("seat:status:")) {
                handleSeatStatusEvent(payload);
            }
            
        } catch (Exception e) {
            log.error("Redis 이벤트 처리 실패", e);
        }
    }
    
    /**
     * 좌석 상태 이벤트 처리
     */
    private void handleSeatStatusEvent(String payload) {
        try {
            SeatStatusEvent event = objectMapper.readValue(payload, SeatStatusEvent.class);
            
            log.info("좌석 상태 이벤트 처리: concertId={}, seatId={}, status={}", 
                event.getConcertId(), event.getSeatId(), event.getStatus());
            
            // 백그라운드 작업 처리
            processBackgroundTasks(event);
            
            // 통계 업데이트
            updateStatistics(event);
            
        } catch (Exception e) {
            log.error("좌석 상태 이벤트 처리 실패: payload={}", payload, e);
        }
    }
    
    /**
     * 백그라운드 작업 처리
     */
    private void processBackgroundTasks(SeatStatusEvent event) {
        try {
            switch (event.getStatus()) {
                case "HELD":
                    // 좌석 점유 시 작업
                    processSeatHoldTasks(event);
                    break;
                case "AVAILABLE":
                    // 좌석 해제 시 작업
                    processSeatReleaseTasks(event);
                    break;
                case "BOOKED":
                    // 좌석 예매 확정 시 작업
                    processSeatBookedTasks(event);
                    break;
            }
        } catch (Exception e) {
            log.error("백그라운드 작업 처리 실패", e);
        }
    }
    
    /**
     * 좌석 점유 시 백그라운드 작업
     */
    private void processSeatHoldTasks(SeatStatusEvent event) {
        // 1. 좌석 점유 시간 타이머 시작
        // 2. 관리자 알림 발송
        // 3. 로그 기록
        log.info("좌석 점유 백그라운드 작업: seatId={}, memberId={}", 
            event.getSeatId(), event.getMemberId());
    }
    
    /**
     * 좌석 해제 시 백그라운드 작업
     */
    private void processSeatReleaseTasks(SeatStatusEvent event) {
        // 1. 타이머 정리
        // 2. 대기자 알림 발송
        // 3. 로그 기록
        log.info("좌석 해제 백그라운드 작업: seatId={}", event.getSeatId());
    }
    
    /**
     * 좌석 예매 확정 시 백그라운드 작업
     */
    private void processSeatBookedTasks(SeatStatusEvent event) {
        // 1. 결제 완료 알림
        // 2. 티켓 발송
        // 3. 통계 업데이트
        log.info("좌석 예매 확정 백그라운드 작업: seatId={}, memberId={}", 
            event.getSeatId(), event.getMemberId());
    }
    
    /**
     * 통계 업데이트
     */
    private void updateStatistics(SeatStatusEvent event) {
        try {
            // 좌석 상태별 통계 업데이트
            // 예: 점유율, 예매율, 해제율 등
            log.debug("통계 업데이트: concertId={}, status={}", 
                event.getConcertId(), event.getStatus());
        } catch (Exception e) {
            log.error("통계 업데이트 실패", e);
        }
    }
} 