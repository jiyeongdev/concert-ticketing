package com.sdemo1.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.sdemo1.entity.Concert;
import com.sdemo1.response.QueueStatusResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 대기열 상태 응답 빌더
 * 상태별 응답 생성을 공통으로 관리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueueStatusResponseBuilder {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Value("${queue.concert.entry-interval-seconds:30}")
    private int entryIntervalSeconds;

    @Value("${queue.concert.group-size:10}")
    private int groupSize;

    /**
     * 예매 종료 상태 응답 생성
     */
    public QueueStatusResponse buildBookingClosedResponse(Long concertId, Concert concert) {
        return QueueStatusResponse.builder()
                .concertId(concertId)
                .concertTitle(concert.getTitle())
                .status("BOOKING_CLOSED")
                .canEnter(false)
                .build();
    }

    /**
     * 대기열에 없음 상태 응답 생성
     */
    public QueueStatusResponse buildNotInQueueResponse(Long concertId, Concert concert) {
        return QueueStatusResponse.builder()
                .concertId(concertId)
                .concertTitle(concert.getTitle())
                .status("NOT_IN_QUEUE")
                .canEnter(false)
                .build();
    }

    /**
     * 입장 준비 완료 상태 응답 생성
     */
    public QueueStatusResponse buildReadyResponse(Long concertId, Concert concert) {
        return QueueStatusResponse.builder()
                .concertId(concertId)
                .concertTitle(concert.getTitle())
                .status("READY")
                .canEnter(true)
                .build();
    }

    /**
     * 입장 완료 상태 응답 생성
     */
    public QueueStatusResponse buildEnteredResponse(Long concertId, Concert concert) {
        return QueueStatusResponse.builder()
                .concertId(concertId)
                .concertTitle(concert.getTitle())
                .status("ENTERED")
                .canEnter(true)
                .build();
    }

    /**
     * 대기 중 상태 응답 생성
     */
    public QueueStatusResponse buildWaitingResponse(
            Long concertId, 
            Concert concert, 
            int queueNumber, 
            int totalWaitingCount, 
            int estimatedWaitTime,
            String estimatedEnterTime) {
        
        return QueueStatusResponse.builder()
                .concertId(concertId)
                .concertTitle(concert.getTitle())
                .queueNumber(queueNumber)
                .totalWaitingCount(totalWaitingCount)
                .estimatedWaitTime(estimatedWaitTime)
                .estimatedEnterTime(estimatedEnterTime)
                .status("WAITING")
                .canEnter(false)
                .build();
    }

    /**
     * 관리자용 대기열 응답 생성
     */
    public QueueStatusResponse buildAdminResponse(
            Long concertId,
            Concert concert,
            Long memberId,
            int queueNumber,
            int totalWaitingCount,
            int estimatedWaitTime,
            String estimatedEnterTime,
            String userStatus) {
        
        return QueueStatusResponse.builder()
                .concertId(concertId)
                .concertTitle(concert.getTitle())
                .queueNumber(queueNumber)
                .totalWaitingCount(totalWaitingCount)
                .estimatedWaitTime(estimatedWaitTime)
                .estimatedEnterTime(estimatedEnterTime)
                .status(userStatus != null ? userStatus : "WAITING")
                .canEnter("READY".equals(userStatus))
                .build();
    }

    /**
     * 예상 입장 시간 계산
     */
    public String calculateEstimatedEnterTime(Concert concert, Long queueNumber) {
        if (queueNumber == null || queueNumber <= 0) {
            return "즉시 입장 가능";
        }

        LocalDateTime openTime = concert.getOpenTime();
        if (openTime == null) {
            return "예상 시간 계산 불가";
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // 현재 시간이 openTime보다 이전인 경우
        if (now.isBefore(openTime)) {
            // openTime + (그룹 - 1) * entryIntervalSeconds
            int groupNumber = (queueNumber.intValue() - 1) / groupSize + 1; // 1~10: 그룹1, 11~20: 그룹2, ...
            int estimatedSeconds = (groupNumber - 1) * entryIntervalSeconds;
            LocalDateTime estimatedTime = openTime.plusSeconds(estimatedSeconds);
            return estimatedTime.format(TIME_FORMATTER);
        } else {
            // openTime이 지났으면 현재 그룹 기준으로 계산
            long secondsSinceOpen = java.time.Duration.between(openTime, now).getSeconds();
            int currentGroup = (int) (secondsSinceOpen / entryIntervalSeconds) + 1;
            int userGroup = (queueNumber.intValue() - 1) / groupSize + 1;
            
            if (userGroup <= currentGroup) {
                return "즉시 입장 가능";
            } else {
                int remainingGroups = userGroup - currentGroup;
                int estimatedSeconds = remainingGroups * entryIntervalSeconds;
                LocalDateTime estimatedTime = now.plusSeconds(estimatedSeconds);
                return estimatedTime.format(TIME_FORMATTER);
            }
        }
    }
} 