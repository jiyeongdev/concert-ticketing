package com.sdemo1.service.seat;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.sdemo1.common.utils.CacheInvalidationUtils;
import com.sdemo1.common.utils.CacheKeyGenerator;
import com.sdemo1.dto.seat.SeatGradeDto;
import com.sdemo1.entity.Concert;
import com.sdemo1.entity.SeatGrade;
import com.sdemo1.repository.ConcertRepository;
import com.sdemo1.repository.SeatGradeRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SeatGradeService {

    private final SeatGradeRepository seatGradeRepository;
    private final ConcertRepository concertRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheInvalidationUtils cacheInvalidationUtils;

    /**
     * 콘서트의 모든 좌석등급 조회 (Redis 캐시 적용)
     */
    public List<SeatGradeDto> getSeatGradesByConcertId(BigInteger concertId) {
        String cacheKey = CacheKeyGenerator.getSeatGradesByConcertKey(concertId);
        
        List<SeatGradeDto> seatGrades = (List<SeatGradeDto>) redisTemplate.opsForValue().get(cacheKey);
        if (seatGrades == null) {
            log.info("=== 콘서트 좌석등급 조회 (DB): {} ===", concertId);
            seatGrades = seatGradeRepository.findByConcertIdOrderByPriceDesc(concertId)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            redisTemplate.opsForValue().set(cacheKey, seatGrades, CacheKeyGenerator.SEAT_GRADE_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.info("=== 좌석등급 캐시 저장: {} ===", cacheKey);
        } else {
            log.info("=== 콘서트 좌석등급 조회 (캐시): {} ===", concertId);
        }
        
        return seatGrades;
    }

    /**
     * 좌석등급 ID로 조회 (Redis 캐시 적용)
     */
    public SeatGradeDto getSeatGradeById(BigInteger id) {
        String cacheKey = CacheKeyGenerator.getSeatGradeByIdKey(id);
        
        SeatGradeDto seatGrade = (SeatGradeDto) redisTemplate.opsForValue().get(cacheKey);
        if (seatGrade == null) {
            log.info("=== 좌석등급 조회 (DB): {} ===", id);
            SeatGrade seatGradeEntity = seatGradeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("좌석등급을 찾을 수 없습니다: " + id));
            seatGrade = convertToDto(seatGradeEntity);
            
            redisTemplate.opsForValue().set(cacheKey, seatGrade, CacheKeyGenerator.SEAT_GRADE_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.info("=== 좌석등급 캐시 저장: {} ===", cacheKey);
        } else {
            log.info("=== 좌석등급 조회 (캐시): {} ===", id);
        }
        
        return seatGrade;
    }

    /**
     * 좌석등급 생성 (Redis 캐시 무효화)
     */
    public SeatGradeDto createSeatGrade(SeatGradeDto seatGradeDto) {
        log.info("=== 좌석등급 생성: {} ===", seatGradeDto.gradeName());
        
        SeatGrade seatGrade = convertToEntity(seatGradeDto);
        SeatGrade savedSeatGrade = seatGradeRepository.save(seatGrade);
        
        // 관련 캐시 무효화
        cacheInvalidationUtils.invalidateSeatGradeCaches(seatGradeDto.concertId());
        
        return convertToDto(savedSeatGrade);
    }

    /**
     * 좌석등급 수정 (Redis 캐시 무효화)
     */
    public SeatGradeDto updateSeatGrade(BigInteger id, SeatGradeDto seatGradeDto) {
        log.info("=== 좌석등급 수정: {} ===", id);
        
        SeatGrade existingSeatGrade = seatGradeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("좌석등급을 찾을 수 없습니다: " + id));
        
        existingSeatGrade.setGradeName(seatGradeDto.gradeName());
        existingSeatGrade.setPrice(seatGradeDto.price());
        
        SeatGrade updatedSeatGrade = seatGradeRepository.save(existingSeatGrade);
        
        // 관련 캐시 무효화
        cacheInvalidationUtils.invalidateSeatGradeCaches(seatGradeDto.concertId());
        
        return convertToDto(updatedSeatGrade);
    }

    /**
     * 좌석등급 삭제 (Redis 캐시 무효화)
     */
    public void deleteSeatGrade(BigInteger id) {
        log.info("=== 좌석등급 삭제: {} ===", id);
        SeatGrade seatGrade = seatGradeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("좌석등급을 찾을 수 없습니다: " + id));
        
        BigInteger concertId = seatGrade.getConcert().getId();
        seatGradeRepository.deleteById(id);
        
        // 관련 캐시 무효화
        cacheInvalidationUtils.invalidateSeatGradeCaches(concertId);
    }

    /**
     * 콘서트 캐시 무효화
     */
    public void evictConcertCache(BigInteger concertId) {
        log.info("=== 콘서트 좌석등급 캐시 무효화: {} ===", concertId);
        cacheInvalidationUtils.invalidateSeatGradeCaches(concertId);
    }

    /**
     * 모든 캐시 무효화
     */
    public void clearAllCache() {
        log.info("=== 모든 좌석등급 캐시 무효화 ===");
        cacheInvalidationUtils.clearAllSeatGradeCaches();
    }

    /**
     * Entity를 DTO로 변환
     */
    private SeatGradeDto convertToDto(SeatGrade seatGrade) {
        return new SeatGradeDto(
            seatGrade.getId(),
            seatGrade.getConcert().getId(),
            seatGrade.getGradeName(),
            seatGrade.getPrice()
        );
    }

    /**
     * DTO를 Entity로 변환
     */
    private SeatGrade convertToEntity(SeatGradeDto seatGradeDto) {
        SeatGrade seatGrade = new SeatGrade();
        seatGrade.setGradeName(seatGradeDto.gradeName());
        seatGrade.setPrice(seatGradeDto.price());
        
        // Concert 엔티티 조회 및 설정
        Concert concert = concertRepository.findById(seatGradeDto.concertId())
                .orElseThrow(() -> new RuntimeException("콘서트를 찾을 수 없습니다: " + seatGradeDto.concertId()));
        seatGrade.setConcert(concert);
        
        return seatGrade;
    }
} 