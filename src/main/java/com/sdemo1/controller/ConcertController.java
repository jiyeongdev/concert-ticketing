package com.sdemo1.controller;

import java.math.BigInteger;
import java.util.List;
import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.config.SwaggerExamples;
import com.sdemo1.dto.ConcertDto;
import com.sdemo1.service.ConcertCacheService;
import com.sdemo1.service.ConcertService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/concerts")
@RequiredArgsConstructor
@Tag(name = "5. 콘서트 관리", description = "콘서트 조회, 검색, 관리 API")
public class ConcertController {

    private final ConcertService concertService;
    private final ConcertCacheService concertCacheService;

    /**
     * 모든 콘서트 조회 (인증 필요)
     */
    @GetMapping("/list")
    @Operation(summary = "콘서트 목록 조회", description = "모든 콘서트 정보를 조회합니다 (인증 필요)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "콘서트 목록 조회 성공",
            content = @Content(examples = @ExampleObject(value = SwaggerExamples.CONCERT_LIST_RESPONSE)))
    })
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
     * 콘서트 제목으로 검색 (인증 필요)
     */
    @GetMapping("/search")
    @Operation(summary = "콘서트 검색", description = "제목으로 콘서트를 검색합니다 (인증 필요)")
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
    @PostMapping("/admin")
    @Operation(summary = "콘서트 생성", description = "새로운 콘서트를 등록합니다 (ADMIN 권한 필요)",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(examples = @ExampleObject(value = SwaggerExamples.CONCERT_CREATE_REQUEST))))
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "콘서트 생성 성공",
            content = @Content(examples = @ExampleObject(value = SwaggerExamples.CONCERT_CREATE_RESPONSE))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN 권한 필요",
            content = @Content(examples = @ExampleObject(value = SwaggerExamples.ERROR_FORBIDDEN)))
    })
    public ResponseEntity<ApiResponse<?>> createConcert(@Valid @RequestBody ConcertDto concertDto) {
        try {
            log.info("=== 콘서트 생성 API 호출 ===");
            ConcertDto createdConcert = concertService.createConcert(concertDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("콘서트 생성 성공", createdConcert, HttpStatus.CREATED));
        } catch (Exception e) {
            log.error("콘서트 생성 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("콘서트 생성 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 콘서트 수정 (ADMIN만 접근 가능)
     */
    @PutMapping("/admin/{id}")
    @Operation(summary = "콘서트 수정", description = "기존 콘서트 정보를 수정합니다 (ADMIN 권한 필요)")
    public ResponseEntity<ApiResponse<?>> updateConcert(@PathVariable("id") BigInteger id, @Valid @RequestBody ConcertDto concertDto) {
        try {
            log.info("=== 콘서트 수정 API 호출: {} ===", id);
            
            ConcertDto updatedConcert = concertService.updateConcert(id, concertDto);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("콘서트 수정 성공", updatedConcert, HttpStatus.OK));
        } catch (Exception e) {
            log.error("콘서트 수정 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("콘서트 수정 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 콘서트 삭제 (ADMIN만 접근 가능)
     */
    @DeleteMapping("/admin/{id}")
    @Operation(summary = "콘서트 삭제", description = "콘서트를 삭제합니다 (ADMIN 권한 필요)")
    public ResponseEntity<ApiResponse<?>> deleteConcert(@PathVariable("id") BigInteger id) {
        try {
            log.info("=== 콘서트 삭제 API 호출: {} ===", id);
            
            concertService.deleteConcert(id);
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("콘서트 삭제 성공", null, HttpStatus.OK));
        } catch (Exception e) {
            log.error("콘서트 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("콘서트 삭제 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 캐시 무효화 (ADMIN만 접근 가능)
     */
    @PostMapping("/admin/cache/clear")
    @Operation(summary = "캐시 무효화", description = "콘서트 관련 캐시를 모두 삭제합니다 (ADMIN 권한 필요)")
    public ResponseEntity<ApiResponse<?>> clearCache() {
        try {
            log.info("=== 콘서트 캐시 무효화 API 호출 ===");
            concertCacheService.evictAllConcertCache();
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("캐시 무효화 성공", null, HttpStatus.OK));
        } catch (Exception e) {
            log.error("캐시 무효화 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("캐시 무효화 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }


} 