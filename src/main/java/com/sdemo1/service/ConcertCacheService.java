package com.sdemo1.service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConcertCacheService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String CONCERT_SEATS_CACHE_KEY = "concert:seats:";
    private static final int CACHE_DURATION_HOURS = 24; // 24시간 캐시
    
    /**
     * 콘서트 좌석 정보 조회 (캐시 우선)
     */
    public List<ConcertSeatInfo> getConcertSeatsFromCache(Long concertId) {
        String cacheKey = CONCERT_SEATS_CACHE_KEY + concertId;
        
        try {
            // Redis에서 캐시된 좌석 정보 조회
            String cachedData = redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedData != null) {
                log.debug("콘서트 좌석 정보 캐시 히트: concertId={}", concertId);
                return objectMapper.readValue(cachedData, new TypeReference<List<ConcertSeatInfo>>() {});
            }
            
            log.debug("콘서트 좌석 정보 캐시 미스: concertId={}", concertId);
            return null;
            
        } catch (Exception e) {
            log.error("콘서트 좌석 정보 캐시 조회 실패: concertId={}", concertId, e);
            return null;
        }
    }
    
    /**
     * 콘서트 좌석 정보를 캐시에 저장
     */
    public void cacheConcertSeats(Long concertId, List<ConcertSeatInfo> seats) {
        String cacheKey = CONCERT_SEATS_CACHE_KEY + concertId;
        
        try {
            String jsonData = objectMapper.writeValueAsString(seats);
            redisTemplate.opsForValue().set(cacheKey, jsonData, CACHE_DURATION_HOURS, TimeUnit.HOURS);
            
            log.info("콘서트 좌석 정보 캐시 저장: concertId={}, 좌석 수={}", concertId, seats.size());
            
        } catch (Exception e) {
            log.error("콘서트 좌석 정보 캐시 저장 실패: concertId={}", concertId, e);
        }
    }
    
    /**
     * 콘서트 관련 캐시 삭제
     */
    public void evictConcertCache(Long concertId) {
        String cacheKey = CONCERT_SEATS_CACHE_KEY + concertId;
        
        try {
            redisTemplate.delete(cacheKey);
            log.info("콘서트 캐시 삭제: concertId={}", concertId);
            
        } catch (Exception e) {
            log.error("콘서트 캐시 삭제 실패: concertId={}", concertId, e);
        }
    }
    
    /**
     * 모든 콘서트 캐시 삭제
     */
    public void evictAllConcertCache() {
        try {
            // 패턴으로 모든 콘서트 캐시 삭제
            String pattern = CONCERT_SEATS_CACHE_KEY + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            log.info("모든 콘서트 캐시 삭제 완료");
            
        } catch (Exception e) {
            log.error("모든 콘서트 캐시 삭제 실패", e);
        }
    }
    
    /**
     * 콘서트 좌석 정보 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ConcertSeatInfo {
        private Long id;
        private Long concertId;
        private Long seatGradeId;
        private String seatRow;
        private String seatNumber;
        private Integer positionX;
        private Integer positionY;
        private String gradeName;
        private Integer price;
    }
} 