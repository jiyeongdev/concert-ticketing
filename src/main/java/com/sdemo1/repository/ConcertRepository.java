package com.sdemo1.repository;

import java.math.BigInteger;
import java.util.List;
import com.sdemo1.entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConcertRepository extends JpaRepository<Concert, BigInteger> {
    List<Concert> findAllByOrderByConcertDateAsc();
    List<Concert> findByTitleContainingIgnoreCase(String title);
    // 예매 오픈 대상 콘서트 (openTime이 현재보다 같거나 이전, closeTime이 null이거나 미래)
    List<Concert> findByOpenTimeLessThanEqualAndCloseTimeAfter(java.time.LocalDateTime now1, java.time.LocalDateTime now2);
    // 예매 종료 대상 콘서트 (closeTime이 현재보다 같거나 이전)
    List<Concert> findByCloseTimeLessThanEqual(java.time.LocalDateTime now);
} 