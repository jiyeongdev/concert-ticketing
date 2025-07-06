package com.sdemo1.service.impl;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.sdemo1.dto.SeatStatusDto;
import com.sdemo1.entity.Member;
import com.sdemo1.entity.Seat;
import com.sdemo1.entity.SeatHold;
import com.sdemo1.repository.MemberRepository;
import com.sdemo1.repository.SeatHoldRepository;
import com.sdemo1.repository.SeatRepository;
import com.sdemo1.request.HoldSeatRequest;
import com.sdemo1.service.SeatReservationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SeatReservationServiceImpl implements SeatReservationService {

    private final SeatRepository seatRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final MemberRepository memberRepository;

    // 좌석 점유 설정
    private static final int HOLD_DURATION_MINUTES = 10; // 10분 점유

    @Override
    @Transactional(readOnly = true)
    public List<SeatStatusDto> getSeatStatusByConcertId(BigInteger concertId) {
        log.info("콘서트 좌석 상태 조회: concertId={}", concertId);

        // 모든 좌석 조회
        List<Seat> seats = seatRepository.findByConcertId(concertId);
        
        // 현재 시간
        LocalDateTime now = LocalDateTime.now();
        
        // 점유 중인 좌석 정보 조회
        List<SeatHold> activeHolds = seatHoldRepository.findByConcertIdAndNotExpired(concertId, now);

        return seats.stream()
                .map(seat -> convertToSeatStatusDto(seat, activeHolds, now))
                .collect(Collectors.toList());
    }

    @Override
    public SeatStatusDto holdSeat(BigInteger memberId, HoldSeatRequest request) {
        log.info("좌석 점유 요청: memberId={}, seatId={}", memberId, request.getSeatId());

        // 사용자 존재 확인
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 좌석 존재 확인
        Seat seat = seatRepository.findById(request.getSeatId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다."));

        // 좌석이 예매 가능한 상태인지 확인
        if (seat.getStatus() != Seat.SeatStatus.AVAILABLE) {
            throw new IllegalStateException("이미 예매되었거나 점유된 좌석입니다.");
        }

        // 현재 시간
        LocalDateTime now = LocalDateTime.now();

        // 이미 점유 중인지 확인
        Optional<SeatHold> existingHold = seatHoldRepository.findBySeatIdAndNotExpired(request.getSeatId(), now);
        if (existingHold.isPresent()) {
            throw new IllegalStateException("이미 다른 사용자가 점유한 좌석입니다.");
        }

        // 사용자가 이미 다른 좌석을 점유하고 있는지 확인
        List<SeatHold> userHolds = seatHoldRepository.findByUserIdAndNotExpired(memberId, now);
        if (!userHolds.isEmpty()) {
            throw new IllegalStateException("이미 다른 좌석을 점유하고 있습니다. 먼저 기존 좌석을 해제해주세요.");
        }

        // 좌석 점유 생성
        SeatHold seatHold = new SeatHold();
        seatHold.setUser(member);
        seatHold.setSeat(seat);
        seatHold.setHoldExpireAt(now.plusMinutes(HOLD_DURATION_MINUTES));

        SeatHold savedHold = seatHoldRepository.save(seatHold);

        // 좌석 상태를 HELD로 변경
        seat.setStatus(Seat.SeatStatus.HELD);
        seatRepository.save(seat);

        log.info("좌석 점유 완료: memberId={}, seatId={}, expireAt={}", memberId, request.getSeatId(), savedHold.getHoldExpireAt());

        return convertToSeatStatusDto(seat, List.of(savedHold), now);
    }

    @Override
    public void releaseSeat(BigInteger memberId, BigInteger seatId) {
        log.info("좌석 점유 해제: memberId={}, seatId={}", memberId, seatId);

        // 점유 정보 조회
        Optional<SeatHold> seatHoldOpt = seatHoldRepository.findBySeatIdAndNotExpired(seatId, LocalDateTime.now());
        
        if (seatHoldOpt.isEmpty()) {
            log.warn("점유 중인 좌석이 아닙니다: seatId={}", seatId);
            return;
        }

        SeatHold seatHold = seatHoldOpt.get();
        
        // 점유한 사용자가 맞는지 확인
        if (!seatHold.getUser().getMemberId().equals(memberId)) {
            throw new IllegalStateException("본인이 점유한 좌석만 해제할 수 있습니다.");
        }

        // 점유 정보 삭제
        seatHoldRepository.delete(seatHold);

        // 좌석 상태를 AVAILABLE로 변경
        Seat seat = seatHold.getSeat();
        seat.setStatus(Seat.SeatStatus.AVAILABLE);
        seatRepository.save(seat);

        log.info("좌석 점유 해제 완료: memberId={}, seatId={}", memberId, seatId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatStatusDto> getUserHeldSeats(BigInteger memberId) {
        log.info("사용자 점유 좌석 조회: memberId={}", memberId);

        LocalDateTime now = LocalDateTime.now();
        List<SeatHold> userHolds = seatHoldRepository.findByUserIdAndNotExpired(memberId, now);

        return userHolds.stream()
                .map(hold -> convertToSeatStatusDto(hold.getSeat(), userHolds, now))
                .collect(Collectors.toList());
    }

    @Override
    public void cleanupExpiredHolds() {
        log.info("만료된 좌석 점유 정리 시작");

        LocalDateTime now = LocalDateTime.now();
        List<SeatHold> expiredHolds = seatHoldRepository.findExpiredHolds(now);

        for (SeatHold hold : expiredHolds) {
            // 좌석 상태를 AVAILABLE로 변경
            Seat seat = hold.getSeat();
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            seatRepository.save(seat);

            // 점유 정보 삭제
            seatHoldRepository.delete(hold);
        }

        log.info("만료된 좌석 점유 정리 완료: {}개 좌석 해제", expiredHolds.size());
    }

    @Override
    public boolean confirmReservation(BigInteger memberId, BigInteger seatId) {
        log.info("좌석 예매 확정: memberId={}, seatId={}", memberId, seatId);

        // 점유 정보 조회
        Optional<SeatHold> seatHoldOpt = seatHoldRepository.findBySeatIdAndNotExpired(seatId, LocalDateTime.now());
        
        if (seatHoldOpt.isEmpty()) {
            throw new IllegalStateException("점유 중인 좌석이 아닙니다.");
        }

        SeatHold seatHold = seatHoldOpt.get();
        
        // 점유한 사용자가 맞는지 확인
        if (!seatHold.getUser().getMemberId().equals(memberId)) {
            throw new IllegalStateException("본인이 점유한 좌석만 예매할 수 있습니다.");
        }

        // 좌석 상태를 BOOKED로 변경
        Seat seat = seatHold.getSeat();
        seat.setStatus(Seat.SeatStatus.BOOKED);
        seatRepository.save(seat);

        // 점유 정보 삭제
        seatHoldRepository.delete(seatHold);

        log.info("좌석 예매 확정 완료: memberId={}, seatId={}", memberId, seatId);
        return true;
    }

    /**
     * Seat 엔티티를 SeatStatusDto로 변환
     */
    private SeatStatusDto convertToSeatStatusDto(Seat seat, List<SeatHold> activeHolds, LocalDateTime now) {
        // 해당 좌석의 점유 정보 찾기
        Optional<SeatHold> seatHold = activeHolds.stream()
                .filter(hold -> hold.getSeat().getId().equals(seat.getId()))
                .findFirst();

        String heldBy = null;
        Long remainingHoldTime = null;

        if (seatHold.isPresent()) {
            SeatHold hold = seatHold.get();
            heldBy = hold.getUser().getName();
            
            // 남은 점유 시간 계산 (초 단위)
            long remainingSeconds = java.time.Duration.between(now, hold.getHoldExpireAt()).getSeconds();
            remainingHoldTime = Math.max(0, remainingSeconds);
        }

        return SeatStatusDto.builder()
                .id(seat.getId())
                .concertId(seat.getConcert().getId())
                .seatGradeId(seat.getSeatGrade() != null ? seat.getSeatGrade().getId() : null)
                .seatRow(seat.getSeatRow())
                .seatNumber(seat.getSeatNumber())
                .positionX(seat.getPositionX())
                .positionY(seat.getPositionY())
                .status(seat.getStatus().name())
                .gradeName(seat.getSeatGrade() != null ? seat.getSeatGrade().getGradeName() : null)
                .price(seat.getSeatGrade() != null ? seat.getSeatGrade().getPrice() : null)
                .heldBy(heldBy)
                .remainingHoldTime(remainingHoldTime)
                .build();
    }
} 