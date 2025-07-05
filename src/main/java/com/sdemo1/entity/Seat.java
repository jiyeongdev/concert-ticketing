package com.sdemo1.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private BigInteger id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "concert_id", nullable = false)
    private Concert concert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_grade_id")
    private SeatGrade seatGrade;

    @Column(name = "seat_row", length = 10)
    private String seatRow;

    @Column(name = "seat_number", length = 10)
    private String seatNumber;

    @Column(name = "position_x")
    private Integer positionX;  // 좌석의 X 좌표

    @Column(name = "position_y")
    private Integer positionY;  // 좌석의 Y 좌표

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private SeatStatus status = SeatStatus.AVAILABLE;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private java.sql.Timestamp updatedAt;



    public enum SeatStatus {
        AVAILABLE,
        HELD,
        BOOKED
    }
} 