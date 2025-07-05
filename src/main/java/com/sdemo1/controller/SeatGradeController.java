package com.sdemo1.controller;

import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.dto.SeatGradeDto;
import com.sdemo1.service.SeatGradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigInteger;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ck/seat-grades")
@RequiredArgsConstructor
public class SeatGradeController {

    private final SeatGradeService seatGradeService;

    /**
     * 콘서트의 모든 좌석등급 조회 (모든 사용자 접근 가능)
     */
    @GetMapping("/concert/{concertId}")
    public ResponseEntity<ApiResponse<?>> getSeatGradesByConcertId(@PathVariable BigInteger concertId) {
        try {
            log.info("=== 콘서트 좌석등급 조회 API 호출: {} ===", concertId);
            List<SeatGradeDto> seatGrades = seatGradeService.getSeatGradesByConcertId(concertId);
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("좌석등급 목록 조회 성공", seatGrades, HttpStatus.OK));
        } catch (Exception e) {
            log.error("좌석등급 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("좌석등급 목록 조회 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 좌석등급 ID로 조회 (모든 사용자 접근 가능)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getSeatGradeById(@PathVariable BigInteger id) {
        try {
            log.info("=== 좌석등급 조회 API 호출: {} ===", id);
            SeatGradeDto seatGrade = seatGradeService.getSeatGradeById(id);
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("좌석등급 조회 성공", seatGrade, HttpStatus.OK));
        } catch (Exception e) {
            log.error("좌석등급 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("좌석등급 조회 실패: " + e.getMessage(), null, HttpStatus.NOT_FOUND));
        }
    }

    /**
     * 좌석등급 생성 (ADMIN만 접근 가능)
     */
    @PostMapping
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
    public ResponseEntity<ApiResponse<?>> updateSeatGrade(@PathVariable BigInteger id, @Valid @RequestBody SeatGradeDto seatGradeDto) {
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
    public ResponseEntity<ApiResponse<?>> deleteSeatGrade(@PathVariable BigInteger id) {
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
     * 캐시 무효화 (ADMIN만 접근 가능)
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<ApiResponse<?>> clearCache() {
        try {
            checkAdminRole();
            log.info("=== 좌석등급 캐시 무효화 API 호출 ===");
            seatGradeService.clearCache();
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("캐시 무효화 성공", null, HttpStatus.OK));
        } catch (AccessDeniedException e) {
            log.error("권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("ADMIN 권한이 필요합니다.", null, HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("캐시 무효화 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("캐시 무효화 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
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