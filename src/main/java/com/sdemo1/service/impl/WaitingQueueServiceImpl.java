package com.sdemo1.service.impl;

import com.sdemo1.dto.QueueMessage;
import com.sdemo1.entity.Concert;
import com.sdemo1.entity.Member;
import com.sdemo1.repository.ConcertRepository;
import com.sdemo1.repository.MemberRepository;
import com.sdemo1.request.JoinQueueRequest;
import com.sdemo1.response.QueueStatusResponse;
import com.sdemo1.service.RabbitMQService;
import com.sdemo1.service.WaitingQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WaitingQueueServiceImpl implements WaitingQueueService {

    private final RabbitMQService rabbitMQService;
    private final ConcertRepository concertRepository;
    private final MemberRepository memberRepository;

    // 대기열 설정
    private static final int ENTRY_INTERVAL_SECONDS = 30; // 30초마다 한 명씩 입장
    private static final int MAX_WAIT_TIME_MINUTES = 120; // 최대 대기 시간 2시간
    
    // 예매 시작 전 대기열 참가 가능 시간 (시간) - 환경변수로 설정 가능
    @Value("${queue.concert.join-start-hours:4}")
    private int joinStartHours;

    @Override
    public QueueStatusResponse joinQueue(BigInteger memberId, JoinQueueRequest request) {
        log.info("대기열 참가 요청: memberId={}, concertId={}", memberId, request.getConcertId());

        // 콘서트 존재 확인
        Concert concert = concertRepository.findById(request.getConcertId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘서트입니다."));

        // 사용자 존재 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 예매 종료 시간 체크
        if (isBookingClosed(concert)) {
            log.warn("콘서트 {} 예매가 종료되었습니다. closeTime: {}", concert.getId(), concert.getCloseTime());
            throw new IllegalStateException("해당 콘서트의 예매 시간이 종료되었습니다.");
        }

        // 대기열 참가 가능 여부 확인 (예매 시작 전 joinStartHours시간 전부터만 가능)
        if (!isJoinAllowed(concert)) {
            throw new IllegalStateException("아직 대기열 참가가 불가능합니다. 예매 시작 전 " + joinStartHours + "시간 전부터 참가 가능합니다.");
        }

        // 이미 대기열에 있는지 확인
        if (rabbitMQService.isUserInQueue(memberId, request.getConcertId())) {
            throw new IllegalStateException("이미 대기열에 참가되어 있습니다.");
        }

        // 현재 대기열 크기 조회하여 다음 순번 계산
        Long currentQueueSize = rabbitMQService.getWaitingQueueSize(request.getConcertId());
        Integer nextQueueNumber = currentQueueSize.intValue() + 1;

        // 예상 대기 시간 계산 (분 단위)
        int estimatedWaitMinutes = calculateEstimatedWaitTime(nextQueueNumber);

        // RabbitMQ 대기열에 메시지 전송
        rabbitMQService.sendToWaitingQueue(
                memberId, 
                request.getConcertId(), 
                member.getName(), 
                concert.getTitle(), 
                nextQueueNumber, 
                estimatedWaitMinutes
        );

        log.info("대기열 참가 완료: queueNumber={}, estimatedWaitTime={}분", nextQueueNumber, estimatedWaitMinutes);

        // 응답 생성
        return QueueStatusResponse.builder()
                .concertId(request.getConcertId())
                .concertTitle(concert.getTitle())
                .queueNumber(nextQueueNumber)
                .totalWaitingCount(nextQueueNumber.intValue())
                .estimatedWaitTime(estimatedWaitMinutes)
                .estimatedEnterTime(calculateEstimatedEnterTime(currentQueueSize))
                .status("WAITING")
                .canEnter(false)
                .build();
    }

    /** 입장 가능 여부를 시간 기반으로 판단 */
    @Override
    @Transactional(readOnly = true)
    public QueueStatusResponse getQueueStatus(BigInteger memberId, BigInteger concertId) {
        log.info("대기열 상태 조회: memberId={}, concertId={}", memberId, concertId);

        // 콘서트 정보 조회
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘서트입니다."));

        // 예매 종료 시간 체크
        if (isBookingClosed(concert)) {
            log.warn("콘서트 {} 예매가 종료되었습니다. closeTime: {}", concert.getId(), concert.getCloseTime());
            return QueueStatusResponse.builder()
                    .concertId(concertId)
                    .concertTitle(concert.getTitle())
                    .status("BOOKING_CLOSED")
                    .canEnter(false)
                    .build();
        }

        // 사용자가 대기열에 있는지 확인
        if (!rabbitMQService.isUserInQueue(memberId, concertId)) {
            return QueueStatusResponse.builder()
                    .concertId(concertId)
                    .status("NOT_IN_QUEUE")
                    .canEnter(false)
                    .build();
        }

        // 사용자의 대기열 정보 조회
        QueueMessage userQueueMessage = rabbitMQService.getUserQueueMessage(memberId, concertId);
        if (userQueueMessage == null) {
            return QueueStatusResponse.builder()
                    .concertId(concertId)
                    .status("NOT_IN_QUEUE")
                    .canEnter(false)
                    .build();
        }

        // 전체 대기자 수
        Long totalWaitingCount = rabbitMQService.getWaitingQueueSize(concertId);
        
        // 사용자의 순번
        Integer userQueueNumber = userQueueMessage.getQueueNumber();
        
        // 예상 입장 시간 계산
        String estimatedEnterTime = calculateEstimatedEnterTime(totalWaitingCount);
        
        // 입장 가능 여부 확인 (예매 시작 시간부터 순차적으로 입장 가능)
        boolean canEnter = isEntryAllowed(concert, userQueueNumber);
        
        // 상태 결정
        String status = canEnter ? "CAN_ENTER" : "WAITING";

        return QueueStatusResponse.builder()
                .concertId(concertId)
                .concertTitle(concert.getTitle())
                .queueNumber(userQueueNumber)
                .totalWaitingCount(totalWaitingCount.intValue())
                .estimatedWaitTime(userQueueMessage.getEstimatedWaitTime())
                .estimatedEnterTime(estimatedEnterTime)
                .status(status)
                .canEnter(canEnter)
                .build();
    }

    /**
     * 입장 시도 시 시간 검증
     */
    @Override
    public boolean enterFromQueue(BigInteger memberId, BigInteger concertId) {
        log.info("대기열 입장 처리: memberId={}, concertId={}", memberId, concertId);

        // 사용자가 대기열에 있는지 확인
        if (!rabbitMQService.isUserInQueue(memberId, concertId)) {
            throw new IllegalStateException("대기열에 참가되어 있지 않습니다.");
        }

        // 사용자의 대기열 정보 조회
        QueueMessage userQueueMessage = rabbitMQService.getUserQueueMessage(memberId, concertId);
        if (userQueueMessage == null) {
            throw new IllegalStateException("대기열 정보를 찾을 수 없습니다.");
        }

        // 콘서트 정보 조회
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘서트입니다."));

        // 입장 가능 여부 확인 (예매 시작 시간부터 순차적으로 입장 가능)
        if (!isEntryAllowed(concert, userQueueMessage.getQueueNumber())) {
            throw new IllegalStateException("아직 입장할 수 없습니다. 예매 시작 시간부터 입장 가능합니다.");
        }

        // 사용자 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 대기열에서 제거
        rabbitMQService.removeFromWaitingQueue(memberId, concertId);
        
        // 처리 큐에 메시지 전송
        rabbitMQService.sendToProcessingQueue(memberId, concertId, member.getName(), concert.getTitle());
        
        log.info("대기열 입장 완료: memberId={}, concertId={}", memberId, concertId);
        return true;
    }

    @Override
    public void leaveQueue(BigInteger memberId, BigInteger concertId) {
        log.info("대기열 나가기: memberId={}, concertId={}", memberId, concertId);

        // 콘서트 정보 조회
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘서트입니다."));

        // 예매 종료 시간 체크
        if (isBookingClosed(concert)) {
            log.warn("콘서트 {} 예매가 종료되었습니다. closeTime: {}", concert.getId(), concert.getCloseTime());
            throw new IllegalStateException("해당 콘서트의 예매 시간이 종료되었습니다.");
        }

        if (rabbitMQService.isUserInQueue(memberId, concertId)) {
            rabbitMQService.removeFromWaitingQueue(memberId, concertId);
            log.info("대기열 나가기 완료: memberId={}, concertId={}", memberId, concertId);
        } else {
            log.warn("대기열에 참가되어 있지 않음: memberId={}, concertId={}", memberId, concertId);
        }
    }

    /** 관리자용 대기열 조회에서도 시간 기반 판단 */
    @Override
    @Transactional(readOnly = true)
    public List<QueueStatusResponse> getQueueByConcertId(BigInteger concertId) {
        log.info("콘서트 대기열 조회: concertId={}", concertId);

        // 콘서트 정보 조회
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 콘서트입니다."));

        // 예매 종료 시간 체크
        if (isBookingClosed(concert)) {
            log.warn("콘서트 {} 예매가 종료되었습니다. closeTime: {}", concert.getId(), concert.getCloseTime());
            // 예매 종료 시 빈 리스트 반환
            return List.of();
        }

        // RabbitMQ에서 큐 메시지들을 조회
        List<QueueMessage> queueMessages = rabbitMQService.getQueueMessages(concertId);
        
        // QueueMessage를 QueueStatusResponse로 변환
        return queueMessages.stream()
                .map(message -> {
                    boolean canEnter = isEntryAllowed(concert, message.getQueueNumber());
                    return QueueStatusResponse.builder()
                            .concertId(concertId)
                            .concertTitle(concert.getTitle())
                            .queueNumber(message.getQueueNumber())
                            .totalWaitingCount(queueMessages.size())
                            .estimatedWaitTime(message.getEstimatedWaitTime())
                            .estimatedEnterTime(calculateEstimatedEnterTime((long) message.getQueueNumber() - 1))
                            .status(canEnter ? "CAN_ENTER" : "WAITING")
                            .canEnter(canEnter)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public void cleanupExpiredQueues() {
        log.info("만료된 대기열 정리 시작");
        
        // RabbitMQ의 TTL 기능으로 자동 만료 처리
        // 추가 정리 작업이 필요한 경우 여기서 처리
        log.info("만료된 대기열 정리 완료");
    }

    /**
     * 예상 대기 시간 계산 (분 단위)
     */
    private int calculateEstimatedWaitTime(int queueNumber) {
        // 30초마다 한 명씩 입장하므로, 대기 시간 = (순번 - 1) * 0.5분
        int estimatedMinutes = (queueNumber - 1) * 30 / 60;
        
        // 최대 2시간으로 제한
        return Math.min(estimatedMinutes, MAX_WAIT_TIME_MINUTES);
    }

    /**
     * 예상 입장 시간 계산
     */
    private String calculateEstimatedEnterTime(Long waitingAheadCount) {
        if (waitingAheadCount == 0) {
            return "즉시 입장 가능";
        }
        
        // 30초마다 한 명씩 입장
        int estimatedSeconds = waitingAheadCount.intValue() * ENTRY_INTERVAL_SECONDS;
        int minutes = estimatedSeconds / 60;
        int seconds = estimatedSeconds % 60;
        
        LocalDateTime estimatedTime = LocalDateTime.now().plusSeconds(estimatedSeconds);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        
        return estimatedTime.format(formatter);
    }

    /**
     * 대기열 참가 가능 여부 확인
     * 예매 시작 전 몇 시간 전부터 대기열 참가 가능
     */
    private boolean isJoinAllowed(Concert concert) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime openTime = concert.getOpenTime(); // JPA Converter가 자동으로 한국 시간으로 변환
        
        if (openTime == null) {
            log.warn("콘서트 {}의 openTime이 설정되지 않았습니다.", concert.getId());
            return false;
        }
        
        // 예매 시작 전 joinStartHours시간 전부터 대기열 참가 가능
        LocalDateTime joinStartTime = openTime.minusHours(joinStartHours);
        
        log.info("콘서트 {} 대기열 참가 시작 시간: {} (현재: {})", 
                concert.getId(), joinStartTime, now);
        
        return now.isAfter(joinStartTime) || now.isEqual(joinStartTime);
    }

    /**
     * 입장 가능 여부 확인
     * 예매 시작 시간부터 순차적으로 입장 가능
     */
    private boolean isEntryAllowed(Concert concert, Integer queueNumber) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime openTime = concert.getOpenTime(); // JPA Converter가 자동으로 한국 시간으로 변환
        
        if (openTime == null) {
            log.warn("콘서트 {}의 openTime이 설정되지 않았습니다.", concert.getId());
            return false;
        }
        
        log.info("콘서트 {} 예매 시작 시간: {} (현재: {}, 순번: {})", 
                concert.getId(), openTime, now, queueNumber);
        
        return now.isAfter(openTime) || now.isEqual(openTime);
    }

    private boolean isBookingClosed(Concert concert) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime closeTime = concert.getCloseTime(); // JPA Converter가 자동으로 한국 시간으로 변환
        
        if (closeTime == null) {
            log.warn("콘서트 {}의 closeTime이 설정되지 않았습니다.", concert.getId());
            return false;
        }
        
        return now.isAfter(closeTime) || now.isEqual(closeTime);
    }
} 