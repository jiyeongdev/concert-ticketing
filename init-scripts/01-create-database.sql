-- 데이터베이스 초기화
CREATE DATABASE IF NOT EXISTS mydb;
USE mydb;

-- UTF-8 인코딩 설정
SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;
SET collation_connection = utf8mb4_unicode_ci;

-- 테이블 삭제 (외래키 관계 순서 고려)

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS seat_holds;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS event_logs;
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS seat_grades;
DROP TABLE IF EXISTS concerts;
DROP TABLE IF EXISTS member;
SET FOREIGN_KEY_CHECKS = 1;

-- 테이블 생성
CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255),
    phone VARCHAR(255),
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE concerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    concert_date TIMESTAMP NOT NULL,
    open_time TIMESTAMP,
    close_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE seat_grades (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    concert_id BIGINT NOT NULL,
    grade_name VARCHAR(20),
    price INT NOT NULL,
    FOREIGN KEY (concert_id) REFERENCES concerts(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    concert_id BIGINT NOT NULL,
    seat_grade_id BIGINT,
    seat_row VARCHAR(10),
    seat_number VARCHAR(10),
    position_x INT,
    position_y INT,
    status ENUM('AVAILABLE', 'HELD', 'BOOKED') DEFAULT 'AVAILABLE',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (concert_id) REFERENCES concerts(id),
    FOREIGN KEY (seat_grade_id) REFERENCES seat_grades(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE seat_holds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT,
    seat_id BIGINT,
    hold_expire_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member(id),
    FOREIGN KEY (seat_id) REFERENCES seats(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    reservation_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_status ENUM('PENDING', 'PAID', 'FAILED', 'CANCELLED') DEFAULT 'PENDING',
    FOREIGN KEY (member_id) REFERENCES member(id),
    FOREIGN KEY (seat_id) REFERENCES seats(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT NOT NULL,
    amount INT NOT NULL,
    status ENUM('PENDING', 'SUCCESS', 'FAIL', 'CANCELLED') DEFAULT 'PENDING',
    payment_gateway VARCHAR(50),
    transaction_id VARCHAR(100),
    paid_at TIMESTAMP,
    attempt_number INT,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE event_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT,
    event_type VARCHAR(50),
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (member_id) REFERENCES member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 