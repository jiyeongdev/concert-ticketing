package com.sdemo1.service;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.sdemo1.common.utils.CacheInvalidationUtils;
import com.sdemo1.common.utils.CacheKeyGenerator;
import com.sdemo1.dto.ConcertDto;
import com.sdemo1.entity.Concert;
import com.sdemo1.repository.ConcertRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConcertService {

    private final ConcertRepository concertRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheInvalidationUtils cacheInvalidationUtils;

    /**
     * 모든 콘서트 조회 (캐시 적용)
     */
    public List<ConcertDto> getAllConcerts() {
        String cacheKey = CacheKeyGenerator.getAllConcertsKey();
        
        List<ConcertDto> concerts = null;
        try {
            concerts = (List<ConcertDto>) redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.warn("=== 콘서트 캐시 읽기 실패, DB 조회로 대체: {} ===", e.getMessage());
            // 캐시 읽기 실패 시 해당 키 삭제
            redisTemplate.delete(cacheKey);
        }
        
        if (concerts == null) {
            log.info("=== 모든 콘서트 조회 (DB) ===");
            concerts = concertRepository.findAllByOrderByConcertDateAsc()
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            try {
                redisTemplate.opsForValue().set(cacheKey, concerts, CacheKeyGenerator.CONCERT_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                log.info("=== 콘서트 캐시 저장: {} ===", cacheKey);
            } catch (Exception e) {
                log.warn("=== 콘서트 캐시 저장 실패: {} ===", e.getMessage());
            }
        } else {
            log.info("=== 모든 콘서트 조회 (캐시) ===");
        }
        
        return concerts;
    }

    /**
     * 콘서트 ID로 조회 (캐시 적용)
     */
    public ConcertDto getConcertById(BigInteger id) {
        String cacheKey = CacheKeyGenerator.getConcertByIdKey(id);
        
        ConcertDto concert = null;
        try {
            concert = (ConcertDto) redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.warn("=== 콘서트 캐시 읽기 실패, DB 조회로 대체: {} ===", e.getMessage());
            redisTemplate.delete(cacheKey);
        }
        
        if (concert == null) {
            log.info("=== 콘서트 조회 (DB): {} ===", id);
            Concert concertEntity = concertRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("콘서트를 찾을 수 없습니다: " + id));
            concert = convertToDto(concertEntity);
            
            try {
                redisTemplate.opsForValue().set(cacheKey, concert, CacheKeyGenerator.CONCERT_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                log.info("=== 콘서트 캐시 저장: {} ===", cacheKey);
            } catch (Exception e) {
                log.warn("=== 콘서트 캐시 저장 실패: {} ===", e.getMessage());
            }
        } else {
            log.info("=== 콘서트 조회 (캐시): {} ===", id);
        }
        
        return concert;
    }

    /**
     * 콘서트 제목으로 검색 (캐시 적용)
     */
    public List<ConcertDto> searchConcertsByTitle(String title) {
        String cacheKey = CacheKeyGenerator.getConcertSearchKey(title);
        
        List<ConcertDto> concerts = null;
        try {
            concerts = (List<ConcertDto>) redisTemplate.opsForValue().get(cacheKey);
        } catch (Exception e) {
            log.warn("=== 콘서트 검색 캐시 읽기 실패, DB 조회로 대체: {} ===", e.getMessage());
            redisTemplate.delete(cacheKey);
        }
        
        if (concerts == null) {
            log.info("=== 콘서트 제목 검색 (DB): {} ===", title);
            concerts = concertRepository.findByTitleContainingIgnoreCase(title)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            try {
                redisTemplate.opsForValue().set(cacheKey, concerts, CacheKeyGenerator.CONCERT_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
                log.info("=== 콘서트 검색 캐시 저장: {} ===", cacheKey);
            } catch (Exception e) {
                log.warn("=== 콘서트 검색 캐시 저장 실패: {} ===", e.getMessage());
            }
        } else {
            log.info("=== 콘서트 제목 검색 (캐시): {} ===", title);
        }
        
        return concerts;
    }

    /**
     * 콘서트 생성 (캐시 무효화)
     */
    public ConcertDto createConcert(ConcertDto concertDto) {
        log.info("=== 콘서트 생성: {} ===", concertDto.title());
        
        Concert concert = convertToEntity(concertDto);
        Concert savedConcert = concertRepository.save(concert);
        
        // 관련 캐시 무효화
        cacheInvalidationUtils.invalidateConcertCaches();
        
        return convertToDto(savedConcert);
    }

    /**
     * 콘서트 수정 (캐시 무효화)
     */
    public ConcertDto updateConcert(BigInteger id, ConcertDto concertDto) {
        log.info("=== 콘서트 수정: {} ===", id);
        
        Concert existingConcert = concertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("콘서트를 찾을 수 없습니다: " + id));
        
        existingConcert.setTitle(concertDto.title());
        existingConcert.setLocation(concertDto.location());
        existingConcert.setConcertDate(concertDto.concertDate());
        existingConcert.setOpenTime(concertDto.openTime());
        existingConcert.setCloseTime(concertDto.closeTime());
        
        Concert updatedConcert = concertRepository.save(existingConcert);
        
        // 관련 캐시 무효화
        cacheInvalidationUtils.invalidateConcertCaches();
        
        return convertToDto(updatedConcert);
    }

    /**
     * 콘서트 삭제 (캐시 무효화)
     */
    public void deleteConcert(BigInteger id) {
        log.info("=== 콘서트 삭제: {} ===", id);
        concertRepository.deleteById(id);
        
        // 관련 캐시 무효화
        cacheInvalidationUtils.invalidateConcertCaches();
    }

    /**
     * 콘서트 캐시 무효화
     */
    public void evictConcertCache(BigInteger concertId) {
        log.info("=== 콘서트 캐시 무효화: {} ===", concertId);
        cacheInvalidationUtils.invalidateConcertCache(concertId);
    }

    /**
     * 모든 콘서트 캐시 무효화
     */
    public void clearAllCache() {
        log.info("=== 모든 콘서트 캐시 무효화 ===");
        cacheInvalidationUtils.clearAllConcertCaches();
    }

    /**
     * Entity를 DTO로 변환
     */
    private ConcertDto convertToDto(Concert concert) {
        return new ConcertDto(
            concert.getId(),
            concert.getTitle(),
            concert.getLocation(),
            concert.getConcertDate(),
            concert.getOpenTime(),
            concert.getCloseTime()
        );
    }

    /**
     * DTO를 Entity로 변환
     */
    private Concert convertToEntity(ConcertDto concertDto) {
        Concert concert = new Concert();
        concert.setTitle(concertDto.title());
        concert.setLocation(concertDto.location());
        concert.setConcertDate(concertDto.concertDate());
        concert.setOpenTime(concertDto.openTime());
        concert.setCloseTime(concertDto.closeTime());
        return concert;
    }
} 