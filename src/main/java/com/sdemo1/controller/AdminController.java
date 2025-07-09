package com.sdemo1.controller;

import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/admin")
@Tag(name = "9. 관리자 전용", description = "시스템 관리 및 캐시 관리 API (ADMIN 권한 필요)")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 카테고리 캐시 상태 확인 (ADMIN만 접근 가능)
     */
    @GetMapping("/cache/status")
    @Operation(summary = "캐시 상태 조회", description = "카테고리 캐시 상태를 확인합니다 (ADMIN 권한 필요)")
    public ApiResponse<String> getCacheStatus() {
        String status = categoryService.getCacheStatus();
        return new ApiResponse<>("캐시 상태 조회 완료", status, HttpStatus.OK);
    }

    /**
     * 카테고리 캐시 수동 갱신 (ADMIN만 접근 가능)
     */
    @PostMapping("/cache/refresh")
    @Operation(summary = "캐시 수동 갱신", description = "카테고리 캐시를 수동으로 갱신합니다 (ADMIN 권한 필요)")
    public ApiResponse<String> refreshCache() {
        try {
            categoryService.refreshCategoryMap();
            return new ApiResponse<>("캐시가 성공적으로 갱신되었습니다.", null, HttpStatus.OK);
        } catch (Exception e) {
            return new ApiResponse<>("캐시 갱신 중 오류 발생: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
} 