package com.sdemo1.common.utils;

import java.math.BigInteger;

/**
 * 캐시 키 생성을 중앙화하는 유틸리티 클래스
 * 
 * - 캐시 키 패턴을 명확하게 정의
 * - 키 생성 로직을 중앙화하여 일관성 보장
 * - 캐시 무효화 시 패턴 매칭 용이
 */
public class CacheKeyGenerator {

    // 캐시 키 접두사
    public static final String SEAT_CACHE_PREFIX = "seats:";
    public static final String CONCERT_CACHE_PREFIX = "concerts:";
    public static final String SEAT_GRADE_CACHE_PREFIX = "seatGrades:";

    // TTL 설정 (분 단위)
    public static final int SEAT_CACHE_TTL_MINUTES = 30;
    public static final int CONCERT_CACHE_TTL_MINUTES = 60;
    public static final int SEAT_GRADE_CACHE_TTL_MINUTES = 60;

    // ===== 좌석 관련 캐시 키 =====
    
    /**
     * 콘서트의 모든 좌석 조회 캐시 키
     */
    public static String getSeatsByConcertKey(BigInteger concertId) {
        return SEAT_CACHE_PREFIX + "concert:" + concertId;
    }

    /**
     * 특정 좌석 조회 캐시 키
     */
    public static String getSeatByIdKey(BigInteger seatId) {
        return SEAT_CACHE_PREFIX + "seat:" + seatId;
    }

    /**
     * 콘서트 + 좌석등급별 좌석 조회 캐시 키
     */
    public static String getSeatsByConcertAndGradeKey(BigInteger concertId, BigInteger seatGradeId) {
        return SEAT_CACHE_PREFIX + "concert:" + concertId + ":grade:" + seatGradeId;
    }

    /**
     * 콘서트 + 좌석 상태별 좌석 조회 캐시 키
     */
    public static String getSeatsByConcertAndStatusKey(BigInteger concertId, String status) {
        return SEAT_CACHE_PREFIX + "concert:" + concertId + ":status:" + status;
    }

    /**
     * 콘서트 관련 모든 좌석 캐시 키 패턴 (무효화용)
     */
    public static String getSeatsByConcertPattern(BigInteger concertId) {
        return SEAT_CACHE_PREFIX + "concert:" + concertId + "*";
    }

    // ===== 콘서트 관련 캐시 키 =====
    
    /**
     * 모든 콘서트 조회 캐시 키
     */
    public static String getAllConcertsKey() {
        return CONCERT_CACHE_PREFIX + "all";
    }

    /**
     * 특정 콘서트 조회 캐시 키
     */
    public static String getConcertByIdKey(BigInteger concertId) {
        return CONCERT_CACHE_PREFIX + "concert:" + concertId;
    }

    /**
     * 콘서트 제목 검색 캐시 키
     */
    public static String getConcertSearchKey(String title) {
        return CONCERT_CACHE_PREFIX + "search:" + title;
    }

    /**
     * 모든 콘서트 캐시 키 패턴 (무효화용)
     */
    public static String getAllConcertsPattern() {
        return CONCERT_CACHE_PREFIX + "*";
    }

    // ===== 좌석등급 관련 캐시 키 =====
    
    /**
     * 콘서트의 모든 좌석등급 조회 캐시 키
     */
    public static String getSeatGradesByConcertKey(BigInteger concertId) {
        return SEAT_GRADE_CACHE_PREFIX + "concert:" + concertId;
    }

    /**
     * 특정 좌석등급 조회 캐시 키
     */
    public static String getSeatGradeByIdKey(BigInteger seatGradeId) {
        return SEAT_GRADE_CACHE_PREFIX + "grade:" + seatGradeId;
    }

    /**
     * 콘서트 관련 모든 좌석등급 캐시 키 패턴 (무효화용)
     */
    public static String getSeatGradesByConcertPattern(BigInteger concertId) {
        return SEAT_GRADE_CACHE_PREFIX + "concert:" + concertId + "*";
    }

    // ===== 캐시 무효화 패턴 =====
    
    /**
     * 모든 좌석 캐시 무효화 패턴
     */
    public static String getAllSeatsPattern() {
        return SEAT_CACHE_PREFIX + "*";
    }

    /**
     * 모든 좌석등급 캐시 무효화 패턴
     */
    public static String getAllSeatGradesPattern() {
        return SEAT_GRADE_CACHE_PREFIX + "*";
    }
} 