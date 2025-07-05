package com.sdemo1.repository;

import com.sdemo1.entity.Concert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface ConcertRepository extends JpaRepository<Concert, BigInteger> {
    List<Concert> findAllByOrderByConcertDateAsc();
    List<Concert> findByTitleContainingIgnoreCase(String title);
} 