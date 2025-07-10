package com.sdemo1.repository;

import java.util.Optional;
import com.sdemo1.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByName(String name);
    Optional<Member> findByPhone(String phone);
    Optional<Member> findByMemberId(Long memberId);
}
