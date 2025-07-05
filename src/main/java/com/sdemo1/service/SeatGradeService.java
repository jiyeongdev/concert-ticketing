package com.sdemo1.service;

import com.sdemo1.dto.SeatGradeDto;
import com.sdemo1.entity.Concert;
import com.sdemo1.entity.SeatGrade;
import com.sdemo1.repository.ConcertRepository;
import com.sdemo1.repository.SeatGradeRepository;
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
public class SeatGradeService {

    private final SeatGradeRepository seatGradeRepository;
    private final ConcertRepository concertRepository;

    /**
     * 콘서트의 모든 좌석등급 조회 (캐시 적용)
     */
    @Cacheable(value = "seatGrades", key = "#concertId")
    public List<SeatGradeDto> getSeatGradesByConcertId(BigInteger concertId) {
        log.info("=== 콘서트 좌석등급 조회: {} ===", concertId);
        return seatGradeRepository.findByConcertIdOrderByPriceDesc(concertId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 좌석등급 ID로 조회 (캐시 적용)
     */
    @Cacheable(value = "seatGrades", key = "#id")
    public SeatGradeDto getSeatGradeById(BigInteger id) {
        log.info("=== 좌석등급 조회: {} ===", id);
        SeatGrade seatGrade = seatGradeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("좌석등급을 찾을 수 없습니다: " + id));
        return convertToDto(seatGrade);
    }

    /**
     * 좌석등급 생성 (캐시 무효화)
     */
    @CacheEvict(value = "seatGrades", allEntries = true)
    public SeatGradeDto createSeatGrade(SeatGradeDto seatGradeDto) {
        log.info("=== 좌석등급 생성: {} ===", seatGradeDto.gradeName());
        
        // 콘서트 존재 여부 확인
        Concert concert = concertRepository.findById(seatGradeDto.concertId())
                .orElseThrow(() -> new RuntimeException("콘서트를 찾을 수 없습니다: " + seatGradeDto.concertId()));
        
        // 동일한 등급명이 이미 존재하는지 확인
        if (seatGradeRepository.findByConcertIdAndGradeName(seatGradeDto.concertId(), seatGradeDto.gradeName()) != null) {
            throw new RuntimeException("이미 존재하는 좌석등급입니다: " + seatGradeDto.gradeName());
        }
        
        SeatGrade seatGrade = convertToEntity(seatGradeDto);
        seatGrade.setConcert(concert);
        SeatGrade savedSeatGrade = seatGradeRepository.save(seatGrade);
        return convertToDto(savedSeatGrade);
    }

    /**
     * 좌석등급 수정 (캐시 무효화)
     */
    @CacheEvict(value = "seatGrades", allEntries = true)
    public SeatGradeDto updateSeatGrade(BigInteger id, SeatGradeDto seatGradeDto) {
        log.info("=== 좌석등급 수정: {} ===", id);
        
        SeatGrade existingSeatGrade = seatGradeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("좌석등급을 찾을 수 없습니다: " + id));
        
        // 콘서트 존재 여부 확인
        Concert concert = concertRepository.findById(seatGradeDto.concertId())
                .orElseThrow(() -> new RuntimeException("콘서트를 찾을 수 없습니다: " + seatGradeDto.concertId()));
        
        existingSeatGrade.setConcert(concert);
        existingSeatGrade.setGradeName(seatGradeDto.gradeName());
        existingSeatGrade.setPrice(seatGradeDto.price());
        
        SeatGrade updatedSeatGrade = seatGradeRepository.save(existingSeatGrade);
        return convertToDto(updatedSeatGrade);
    }

    /**
     * 좌석등급 삭제 (캐시 무효화)
     */
    @CacheEvict(value = "seatGrades", allEntries = true)
    public void deleteSeatGrade(BigInteger id) {
        log.info("=== 좌석등급 삭제: {} ===", id);
        if (!seatGradeRepository.existsById(id)) {
            throw new RuntimeException("해당 좌석id를 찾을 수 없습니다: " + id);
        }
        seatGradeRepository.deleteById(id);
    }

    /**
     * 캐시 무효화
     */
    @CacheEvict(value = "seatGrades", allEntries = true)
    public void clearCache() {
        log.info("=== 좌석등급 캐시 무효화 ===");
    }

    private SeatGradeDto convertToDto(SeatGrade seatGrade) {
        return new SeatGradeDto(
            seatGrade.getId(),
            seatGrade.getConcert().getId(),
            seatGrade.getGradeName(),
            seatGrade.getPrice()
        );
    }

    private SeatGrade convertToEntity(SeatGradeDto seatGradeDto) {
        SeatGrade seatGrade = new SeatGrade();
        seatGrade.setGradeName(seatGradeDto.gradeName());
        seatGrade.setPrice(seatGradeDto.price());
        return seatGrade;
    }
} 