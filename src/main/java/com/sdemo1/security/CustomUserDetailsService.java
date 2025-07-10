package com.sdemo1.security;

import java.util.Collections;
import com.sdemo1.entity.Member;
import com.sdemo1.repository.MemberRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("사용자 조회 시도: email={}", email);
        
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없습니다: {}", email);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
                });

        log.info("사용자 발견: memberId={}, email={}, role={}", 
                member.getMemberId(), member.getEmail(), member.getRole());

        return CustomUserDetails.builder()
                .memberId(member.getMemberId())
                .name(member.getName())
                .role(member.getRole().name())
                .phone(member.getPhone())
                .password(member.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())))
                .build();
    }
} 