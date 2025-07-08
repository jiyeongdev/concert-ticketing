package com.sdemo1.dto.seat;

import java.math.BigInteger;

/**
 * 좌석 정보 DTO
 * Java 14+ Record 클래스 사용으로 불변 객체 생성
 */
public record SeatDto(
    BigInteger id,
    BigInteger concertId,
    BigInteger seatGradeId,
    String seatRow,      // A, B, C 등 행 번호
    String seatNumber,   // 1, 2, 3 등 좌석 번호
    Integer positionX,   // 좌석의 X 좌표
    Integer positionY,   // 좌석의 Y 좌표
    String status,       // AVAILABLE, HELD, BOOKED
    String gradeName,    // 좌석등급명 (VIP, R, S, A, B)
    Integer price        // 좌석 가격
) {
    // Record는 자동으로 equals, hashCode, toString 메서드를 생성
    // 생성자도 자동으로 생성됨
} 