package com.sdemo1.controller.seat;

import java.math.BigInteger;
import java.util.List;
import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.dto.seat.SeatGradeDto;
import com.sdemo1.service.seat.SeatGradeService;
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
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/seat-grades")
@RequiredArgsConstructor
@Tag(name = "8. 좌석 등급 관리", description = "좌석 등급 조회, 생성, 수정, 삭제 API")
public class SeatGradeController {

    private final SeatGradeService seatGradeService;

    /**
     * 콘서트별 좌석등급 조회 (인증 필요)
     */
    @GetMapping("/concert/{concertId}")
    @Operation(summary = "콘서트별 좌석등급 조회", description = "특정 콘서트의 모든 좌석등급 정보를 조회합니다 (인증 필요)")
    public ResponseEntity<ApiResponse<?>> getSeatGradesByConcertId(@PathVariable("concertId") BigInteger concertId) {
        try {
            log.info("=== 콘서트별 좌석등급 조회 API 호출: {} ===", concertId);
            
            List<SeatGradeDto> seatGrades = seatGradeService.getSeatGradesByConcertId(concertId);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("콘서트별 좌석등급 조회 성공", seatGrades, HttpStatus.OK));
        } catch (Exception e) {
            log.error("콘서트별 좌석등급 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("콘서트별 좌석등급 조회 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 좌석등급 상세 조회 (인증 필요)
     */
    @GetMapping("/{id}")
    @Operation(summary = "좌석등급 상세 조회", description = "특정 좌석등급의 상세 정보를 조회합니다 (인증 필요)")
    public ResponseEntity<ApiResponse<?>> getSeatGradeById(@PathVariable("id") BigInteger id) {
        try {
            log.info("=== 좌석등급 상세 조회 API 호출: {} ===", id);
            
            SeatGradeDto seatGrade = seatGradeService.getSeatGradeById(id);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("좌석등급 상세 조회 성공", seatGrade, HttpStatus.OK));
        } catch (Exception e) {
            log.error("좌석등급 상세 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("좌석등급 상세 조회 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 좌석등급 생성 (ADMIN만 접근 가능)
     */
    @PostMapping
    @Operation(summary = "좌석등급 생성", description = "새로운 좌석등급을 생성합니다 (ADMIN 권한 필요)")
    public ResponseEntity<ApiResponse<?>> createSeatGrade(@Valid @RequestBody SeatGradeDto seatGradeDto) {
        try {
            checkAdminRole();
            log.info("=== 좌석등급 생성 API 호출 ===");
            SeatGradeDto createdSeatGrade = seatGradeService.createSeatGrade(seatGradeDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("좌석등급 생성 성공", createdSeatGrade, HttpStatus.CREATED));
        } catch (AccessDeniedException e) {
            log.error("권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("ADMIN 권한이 필요합니다.", null, HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("좌석등급 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("좌석등급 생성 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 좌석등급 수정 (ADMIN만 접근 가능)
     */
    @PutMapping("/{id}")
    @Operation(summary = "좌석등급 수정", description = "특정 좌석등급 정보를 수정합니다 (ADMIN 권한 필요)")
    public ResponseEntity<ApiResponse<?>> updateSeatGrade(@PathVariable("id") BigInteger id, @Valid @RequestBody SeatGradeDto seatGradeDto) {
        try {
            checkAdminRole();
            log.info("=== 좌석등급 수정 API 호출: {} ===", id);
            
            SeatGradeDto updatedSeatGrade = seatGradeService.updateSeatGrade(id, seatGradeDto);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("좌석등급 수정 성공", updatedSeatGrade, HttpStatus.OK));
        } catch (AccessDeniedException e) {
            log.error("권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("ADMIN 권한이 필요합니다.", null, HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("좌석등급 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("좌석등급 수정 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 좌석등급 삭제 (ADMIN만 접근 가능)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "좌석등급 삭제", description = "특정 좌석등급을 삭제합니다 (ADMIN 권한 필요)")
    public ResponseEntity<ApiResponse<?>> deleteSeatGrade(@PathVariable("id") BigInteger id) {
        try {
            checkAdminRole();
            log.info("=== 좌석등급 삭제 API 호출: {} ===", id);
            
            seatGradeService.deleteSeatGrade(id);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("좌석등급 삭제 성공", null, HttpStatus.OK));
        } catch (AccessDeniedException e) {
            log.error("권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("ADMIN 권한이 필요합니다.", null, HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("좌석등급 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("좌석등급 삭제 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
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