package com.sdemo1.service.seat.hold;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.sdemo1.entity.Member;
import com.sdemo1.entity.Seat;
import com.sdemo1.entity.SeatHold;
import com.sdemo1.repository.MemberRepository;
import com.sdemo1.repository.SeatHoldRepository;
import com.sdemo1.repository.SeatRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSeatHoldService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final SeatHoldRepository seatHoldRepository;
    private final SeatRepository seatRepository;
    private final MemberRepository memberRepository;
    private final RedisMessageListenerContainer messageListenerContainer;
    
    private static final int HOLD_DURATION_SECONDS = 600; // 10분
    
      // 상수 정의
    private static final String ALREADY_OCCUPIED_ERROR_MESSAGE = "이미 점유된 좌석입니다.";
    private static final String HOLD_CONFLICT_ERROR_MESSAGE = "같은 콘서트에서 이미 다른 좌석을 점유하고 있습니다.";
    
    // seat:concerthold:{concertId}:{seatId} 패턴
    private static final Pattern SEAT_HOLD_KEY_PATTERN = Pattern.compile("seat:(concerthold|userhold):(\\d+):(\\d+)");
    
    // Redis 키 생성 함수형 인터페이스
    @FunctionalInterface
    private interface RedisKeyGenerator {
        String generate(Long concertId, Long seatId, Long memberId);
    }
    
    // Redis 트랜잭션 실행 함수형 인터페이스
    @FunctionalInterface
    private interface RedisTransactionExecutor {
        List<Object> execute(RedisOperations<String, String> operations);
    }
    
    // 결과 검증 함수형 인터페이스
    @FunctionalInterface
    private interface ResultValidator {
        void validate(List<Object> results, Long concertId, Long seatId, Long memberId);
    }
    
    // Redis 키 생성기들
    private static final RedisKeyGenerator SEAT_KEY_GENERATOR = 
        (concertId, seatId, memberId) -> String.format("seat:concerthold:%d:%d", concertId, seatId);
    
    private static final RedisKeyGenerator USER_KEY_GENERATOR = 
        (concertId, seatId, memberId) -> String.format("seat:userhold:%d:%d", concertId, memberId);
    
    /**
     * Redis Key Events 리스너 초기화
     */
    public void initializeKeyExpirationListener() {
        KeyExpirationEventMessageListener listener = new KeyExpirationEventMessageListener(messageListenerContainer) {
            @Override
            public void onMessage(@NonNull org.springframework.data.redis.connection.Message message, @Nullable byte[] pattern) {
                String expiredKey = new String(message.getBody());
                handleKeyExpiration(expiredKey);
            }
        };
        
        // 모든 키 만료 이벤트 구독
        messageListenerContainer.addMessageListener(listener, 
            org.springframework.data.redis.listener.PatternTopic.of("__keyevent@*__:expired"));
        
        log.info("Redis Key Events 리스너 초기화 완료!!");
    }
    
    /**
     * 키 만료 이벤트 처리
     */
    private void handleKeyExpiration(String expiredKey) {
        try {
            // seat:concerthold 또는 seat:userhold 키인지 확인
            Matcher matcher = SEAT_HOLD_KEY_PATTERN.matcher(expiredKey);
            if (matcher.matches()) {
                String keyType = matcher.group(1); // concerthold 또는 userhold
                Long concertId = Long.valueOf(matcher.group(2));
                Long seatId = Long.valueOf(matcher.group(3));
                
                log.info("좌석 점유 만료 감지: keyType={}, concertId={}, seatId={}", keyType, concertId, seatId);
                
                // concerthold 키가 만료된 경우에만 DB 정리 수행
                if ("concerthold".equals(keyType)) {
                cleanupExpiredSeatHold(concertId, seatId);
                }
            }
        } catch (Exception e) {
            log.error("키 만료 이벤트 처리 중 오류 발생: key={}", expiredKey, e);
        }
    }
    
    /**
     * 만료된 좌석 점유 정보 정리
     */
    private void cleanupExpiredSeatHold(Long concertId, Long seatId) {
        try {
            // DB에서 만료된 점유 정보 삭제
                seatHoldRepository.deleteBySeatId(seatId);
                
            log.info("만료된 좌석 점유 정보 DB 정리 완료: concertId={}, seatId={}", concertId, seatId);
                
            // TODO: 필요시 WebSocket을 통한 실시간 알림 추가
            // webSocketMessageService.broadcastSeatRelease(concertId, seatId);
            
        } catch (Exception e) {
            log.error("만료된 좌석 점유 정보 정리 중 오류 발생: concertId={}, seatId={}", concertId, seatId, e);
        }
    }
    
    /**
     * 공통 Redis 작업 실행 메서드
     */
    private <T> T executeRedisOperation(
            String operationName,
            Long concertId, 
            Long seatId, 
            Long memberId,
            RedisTransactionExecutor transactionExecutor,
            ResultValidator resultValidator,
            Function<List<Object>, T> resultProcessor,
            Consumer<Exception> errorHandler) {
        
        try {
            log.debug("Redis 작업 시작: {} - concertId={}, seatId={}, memberId={}", 
                operationName, concertId, seatId, memberId);
            
            // Redis 트랜잭션 실행
            List<Object> results = redisTemplate.execute(new SessionCallback<List<Object>>() {
                @Override
                public List<Object> execute(@NonNull RedisOperations operations) throws DataAccessException {
                    return transactionExecutor.execute(operations);
                }
            });
            
            // 결과 검증
            resultValidator.validate(results, concertId, seatId, memberId);
            
            // 결과 처리
            T result = resultProcessor.apply(results);
            
            log.info("Redis 작업 성공: {} - concertId={}, seatId={}, memberId={}", 
                operationName, concertId, seatId, memberId);
            
            return result;
            
        } catch (Exception e) {
            log.error("Redis 작업 실패: {} - concertId={}, seatId={}, memberId={}", 
                operationName, concertId, seatId, memberId, e);
            
            // 비즈니스 로직 예외는 롤백하지 않고 바로 전달 - 이미 점유/점유해제된 경우
            if (e instanceof IllegalStateException) {
                throw e;
            }
            
            // 시스템 오류만 롤백 처리
            errorHandler.accept(e);
            throw e;
        }
    }
    
    /**
     * 좌석 점유 (Redis + DB 트랜잭션으로 원자적 처리)
     */
    @Transactional
    public boolean holdSeat(Long concertId, Long seatId, Long memberId) {
        return executeRedisOperation(
            "좌석 점유",
            concertId, seatId, memberId,
            // Redis 트랜잭션 실행
            operations -> {
                operations.multi();
                
                // 1. 좌석이 이미 점유되어 있는지 확인
                operations.opsForValue().get(SEAT_KEY_GENERATOR.generate(concertId, seatId, memberId));
                
                // 2. 사용자가 이미 같은 콘서트의 다른 좌석을 점유하고 있는지 확인
                SetOperations<String, String> setOps = operations.opsForSet();
                setOps.size(USER_KEY_GENERATOR.generate(concertId, seatId, memberId));
                
                return operations.exec();
            },
            // 결과 검증
            (results, cId, sId, mId) -> {
                String existingHolder = (String) results.get(0);
                Long userHoldCount = (Long) results.get(1);
                
                if (existingHolder != null) {
                    log.warn("좌석이 이미 점유됨: concertId={}, seatId={}, existingHolder={}", cId, sId, existingHolder);
                    throw new IllegalStateException(ALREADY_OCCUPIED_ERROR_MESSAGE);
                }
                
                if (userHoldCount != null && userHoldCount > 0) {
                    log.warn("사용자가 이미 같은 콘서트의 다른 좌석을 점유 중: memberId={}, concertId={}, holdCount={}", 
                        mId, cId, userHoldCount);
                    throw new IllegalStateException(HOLD_CONFLICT_ERROR_MESSAGE);
                }
            },
            // 결과 처리
            results -> {
                // 새로운 점유 설정
                setNewSeatHold(concertId, seatId, memberId);
                syncHoldToDatabaseInternal(concertId, seatId, memberId);
                return true;
            },
            // 에러 처리
            e -> {
                try {
                    rollbackRedisHold(concertId, seatId, memberId);
                } catch (Exception rollbackEx) {
                    log.error("Redis 롤백 실패: concertId={}, seatId={}, memberId={}", concertId, seatId, memberId, rollbackEx);
                }
            }
        );
    }
    
    /**
     * 좌석 점유 해제 (Redis + DB 트랜잭션으로 원자적 처리)
     */
    @Transactional
    public boolean releaseSeat(Long concertId, Long seatId, Long memberId) {
        return executeRedisOperation(
            "좌석 해제",
            concertId, seatId, memberId,
            // Redis 트랜잭션 실행
            operations -> {
                operations.multi();
                
                // 1. 현재 점유자 확인
                operations.opsForValue().get(SEAT_KEY_GENERATOR.generate(concertId, seatId, memberId));
                
                return operations.exec();
            },
            // 결과 검증
            (results, cId, sId, mId) -> {
                String currentHolder = (String) results.get(0);
                
                if (currentHolder == null) {
                    log.warn("점유되지 않은 좌석 해제 시도: concertId={}, seatId={}", cId, sId);
                    throw new IllegalStateException("점유되지 않은 좌석입니다.");
                }
                
                if (!currentHolder.equals(mId.toString())) {
                    log.warn("다른 사용자가 점유한 좌석 해제 시도: concertId={}, seatId={}, requester={}, holder={}", 
                        cId, sId, mId, currentHolder);
                    throw new IllegalStateException("본인이 점유한 좌석만 해제할 수 있습니다.");
                }
            },
            // 결과 처리
            results -> {
                // 좌석 해제 실행
                releaseSeatHold(concertId, seatId, memberId);
                syncReleaseToDatabaseInternal(concertId, seatId, memberId);
                return true;
            },
            // 에러 처리
            e -> {} // 해제는 롤백 불필요
        );
    }
    
    /**
     * 사용자의 점유 좌석 조회
     */
    public List<Long> getUserHeldSeats(Long memberId, Long concertId) {
        String userKey = USER_KEY_GENERATOR.generate(concertId, 0L, memberId);
        Set<String> members = redisTemplate.opsForSet().members(userKey);
        if (members == null) {
            return List.of();
        }
        return members.stream()
            .map(Long::valueOf)
            .toList();
    }
    
    /**
     * 좌석 상태 조회
     */
    public String getSeatStatus(Long concertId, Long seatId) {
        String seatKey = SEAT_KEY_GENERATOR.generate(concertId, seatId, 0L);
        String heldBy = redisTemplate.opsForValue().get(seatKey);
        
        if (heldBy != null) {
            return "HELD";
        }
        
        // TODO: DB에서 BOOKED 상태 확인 필요
        return "AVAILABLE";
    }
    
    /**
     * 좌석 점유 정보 조회
     */
    public SeatHoldInfo getSeatHoldInfo(Long concertId, Long seatId) {
        String seatKey = SEAT_KEY_GENERATOR.generate(concertId, seatId, 0L);
        String heldBy = redisTemplate.opsForValue().get(seatKey);
        
        if (heldBy == null) {
            return null;
        }
        
        // TTL 조회
        Long ttl = redisTemplate.getExpire(seatKey, TimeUnit.SECONDS);
        
        return SeatHoldInfo.builder()
            .memberId(Long.valueOf(heldBy))
            .remainingSeconds(ttl != null ? ttl : 0L)
            .build();
    }
    
    /**
     * 좌석 예매 확정 (Redis + DB 트랜잭션으로 원자적 처리)
     */
    @Transactional
    public boolean confirmReservation(Long concertId, Long seatId, Long memberId) {
        return executeRedisOperation(
            "좌석 예매 확정",
            concertId, seatId, memberId,
            // Redis 트랜잭션 실행
            operations -> {
                operations.multi();
                
                // 1. 현재 점유자 확인
                operations.opsForValue().get(SEAT_KEY_GENERATOR.generate(concertId, seatId, memberId));
                
                // 2. 예매 확정 (점유 정보 삭제)
                operations.delete(SEAT_KEY_GENERATOR.generate(concertId, seatId, memberId));
                ((SetOperations<String, String>) operations.opsForSet())
                    .remove(USER_KEY_GENERATOR.generate(concertId, seatId, memberId), seatId.toString());
                
                return operations.exec();
            },
            // 결과 검증
            (results, cId, sId, mId) -> {
                @SuppressWarnings("unchecked")
                String currentHolder = (String) results.get(0);
                
                if (currentHolder == null) {
                    log.warn("점유되지 않은 좌석 예매 확정 시도: concertId={}, seatId={}", cId, sId);
                    throw new IllegalStateException("점유 중인 좌석이 아닙니다.");
                }
                
                if (!currentHolder.equals(mId.toString())) {
                    log.warn("다른 사용자가 점유한 좌석 예매 확정 시도: concertId={}, seatId={}, requester={}, holder={}", 
                        cId, sId, mId, currentHolder);
                    throw new IllegalStateException("점유 중인 좌석이 아닙니다.");
                }
            },
            // 결과 처리
            results -> {
                syncConfirmToDatabaseInternal(concertId, seatId, memberId);
                return true;
            },
            // 에러 처리
            e -> {} // 예매 확정은 롤백 불필요
        );
    }
    
    /**
     * 새로운 좌석 점유 설정 (Redis)
     */
    private void setNewSeatHold(Long concertId, Long seatId, Long memberId) {
        String seatKey = SEAT_KEY_GENERATOR.generate(concertId, seatId, memberId);
        String userKey = USER_KEY_GENERATOR.generate(concertId, seatId, memberId);
        
        try {
            // 좌석 점유 설정 (10분 TTL)
            redisTemplate.opsForValue().set(seatKey, memberId.toString(), HOLD_DURATION_SECONDS, TimeUnit.SECONDS);
            
            // 사용자 점유 좌석 Set에 추가 (10분 TTL)
            redisTemplate.opsForSet().add(userKey, seatId.toString());
            redisTemplate.expire(userKey, HOLD_DURATION_SECONDS, TimeUnit.SECONDS);
            
            log.info("새로운 좌석 점유 설정 완료: concertId={}, seatId={}, memberId={}", concertId, seatId, memberId);
        } catch (Exception e) {
            log.error("새로운 좌석 점유 설정 실패: concertId={}, seatId={}, memberId={}", concertId, seatId, memberId, e);
            throw e;
        }
    }
    
    /**
     * 좌석 해제 (Redis)
     */
    private void releaseSeatHold(Long concertId, Long seatId, Long memberId) {
        String seatKey = SEAT_KEY_GENERATOR.generate(concertId, seatId, memberId);
        String userKey = USER_KEY_GENERATOR.generate(concertId, seatId, memberId);
        
        try {
            // 좌석 해제
            redisTemplate.delete(seatKey);
            redisTemplate.opsForSet().remove(userKey, seatId.toString());
            
            log.info("좌석 해제 완료: concertId={}, seatId={}, memberId={}", concertId, seatId, memberId);
        } catch (Exception e) {
            log.error("좌석 해제 실패: concertId={}, seatId={}, memberId={}", concertId, seatId, memberId, e);
            throw e;
        }
    }
    
    /**
     * Redis 점유 롤백
     */
    private void rollbackRedisHold(Long concertId, Long seatId, Long memberId) {
        String seatKey = SEAT_KEY_GENERATOR.generate(concertId, seatId, memberId);
        String userKey = USER_KEY_GENERATOR.generate(concertId, seatId, memberId);
        
        try {
            redisTemplate.delete(seatKey);
            redisTemplate.opsForSet().remove(userKey, seatId.toString());
            log.info("Redis 점유 롤백 완료: concertId={}, seatId={}, memberId={}", concertId, seatId, memberId);
        } catch (Exception e) {
            log.error("Redis 점유 롤백 실패: concertId={}, seatId={}, memberId={}", concertId, seatId, memberId, e);
        }
    }
    
    /**
     * 좌석 점유 정보 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class SeatHoldInfo {
        private Long memberId;
        private Long remainingSeconds;
    }
    
    /**
     * Redis → DB 동기화 (좌석 점유) - 내부 메서드 (트랜잭션 내에서 호출)
     */
    private void syncHoldToDatabaseInternal(Long concertId, Long seatId, Long memberId) {
        try {
            log.info("DB 동기화 시작: 좌석 점유 - concertId={}, seatId={}, memberId={}", 
                concertId, seatId, memberId);
            
            // DB에 점유 정보 저장
            Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
            
            Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));
            
            // 만료 시간 설정
            LocalDateTime expireAt = LocalDateTime.now().plusSeconds(HOLD_DURATION_SECONDS);
            
            // 기존 점유 정보 삭제 (중복 방지)
            seatHoldRepository.deleteBySeatId(seatId);
            
            // 새로운 점유 정보 저장
            SeatHold seatHold = new SeatHold();
            seatHold.setMember(member);
            seatHold.setSeat(seat);
            seatHold.setHoldExpireAt(expireAt);
            
            seatHoldRepository.save(seatHold);
            
            log.info("DB 동기화 완료: 좌석 점유 - concertId={}, seatId={}, memberId={}", 
                concertId, seatId, memberId);
                
        } catch (Exception e) {
            log.error("DB 동기화 실패: 좌석 점유 - concertId={}, seatId={}, memberId={}", 
                concertId, seatId, memberId, e);
            throw e; // 예외를 다시 던져서 트랜잭션 롤백
        }
    }
    
    /**
     * Redis → DB 동기화 (좌석 해제) - 내부 메서드 (트랜잭션 내에서 호출)
     */
    private void syncReleaseToDatabaseInternal(Long concertId, Long seatId, Long memberId) {
        try {
            log.info("DB 동기화 시작: 좌석 해제 - concertId={}, seatId={}, memberId={}", 
                concertId, seatId, memberId);
            
            // DB에서 점유 정보 삭제
            seatHoldRepository.deleteBySeatId(seatId);
            
            log.info("DB 동기화 완료: 좌석 해제 - concertId={}, seatId={}, memberId={}", 
                concertId, seatId, memberId);
                
        } catch (Exception e) {
            log.error("DB 동기화 실패: 좌석 해제 - concertId={}, seatId={}, memberId={}", 
                concertId, seatId, memberId, e);
            throw e; // 예외를 다시 던져서 트랜잭션 롤백
        }
    }
    
    /**
     * Redis → DB 동기화 (예매 확정) - 내부 메서드 (트랜잭션 내에서 호출)
     */
    private void syncConfirmToDatabaseInternal(Long concertId, Long seatId, Long memberId) {
        try {
            log.info("DB 동기화 시작: 예매 확정 - concertId={}, seatId={}, memberId={}", 
                concertId, seatId, memberId);
            
            // DB에서 점유 정보 삭제
            seatHoldRepository.deleteBySeatId(seatId);
            
            // 좌석 상태를 BOOKED로 변경
            Seat seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));
            seat.setStatus(Seat.SeatStatus.BOOKED);
            seatRepository.save(seat);
            
            log.info("DB 동기화 완료: 예매 확정 - concertId={}, seatId={}, memberId={}", 
                concertId, seatId, memberId);
                
        } catch (Exception e) {
            log.error("DB 동기화 실패: 예매 확정 - concertId={}, seatId={}, memberId={}", 
                concertId, seatId, memberId, e);
            throw e; // 예외를 다시 던져서 트랜잭션 롤백
        }
    }
    
    // 기존 외부 동기화 메서드들은 호환성을 위해 유지 (별도 트랜잭션으로 실행)
    /**
     * Redis → DB 동기화 (좌석 점유) - 외부 호출용
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void syncHoldToDatabase(Long concertId, Long seatId, Long memberId) {
        syncHoldToDatabaseInternal(concertId, seatId, memberId);
    }
    
    /**
     * Redis → DB 동기화 (좌석 해제) - 외부 호출용
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void syncReleaseToDatabase(Long concertId, Long seatId, Long memberId) {
        syncReleaseToDatabaseInternal(concertId, seatId, memberId);
    }
    
    /**
     * Redis → DB 동기화 (예매 확정) - 외부 호출용
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void syncConfirmToDatabase(Long concertId, Long seatId, Long memberId) {
        syncConfirmToDatabaseInternal(concertId, seatId, memberId);
    }
} 