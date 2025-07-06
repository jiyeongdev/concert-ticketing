package com.sdemo1.entity;

import com.sdemo1.config.TimeZoneConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigInteger;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "concerts")
public class Concert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private BigInteger id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "location", nullable = false, length = 100)
    private String location;

    @Column(name = "concert_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Convert(converter = TimeZoneConverter.class)
    private LocalDateTime concertDate;

    @Column(name = "open_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Convert(converter = TimeZoneConverter.class)
    private LocalDateTime openTime;

    @Column(name = "close_time")
    @Temporal(TemporalType.TIMESTAMP)
    @Convert(converter = TimeZoneConverter.class)
    private LocalDateTime closeTime;

    @Column(name = "created_at", insertable = false, updatable = false)
    private java.sql.Timestamp createdAt;
} 