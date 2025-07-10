package com.sdemo1.service.seat.hold;

import com.sdemo1.common.UserStatus;
import com.sdemo1.common.utils.BookingEligibilityChecker;
import com.sdemo1.dto.seat.SeatHoldResult;
import com.sdemo1.dto.seat.SeatStatusDto;
import com.sdemo1.service.queue.RedisQueueService;
import com.sdemo1.service.seat.status.SeatStatusService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 좌석 점유 비즈니스 로직 조율자 (Orchestrator)
 * 
 * 역할:
 * 1. 예매 자격 확인 (대기열 상태 체크)
 * 2. RedisSeatHoldService를 통한 좌석 점유/해제
 * 3. 사용자 친화적인 결과 제공
 * 
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeatHoldService {

    private final RedisSeatHoldService redisSeatHoldService;
    private final SeatStatusService seatStatusService;
    private final RedisQueueService redisQueueService;
    private final BookingEligibilityChecker eligibilityChecker;

    // 상수 정의
    private static final String HOLD_OPERATION = "좌석 점유";
    private static final String RELEASE_OPERATION = "좌석 점유 해제";
    private static final String ELIGIBILITY_ERROR_MESSAGE = "예매 자격이 없습니다. 대기열에 입장 후 예매 시간을 기다려주세요.";
    private static final String HOLD_SUCCESS_MESSAGE = "좌석이 점유되었습니다.";
    private static final String RELEASE_SUCCESS_MESSAGE = "좌석이 해제되었습니다.";
    private static final String HOLD_FAILURE_MESSAGE = "좌석 점유에 실패했습니다.";
    private static final String RELEASE_FAILURE_MESSAGE = "좌석 점유 해제에 실패했습니다.";
    private static final long HOLD_TTL_SECONDS = 600L;

    /**
     * 좌석 점유 처리 (비즈니스 로직 포함)
     * 
     * 처리 순서:
     * 1. 예매 자격 확인 (대기열 상태 체크)
     * 2. Redis 기반 좌석 점유
     * 3. 상세한 결과 정보 반환
     */
    public SeatHoldResult holdSeat(Long concertId, Long seatId, Long memberId, String sessionId) {
        return processSeatOperation(
            concertId, seatId, memberId, sessionId,
            HOLD_OPERATION,
            redisSeatHoldService::holdSeat,
            HOLD_SUCCESS_MESSAGE,
            HOLD_FAILURE_MESSAGE,
            "HOLD"
        );
    }

    /**
     * 좌석 점유 해제 처리 (비즈니스 로직 포함)
     * 
     * 처리 순서:
     * 1. Redis 기반 좌석 점유 해제
     * 2. 상세한 결과 정보 반환
     */
    public SeatHoldResult releaseSeat(Long concertId, Long seatId, Long memberId, String sessionId) {
        return processSeatOperation(
            concertId, seatId, memberId, sessionId,
            RELEASE_OPERATION,
            redisSeatHoldService::releaseSeat,
            RELEASE_SUCCESS_MESSAGE,
            RELEASE_FAILURE_MESSAGE,
            "RELEASE"
        );
    }

    /**
     * 좌석 작업 공통 처리 메서드
     */
    private SeatHoldResult processSeatOperation(Long concertId, Long seatId, Long memberId,
                                               String sessionId, String operationName, SeatOperation operation,
                                               String successMessage, String failureMessage,
                                               String operationType) {
        try {
            log.info("{} 요청: concertId={}, seatId={}, memberId={}", operationName, concertId, seatId, memberId);
            
            // 예매 자격 확인
            UserStatus userStatus = getUserStatus(memberId, concertId);
            if (!eligibilityChecker.checkEntered(memberId, concertId)) {
                SeatHoldResult failureResult = createFailureResultWithStatus(ELIGIBILITY_ERROR_MESSAGE, userStatus);
                return failureResult;
            }
            
            // Redis 기반 좌석 작업 실행
            boolean success = operation.execute(concertId, seatId, memberId);
            
            if (success) {
                // 작업된 좌석 정보 조회
                SeatStatusDto seatInfo = getSeatInfo(concertId, seatId);
                log.info("{}된 좌석 정보 seatInfo: {}", operationName, seatInfo);
                
                SeatHoldResult successResult = createSuccessResult(successMessage, seatInfo);
                return successResult;
            } else {
                SeatHoldResult failureResult = createFailureResultWithStatus(failureMessage, userStatus);
                return failureResult;
            }
            
        } catch (IllegalStateException e) {
            log.error("{} 실패 - 상태 오류: {}", operationName, e.getMessage());
            SeatHoldResult failureResult = createFailureResult(e.getMessage());
            return failureResult;
        } catch (Exception e) {
            log.error("{} 실패: {}", operationName, e.getMessage(), e);
            SeatHoldResult failureResult = createFailureResult(operationName + " 실패: " + e.getMessage());
            return failureResult;
        }
    }

    /**
     * 사용자 상태 조회
     */
    private UserStatus getUserStatus(Long memberId, Long concertId) {
        try {
            String userStatusStr = redisQueueService.getUserStatus(memberId, concertId);
            if (userStatusStr == null) {
                return null;
            }
            return UserStatus.fromString(userStatusStr);
        } catch (Exception e) {
            log.error("사용자 상태 조회 중 오류 발생: memberId={}, concertId={}", memberId, concertId, e);
            return null;
        }
    }

    /**
     * 성공 결과 생성 헬퍼 메서드
     */
    private SeatHoldResult createSuccessResult(String message, SeatStatusDto seatInfo) {
        return SeatHoldResult.builder()
            .success(true)
            .message(message)
            .seatInfo(seatInfo)
            .build();
    }

    /**
     * 실패 결과 생성 헬퍼 메서드
     */
    private SeatHoldResult createFailureResult(String message) {
        return SeatHoldResult.builder()
            .success(false)
            .message(message)
            .seatInfo(null)
            .build();
    }

    /**
     * 상태 정보가 포함된 실패 결과 생성 헬퍼 메서드
     */
    private SeatHoldResult createFailureResultWithStatus(String message, UserStatus userStatus) {
        return SeatHoldResult.builder()
            .success(false)
            .message(message)
            .seatInfo(null)
            .userStatus(userStatus)
            .build();
    }

    /**
     * 좌석 정보 조회 헬퍼 메서드
     */
    private SeatStatusDto getSeatInfo(Long concertId, Long seatId) {
        return seatStatusService.getSeatStatusFromRedis(concertId).stream()
            .filter(seat -> seat.getId().equals(seatId))
            .findFirst()
            .orElse(null);
    }

    /**
     * 좌석 작업 인터페이스
     */
    @FunctionalInterface
    private interface SeatOperation {
        boolean execute(Long concertId, Long seatId, Long memberId);
    }
} 