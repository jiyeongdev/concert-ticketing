package com.sdemo1.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.sdemo1.dto.SeatDto;
import com.sdemo1.entity.Concert;
import com.sdemo1.entity.Seat;
import com.sdemo1.entity.SeatGrade;
import com.sdemo1.repository.ConcertRepository;
import com.sdemo1.repository.SeatGradeRepository;
import com.sdemo1.repository.SeatRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SeatService {

    private final SeatRepository seatRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final ConcertRepository concertRepository;

    /**
     * 콘서트의 모든 좌석 조회 (캐시 적용)
     */
    @Cacheable(value = "seats", key = "#concertId")
    public List<SeatDto> getSeatsByConcertId(BigInteger concertId) {
        log.info("=== 콘서트 좌석 조회: {} ===", concertId);
        return seatRepository.findByConcertIdOrderBySeatRowAscSeatNumberAsc(concertId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 좌석 ID로 조회 (캐시 적용)
     */
    @Cacheable(value = "seats", key = "#id")
    public SeatDto getSeatById(BigInteger id) {
        log.info("=== 좌석 조회: {} ===", id);
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다: " + id));
        return convertToDto(seat);
    }

    /**
     * 콘서트 ID와 좌석등급 ID로 좌석 조회 (캐시 적용)
     */
    @Cacheable(value = "seats", key = "'concert_' + #concertId + '_grade_' + #seatGradeId")
    public List<SeatDto> getSeatsByConcertIdAndGradeId(BigInteger concertId, BigInteger seatGradeId) {
        log.info("=== 콘서트 좌석등급별 좌석 조회: {} - {} ===", concertId, seatGradeId);
        return seatRepository.findByConcertIdAndSeatGradeIdOrderBySeatRowAscSeatNumberAsc(concertId, seatGradeId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 콘서트 ID와 좌석 상태로 조회 (캐시 적용)
     */
    @Cacheable(value = "seats", key = "'concert_' + #concertId + '_status_' + #status")
    public List<SeatDto> getSeatsByConcertIdAndStatus(BigInteger concertId, Seat.SeatStatus status) {
        log.info("=== 콘서트 상태별 좌석 조회: {} - {} ===", concertId, status);
        return seatRepository.findByConcertIdAndStatusOrderBySeatRowAscSeatNumberAsc(concertId, status)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 좌석 생성 (캐시 무효화) - 단일 좌석
     */
    @CacheEvict(value = "seats", allEntries = true)
    public SeatDto createSeat(SeatDto seatDto) {
        log.info("=== 좌석 생성: {}행 {}번 ===", seatDto.seatRow(), seatDto.seatNumber());
        
        // 콘서트 존재 여부 확인
        Concert concert = concertRepository.findById(seatDto.concertId())
                .orElseThrow(() -> new RuntimeException("콘서트를 찾을 수 없습니다: " + seatDto.concertId()));
        
        // 좌석등급 존재 여부 확인
        SeatGrade seatGrade = null;
        if (seatDto.seatGradeId() != null) {
            seatGrade = seatGradeRepository.findById(seatDto.seatGradeId())
                    .orElseThrow(() -> new RuntimeException("좌석등급을 찾을 수 없습니다: " + seatDto.seatGradeId()));
        }
        
        // 동일한 좌석이 이미 존재하는지 확인
        if (seatRepository.findByConcertIdAndSeatRowAndSeatNumber(seatDto.concertId(), seatDto.seatRow(), seatDto.seatNumber()) != null) {
            throw new RuntimeException("이미 존재하는 좌석입니다: " + seatDto.seatRow() + "행 " + seatDto.seatNumber() + "번");
        }
        
        Seat seat = convertToEntity(seatDto);
        seat.setConcert(concert);
        seat.setSeatGrade(seatGrade);
        Seat savedSeat = seatRepository.save(seat);
        return convertToDto(savedSeat);
    }

    /**
     * 좌석 생성 (캐시 무효화) - 배열 형태로 여러 좌석 생성
     */
    @CacheEvict(value = "seats", allEntries = true)
    public List<SeatDto> createSeats(List<SeatDto> seatDtos) {
        log.info("=== 좌석 배열 생성: {}개 ===", seatDtos.size());
        
        List<Seat> seatsToSave = new ArrayList<>();
        List<String> existingSeats = new ArrayList<>();
        
        for (SeatDto seatDto : seatDtos) {
            // 콘서트 존재 여부 확인
            Concert concert = concertRepository.findById(seatDto.concertId())
                    .orElseThrow(() -> new RuntimeException("콘서트를 찾을 수 없습니다: " + seatDto.concertId()));
            
            // 좌석등급 존재 여부 확인
            SeatGrade seatGrade = null;
            if (seatDto.seatGradeId() != null) {
                seatGrade = seatGradeRepository.findById(seatDto.seatGradeId())
                        .orElseThrow(() -> new RuntimeException("좌석등급을 찾을 수 없습니다: " + seatDto.seatGradeId()));
            }
            
            // 동일한 좌석이 이미 존재하는지 확인
            if (seatRepository.findByConcertIdAndSeatRowAndSeatNumber(seatDto.concertId(), seatDto.seatRow(), seatDto.seatNumber()) != null) {
                existingSeats.add(seatDto.seatRow() + "행 " + seatDto.seatNumber() + "번");
                continue;
            }
            
            Seat seat = convertToEntity(seatDto);
            seat.setConcert(concert);
            seat.setSeatGrade(seatGrade);
            seatsToSave.add(seat);
        }
        
        // 벌크 저장
        List<Seat> savedSeats = seatRepository.saveAll(seatsToSave);
        
        // 기존 좌석이 있었다면 경고 로그
        if (!existingSeats.isEmpty()) {
            log.warn("=== 기존 좌석 건너뛰기: {} ===", String.join(", ", existingSeats));
        }
        
        log.info("=== 좌석 배열 생성 완료: {}개 생성, {}개 건너뛰기 ===", savedSeats.size(), existingSeats.size());
        
        return savedSeats.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 좌석 수정 (캐시 무효화)
     */
    @CacheEvict(value = "seats", allEntries = true)
    public SeatDto updateSeat(BigInteger id, SeatDto seatDto) {
        log.info("=== 좌석 수정: {} ===", id);
        
        Seat existingSeat = seatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다: " + id));
        
        // 콘서트 존재 여부 확인
        Concert concert = concertRepository.findById(seatDto.concertId())
                .orElseThrow(() -> new RuntimeException("콘서트를 찾을 수 없습니다: " + seatDto.concertId()));
        
        // 좌석등급 존재 여부 확인
        SeatGrade seatGrade = null;
        if (seatDto.seatGradeId() != null) {
            seatGrade = seatGradeRepository.findById(seatDto.seatGradeId())
                    .orElseThrow(() -> new RuntimeException("좌석등급을 찾을 수 없습니다: " + seatDto.seatGradeId()));
        }
        
        existingSeat.setConcert(concert);
        existingSeat.setSeatGrade(seatGrade);
        existingSeat.setSeatRow(seatDto.seatRow());
        existingSeat.setSeatNumber(seatDto.seatNumber());
        existingSeat.setPositionX(seatDto.positionX());
        existingSeat.setPositionY(seatDto.positionY());
        existingSeat.setStatus(Seat.SeatStatus.valueOf(seatDto.status()));
        
        Seat updatedSeat = seatRepository.save(existingSeat);
        return convertToDto(updatedSeat);
    }

    /**
     * 좌석 삭제 (캐시 무효화)
     */
    @CacheEvict(value = "seats", allEntries = true)
    public void deleteSeat(BigInteger id) {
        log.info("=== 좌석 삭제: {} ===", id);
        if (!seatRepository.existsById(id)) {
            throw new RuntimeException("좌석을 찾을 수 없습니다: " + id);
        }
        seatRepository.deleteById(id);
    }

    /**
     * 콘서트별 좌석 수정 (캐시 무효화)
     */
    @CacheEvict(value = "seats", allEntries = true)
    public List<SeatDto> updateSeatsByConcert(BigInteger concertId, List<SeatDto> seatDtos) {
        log.info("=== 콘서트별 좌석 수정: {} - {}개 ===", concertId, seatDtos.size());
        
        // 기존 좌석들 삭제
        seatRepository.deleteByConcertId(concertId);
        
        // 새로운 좌석들 생성
        return createSeats(seatDtos);
    }

    /**
     * 콘서트별 좌석 삭제 (캐시 무효화)
     */
    @CacheEvict(value = "seats", allEntries = true)
    public void deleteSeatsByConcert(BigInteger concertId) {
        log.info("=== 콘서트별 좌석 삭제: {} ===", concertId);
        seatRepository.deleteByConcertId(concertId);
    }

    /**
     * 좌석 상태 변경 (캐시 무효화)
     */
    @CacheEvict(value = "seats", allEntries = true)
    public SeatDto updateSeatStatus(BigInteger id, Seat.SeatStatus status) {
        log.info("=== 좌석 상태 변경: {} - {} ===", id, status);
        
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다: " + id));
        
        seat.setStatus(status);
        Seat updatedSeat = seatRepository.save(seat);
        return convertToDto(updatedSeat);
    }

    /**
     * 콘서트의 사용 가능한 좌석 수 조회
     */
    public long getAvailableSeatCount(BigInteger concertId) {
        return seatRepository.countAvailableSeatsByConcertId(concertId);
    }

    /**
     * 캐시 무효화
     */
    @CacheEvict(value = "seats", allEntries = true)
    public void clearCache() {
        log.info("=== 좌석 캐시 무효화 ===");
    }

    private SeatDto convertToDto(Seat seat) {
        return new SeatDto(
            seat.getId(),
            seat.getConcert().getId(),
            seat.getSeatGrade() != null ? seat.getSeatGrade().getId() : null,
            seat.getSeatRow(),
            seat.getSeatNumber(),
            seat.getPositionX(),
            seat.getPositionY(),
            seat.getStatus().name(),
            seat.getSeatGrade() != null ? seat.getSeatGrade().getGradeName() : null,
            seat.getSeatGrade() != null ? seat.getSeatGrade().getPrice() : null
        );
    }

    private Seat convertToEntity(SeatDto seatDto) {
        Seat seat = new Seat();
        seat.setSeatRow(seatDto.seatRow());
        seat.setSeatNumber(seatDto.seatNumber());
        seat.setPositionX(seatDto.positionX());
        seat.setPositionY(seatDto.positionY());
        seat.setStatus(Seat.SeatStatus.valueOf(seatDto.status()));
        return seat;
    }
} 