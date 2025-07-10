
-- 초기 테스트 데이터 삽입
-- 자동 생성된 파일입니다. 직접 편집하지 마세요.
CREATE DATABASE IF NOT EXISTS mydb;
USE mydb;

-- USER 역할 사용자 100명
INSERT INTO member (email, password, name, phone, role) VALUES
('user1-teste1@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 1', '010-1001-0001', 'USER'),
('user2@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 2', '010-1002-0002', 'USER'),
('user3@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 3', '010-1003-0003', 'USER'),
('user4@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 4', '010-1004-0004', 'USER'),
('user5@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 5', '010-1005-0005', 'USER'),
('user6@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 6', '010-1006-0006', 'USER'),
('user7@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 7', '010-1007-0007', 'USER'),
('user8@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 8', '010-1008-0008', 'USER'),
('user9@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 9', '010-1009-0009', 'USER'),
('user10@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 10', '010-1010-0010', 'USER'),
('user11@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 11', '010-1011-0011', 'USER'),
('user12@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 12', '010-1012-0012', 'USER'),
('user13@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 13', '010-1013-0013', 'USER'),
('user14@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 14', '010-1014-0014', 'USER'),
('user15@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 15', '010-1015-0015', 'USER'),
('user16@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 16', '010-1016-0016', 'USER'),
('user17@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 17', '010-1017-0017', 'USER'),
('user18@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 18', '010-1018-0018', 'USER'),
('user19@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 19', '010-1019-0019', 'USER'),
('user20@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'User 20', '010-1020-0020', 'USER');

-- ADMIN 역할 사용자 100명
INSERT INTO member (email, password, name, phone, role) VALUES
('admin1@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 1', '010-2001-0001', 'ADMIN'),
('admin2@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 2', '010-2002-0002', 'ADMIN'),
('admin3@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 3', '010-2003-0003', 'ADMIN'),
('admin4@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 4', '010-2004-0004', 'ADMIN'),
('admin5@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 5', '010-2005-0005', 'ADMIN'),
('admin6@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 6', '010-2006-0006', 'ADMIN'),
('admin7@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 7', '010-2007-0007', 'ADMIN'),
('admin8@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 8', '010-2008-0008', 'ADMIN'),
('admin9@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 9', '010-2009-0009', 'ADMIN'),
('admin10@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 10', '010-2010-0010', 'ADMIN'),
('admin11@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 11', '010-2011-0011', 'ADMIN'),
('admin12@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 12', '010-2012-0012', 'ADMIN'),
('admin13@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 13', '010-2013-0013', 'ADMIN'),
('admin14@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 14', '010-2014-0014', 'ADMIN'),
('admin15@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 15', '010-2015-0015', 'ADMIN'),
('admin16@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 16', '010-2016-0016', 'ADMIN'),
('admin17@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 17', '010-2017-0017', 'ADMIN'),
('admin18@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 18', '010-2018-0018', 'ADMIN'),
('admin19@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 19', '010-2019-0019', 'ADMIN'),
('admin20@test.com', '$2a$10$yWoFDsuI.JAnzKYIhi33kOKsetmePeQlWP2zsRu6kB9LowLebG0BO', 'Admin 20', '010-2020-0020', 'ADMIN');

-- 참고사항:
-- 비밀번호: 'password1234' (모든 사용자 공통)
-- BCrypt 해시: password1234
-- 실제 운영 환경에서는 더 강력한 비밀번호를 사용해야 합니다.

-- 콘서트 데이터 삽입 (10개)
INSERT INTO concerts (title, location, concert_date, open_time, close_time) VALUES
-- 예매 가능한 콘서트 (3개) - 현재 예매 가능
('BTS WORLD TOUR 2024 in Seoul', '서울 잠실종합운동장 주경기장', '2024-05-15 19:30:00', NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH)),
('IU The Golden Hour Concert', '서울 올림픽공원 KSPO DOME', '2024-05-20 18:00:00', NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH)),
('BLACKPINK BORN PINK FINALE', '인천 인스파이어 엔터테인먼트 리조트', '2024-05-25 19:00:00', NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH)),

-- 예매 예정인 콘서트 (7개) - 1달 후 예매 시작
('NewJeans Get Up Tour', '서울 고척스카이돔', '2024-06-10 19:00:00', DATE_ADD(NOW(), INTERVAL 1 MONTH), DATE_ADD(NOW(), INTERVAL 2 MONTH)),
('aespa MY WORLD Tour in Korea', '부산 BEXCO 오디토리움', '2024-06-15 18:30:00', DATE_ADD(NOW(), INTERVAL 1 MONTH), DATE_ADD(NOW(), INTERVAL 2 MONTH)),
('TWICE READY TO BE Tour Seoul', '서울 잠실학생체육관', '2024-06-20 19:00:00', DATE_ADD(NOW(), INTERVAL 1 MONTH), DATE_ADD(NOW(), INTERVAL 2 MONTH)),
('(G)I-DLE I LOVE Concert', '대구 엑스코', '2024-06-25 18:00:00', DATE_ADD(NOW(), INTERVAL 1 MONTH), DATE_ADD(NOW(), INTERVAL 2 MONTH)),
('ITZY CHECKMATE World Tour', '서울 잠실학생체육관', '2024-07-05 19:30:00', DATE_ADD(NOW(), INTERVAL 1 MONTH), DATE_ADD(NOW(), INTERVAL 2 MONTH)),
('Stray Kids CIRCUS Tour', '서울 올림픽체조경기장', '2024-07-10 19:00:00', DATE_ADD(NOW(), INTERVAL 1 MONTH), DATE_ADD(NOW(), INTERVAL 2 MONTH)),
('SEVENTEEN GOD OF MUSIC Tour', '서울 잠실종합운동장 실내체육관', '2024-07-15 18:30:00', DATE_ADD(NOW(), INTERVAL 1 MONTH), DATE_ADD(NOW(), INTERVAL 2 MONTH));

-- 각 콘서트별 좌석 등급 생성
INSERT INTO seat_grades (concert_id, grade_name, price) VALUES
-- BTS 콘서트 (concert_id: 1)
(1, 'VIP', 250000),
(1, 'R석', 180000),
(1, 'S석', 120000),
(1, 'A석', 80000),

-- IU 콘서트 (concert_id: 2)
(2, 'VIP', 200000),
(2, 'R석', 150000),
(2, 'S석', 100000),
(2, 'A석', 70000),

-- BLACKPINK 콘서트 (concert_id: 3)
(3, 'VIP', 220000),
(3, 'R석', 160000),
(3, 'S석', 110000),
(3, 'A석', 75000),

-- NewJeans 콘서트 (concert_id: 4)
(4, 'VIP', 180000),
(4, 'R석', 140000),
(4, 'S석', 90000),
(4, 'A석', 60000),

-- aespa 콘서트 (concert_id: 5)
(5, 'VIP', 170000),
(5, 'R석', 130000),
(5, 'S석', 85000),
(5, 'A석', 55000),

-- TWICE 콘서트 (concert_id: 6)
(6, 'VIP', 190000),
(6, 'R석', 145000),
(6, 'S석', 95000),
(6, 'A석', 65000),

-- (G)I-DLE 콘서트 (concert_id: 7)
(7, 'VIP', 160000),
(7, 'R석', 120000),
(7, 'S석', 80000),
(7, 'A석', 50000),

-- ITZY 콘서트 (concert_id: 8)
(8, 'VIP', 165000),
(8, 'R석', 125000),
(8, 'S석', 82000),
(8, 'A석', 52000),

-- Stray Kids 콘서트 (concert_id: 9)
(9, 'VIP', 175000),
(9, 'R석', 135000),
(9, 'S석', 88000),
(9, 'A석', 58000),

-- SEVENTEEN 콘서트 (concert_id: 10)
(10, 'VIP', 185000),
(10, 'R석', 140000),
(10, 'S석', 92000),
(10, 'A석', 62000);

-- 각 콘서트별 샘플 좌석 생성 (각 등급별 5석씩, 총 20석)
INSERT INTO seats (concert_id, seat_grade_id, seat_row, seat_number, position_x, position_y, status) VALUES
-- BTS 콘서트 좌석들 (concert_id: 1, seat_grade_id: 1-4)
(1, 1, 'VIP1', '1', 100, 50, 'AVAILABLE'), (1, 1, 'VIP1', '2', 120, 50, 'AVAILABLE'), (1, 1, 'VIP1', '3', 140, 50, 'AVAILABLE'), (1, 1, 'VIP1', '4', 160, 50, 'AVAILABLE'), (1, 1, 'VIP1', '5', 180, 50, 'AVAILABLE'),
(1, 2, 'R1', '1', 80, 130, 'AVAILABLE'), (1, 2, 'R1', '2', 100, 130, 'AVAILABLE'), (1, 2, 'R1', '3', 120, 130, 'AVAILABLE'), (1, 2, 'R1', '4', 140, 130, 'AVAILABLE'), (1, 2, 'R1', '5', 160, 130, 'AVAILABLE'),
(1, 3, 'S1', '1', 60, 210, 'AVAILABLE'), (1, 3, 'S1', '2', 80, 210, 'AVAILABLE'), (1, 3, 'S1', '3', 100, 210, 'AVAILABLE'), (1, 3, 'S1', '4', 120, 210, 'AVAILABLE'), (1, 3, 'S1', '5', 140, 210, 'AVAILABLE'),
(1, 4, 'A1', '1', 40, 290, 'AVAILABLE'), (1, 4, 'A1', '2', 60, 290, 'AVAILABLE'), (1, 4, 'A1', '3', 80, 290, 'AVAILABLE'), (1, 4, 'A1', '4', 100, 290, 'AVAILABLE'), (1, 4, 'A1', '5', 120, 290, 'AVAILABLE'),

-- IU 콘서트 좌석들 (concert_id: 2, seat_grade_id: 5-8)
(2, 5, 'VIP1', '1', 100, 50, 'AVAILABLE'), (2, 5, 'VIP1', '2', 120, 50, 'AVAILABLE'), (2, 5, 'VIP1', '3', 140, 50, 'AVAILABLE'), (2, 5, 'VIP1', '4', 160, 50, 'AVAILABLE'), (2, 5, 'VIP1', '5', 180, 50, 'AVAILABLE'),
(2, 6, 'R1', '1', 80, 130, 'AVAILABLE'), (2, 6, 'R1', '2', 100, 130, 'AVAILABLE'), (2, 6, 'R1', '3', 120, 130, 'AVAILABLE'), (2, 6, 'R1', '4', 140, 130, 'AVAILABLE'), (2, 6, 'R1', '5', 160, 130, 'AVAILABLE'),
(2, 7, 'S1', '1', 60, 210, 'AVAILABLE'), (2, 7, 'S1', '2', 80, 210, 'AVAILABLE'), (2, 7, 'S1', '3', 100, 210, 'AVAILABLE'), (2, 7, 'S1', '4', 120, 210, 'AVAILABLE'), (2, 7, 'S1', '5', 140, 210, 'AVAILABLE'),
(2, 8, 'A1', '1', 40, 290, 'AVAILABLE'), (2, 8, 'A1', '2', 60, 290, 'AVAILABLE'), (2, 8, 'A1', '3', 80, 290, 'AVAILABLE'), (2, 8, 'A1', '4', 100, 290, 'AVAILABLE'), (2, 8, 'A1', '5', 120, 290, 'AVAILABLE'),

-- BLACKPINK 콘서트 좌석들 (concert_id: 3, seat_grade_id: 9-12)
(3, 9, 'VIP1', '1', 100, 50, 'AVAILABLE'), (3, 9, 'VIP1', '2', 120, 50, 'AVAILABLE'), (3, 9, 'VIP1', '3', 140, 50, 'AVAILABLE'), (3, 9, 'VIP1', '4', 160, 50, 'AVAILABLE'), (3, 9, 'VIP1', '5', 180, 50, 'AVAILABLE'),
(3, 10, 'R1', '1', 80, 130, 'AVAILABLE'), (3, 10, 'R1', '2', 100, 130, 'AVAILABLE'), (3, 10, 'R1', '3', 120, 130, 'AVAILABLE'), (3, 10, 'R1', '4', 140, 130, 'AVAILABLE'), (3, 10, 'R1', '5', 160, 130, 'AVAILABLE'),
(3, 11, 'S1', '1', 60, 210, 'AVAILABLE'), (3, 11, 'S1', '2', 80, 210, 'AVAILABLE'), (3, 11, 'S1', '3', 100, 210, 'AVAILABLE'), (3, 11, 'S1', '4', 120, 210, 'AVAILABLE'), (3, 11, 'S1', '5', 140, 210, 'AVAILABLE'),
(3, 12, 'A1', '1', 40, 290, 'AVAILABLE'), (3, 12, 'A1', '2', 60, 290, 'AVAILABLE'), (3, 12, 'A1', '3', 80, 290, 'AVAILABLE'), (3, 12, 'A1', '4', 100, 290, 'AVAILABLE'), (3, 12, 'A1', '5', 120, 290, 'AVAILABLE'),

-- NewJeans 콘서트 좌석들 (concert_id: 4, seat_grade_id: 13-16)
(4, 13, 'VIP1', '1', 100, 50, 'AVAILABLE'), (4, 13, 'VIP1', '2', 120, 50, 'AVAILABLE'), (4, 13, 'VIP1', '3', 140, 50, 'AVAILABLE'), (4, 13, 'VIP1', '4', 160, 50, 'AVAILABLE'), (4, 13, 'VIP1', '5', 180, 50, 'AVAILABLE'),
(4, 14, 'R1', '1', 80, 130, 'AVAILABLE'), (4, 14, 'R1', '2', 100, 130, 'AVAILABLE'), (4, 14, 'R1', '3', 120, 130, 'AVAILABLE'), (4, 14, 'R1', '4', 140, 130, 'AVAILABLE'), (4, 14, 'R1', '5', 160, 130, 'AVAILABLE'),
(4, 15, 'S1', '1', 60, 210, 'AVAILABLE'), (4, 15, 'S1', '2', 80, 210, 'AVAILABLE'), (4, 15, 'S1', '3', 100, 210, 'AVAILABLE'), (4, 15, 'S1', '4', 120, 210, 'AVAILABLE'), (4, 15, 'S1', '5', 140, 210, 'AVAILABLE'),
(4, 16, 'A1', '1', 40, 290, 'AVAILABLE'), (4, 16, 'A1', '2', 60, 290, 'AVAILABLE'), (4, 16, 'A1', '3', 80, 290, 'AVAILABLE'), (4, 16, 'A1', '4', 100, 290, 'AVAILABLE'), (4, 16, 'A1', '5', 120, 290, 'AVAILABLE'),

-- aespa 콘서트 좌석들 (concert_id: 5, seat_grade_id: 17-20)
(5, 17, 'VIP1', '1', 100, 50, 'AVAILABLE'), (5, 17, 'VIP1', '2', 120, 50, 'AVAILABLE'), (5, 17, 'VIP1', '3', 140, 50, 'AVAILABLE'), (5, 17, 'VIP1', '4', 160, 50, 'AVAILABLE'), (5, 17, 'VIP1', '5', 180, 50, 'AVAILABLE'),
(5, 18, 'R1', '1', 80, 130, 'AVAILABLE'), (5, 18, 'R1', '2', 100, 130, 'AVAILABLE'), (5, 18, 'R1', '3', 120, 130, 'AVAILABLE'), (5, 18, 'R1', '4', 140, 130, 'AVAILABLE'), (5, 18, 'R1', '5', 160, 130, 'AVAILABLE'),
(5, 19, 'S1', '1', 60, 210, 'AVAILABLE'), (5, 19, 'S1', '2', 80, 210, 'AVAILABLE'), (5, 19, 'S1', '3', 100, 210, 'AVAILABLE'), (5, 19, 'S1', '4', 120, 210, 'AVAILABLE'), (5, 19, 'S1', '5', 140, 210, 'AVAILABLE'),
(5, 20, 'A1', '1', 40, 290, 'AVAILABLE'), (5, 20, 'A1', '2', 60, 290, 'AVAILABLE'), (5, 20, 'A1', '3', 80, 290, 'AVAILABLE'), (5, 20, 'A1', '4', 100, 290, 'AVAILABLE'), (5, 20, 'A1', '5', 120, 290, 'AVAILABLE'),

-- TWICE 콘서트 좌석들 (concert_id: 6, seat_grade_id: 21-24)
(6, 21, 'VIP1', '1', 100, 50, 'AVAILABLE'), (6, 21, 'VIP1', '2', 120, 50, 'AVAILABLE'), (6, 21, 'VIP1', '3', 140, 50, 'AVAILABLE'), (6, 21, 'VIP1', '4', 160, 50, 'AVAILABLE'), (6, 21, 'VIP1', '5', 180, 50, 'AVAILABLE'),
(6, 22, 'R1', '1', 80, 130, 'AVAILABLE'), (6, 22, 'R1', '2', 100, 130, 'AVAILABLE'), (6, 22, 'R1', '3', 120, 130, 'AVAILABLE'), (6, 22, 'R1', '4', 140, 130, 'AVAILABLE'), (6, 22, 'R1', '5', 160, 130, 'AVAILABLE'),
(6, 23, 'S1', '1', 60, 210, 'AVAILABLE'), (6, 23, 'S1', '2', 80, 210, 'AVAILABLE'), (6, 23, 'S1', '3', 100, 210, 'AVAILABLE'), (6, 23, 'S1', '4', 120, 210, 'AVAILABLE'), (6, 23, 'S1', '5', 140, 210, 'AVAILABLE'),
(6, 24, 'A1', '1', 40, 290, 'AVAILABLE'), (6, 24, 'A1', '2', 60, 290, 'AVAILABLE'), (6, 24, 'A1', '3', 80, 290, 'AVAILABLE'), (6, 24, 'A1', '4', 100, 290, 'AVAILABLE'), (6, 24, 'A1', '5', 120, 290, 'AVAILABLE'),

-- (G)I-DLE 콘서트 좌석들 (concert_id: 7, seat_grade_id: 25-28)
(7, 25, 'VIP1', '1', 100, 50, 'AVAILABLE'), (7, 25, 'VIP1', '2', 120, 50, 'AVAILABLE'), (7, 25, 'VIP1', '3', 140, 50, 'AVAILABLE'), (7, 25, 'VIP1', '4', 160, 50, 'AVAILABLE'), (7, 25, 'VIP1', '5', 180, 50, 'AVAILABLE'),
(7, 26, 'R1', '1', 80, 130, 'AVAILABLE'), (7, 26, 'R1', '2', 100, 130, 'AVAILABLE'), (7, 26, 'R1', '3', 120, 130, 'AVAILABLE'), (7, 26, 'R1', '4', 140, 130, 'AVAILABLE'), (7, 26, 'R1', '5', 160, 130, 'AVAILABLE'),
(7, 27, 'S1', '1', 60, 210, 'AVAILABLE'), (7, 27, 'S1', '2', 80, 210, 'AVAILABLE'), (7, 27, 'S1', '3', 100, 210, 'AVAILABLE'), (7, 27, 'S1', '4', 120, 210, 'AVAILABLE'), (7, 27, 'S1', '5', 140, 210, 'AVAILABLE'),
(7, 28, 'A1', '1', 40, 290, 'AVAILABLE'), (7, 28, 'A1', '2', 60, 290, 'AVAILABLE'), (7, 28, 'A1', '3', 80, 290, 'AVAILABLE'), (7, 28, 'A1', '4', 100, 290, 'AVAILABLE'), (7, 28, 'A1', '5', 120, 290, 'AVAILABLE'),

-- ITZY 콘서트 좌석들 (concert_id: 8, seat_grade_id: 29-32)
(8, 29, 'VIP1', '1', 100, 50, 'AVAILABLE'), (8, 29, 'VIP1', '2', 120, 50, 'AVAILABLE'), (8, 29, 'VIP1', '3', 140, 50, 'AVAILABLE'), (8, 29, 'VIP1', '4', 160, 50, 'AVAILABLE'), (8, 29, 'VIP1', '5', 180, 50, 'AVAILABLE'),
(8, 30, 'R1', '1', 80, 130, 'AVAILABLE'), (8, 30, 'R1', '2', 100, 130, 'AVAILABLE'), (8, 30, 'R1', '3', 120, 130, 'AVAILABLE'), (8, 30, 'R1', '4', 140, 130, 'AVAILABLE'), (8, 30, 'R1', '5', 160, 130, 'AVAILABLE'),
(8, 31, 'S1', '1', 60, 210, 'AVAILABLE'), (8, 31, 'S1', '2', 80, 210, 'AVAILABLE'), (8, 31, 'S1', '3', 100, 210, 'AVAILABLE'), (8, 31, 'S1', '4', 120, 210, 'AVAILABLE'), (8, 31, 'S1', '5', 140, 210, 'AVAILABLE'),
(8, 32, 'A1', '1', 40, 290, 'AVAILABLE'), (8, 32, 'A1', '2', 60, 290, 'AVAILABLE'), (8, 32, 'A1', '3', 80, 290, 'AVAILABLE'), (8, 32, 'A1', '4', 100, 290, 'AVAILABLE'), (8, 32, 'A1', '5', 120, 290, 'AVAILABLE'),

-- Stray Kids 콘서트 좌석들 (concert_id: 9, seat_grade_id: 33-36)
(9, 33, 'VIP1', '1', 100, 50, 'AVAILABLE'), (9, 33, 'VIP1', '2', 120, 50, 'AVAILABLE'), (9, 33, 'VIP1', '3', 140, 50, 'AVAILABLE'), (9, 33, 'VIP1', '4', 160, 50, 'AVAILABLE'), (9, 33, 'VIP1', '5', 180, 50, 'AVAILABLE'),
(9, 34, 'R1', '1', 80, 130, 'AVAILABLE'), (9, 34, 'R1', '2', 100, 130, 'AVAILABLE'), (9, 34, 'R1', '3', 120, 130, 'AVAILABLE'), (9, 34, 'R1', '4', 140, 130, 'AVAILABLE'), (9, 34, 'R1', '5', 160, 130, 'AVAILABLE'),
(9, 35, 'S1', '1', 60, 210, 'AVAILABLE'), (9, 35, 'S1', '2', 80, 210, 'AVAILABLE'), (9, 35, 'S1', '3', 100, 210, 'AVAILABLE'), (9, 35, 'S1', '4', 120, 210, 'AVAILABLE'), (9, 35, 'S1', '5', 140, 210, 'AVAILABLE'),
(9, 36, 'A1', '1', 40, 290, 'AVAILABLE'), (9, 36, 'A1', '2', 60, 290, 'AVAILABLE'), (9, 36, 'A1', '3', 80, 290, 'AVAILABLE'), (9, 36, 'A1', '4', 100, 290, 'AVAILABLE'), (9, 36, 'A1', '5', 120, 290, 'AVAILABLE'),

-- SEVENTEEN 콘서트 좌석들 (concert_id: 10, seat_grade_id: 37-40)
(10, 37, 'VIP1', '1', 100, 50, 'AVAILABLE'), (10, 37, 'VIP1', '2', 120, 50, 'AVAILABLE'), (10, 37, 'VIP1', '3', 140, 50, 'AVAILABLE'), (10, 37, 'VIP1', '4', 160, 50, 'AVAILABLE'), (10, 37, 'VIP1', '5', 180, 50, 'AVAILABLE'),
(10, 38, 'R1', '1', 80, 130, 'AVAILABLE'), (10, 38, 'R1', '2', 100, 130, 'AVAILABLE'), (10, 38, 'R1', '3', 120, 130, 'AVAILABLE'), (10, 38, 'R1', '4', 140, 130, 'AVAILABLE'), (10, 38, 'R1', '5', 160, 130, 'AVAILABLE'),
(10, 39, 'S1', '1', 60, 210, 'AVAILABLE'), (10, 39, 'S1', '2', 80, 210, 'AVAILABLE'), (10, 39, 'S1', '3', 100, 210, 'AVAILABLE'), (10, 39, 'S1', '4', 120, 210, 'AVAILABLE'), (10, 39, 'S1', '5', 140, 210, 'AVAILABLE'),
(10, 40, 'A1', '1', 40, 290, 'AVAILABLE'), (10, 40, 'A1', '2', 60, 290, 'AVAILABLE'), (10, 40, 'A1', '3', 80, 290, 'AVAILABLE'), (10, 40, 'A1', '4', 100, 290, 'AVAILABLE'), (10, 40, 'A1', '5', 120, 290, 'AVAILABLE');
