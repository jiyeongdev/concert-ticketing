package com.sdemo1.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.sdemo1.common.response.ApiResponse;
import com.sdemo1.entity.Member;
import com.sdemo1.response.MemberResponse;
import com.sdemo1.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Tag(name = "2. 회원 관리", description = "회원 정보 조회, 프로필 수정 API")
public class MemberController {

    private final MemberService memberService;

    /**
     * 현재 로그인한 사용자 정보 조회
     */
    @GetMapping("/profile")
    @Operation(summary = "회원 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberProfile() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String memberIdStr = auth.getName();
            Long memberId = Long.parseLong(memberIdStr);
            
            Optional<Member> memberOptional = memberService.findOneById(memberId);
            if (memberOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>("사용자를 찾을 수 없습니다.", null, HttpStatus.NOT_FOUND));
            }
            
            Member member = memberOptional.get();
            
            MemberResponse response = MemberResponse.builder()
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .name(member.getName())
                .phone(member.getPhone())
                .role(member.getRole().name())
                .build();
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("사용자 정보 조회 성공", response, HttpStatus.OK));
        } catch (Exception e) {
            log.error("회원 정보 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("회원 정보 조회 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    /**
     * 모든 회원 조회 (ADMIN만 접근 가능)
     */
    @GetMapping("/admin/all")
    @Operation(summary = "전체 회원 조회", description = "모든 회원의 정보를 조회합니다 (관리자 전용)")

    public ResponseEntity<ApiResponse<List<MemberResponse>>> getAllMembers() {
        try {
            log.info("=== 전체 회원 조회 API 호출 ===");
            
            List<Member> members = memberService.findMembers();
            
            List<MemberResponse> memberResponses = members.stream()
                    .<MemberResponse>map(member -> MemberResponse.builder()
                        .memberId(member.getMemberId())
                        .email(member.getEmail())
                        .name(member.getName())
                        .phone(member.getPhone())
                        .role(member.getRole().name())
                        .build())
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok()
                    .body(new ApiResponse<>("전체 회원 조회 성공", memberResponses, HttpStatus.OK));
        } catch (Exception e) {
            log.error("전체 회원 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>("전체 회원 조회 실패: " + e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
} 