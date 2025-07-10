package com.sdemo1.controller.seat;

import java.util.List;
import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.dto.seat.SeatDto;
import com.sdemo1.service.seat.SeatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
@Tag(name = "4. 좌석 관리", description = "좌석 조회, 생성, 수정, 삭제 API")
public class SeatController {

    private final SeatService seatService;

    /**
     * 콘서트의 모든 좌석 조회 (인증 필요)
     */
    @GetMapping("/concert/{concertId}")
    @Operation(summary = "콘서트별 좌석 조회", description = "특정 콘서트의 모든 좌석 정보를 조회합니다 (인증 필요)")
    public ResponseEntity<ApiResponse<?>> getSeatsByConcertId(@PathVariable("concertId") Long concertId) {
        try {
            log.info("=== 콘서트 좌석 조회 API 호출: {} ===", concertId);
            List<SeatDto> seats = seatService.getSeatsByConcertId(concertId);
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("좌석 목록 조회 성공", seats, HttpStatus.OK));
        } catch (Exception e) {
            log.error("좌석 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("좌석 목록 조회 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 콘서트별 좌석 일괄 생성 (ADMIN만 접근 가능)
     */
    @PostMapping("/admin/{concertId}")
    @Operation(summary = "콘서트별 좌석 일괄 생성", description = "특정 콘서트의 좌석들을 일괄 생성합니다 (ADMIN 권한 필요)")
    public ResponseEntity<ApiResponse<?>> createSeatsByConcert(@PathVariable("concertId") Long concertId, @Valid @RequestBody List<SeatDto> seatDtos) {
        try {
            log.info("=== 콘서트별 좌석 일괄 생성 API 호출: {} ===", concertId);
            
            // 모든 좌석의 콘서트 ID를 설정
            seatDtos.forEach(seatDto -> {
                if (seatDto.concertId() == null || !seatDto.concertId().equals(concertId)) {
                    throw new RuntimeException("모든 좌석의 콘서트 ID가 일치해야 합니다: " + concertId);
                }
            });
            
            List<SeatDto> createdSeats = seatService.createSeats(seatDtos);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("콘서트별 좌석 일괄 생성 성공", createdSeats, HttpStatus.OK));
        } catch (Exception e) {
            log.error("콘서트별 좌석 일괄 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("콘서트별 좌석 일괄 생성 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 좌석 수정 (ADMIN만 접근 가능)
     */
    @PutMapping("/admin/{seatId}")
    @Operation(summary = "좌석 수정", description = "특정 좌석 정보를 수정합니다 (ADMIN 권한 필요)")
    public ResponseEntity<ApiResponse<?>> updateSeat(@PathVariable("seatId") Long seatId,
                                                   @Valid @RequestBody SeatDto seatDto) {
        try {
            SeatDto updatedSeat = seatService.updateSeat(seatId, seatDto);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("좌석 수정 성공", updatedSeat, HttpStatus.OK));
        } catch (Exception e) {
            log.error("좌석 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("좌석 수정 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 좌석 삭제 (ADMIN만 접근 가능)
     */
    @DeleteMapping("/admin/{seatId}")
    @Operation(summary = "좌석 삭제", description = "특정 좌석을 삭제합니다 (ADMIN 권한 필요)")
    public ResponseEntity<ApiResponse<?>> deleteSeat(@PathVariable("seatId") Long seatId) {
        try {
            log.info("=== 좌석 삭제 API 호출: {} ===", seatId);
            
            seatService.deleteSeat(seatId);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("좌석 삭제 성공", null, HttpStatus.OK));
        } catch (Exception e) {
            log.error("좌석 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("좌석 삭제 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
} 