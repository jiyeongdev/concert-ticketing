package com.sdemo1.repository;

import com.sdemo1.entity.SeatGrade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface SeatGradeRepository extends JpaRepository<SeatGrade, BigInteger> {
    
    /**
     * 콘서트 ID로 좌석등급 조회
     */
    List<SeatGrade> findByConcertIdOrderByPriceDesc(BigInteger concertId);
    
    /**
     * 콘서트 ID로 좌석등급 존재 여부 확인
     */
    boolean existsByConcertId(BigInteger concertId);
    
    /**
     * 콘서트 ID와 등급명으로 좌석등급 조회
     */
    SeatGrade findByConcertIdAndGradeName(BigInteger concertId, String gradeName);
} 