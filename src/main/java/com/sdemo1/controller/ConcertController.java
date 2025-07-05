package com.sdemo1.controller;

import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.dto.ConcertDto;
import com.sdemo1.entity.Member;
import com.sdemo1.service.ConcertService;
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
@RequestMapping("/ck/concerts")
@RequiredArgsConstructor
public class ConcertController {

    private final ConcertService concertService;

    /**
     * 모든 콘서트 조회 (모든 사용자 접근 가능)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllConcerts() {
        try {
            log.info("=== 모든 콘서트 조회 API 호출 ===");
            List<ConcertDto> concerts = concertService.getAllConcerts();
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("콘서트 목록 조회 성공", concerts, HttpStatus.OK));
        } catch (Exception e) {
            log.error("콘서트 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("콘서트 목록 조회 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 콘서트 제목으로 검색 (모든 사용자 접근 가능)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<?>> searchConcertsByTitle(@RequestParam String title) {
        try {
            log.info("=== 콘서트 검색 API 호출: {} ===", title);
            List<ConcertDto> concerts = concertService.searchConcertsByTitle(title);
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("콘서트 검색 성공", concerts, HttpStatus.OK));
        } catch (Exception e) {
            log.error("콘서트 검색 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("콘서트 검색 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 콘서트 생성 (ADMIN만 접근 가능)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createConcert(@Valid @RequestBody ConcertDto concertDto) {
        try {
            checkAdminRole();
            log.info("=== 콘서트 생성 API 호출 ===");
            ConcertDto createdConcert = concertService.createConcert(concertDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("콘서트 생성 성공", createdConcert, HttpStatus.CREATED));
        } catch (AccessDeniedException e) {
            log.error("권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("ADMIN 권한이 필요합니다.", null, HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("콘서트 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("콘서트 생성 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 콘서트 수정 (ADMIN만 접근 가능)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateConcert(@PathVariable BigInteger id, @Valid @RequestBody ConcertDto concertDto) {
        try {
            checkAdminRole();
            log.info("=== 콘서트 수정 API 호출: {} ===", id);
            ConcertDto updatedConcert = concertService.updateConcert(id, concertDto);
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("콘서트 수정 성공", updatedConcert, HttpStatus.OK));
        } catch (AccessDeniedException e) {
            log.error("권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("ADMIN 권한이 필요합니다.", null, HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("콘서트 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("콘서트 수정 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 콘서트 삭제 (ADMIN만 접근 가능)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteConcert(@PathVariable BigInteger id) {
        try {
            checkAdminRole();
            log.info("=== 콘서트 삭제 API 호출: {} ===", id);
            concertService.deleteConcert(id);
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("콘서트 삭제 성공", null, HttpStatus.OK));
        } catch (AccessDeniedException e) {
            log.error("권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>("ADMIN 권한이 필요합니다.", null, HttpStatus.FORBIDDEN));
        } catch (Exception e) {
            log.error("콘서트 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("콘서트 삭제 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 캐시 무효화 (ADMIN만 접근 가능)
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<ApiResponse<?>> clearCache() {
        try {
            checkAdminRole();
            log.info("=== 콘서트 캐시 무효화 API 호출 ===");
            concertService.clearCache();
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