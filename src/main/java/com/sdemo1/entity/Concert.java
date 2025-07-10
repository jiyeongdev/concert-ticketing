package com.sdemo1.entity;

import java.time.LocalDateTime;
import com.sdemo1.config.TimeZoneConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private Long id;

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