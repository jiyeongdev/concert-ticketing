package com.sdemo1.repository;

import java.util.List;
import com.sdemo1.entity.SeatGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatGradeRepository extends JpaRepository<SeatGrade, Long> {
    
    /**
     * 콘서트 ID로 좌석등급 조회
     */
    List<SeatGrade> findByConcertIdOrderByPriceDesc(Long concertId);
    
    /**
     * 콘서트 ID로 좌석등급 존재 여부 확인
     */
    boolean existsByConcertId(Long concertId);
    
    /**
     * 콘서트 ID와 등급명으로 좌석등급 조회
     */
    SeatGrade findByConcertIdAndGradeName(Long concertId, String gradeName);
} 