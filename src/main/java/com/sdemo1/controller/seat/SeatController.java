package com.sdemo1.controller.seat;

import java.math.BigInteger;
import java.util.List;
import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.dto.seat.SeatDto;
import com.sdemo1.entity.Seat;
import com.sdemo1.service.seat.SeatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    /**
     * 콘서트의 모든 좌석 조회 (모든 사용자 접근 가능)
     */
    @GetMapping("/concert/{concertId}")
    public ResponseEntity<ApiResponse<?>> getSeatsByConcertId(@PathVariable("concertId") BigInteger concertId) {
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
     * 콘서트 ID와 좌석등급 ID로 좌석 조회 (모든 사용자 접근 가능)
     */
    @GetMapping("/concert/{concertId}/grade/{seatGradeId}")
    public ResponseEntity<ApiResponse<?>> getSeatsByConcertIdAndGradeId(@PathVariable("concertId") BigInteger concertId, @PathVariable("seatGradeId") BigInteger seatGradeId) {
        try {
            log.info("=== 콘서트 좌석등급별 좌석 조회 API 호출: {} - {} ===", concertId, seatGradeId);
            List<SeatDto> seats = seatService.getSeatsByConcertIdAndGradeId(concertId, seatGradeId);
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("좌석등급별 좌석 목록 조회 성공", seats, HttpStatus.OK));
        } catch (Exception e) {
            log.error("좌석등급별 좌석 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("좌석등급별 좌석 목록 조회 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 콘서트 ID와 좌석 상태로 조회 (모든 사용자 접근 가능)
     */
    @GetMapping("/concert/{concertId}/status/{status}")
    public ResponseEntity<ApiResponse<?>> getSeatsByConcertIdAndStatus(@PathVariable("concertId") BigInteger concertId, @PathVariable("status") String status) {
        try {
            log.info("=== 콘서트 상태별 좌석 조회 API 호출: {} - {} ===", concertId, status);
            Seat.SeatStatus seatStatus = Seat.SeatStatus.valueOf(status.toUpperCase());
            List<SeatDto> seats = seatService.getSeatsByConcertIdAndStatus(concertId, seatStatus);
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("상태별 좌석 목록 조회 성공", seats, HttpStatus.OK));
        } catch (IllegalArgumentException e) {
            log.error("잘못된 좌석 상태: {}", status);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("잘못된 좌석 상태입니다: " + status, null, HttpStatus.BAD_REQUEST));
        } catch (Exception e) {
            log.error("상태별 좌석 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("상태별 좌석 목록 조회 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 콘서트의 사용 가능한 좌석 수 조회 (모든 사용자 접근 가능)
     */
    @GetMapping("/concert/{concertId}/available-count")
    public ResponseEntity<ApiResponse<?>> getAvailableSeatCount(@PathVariable("concertId") BigInteger concertId) {
        try {
            log.info("=== 콘서트 사용 가능한 좌석 수 조회 API 호출: {} ===", concertId);
            long availableCount = seatService.getAvailableSeatCount(concertId);
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("사용 가능한 좌석 수 조회 성공", availableCount, HttpStatus.OK));
        } catch (Exception e) {
            log.error("사용 가능한 좌석 수 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("사용 가능한 좌석 수 조회 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 좌석 ID로 조회 (모든 사용자 접근 가능)
     */
    @GetMapping("/{seatId}")
    public ResponseEntity<ApiResponse<?>> getSeatById(@PathVariable("seatId") BigInteger seatId) {
        try {
            log.info("=== 좌석 조회 API 호출: {} ===", seatId);
            SeatDto seat = seatService.getSeatById(seatId);
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("좌석 조회 성공", seat, HttpStatus.OK));
        } catch (Exception e) {
            log.error("좌석 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("좌석 조회 실패: " + e.getMessage(), null, HttpStatus.NOT_FOUND));
        }
    }

    /**
     * 좌석 생성 (ADMIN만 접근 가능) - 배열 형태로 1개 이상 생성 가능
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createSeats(@Valid @RequestBody List<SeatDto> seatDtos) {
        try {
            checkAdminRole();
            log.info("=== 좌석 생성 API 호출: {}개 ===", seatDtos.size());
            List<SeatDto> createdSeats = seatService.createSeats(seatDtos);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("좌석 생성 성공", createdSeats, HttpStatus.CREATED));
        } catch (AccessDeniedException e) {
            log.error("권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("ADMIN 권한이 필요합니다.", null, HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("좌석 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("좌석 생성 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 콘서트별 좌석 일괄 생성 (ADMIN만 접근 가능)
     */
    @PostMapping("/concert/{concertId}")
    public ResponseEntity<ApiResponse<?>> createSeatsByConcert(@PathVariable("concertId") BigInteger concertId, @Valid @RequestBody List<SeatDto> seatDtos) {
        try {
            checkAdminRole();
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
        } catch (AccessDeniedException e) {
            log.error("권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("ADMIN 권한이 필요합니다.", null, HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("콘서트별 좌석 일괄 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("콘서트별 좌석 일괄 생성 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 좌석 수정 (ADMIN만 접근 가능)
     */
    @PutMapping("/{seatId}")
    public ResponseEntity<ApiResponse<?>> updateSeat(@PathVariable("seatId") BigInteger seatId,
                                                   @Valid @RequestBody SeatDto seatDto) {
        try {
            checkAdminRole();
            log.info("=== 좌석 수정 API 호출: {} ===", seatId);
            
            SeatDto updatedSeat = seatService.updateSeat(seatId, seatDto);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("좌석 수정 성공", updatedSeat, HttpStatus.OK));
        } catch (AccessDeniedException e) {
            log.error("권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("ADMIN 권한이 필요합니다.", null, HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("좌석 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("좌석 수정 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 콘서트별 좌석 일괄 수정 (ADMIN만 접근 가능)
     */
    @PutMapping("/concert/{concertId}")
    public ResponseEntity<ApiResponse<?>> updateSeatsByConcert(@PathVariable("concertId") BigInteger concertId, @Valid @RequestBody List<SeatDto> seatDtos) {
        try {
            checkAdminRole();
            log.info("=== 콘서트별 좌석 일괄 수정 API 호출: {} ===", concertId);
            
            List<SeatDto> updatedSeats = seatService.updateSeatsByConcert(concertId, seatDtos);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("콘서트별 좌석 일괄 수정 성공", updatedSeats, HttpStatus.OK));
        } catch (AccessDeniedException e) {
            log.error("권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("ADMIN 권한이 필요합니다.", null, HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("콘서트별 좌석 일괄 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("콘서트별 좌석 일괄 수정 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 좌석 삭제 (ADMIN만 접근 가능)
     */
    @DeleteMapping("/{seatId}")
    public ResponseEntity<ApiResponse<?>> deleteSeat(@PathVariable("seatId") BigInteger seatId) {
        try {
            checkAdminRole();
            log.info("=== 좌석 삭제 API 호출: {} ===", seatId);
            
            seatService.deleteSeat(seatId);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("좌석 삭제 성공", null, HttpStatus.OK));
        } catch (AccessDeniedException e) {
            log.error("권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("ADMIN 권한이 필요합니다.", null, HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("좌석 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("좌석 삭제 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 콘서트별 좌석 일괄 삭제 (ADMIN만 접근 가능)
     */
    @DeleteMapping("/concert/{concertId}")
    public ResponseEntity<ApiResponse<?>> deleteSeatsByConcert(@PathVariable("concertId") BigInteger concertId) {
        try {
            checkAdminRole();
            log.info("=== 콘서트별 좌석 일괄 삭제 API 호출: {} ===", concertId);
            
            seatService.deleteSeatsByConcert(concertId);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("콘서트별 좌석 일괄 삭제 성공", null, HttpStatus.OK));
        } catch (AccessDeniedException e) {
            log.error("권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("ADMIN 권한이 필요합니다.", null, HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("콘서트별 좌석 일괄 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("콘서트별 좌석 일괄 삭제 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 좌석 상태 수정 (ADMIN만 접근 가능)
     */
    @PutMapping("/{seatId}/status")
    public ResponseEntity<ApiResponse<?>> updateSeatStatus(@PathVariable("seatId") BigInteger seatId,
                                                          @RequestParam("status") String status) {
        try {
            checkAdminRole();
            log.info("=== 좌석 상태 수정 API 호출: seatId={}, status={} ===", seatId, status);
            
            Seat.SeatStatus seatStatus = Seat.SeatStatus.valueOf(status.toUpperCase());
            SeatDto updatedSeat = seatService.updateSeatStatus(seatId, seatStatus);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("좌석 상태 수정 성공", updatedSeat, HttpStatus.OK));
        } catch (AccessDeniedException e) {
            log.error("권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("ADMIN 권한이 필요합니다.", null, HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("좌석 상태 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("좌석 상태 수정 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * ADMIN 권한 확인
     */
    private void checkAdminRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ADMIN"));
        
        if (!isAdmin) {
            throw new AccessDeniedException("ADMIN 권한이 필요합니다.");
        }
    }
} 