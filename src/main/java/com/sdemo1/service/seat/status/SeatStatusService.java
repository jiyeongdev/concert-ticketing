package com.sdemo1.service.seat.status;

import java.util.List;
import com.sdemo1.dto.seat.SeatStatusDto;
import com.sdemo1.entity.Member;
import com.sdemo1.repository.MemberRepository;
import com.sdemo1.service.seat.SeatService;
import com.sdemo1.service.seat.hold.RedisSeatHoldService;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatStatusService {

    private final SeatService seatService;
    private final RedisSeatHoldService redisSeatHoldService;
    private final MemberRepository memberRepository;

    /**
     * Redis 기반 좌석 상태 조회 (공통 로직)
     */
    public List<SeatStatusDto> getSeatStatusFromRedis(Long concertId) {   
        try {
            // DB에서 좌석 정보 조회 (SeatService를 통해)
            List<SeatStatusDto> dbSeats = getSeatStatusFromDatabase(concertId);
            
            // Redis 점유 정보와 조합하여 반환
            return dbSeats.stream()
                .map(seat -> {
                    RedisSeatHoldService.SeatHoldInfo holdInfo = 
                        redisSeatHoldService.getSeatHoldInfo(concertId, seat.getId());
                    String status = redisSeatHoldService.getSeatStatus(concertId, seat.getId());
                    
                    return SeatStatusDto.builder()
                        .id(seat.getId())
                        .concertId(seat.getConcertId())
                        .seatGradeId(seat.getSeatGradeId())
                        .seatRow(seat.getSeatRow())
                        .seatNumber(seat.getSeatNumber())
                        .positionX(seat.getPositionX())
                        .positionY(seat.getPositionY())
                        .status(status)
                        .gradeName(seat.getGradeName())
                        .price(seat.getPrice())
                        .heldBy(holdInfo != null ? getMemberName(holdInfo.getMemberId()) : null)
                        .remainingHoldTime(holdInfo != null ? holdInfo.getRemainingSeconds() : null)
                        .build();
                })
                .toList();
                
        } catch (Exception e) {
            log.error("좌석 상태 조회 실패: concertId={}", concertId, e);
            // 에러 시 DB에서 직접 조회
            return getSeatStatusFromDatabase(concertId);
        }
    }

    /**
     * DB에서 좌석 상태 조회
     */
    private List<SeatStatusDto> getSeatStatusFromDatabase(Long concertId) {
        return seatService.getSeatsByConcertId(concertId).stream()
            .map(seatDto -> SeatStatusDto.builder()
                .id(seatDto.id())
                .concertId(seatDto.concertId())
                .seatGradeId(seatDto.seatGradeId())
                .seatRow(seatDto.seatRow())
                .seatNumber(seatDto.seatNumber())
                .positionX(seatDto.positionX())
                .positionY(seatDto.positionY())
                .status(seatDto.status())
                .gradeName(seatDto.gradeName())
                .price(seatDto.price())
                .heldBy(null)
                .remainingHoldTime(null)
                .build())
            .toList();
    }

    /**
     * 사용자 이름 조회 (공통 로직)
     */
    public String getMemberName(Long memberId) {
        try {
            Member member = memberRepository.findById(memberId).orElse(null);
            return member != null ? member.getName() : "사용자_" + memberId;
        } catch (Exception e) {
            log.error("사용자 이름 조회 실패: memberId={}", memberId, e);
            return "사용자_" + memberId;
        }
    }
} 