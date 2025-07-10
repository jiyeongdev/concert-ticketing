package com.sdemo1.controller.seat;

import com.sdemo1.dto.seat.SeatHoldResult;
import com.sdemo1.dto.seat.SeatHoldWebSocketDto.SeatHoldRequest;
import com.sdemo1.dto.seat.SeatHoldWebSocketDto.SeatReleaseRequest;
import com.sdemo1.service.seat.hold.SeatHoldService;
import com.sdemo1.service.seat.websocket.WebSocketMessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 좌석 점유/해제 WebSocket 컨트롤러
 * 실시간 좌석 점유 및 해제 기능을 WebSocket을 통해 제공
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class SeatHoldController {

    private final SeatHoldService seatHoldService;
    private final WebSocketMessageService webSocketMessageService;

    // 응답 타입 상수
    private static final String HOLD_SUCCESS = "HOLD_SUCCESS";
    private static final String HOLD_FAILED = "HOLD_FAILED";
    private static final String HOLD_ERROR = "HOLD_ERROR";
    private static final String RELEASE_SUCCESS = "RELEASE_SUCCESS";
    private static final String RELEASE_FAILED = "RELEASE_FAILED";
    private static final String RELEASE_ERROR = "RELEASE_ERROR";

    /**
     * 좌석 점유 요청 처리 (WebSocket)
     */
    @MessageMapping("/{concertId}/hold")
    public void holdSeat(@DestinationVariable("concertId") Long concertId, 
                        SeatHoldRequest request, 
                        SimpMessageHeaderAccessor headerAccessor) {
        processSeatOperation(
            concertId, 
            request.getSeatId(), 
            request.getMemberId(), 
            headerAccessor.getSessionId(),
            "좌석 점유",
            (c, s, m, sessionId) -> seatHoldService.holdSeat(c, s, m, sessionId),
            "HOLD"
        );
    }

    /**
     * 좌석 해제 요청 처리 (WebSocket)
     */
    @MessageMapping("/{concertId}/release")
    public void releaseSeat(@DestinationVariable("concertId") Long concertId, 
                           SeatReleaseRequest request, 
                           SimpMessageHeaderAccessor headerAccessor) {  
        processSeatOperation(
            concertId, 
            request.getSeatId(), 
            request.getMemberId(), 
            headerAccessor.getSessionId(),
            "좌석 해제",
            (c, s, m, sessionId) -> seatHoldService.releaseSeat(c, s, m, sessionId),
            "RELEASE"
        );
    }

    /**
     * 좌석 작업 공통 처리 메서드
     */
    private void processSeatOperation(Long concertId, Long seatId, Long memberId,
                                    String sessionId, String operationName,
                                    SeatOperation operation, String operationType) {
        
        logSeatOperation(operationName, concertId, seatId, memberId);

        try {
            // 비즈니스 로직 실행
            SeatHoldResult result = operation.execute(concertId, seatId, memberId, sessionId);
            
            if (result.isSuccess()) {
                log.info("좌석 작업 성공: concertId={}, seatId={}, message={}", 
                    concertId, seatId, result.getMessage());
                
                // 성공 시 브로드캐스트
                handleSuccess(concertId, seatId, result, operationType);
            } else {
                log.warn("좌석 작업 실패: concertId={}, seatId={}, message={}", 
                    concertId, seatId, result.getMessage());
                
                // 실패 시 1:1 에러 메시지
                handleFailure(sessionId, concertId, result);
            }
            
        } catch (Exception e) {
            log.error("{} 실패 (WebSocket): concertId={}, seatId={}", 
                operationName, concertId, seatId, e);
            
            // 예외 발생 시 1:1 에러 메시지
            SeatHoldResult errorResult = SeatHoldResult.builder()
                .success(false)
                .message(operationName + " 중 오류 발생: " + e.getMessage())
                .build();
            
            handleFailure(sessionId, concertId, errorResult);
        }
    }

    /**
     * 성공 처리
     */
    private void handleSuccess(Long concertId, Long seatId, SeatHoldResult result, String operationType) {
        try {
            String broadcastTopic = determineBroadcastTopic(concertId, operationType);
            webSocketMessageService.broadcastMessage(broadcastTopic, result);
            log.info("좌석 작업 성공 브로드캐스트 완료: topic={}", broadcastTopic);
        } catch (Exception e) {
            log.error("성공 브로드캐스트 실패: concertId={}, seatId={}", concertId, seatId, e);
        }
    }

    /**
     * 실패 처리
     */
    private void handleFailure(String sessionId, Long concertId, SeatHoldResult result) {
        try {
            if (sessionId != null) {
                webSocketMessageService.sendSeatHoldError(sessionId, concertId, result);
                log.info("좌석 작업 실패 에러 메시지 전송 완료: sessionId={}", sessionId);
            }
        } catch (Exception e) {
            log.error("실패 에러 메시지 전송 실패: sessionId={}, concertId={}", sessionId, concertId, e);
        }
    }

    /**
     * 브로드캐스트 토픽 결정
     */
    private String determineBroadcastTopic(Long concertId, String operationType) {
        switch (operationType) {
            case "HOLD":
                return "/topic/" + concertId + "/hold";
            case "RELEASE":
                return "/topic/" + concertId + "/release";
            default:
                return "/topic/" + concertId + "/status";
        }
    }

    /**
     * 좌석 작업 인터페이스
     */
    @FunctionalInterface
    private interface SeatOperation {
        SeatHoldResult execute(Long concertId, Long seatId, Long memberId, String sessionId);
    }

    /**
     * 좌석 작업 로깅
     */
    private void logSeatOperation(String operationName, Long concertId, Long seatId, 
                                 Long memberId) {
        log.info("{} 요청 (WebSocket): concertId={}, seatId={}, memberId={}", 
            operationName, concertId, seatId, memberId);
    }
} 