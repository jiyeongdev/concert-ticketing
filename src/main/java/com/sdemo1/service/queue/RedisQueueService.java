package com.sdemo1.service.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisQueueService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String WAITING_ROOM_KEY = "waiting_room";
    private static final String USER_STATUS_PREFIX = "user:";
    private static final String CONCERT_PREFIX = "concert:";

    // TTL 설정 (분 단위)
    @Value("${queue.redis.ttl.waiting:5}")
    private int waitingTtlMinutes;

    @Value("${queue.redis.ttl.ready:5}")
    private int readyTtlMinutes;

    @Value("${queue.redis.ttl.entered:1}")
    private int enteredTtlMinutes;

    @Value("${queue.concert.max-estimated-wait-time:120}")
    private int maxEstimatedWaitTimeMinutes;

    /**
     * 대기열에 사용자 등록
     */
    public boolean joinWaitingRoom(BigInteger memberId, BigInteger concertId) {
        String concertKey = CONCERT_PREFIX + concertId;
        String userStatusKey = USER_STATUS_PREFIX + memberId + ":" + concertId;
        
        try {
            // 이미 대기열에 있는지 확인
            if (isUserInWaitingRoom(memberId, concertId)) {
                log.warn("사용자가 이미 대기열에 있습니다: memberId={}, concertId={}", memberId, concertId);
                return false;
            }

            // 대기열에 추가 (timestamp를 score로 사용)
            double score = System.currentTimeMillis();
            Boolean added = redisTemplate.opsForZSet().add(concertKey, memberId.toString(), score);
            
            if (Boolean.TRUE.equals(added)) {
                // ZSet에도 TTL 설정 (상태 키와 동일한 TTL)
                redisTemplate.expire(concertKey, waitingTtlMinutes, TimeUnit.MINUTES);
                
                // 사용자 상태를 WAITING으로 설정 (설정된 TTL)
                redisTemplate.opsForValue().set(userStatusKey, "WAITING", waitingTtlMinutes, TimeUnit.MINUTES);
                
                log.info("대기열 등록 성공: memberId={}, concertId={}, score={}, ttl={}분", 
                        memberId, concertId, score, waitingTtlMinutes);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            log.error("대기열 등록 실패: memberId={}, concertId={}", memberId, concertId, e);
            return false;
        }
    }

    /**
     * 사용자가 대기열에 있는지 확인
     */
    public boolean isUserInWaitingRoom(BigInteger memberId, BigInteger concertId) {
        String concertKey = CONCERT_PREFIX + concertId;
        String userStatusKey = USER_STATUS_PREFIX + memberId + ":" + concertId;
        
        try {
            // 상태 키 확인 (우선순위)
            Object status = redisTemplate.opsForValue().get(userStatusKey);
            if (status != null) {
                String statusStr = status.toString();
                // WAITING, READY, ENTERED 상태 모두 대기열에 있는 것으로 간주
                return "WAITING".equals(statusStr) || "READY".equals(statusStr) || "ENTERED".equals(statusStr);
            }
            
            // 상태 키가 없는 경우 ZSet에서 확인 (레거시 데이터 처리)
            Double score = redisTemplate.opsForZSet().score(concertKey, memberId.toString());
            return score != null;
        } catch (Exception e) {
            log.error("대기열 상태 확인 실패: memberId={}, concertId={}", memberId, concertId, e);
            return false;
        }
    }

    /**
     * 사용자의 대기 순번 조회
     */
    public Long getUserQueueNumber(BigInteger memberId, BigInteger concertId) {
        String concertKey = CONCERT_PREFIX + concertId;
        
        try {
            Long rank = redisTemplate.opsForZSet().rank(concertKey, memberId.toString());
            return rank != null ? rank + 1 : null; // 0-based를 1-based로 변환
        } catch (Exception e) {
            log.error("대기 순번 조회 실패: memberId={}, concertId={}", memberId, concertId, e);
            return null;
        }
    }

    /**
     * 전체 대기자 수 조회
     */
    public Long getWaitingCount(BigInteger concertId) {
        String concertKey = CONCERT_PREFIX + concertId;
        
        try {
            return redisTemplate.opsForZSet().zCard(concertKey);
        } catch (Exception e) {
            log.error("대기자 수 조회 실패: concertId={}", concertId, e);
            return 0L;
        }
    }

    /**
     * 대기열에서 사용자 제거
     */
    public boolean leaveWaitingRoom(BigInteger memberId, BigInteger concertId) {
        String concertKey = CONCERT_PREFIX + concertId;
        String userStatusKey = USER_STATUS_PREFIX + memberId + ":" + concertId;
        
        try {
            // ZSet에서 제거
            Long removed = redisTemplate.opsForZSet().remove(concertKey, memberId.toString());
            
            // 상태 키 삭제
            redisTemplate.delete(userStatusKey);
            
            log.info("대기열에서 제거 완료: memberId={}, concertId={}, removed={}", memberId, concertId, removed);
            return removed != null && removed > 0;
        } catch (Exception e) {
            log.error("대기열 제거 실패: memberId={}, concertId={}", memberId, concertId, e);
            return false;
        }
    }

    /**
     * 예매 오픈 시 상위 N명 추출
     */
    public List<String> getTopWaitingUsers(BigInteger concertId, int count) {
        String concertKey = CONCERT_PREFIX + concertId;
        
        try {
            Set<Object> topUsers = redisTemplate.opsForZSet().range(concertKey, 0, count - 1);
            if (topUsers != null) {
                return topUsers.stream()
                        .map(Object::toString)
                        .toList();
            }
            return List.of();
        } catch (Exception e) {
            log.error("상위 대기자 조회 실패: concertId={}, count={}", concertId, count, e);
            return List.of();
        }
    }

    /**
     * 예매 오픈 시 상위 N명 제거
     */
    public boolean removeTopWaitingUsers(BigInteger concertId, int count) {
        String concertKey = CONCERT_PREFIX + concertId;
        
        try {
            Set<Object> topUsers = redisTemplate.opsForZSet().range(concertKey, 0, count - 1);
            if (topUsers != null && !topUsers.isEmpty()) {
                Long removed = redisTemplate.opsForZSet().remove(concertKey, topUsers.toArray());
                log.info("상위 대기자 제거 완료: concertId={}, count={}, removed={}", concertId, count, removed);
                return removed != null && removed > 0;
            }
            return false;
        } catch (Exception e) {
            log.error("상위 대기자 제거 실패: concertId={}, count={}", concertId, count, e);
            return false;
        }
    }

    /**
     * 사용자 상태를 READY로 변경 (예매 입장 허용)
     */
    public boolean setUserReady(BigInteger memberId, BigInteger concertId) {
        String userStatusKey = USER_STATUS_PREFIX + memberId + ":" + concertId;
        
        try {
            // 설정된 TTL로 READY 상태 설정
            redisTemplate.opsForValue().set(userStatusKey, "READY", readyTtlMinutes, TimeUnit.MINUTES);
            log.info("사용자 상태를 READY로 변경: memberId={}, concertId={}, ttl={}분", 
                    memberId, concertId, readyTtlMinutes);
            return true;
        } catch (Exception e) {
            log.error("사용자 상태 변경 실패: memberId={}, concertId={}", memberId, concertId, e);
            return false;
        }
    }

    /**
     * 사용자 상태 확인
     */
    public String getUserStatus(BigInteger memberId, BigInteger concertId) {
        String userStatusKey = USER_STATUS_PREFIX + memberId + ":" + concertId;
        
        try {
            Object status = redisTemplate.opsForValue().get(userStatusKey);
            return status != null ? status.toString() : null;
        } catch (Exception e) {
            log.error("사용자 상태 조회 실패: memberId={}, concertId={}", memberId, concertId, e);
            return null;
        }
    }

    /**
     * 사용자 상태를 ENTERED로 변경 (입장 완료)
     */
    public boolean setUserEntered(BigInteger memberId, BigInteger concertId) {
        String userStatusKey = USER_STATUS_PREFIX + memberId + ":" + concertId;
        
        try {
            // 설정된 TTL로 ENTERED 상태 설정
            redisTemplate.opsForValue().set(userStatusKey, "ENTERED", enteredTtlMinutes, TimeUnit.MINUTES);
            log.info("사용자 상태를 ENTERED로 변경: memberId={}, concertId={}, ttl={}분", 
                    memberId, concertId, enteredTtlMinutes);
            return true;
        } catch (Exception e) {
            log.error("사용자 상태 변경 실패: memberId={}, concertId={}", memberId, concertId, e);
            return false;
        }
    }

    /**
     * 예상 대기 시간 계산 (분 단위)
     */
    public int calculateEstimatedWaitTime(Long queueNumber, int entryIntervalSeconds) {
        if (queueNumber == null || queueNumber <= 0) {
            log.debug("대기 시간 계산: queueNumber={}, result=0 (null or <= 0)", queueNumber);
            return 0;
        }
        
        // 30초마다 한 명씩 입장하므로, 대기 시간 = (순번 - 1) * 30초 / 60초
        // 정수 나눗셈 문제 해결을 위해 double 사용 후 반올림
        double estimatedMinutesDouble = (queueNumber.intValue() - 1) * entryIntervalSeconds / 60.0;
        int estimatedMinutes = (int) Math.round(estimatedMinutesDouble);
        
        // 최대 2시간으로 제한
        int result = Math.min(estimatedMinutes, maxEstimatedWaitTimeMinutes);
        
        log.debug("대기 시간 계산: queueNumber={}, entryIntervalSeconds={}, estimatedMinutesDouble={}, estimatedMinutes={}, result={}", 
                queueNumber, entryIntervalSeconds, estimatedMinutesDouble, estimatedMinutes, result);
        
        return result;
    }

    /**
     * 만료된 대기열 정리
     */
    public void cleanupExpiredQueues() {
        try {
            // Redis의 TTL 기능으로 자동 만료 처리
            // 추가 정리 작업이 필요한 경우 여기서 처리
            log.info("만료된 대기열 정리 완료");
        } catch (Exception e) {
            log.error("만료된 대기열 정리 실패", e);
        }
    }


} 