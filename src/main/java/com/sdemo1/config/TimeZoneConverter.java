package com.sdemo1.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * JPA Converter: UTC ↔ 한국 시간 자동 변환
 * 
 * 사용법:
 * @Convert(converter = TimeZoneConverter.class)
 * private LocalDateTime openTime;
 * 
 * 동작 방식:
 * - DB 저장 시: 한국 시간 → UTC 변환
 * - DB 조회 시: UTC → 한국 시간 변환
 */
@Slf4j
@Converter
public class TimeZoneConverter implements AttributeConverter<LocalDateTime, LocalDateTime> {

    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");

    @Override
    public LocalDateTime convertToDatabaseColumn(LocalDateTime koreaTime) {
        // 한국 시간을 UTC로 변환하여 DB에 저장
        if (koreaTime == null) {
            return null;
        }
        LocalDateTime utcTime = koreaTime
            .atZone(KOREA_ZONE)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();
        log.debug("한국 시간 → UTC 변환: {} → {}", koreaTime, utcTime);
        return utcTime;
    }

    @Override
    public LocalDateTime convertToEntityAttribute(LocalDateTime utcTime) {
        // DB의 UTC를 한국 시간으로 변환하여 반환
        if (utcTime == null) {
            return null;
        }
        LocalDateTime koreaTime = utcTime
            .atZone(ZoneOffset.UTC)
            .withZoneSameInstant(KOREA_ZONE)
            .toLocalDateTime();
        log.debug("UTC → 한국 시간 변환: {} → {}", utcTime, koreaTime);
        return koreaTime;
    }
} 