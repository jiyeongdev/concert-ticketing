package com.sdemo1.service;

import com.sdemo1.dto.ConcertDto;
import com.sdemo1.entity.Concert;
import com.sdemo1.repository.ConcertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ConcertService {

    private final ConcertRepository concertRepository;

    /**
     * 모든 콘서트 조회 (캐시 적용)
     */
    @Cacheable(value = "concerts", key = "'all'")
    public List<ConcertDto> getAllConcerts() {
        log.info("=== 모든 콘서트 조회 (캐시 적용) ===");
        return concertRepository.findAllByOrderByConcertDateAsc()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 콘서트 ID로 조회 (캐시 적용)
     */
    @Cacheable(value = "concerts", key = "#id")
    public ConcertDto getConcertById(BigInteger id) {
        log.info("=== 콘서트 조회: {} ===", id);
        Concert concert = concertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("콘서트를 찾을 수 없습니다: " + id));
        return convertToDto(concert);
    }

    /**
     * 콘서트 제목으로 검색 (캐시 적용)
     */
    @Cacheable(value = "concerts", key = "'search_' + #title")
    public List<ConcertDto> searchConcertsByTitle(String title) {
        log.info("=== 콘서트 제목 검색: {} ===", title);
        return concertRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 콘서트 생성 (캐시 무효화)
     */
    @CacheEvict(value = "concerts", allEntries = true)
    public ConcertDto createConcert(ConcertDto concertDto) {
        log.info("=== 콘서트 생성: {} ===", concertDto.title());
        Concert concert = convertToEntity(concertDto);
        Concert savedConcert = concertRepository.save(concert);
        return convertToDto(savedConcert);
    }

    /**
     * 콘서트 수정 (캐시 무효화)
     */
    @CacheEvict(value = "concerts", allEntries = true)
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
        return convertToDto(updatedConcert);
    }

    /**
     * 콘서트 삭제 (캐시 무효화)
     */
    @CacheEvict(value = "concerts", allEntries = true)
    public void deleteConcert(BigInteger id) {
        log.info("=== 콘서트 삭제: {} ===", id);
        if (!concertRepository.existsById(id)) {
            throw new RuntimeException("콘서트를 찾을 수 없습니다: " + id);
        }
        concertRepository.deleteById(id);
    }

    /**
     * 캐시 무효화
     */
    @CacheEvict(value = "concerts", allEntries = true)
    public void clearCache() {
        log.info("=== 콘서트 캐시 무효화 ===");
    }

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