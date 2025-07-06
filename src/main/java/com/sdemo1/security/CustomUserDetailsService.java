package com.sdemo1.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sdemo1.repository.MemberRepository;
import com.sdemo1.entity.Member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final MemberRepository memberRepository;

    /**
     * Spring Security가 사용자 인증을 할 때 사용자 정보를 로드하는 역할
     * loadUserByUsername 메서드는 로그인 시도 시 호출됨
     * 
     * 사용 시점:
     * 일반 로그인 시도할 때
     * JWT 토큰 검증 시
     * Spring Security의 인증 필터에서 사용자 정보가 필요할 때

     * @param email 사용자 이메일
     * @return 사용자 정보
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때 발생
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // email로 Member를 직접 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
        
        log.info("사용자 정보: memberId={}, name={}, email={}", member.getMemberId(), member.getName(), member.getEmail());
        
        // CustomUserDetails 생성 (Builder 패턴 사용)
        // 실제 암호화된 비밀번호를 설정하여 Spring Security가 올바르게 비교할 수 있도록 함
        return CustomUserDetails.builder()
                .memberId(member.getMemberId())
                .name(member.getName())
                .role(member.getRole().toString())
                .phone(member.getPhone())
                .password(member.getPassword()) // 실제 암호화된 비밀번호 설정
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + member.getRole())))
                .build();
    }
} 