package com.sdemo1.common.utils;

import java.math.BigInteger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 캐시 무효화 공통 유틸리티
 * 
 * 중복된 캐시 무효화 로직을 통합하여 재사용성 향상
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 좌석 관련 캐시 무효화 (콘서트 ID 기반)
     */
    public void invalidateSeatCaches(BigInteger concertId) {
        String pattern = CacheKeyGenerator.getSeatsByConcertPattern(concertId);
        redisTemplate.delete(redisTemplate.keys(pattern));
        log.info("=== 좌석 캐시 무효화 완료: {} ===", pattern);
    }

    /**
     * 좌석등급 관련 캐시 무효화 (콘서트 ID 기반)
     */
    public void invalidateSeatGradeCaches(BigInteger concertId) {
        String pattern = CacheKeyGenerator.getSeatGradesByConcertPattern(concertId);
        redisTemplate.delete(redisTemplate.keys(pattern));
        log.info("=== 좌석등급 캐시 무효화 완료: {} ===", pattern);
    }

    /**
     * 콘서트 관련 캐시 무효화
     */
    public void invalidateConcertCaches() {
        redisTemplate.delete(redisTemplate.keys(CacheKeyGenerator.getAllConcertsPattern()));
        log.info("=== 콘서트 캐시 무효화 완료 ===");
    }

    /**
     * 특정 콘서트 캐시 무효화
     */
    public void invalidateConcertCache(BigInteger concertId) {
        String cacheKey = CacheKeyGenerator.getConcertByIdKey(concertId);
        redisTemplate.delete(cacheKey);
        log.info("=== 콘서트 캐시 무효화 완료: {} ===", cacheKey);
    }

    /**
     * 모든 좌석 캐시 무효화
     */
    public void clearAllSeatCaches() {
        redisTemplate.delete(redisTemplate.keys(CacheKeyGenerator.getAllSeatsPattern()));
        log.info("=== 모든 좌석 캐시 무효화 완료 ===");
    }

    /**
     * 모든 좌석등급 캐시 무효화
     */
    public void clearAllSeatGradeCaches() {
        redisTemplate.delete(redisTemplate.keys(CacheKeyGenerator.getAllSeatGradesPattern()));
        log.info("=== 모든 좌석등급 캐시 무효화 완료 ===");
    }

    /**
     * 모든 콘서트 캐시 무효화
     */
    public void clearAllConcertCaches() {
        redisTemplate.delete(redisTemplate.keys(CacheKeyGenerator.getAllConcertsPattern()));
        log.info("=== 모든 콘서트 캐시 무효화 완료 ===");
    }

    /**
     * 콘서트 관련 모든 캐시 무효화 (좌석, 좌석등급, 콘서트)
     */
    public void invalidateAllConcertRelatedCaches(BigInteger concertId) {
        invalidateSeatCaches(concertId);
        invalidateSeatGradeCaches(concertId);
        invalidateConcertCache(concertId);
        log.info("=== 콘서트 관련 모든 캐시 무효화 완료: {} ===", concertId);
    }
} 