package com.sdemo1.service.seat.websocket;

import com.sdemo1.dto.seat.SeatHoldResult;
import com.sdemo1.dto.seat.SeatHoldWebSocketDto.SeatHoldResponse;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocket 메시지 전송 전담 서비스
 * 
 * 역할:
 * 1. 브로드캐스트 메시지 전송
 * 2. 1:1 에러 메시지 전송
 * 3. 메시지 형식 변환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketMessageService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * 브로드캐스트 메시지 전송
     */
    public void broadcastMessage(String destination, Object message) {
        try {
            messagingTemplate.convertAndSend(destination, message);
            log.info("브로드캐스트 메시지 전송 성공: destination={}", destination);
        } catch (Exception e) {
            log.error("브로드캐스트 메시지 전송 실패: destination={}", destination, e);
        }
    }
    
    /**
     * 1:1 에러 메시지 전송
     */
    public void sendUserError(String sessionId, Object errorResponse) {
        try {
            messagingTemplate.convertAndSendToUser(
                sessionId,
                "/queue/errors",
                errorResponse,
                createHeaders(sessionId)
            );
            log.info("1:1 에러 메시지 전송 성공: sessionId={}", sessionId);
        } catch (Exception e) {
            log.error("1:1 에러 메시지 전송 실패: sessionId={}", sessionId, e);
        }
    }
    
    /**
     * 좌석 점유/해제 에러 메시지 전송
     */
    public void sendSeatHoldError(String sessionId, Long concertId, SeatHoldResult result) {
        try {
            // 에러 메시지 생성
            SeatHoldResponse errorResponse = SeatHoldResponse.builder()
                .concertId(concertId)
                .seats(null)
                .type(result.isSuccess() ? "HOLD_SUCCESS" : "HOLD_FAILED")
                .message(result.getMessage())
                .userStatus(result.getUserStatus())
                .build();
            
            // 1:1 에러 메시지 전송
            sendUserError(sessionId, errorResponse);
            
            log.info("좌석 점유/해제 에러 메시지 전송: sessionId={}, concertId={}, message={}", 
                sessionId, concertId, result.getMessage());
                
        } catch (Exception e) {
            log.error("좌석 점유/해제 에러 메시지 전송 실패: sessionId={}, concertId={}", 
                sessionId, concertId, e);
        }
    }
    
    /**
     * convertAndSendToUser 전송시 필요한 헤더 생성
     */
    private MessageHeaders createHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor
            .create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }
} 