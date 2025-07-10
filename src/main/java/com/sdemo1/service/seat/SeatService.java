package com.sdemo1.service.seat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.sdemo1.common.utils.CacheInvalidationUtils;
import com.sdemo1.common.utils.CacheKeyGenerator;
import com.sdemo1.dto.seat.SeatDto;
import com.sdemo1.entity.Concert;
import com.sdemo1.entity.Seat;
import com.sdemo1.entity.SeatGrade;
import com.sdemo1.repository.ConcertRepository;
import com.sdemo1.repository.ReservationRepository;
import com.sdemo1.repository.SeatGradeRepository;
import com.sdemo1.repository.SeatRepository;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheInvalidationUtils cacheInvalidationUtils;
    private final ReservationRepository reservationRepository;

    /**
     * 콘서트의 모든 좌석 조회 (Redis 캐시 적용)
     */
    public List<SeatDto> getSeatsByConcertId(Long concertId) {
        String cacheKey = CacheKeyGenerator.getSeatsByConcertKey(concertId);
        
        List<SeatDto> seats = (List<SeatDto>) redisTemplate.opsForValue().get(cacheKey);
        if (seats == null) {
            log.info("=== 콘서트 좌석 조회 (DB): {} ===", concertId);
            seats = seatRepository.findByConcertIdOrderBySeatRowAscSeatNumberAsc(concertId)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            redisTemplate.opsForValue().set(cacheKey, seats, CacheKeyGenerator.SEAT_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.info("=== 좌석 캐시 저장: {} ===", cacheKey);
        } else {
            log.info("=== 콘서트 좌석 조회 (캐시): {} ===", concertId);
        }
        
        return seats;
    }

    /**
     * 좌석 ID로 조회 (Redis 캐시 적용)
     */
    public SeatDto getSeatById(Long id) {
        String cacheKey = CacheKeyGenerator.getSeatByIdKey(id);
        
        SeatDto seat = (SeatDto) redisTemplate.opsForValue().get(cacheKey);
        if (seat == null) {
            log.info("=== 좌석 조회 (DB): {} ===", id);
            Seat seatEntity = seatRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다: " + id));
            seat = convertToDto(seatEntity);
            
            redisTemplate.opsForValue().set(cacheKey, seat, CacheKeyGenerator.SEAT_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.info("=== 좌석 캐시 저장: {} ===", cacheKey);
        } else {
            log.info("=== 좌석 조회 (캐시): {} ===", id);
        }
        
        return seat;
    }

    /**
     * 콘서트 ID와 좌석등급 ID로 좌석 조회 (Redis 캐시 적용)
     */
    public List<SeatDto> getSeatsByConcertIdAndGradeId(Long concertId, Long seatGradeId) {
        String cacheKey = CacheKeyGenerator.getSeatsByConcertAndGradeKey(concertId, seatGradeId);
        
        List<SeatDto> seats = (List<SeatDto>) redisTemplate.opsForValue().get(cacheKey);
        if (seats == null) {
            log.info("=== 콘서트 좌석등급별 좌석 조회 (DB): {} - {} ===", concertId, seatGradeId);
            seats = seatRepository.findByConcertIdAndSeatGradeIdOrderBySeatRowAscSeatNumberAsc(concertId, seatGradeId)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            redisTemplate.opsForValue().set(cacheKey, seats, CacheKeyGenerator.SEAT_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.info("=== 좌석등급별 캐시 저장: {} ===", cacheKey);
        } else {
            log.info("=== 콘서트 좌석등급별 좌석 조회 (캐시): {} - {} ===", concertId, seatGradeId);
        }
        
        return seats;
    }

    /**
     * 콘서트 ID와 좌석 상태로 조회 (Redis 캐시 적용)
     */
    public List<SeatDto> getSeatsByConcertIdAndStatus(Long concertId, Seat.SeatStatus status) {
        String cacheKey = CacheKeyGenerator.getSeatsByConcertAndStatusKey(concertId, status.toString());
        
        List<SeatDto> seats = (List<SeatDto>) redisTemplate.opsForValue().get(cacheKey);
        if (seats == null) {
            log.info("=== 콘서트 상태별 좌석 조회 (DB): {} - {} ===", concertId, status);
            seats = seatRepository.findByConcertIdAndStatusOrderBySeatRowAscSeatNumberAsc(concertId, status)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
            
            redisTemplate.opsForValue().set(cacheKey, seats, CacheKeyGenerator.SEAT_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
            log.info("=== 상태별 캐시 저장: {} ===", cacheKey);
        } else {
            log.info("=== 콘서트 상태별 좌석 조회 (캐시): {} - {} ===", concertId, status);
        }
        
        return seats;
    }

    /**
     * 좌석 생성 (Redis 캐시 무효화)
     */
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
        
        // 관련 캐시 무효화
        cacheInvalidationUtils.invalidateSeatCaches(seatDto.concertId());
        
        return convertToDto(savedSeat);
    }

    /**
     * 좌석 생성 (Redis 캐시 무효화) - 배열 형태로 여러 좌석 생성
     */
    public List<SeatDto> createSeats(List<SeatDto> seatDtos) {
        log.info("=== 좌석 일괄 생성: {}개 ===", seatDtos.size());
        
        List<Seat> seatsToSave = new ArrayList<>();
        
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
                throw new RuntimeException("이미 존재하는 좌석입니다: " + seatDto.seatRow() + "행 " + seatDto.seatNumber() + "번");
            }
            
            Seat seat = convertToEntity(seatDto);
            seat.setConcert(concert);
            seat.setSeatGrade(seatGrade);
            seatsToSave.add(seat);
        }
        
        List<Seat> savedSeats = seatRepository.saveAll(seatsToSave);
        
        // 관련 캐시 무효화 (첫 번째 좌석의 콘서트 ID 사용)
        if (!seatDtos.isEmpty()) {
            cacheInvalidationUtils.invalidateSeatCaches(seatDtos.get(0).concertId());
        }
        
        return savedSeats.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 좌석 수정 (Redis 캐시 무효화)
     */
    public SeatDto updateSeat(Long id, SeatDto seatDto) {
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
        
        // 관련 캐시 무효화
        cacheInvalidationUtils.invalidateSeatCaches(seatDto.concertId());
        
        return convertToDto(updatedSeat);
    }

    /**
     * 좌석 삭제 (Redis 캐시 무효화)
     */
    public void deleteSeat(Long id) {
        log.info("=== 좌석 삭제: {} ===", id);
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다: " + id));
        
        Long concertId = seat.getConcert().getId();
        seatRepository.deleteById(id);
        
        // 관련 캐시 무효화
        cacheInvalidationUtils.invalidateSeatCaches(concertId);
    }

    /**
     * 콘서트의 모든 좌석 수정 (Redis 캐시 무효화)
     */
    public List<SeatDto> updateSeatsByConcert(Long concertId, List<SeatDto> seatDtos) {
        log.info("=== 콘서트별 좌석 수정: {} - {}개 ===", concertId, seatDtos.size());
        
        // 콘서트 존재 여부 확인
        Concert concert = concertRepository.findById(concertId)
                .orElseThrow(() -> new RuntimeException("콘서트를 찾을 수 없습니다: " + concertId));
        
        // 기존 좌석 삭제
        seatRepository.deleteByConcertId(concertId);
        
        // 새로운 좌석 생성
        List<Seat> seatsToSave = seatDtos.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
        
        List<Seat> savedSeats = seatRepository.saveAll(seatsToSave);
        
        // 관련 캐시 무효화
        cacheInvalidationUtils.invalidateSeatCaches(concertId);
        
        return savedSeats.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 콘서트의 모든 좌석 삭제 (Redis 캐시 무효화)
     * 예약이 있는 좌석은 삭제하지 않음
     */
    public void deleteSeatsByConcert(Long concertId) {
        log.info("=== 콘서트별 좌석 삭제: {} ===", concertId);
        
        // 예약이 있는 좌석 ID 목록 조회
        List<Long> reservedSeatIds = reservationRepository.findReservedSeatIdsByConcertId(concertId);
        
        if (!reservedSeatIds.isEmpty()) {
            log.warn("예약이 있는 좌석이 있어 삭제를 건너뜁니다. 예약된 좌석 수: {}", reservedSeatIds.size());
            throw new RuntimeException("예약이 있는 좌석이 있어 삭제할 수 없습니다. 예약된 좌석 수: " + reservedSeatIds.size());
        }
        
        // 예약이 없는 좌석만 삭제
        seatRepository.deleteByConcertId(concertId);
        
        // 관련 캐시 무효화
        cacheInvalidationUtils.invalidateSeatCaches(concertId);
        
        log.info("=== 콘서트별 좌석 삭제 완료: {} ===", concertId);
    }

    /**
     * 좌석 상태 변경 (Redis 캐시 무효화)
     */
    public SeatDto updateSeatStatus(Long id, Seat.SeatStatus status) {
        log.info("=== 좌석 상태 변경: {} -> {} ===", id, status);
        
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("좌석을 찾을 수 없습니다: " + id));
        
        seat.setStatus(status);
        Seat updatedSeat = seatRepository.save(seat);
        
        // 관련 캐시 무효화
        cacheInvalidationUtils.invalidateSeatCaches(seat.getConcert().getId());
        
        return convertToDto(updatedSeat);
    }

    /**
     * 사용 가능한 좌석 수 조회
     */
    public long getAvailableSeatCount(Long concertId) {
        return seatRepository.countAvailableSeatsByConcertId(concertId);
    }

    /**
     * 콘서트 캐시 무효화
     */
    public void evictConcertCache(Long concertId) {
        log.info("=== 콘서트 좌석 캐시 무효화: {} ===", concertId);
        cacheInvalidationUtils.invalidateSeatCaches(concertId);
    }

    /**
     * 모든 캐시 무효화
     */
    public void clearAllCache() {
        log.info("=== 모든 좌석 캐시 무효화 ===");
        cacheInvalidationUtils.clearAllSeatCaches();
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