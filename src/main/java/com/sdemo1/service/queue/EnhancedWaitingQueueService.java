package com.sdemo1.service.queue;

import java.time.LocalDateTime;
import java.util.List;
import com.sdemo1.common.UserStatus;
import com.sdemo1.dto.QueueStatusResponseBuilder;
import com.sdemo1.entity.Concert;
import com.sdemo1.entity.Member;
import com.sdemo1.repository.ConcertRepository;
import com.sdemo1.repository.MemberRepository;
import com.sdemo1.request.JoinQueueRequest;
import com.sdemo1.response.QueueStatusResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EnhancedWaitingQueueService {

    private final RedisQueueService redisQueueService;
    private final RabbitMQService rabbitMQService;
    private final ConcertRepository concertRepository;
    private final MemberRepository memberRepository;
    private final QueueStatusResponseBuilder responseBuilder;

    // 대기열 설정
    @Value("${queue.concert.join-start-hours:4}")
    private int joinStartHours;

    @Value("${queue.concert.entry-interval-seconds:30}")
    private int entryIntervalSeconds;

    @Value("${queue.concert.group-size:10}")
    private int groupSize;

    /**
     * 대기열 입장 요청
     */
    public QueueStatusResponse enterWaitingRoom(Long memberId, JoinQueueRequest request) {
        log.info("대기열 입장 요청: memberId={}, concertId={}", memberId, request.getConcertId());

        // 콘서트 정보 조회
        Concert concert = concertRepository.findById(request.getConcertId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘서트입니다."));

        // 예매 종료 시간 체크
        if (isBookingClosed(concert)) {
            return responseBuilder.buildBookingClosedResponse(request.getConcertId(), concert);
        }

        // 대기열 입장 가능 여부 확인
        if (!isJoinAllowed(concert)) {
            throw new IllegalStateException("아직 대기열 입장 시간이 되지 않았습니다.");
        }

        // 이미 대기열에 있는지 확인
        if (redisQueueService.isUserInWaitingRoom(memberId, request.getConcertId())) {
            throw new IllegalStateException("이미 대기열에 있습니다.");
        }

        // Redis에 대기열 등록
        boolean success = redisQueueService.joinWaitingRoom(memberId, request.getConcertId());
        if (!success) {
            throw new IllegalStateException("대기열 입장에 실패했습니다.");
        }

        // 대기 순번 조회
        Long queueNumber = redisQueueService.getUserQueueNumber(memberId, request.getConcertId());
        Long totalWaitingCount = redisQueueService.getWaitingCount(request.getConcertId());

        // 예상 대기 시간 계산
        int estimatedWaitTime = redisQueueService.calculateEstimatedWaitTime(queueNumber, entryIntervalSeconds);

        log.info("대기열 입장 성공: memberId={}, concertId={}, queueNumber={}", 
                memberId, request.getConcertId(), queueNumber);

        return responseBuilder.buildWaitingResponse(
                request.getConcertId(),
                concert,
                queueNumber.intValue(),
                totalWaitingCount.intValue(),
                estimatedWaitTime,
                responseBuilder.calculateEstimatedEnterTime(concert, queueNumber)
        );
    }

    /**
     * 대기열 상태 조회
     */
    @Transactional(readOnly = true)
    public QueueStatusResponse getWaitingRoomStatus(Long memberId, Long concertId) {
        log.info("대기열 상태 조회: memberId={}, concertId={}", memberId, concertId);

        // 콘서트 정보 조회
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘서트입니다."));

        // 예매 종료 시간 체크
        if (isBookingClosed(concert)) {
            return responseBuilder.buildBookingClosedResponse(concertId, concert);
        }

        // 사용자 상태 확인 (우선순위: READY > ENTERED > WAITING > NOT_IN_QUEUE)
        String userStatusStr = redisQueueService.getUserStatus(memberId, concertId);
        UserStatus userStatus = UserStatus.fromString(userStatusStr);
        
        if (userStatus.isReady()) {
            return responseBuilder.buildReadyResponse(concertId, concert);
        }
        
        if (userStatus.isEntered()) {
            return responseBuilder.buildEnteredResponse(concertId, concert);
        }
        
        if (userStatus.isWaiting()) {
            // 대기 순번 조회
            Long queueNumber = redisQueueService.getUserQueueNumber(memberId, concertId);
            Long totalWaitingCount = redisQueueService.getWaitingCount(concertId);
            
            if (queueNumber == null) {
                // 상태는 WAITING이지만 ZSet에서 순번을 찾을 수 없는 경우
                log.warn("상태 불일치: memberId={}, concertId={}, status=WAITING but queueNumber=null", 
                        memberId, concertId);
                return responseBuilder.buildNotInQueueResponse(concertId, concert);
            }

            // 예상 대기 시간 계산
            int estimatedWaitTime = redisQueueService.calculateEstimatedWaitTime(queueNumber, entryIntervalSeconds);

            return responseBuilder.buildWaitingResponse(
                    concertId,
                    concert,
                    queueNumber.intValue(),
                    totalWaitingCount.intValue(),
                    estimatedWaitTime,
                    responseBuilder.calculateEstimatedEnterTime(concert, queueNumber)
            );
        }

        // 사용자가 대기열에 없는 경우
        return responseBuilder.buildNotInQueueResponse(concertId, concert);
    }

    /**
     * 대기열에서 나가기
     */
    public void leaveWaitingRoom(Long memberId, Long concertId) {
        log.info("대기열 나가기: memberId={}, concertId={}", memberId, concertId);

        // 콘서트 정보 조회
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘서트입니다."));

        // 예매 종료 시간 체크
        if (isBookingClosed(concert)) {
            log.warn("콘서트 {} 예매가 종료되었습니다.", concert.getId());
            throw new IllegalStateException("해당 콘서트의 예매 시간이 종료되었습니다.");
        }

        // Redis에서 대기열 제거
        redisQueueService.leaveWaitingRoom(memberId, concertId);
        log.info("대기열 나가기 완료: memberId={}, concertId={}", memberId, concertId);
    }

    /**
     * 예매 시작 처리 (관리자/자동 트리거)
     */
    public void startBooking(Long concertId, int batchSize) {
        log.info("예매 시작 처리: concertId={}, batchSize={}", concertId, batchSize);

        // 콘서트 정보 조회
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘서트입니다."));

        // 예매 시작 시간 확인
        if (!isReservationOpen(concert)) {
            throw new IllegalStateException("아직 예매 시작 시간이 되지 않았습니다.");
        }

        // Redis에서 상위 N명 추출
        List<String> topUsers = redisQueueService.getTopWaitingUsers(concertId, batchSize);
        
        if (topUsers.isEmpty()) {
            log.info("대기열에 사용자가 없습니다: concertId={}", concertId);
            return;
        }

        // Redis에서 해당 사용자들 제거
        redisQueueService.removeTopWaitingUsers(concertId, batchSize);

        // RabbitMQ에 메시지 발송 (순차 처리)
        for (String memberIdStr : topUsers) {
            Long memberId = Long.valueOf(memberIdStr);
            rabbitMQService.sendToProcessingQueue(memberId, concertId, concert.getTitle());
            log.info("예매 입장 메시지 발송: memberId={}, concertId={}", memberId, concertId);
        }

        log.info("예매 시작 처리 완료: concertId={}, processedUsers={}", concertId, topUsers.size());
    }

    /**
     * 예매 토큰 발급 (사용자 입장 자격 확인)
     */
    public QueueStatusResponse getBookingToken(Long memberId, Long concertId) {
        log.info("예매 토큰 요청: memberId={}, concertId={}", memberId, concertId);

        // 콘서트 정보 조회
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘서트입니다."));

        // 사용자 상태 확인
        String userStatusStr = redisQueueService.getUserStatus(memberId, concertId);
        UserStatus userStatus = UserStatus.fromString(userStatusStr);
        
        if (!userStatus.isReady()) {
            throw new IllegalStateException("예매 입장 자격이 없습니다. 상태: " + userStatus.getDescription());
        }

        // 사용자 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 상태를 ENTERED로 변경 (입장 완료)
        redisQueueService.setUserEntered(memberId, concertId);

        log.info("예매 토큰 발급 완료: memberId={}, concertId={}", memberId, concertId);

        return responseBuilder.buildEnteredResponse(concertId, concert);
    }

    // ========== Private Helper Methods ==========

    /**
     * 대기열 입장 가능 여부 확인
     */
    private boolean isJoinAllowed(Concert concert) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime openTime = concert.getOpenTime();
        
        if (openTime == null) {
            log.warn("콘서트 {}의 openTime이 설정되지 않았습니다.", concert.getId());
            return false;
        }
        
        // 예매 시작 전 joinStartHours시간 전부터 대기열 입장 가능
        LocalDateTime joinStartTime = openTime.minusHours(joinStartHours);
        
        log.info("콘서트 {} 대기열 입장 시작 시간: {} (현재: {})", 
                concert.getId(), joinStartTime, now);
        
        return now.isAfter(joinStartTime) || now.isEqual(joinStartTime);
    }

    /**
     * 예매 시작 여부 확인
     */
    private boolean isReservationOpen(Concert concert) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime openTime = concert.getOpenTime();
        
        if (openTime == null) {
            log.warn("콘서트 {}의 openTime이 설정되지 않았습니다.", concert.getId());
            return false;
        }
        
        return now.isAfter(openTime) || now.isEqual(openTime);
    }

    /**
     * 예매 종료 여부 확인
     */
    private boolean isBookingClosed(Concert concert) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime closeTime = concert.getCloseTime();
        
        if (closeTime == null) {
            log.warn("콘서트 {}의 closeTime이 설정되지 않았습니다.", concert.getId());
            return false;
        }
        
        return now.isAfter(closeTime) || now.isEqual(closeTime);
    }
} 