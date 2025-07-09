-- UTF-8 인코딩 설정
SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;
SET collation_connection = utf8mb4_unicode_ci;

-- 초기 테스트 데이터 삽입
-- 자동 생성된 파일입니다. 직접 편집하지 마세요.
USE mydb;

-- USER 역할 사용자 100명
INSERT INTO member (email, password, name, phone, role) VALUES
('user1@test.com', 'password1234', 'User 1', '010-1001-0001', 'USER'),
('user2@test.com', 'password1234', 'User 2', '010-1002-0002', 'USER'),
('user3@test.com', 'password1234', 'User 3', '010-1003-0003', 'USER'),
('user4@test.com', 'password1234', 'User 4', '010-1004-0004', 'USER'),
('user5@test.com', 'password1234', 'User 5', '010-1005-0005', 'USER'),
('user6@test.com', 'password1234', 'User 6', '010-1006-0006', 'USER'),
('user7@test.com', 'password1234', 'User 7', '010-1007-0007', 'USER'),
('user8@test.com', 'password1234', 'User 8', '010-1008-0008', 'USER'),
('user9@test.com', 'password1234', 'User 9', '010-1009-0009', 'USER'),
('user10@test.com', 'password1234', 'User 10', '010-1010-0010', 'USER'),
('user11@test.com', 'password1234', 'User 11', '010-1011-0011', 'USER'),
('user12@test.com', 'password1234', 'User 12', '010-1012-0012', 'USER'),
('user13@test.com', 'password1234', 'User 13', '010-1013-0013', 'USER'),
('user14@test.com', 'password1234', 'User 14', '010-1014-0014', 'USER'),
('user15@test.com', 'password1234', 'User 15', '010-1015-0015', 'USER'),
('user16@test.com', 'password1234', 'User 16', '010-1016-0016', 'USER'),
('user17@test.com', 'password1234', 'User 17', '010-1017-0017', 'USER'),
('user18@test.com', 'password1234', 'User 18', '010-1018-0018', 'USER'),
('user19@test.com', 'password1234', 'User 19', '010-1019-0019', 'USER'),
('user20@test.com', 'password1234', 'User 20', '010-1020-0020', 'USER'),
('user21@test.com', 'password1234', 'User 21', '010-1021-0021', 'USER'),
('user22@test.com', 'password1234', 'User 22', '010-1022-0022', 'USER'),
('user23@test.com', 'password1234', 'User 23', '010-1023-0023', 'USER'),
('user24@test.com', 'password1234', 'User 24', '010-1024-0024', 'USER'),
('user25@test.com', 'password1234', 'User 25', '010-1025-0025', 'USER'),
('user26@test.com', 'password1234', 'User 26', '010-1026-0026', 'USER'),
('user27@test.com', 'password1234', 'User 27', '010-1027-0027', 'USER'),
('user28@test.com', 'password1234', 'User 28', '010-1028-0028', 'USER'),
('user29@test.com', 'password1234', 'User 29', '010-1029-0029', 'USER'),
('user30@test.com', 'password1234', 'User 30', '010-1030-0030', 'USER'),
('user31@test.com', 'password1234', 'User 31', '010-1031-0031', 'USER'),
('user32@test.com', 'password1234', 'User 32', '010-1032-0032', 'USER'),
('user33@test.com', 'password1234', 'User 33', '010-1033-0033', 'USER'),
('user34@test.com', 'password1234', 'User 34', '010-1034-0034', 'USER'),
('user35@test.com', 'password1234', 'User 35', '010-1035-0035', 'USER'),
('user36@test.com', 'password1234', 'User 36', '010-1036-0036', 'USER'),
('user37@test.com', 'password1234', 'User 37', '010-1037-0037', 'USER'),
('user38@test.com', 'password1234', 'User 38', '010-1038-0038', 'USER'),
('user39@test.com', 'password1234', 'User 39', '010-1039-0039', 'USER'),
('user40@test.com', 'password1234', 'User 40', '010-1040-0040', 'USER'),
('user41@test.com', 'password1234', 'User 41', '010-1041-0041', 'USER'),
('user42@test.com', 'password1234', 'User 42', '010-1042-0042', 'USER'),
('user43@test.com', 'password1234', 'User 43', '010-1043-0043', 'USER'),
('user44@test.com', 'password1234', 'User 44', '010-1044-0044', 'USER'),
('user45@test.com', 'password1234', 'User 45', '010-1045-0045', 'USER'),
('user46@test.com', 'password1234', 'User 46', '010-1046-0046', 'USER'),
('user47@test.com', 'password1234', 'User 47', '010-1047-0047', 'USER'),
('user48@test.com', 'password1234', 'User 48', '010-1048-0048', 'USER'),
('user49@test.com', 'password1234', 'User 49', '010-1049-0049', 'USER'),
('user50@test.com', 'password1234', 'User 50', '010-1050-0050', 'USER'),
('user51@test.com', 'password1234', 'User 51', '010-1051-0051', 'USER'),
('user52@test.com', 'password1234', 'User 52', '010-1052-0052', 'USER'),
('user53@test.com', 'password1234', 'User 53', '010-1053-0053', 'USER'),
('user54@test.com', 'password1234', 'User 54', '010-1054-0054', 'USER'),
('user55@test.com', 'password1234', 'User 55', '010-1055-0055', 'USER'),
('user56@test.com', 'password1234', 'User 56', '010-1056-0056', 'USER'),
('user57@test.com', 'password1234', 'User 57', '010-1057-0057', 'USER'),
('user58@test.com', 'password1234', 'User 58', '010-1058-0058', 'USER'),
('user59@test.com', 'password1234', 'User 59', '010-1059-0059', 'USER'),
('user60@test.com', 'password1234', 'User 60', '010-1060-0060', 'USER'),
('user61@test.com', 'password1234', 'User 61', '010-1061-0061', 'USER'),
('user62@test.com', 'password1234', 'User 62', '010-1062-0062', 'USER'),
('user63@test.com', 'password1234', 'User 63', '010-1063-0063', 'USER'),
('user64@test.com', 'password1234', 'User 64', '010-1064-0064', 'USER'),
('user65@test.com', 'password1234', 'User 65', '010-1065-0065', 'USER'),
('user66@test.com', 'password1234', 'User 66', '010-1066-0066', 'USER'),
('user67@test.com', 'password1234', 'User 67', '010-1067-0067', 'USER'),
('user68@test.com', 'password1234', 'User 68', '010-1068-0068', 'USER'),
('user69@test.com', 'password1234', 'User 69', '010-1069-0069', 'USER'),
('user70@test.com', 'password1234', 'User 70', '010-1070-0070', 'USER'),
('user71@test.com', 'password1234', 'User 71', '010-1071-0071', 'USER'),
('user72@test.com', 'password1234', 'User 72', '010-1072-0072', 'USER'),
('user73@test.com', 'password1234', 'User 73', '010-1073-0073', 'USER'),
('user74@test.com', 'password1234', 'User 74', '010-1074-0074', 'USER'),
('user75@test.com', 'password1234', 'User 75', '010-1075-0075', 'USER'),
('user76@test.com', 'password1234', 'User 76', '010-1076-0076', 'USER'),
('user77@test.com', 'password1234', 'User 77', '010-1077-0077', 'USER'),
('user78@test.com', 'password1234', 'User 78', '010-1078-0078', 'USER'),
('user79@test.com', 'password1234', 'User 79', '010-1079-0079', 'USER'),
('user80@test.com', 'password1234', 'User 80', '010-1080-0080', 'USER'),
('user81@test.com', 'password1234', 'User 81', '010-1081-0081', 'USER'),
('user82@test.com', 'password1234', 'User 82', '010-1082-0082', 'USER'),
('user83@test.com', 'password1234', 'User 83', '010-1083-0083', 'USER'),
('user84@test.com', 'password1234', 'User 84', '010-1084-0084', 'USER'),
('user85@test.com', 'password1234', 'User 85', '010-1085-0085', 'USER'),
('user86@test.com', 'password1234', 'User 86', '010-1086-0086', 'USER'),
('user87@test.com', 'password1234', 'User 87', '010-1087-0087', 'USER'),
('user88@test.com', 'password1234', 'User 88', '010-1088-0088', 'USER'),
('user89@test.com', 'password1234', 'User 89', '010-1089-0089', 'USER'),
('user90@test.com', 'password1234', 'User 90', '010-1090-0090', 'USER'),
('user91@test.com', 'password1234', 'User 91', '010-1091-0091', 'USER'),
('user92@test.com', 'password1234', 'User 92', '010-1092-0092', 'USER'),
('user93@test.com', 'password1234', 'User 93', '010-1093-0093', 'USER'),
('user94@test.com', 'password1234', 'User 94', '010-1094-0094', 'USER'),
('user95@test.com', 'password1234', 'User 95', '010-1095-0095', 'USER'),
('user96@test.com', 'password1234', 'User 96', '010-1096-0096', 'USER'),
('user97@test.com', 'password1234', 'User 97', '010-1097-0097', 'USER'),
('user98@test.com', 'password1234', 'User 98', '010-1098-0098', 'USER'),
('user99@test.com', 'password1234', 'User 99', '010-1099-0099', 'USER'),
('user100@test.com', 'password1234', 'User 100', '010-1100-0100', 'USER');

-- ADMIN 역할 사용자 100명
INSERT INTO member (email, password, name, phone, role) VALUES
('admin1@test.com', 'password1234', 'Admin 1', '010-2001-0001', 'ADMIN'),
('admin2@test.com', 'password1234', 'Admin 2', '010-2002-0002', 'ADMIN'),
('admin3@test.com', 'password1234', 'Admin 3', '010-2003-0003', 'ADMIN'),
('admin4@test.com', 'password1234', 'Admin 4', '010-2004-0004', 'ADMIN'),
('admin5@test.com', 'password1234', 'Admin 5', '010-2005-0005', 'ADMIN'),
('admin6@test.com', 'password1234', 'Admin 6', '010-2006-0006', 'ADMIN'),
('admin7@test.com', 'password1234', 'Admin 7', '010-2007-0007', 'ADMIN'),
('admin8@test.com', 'password1234', 'Admin 8', '010-2008-0008', 'ADMIN'),
('admin9@test.com', 'password1234', 'Admin 9', '010-2009-0009', 'ADMIN'),
('admin10@test.com', 'password1234', 'Admin 10', '010-2010-0010', 'ADMIN'),
('admin11@test.com', 'password1234', 'Admin 11', '010-2011-0011', 'ADMIN'),
('admin12@test.com', 'password1234', 'Admin 12', '010-2012-0012', 'ADMIN'),
('admin13@test.com', 'password1234', 'Admin 13', '010-2013-0013', 'ADMIN'),
('admin14@test.com', 'password1234', 'Admin 14', '010-2014-0014', 'ADMIN'),
('admin15@test.com', 'password1234', 'Admin 15', '010-2015-0015', 'ADMIN'),
('admin16@test.com', 'password1234', 'Admin 16', '010-2016-0016', 'ADMIN'),
('admin17@test.com', 'password1234', 'Admin 17', '010-2017-0017', 'ADMIN'),
('admin18@test.com', 'password1234', 'Admin 18', '010-2018-0018', 'ADMIN'),
('admin19@test.com', 'password1234', 'Admin 19', '010-2019-0019', 'ADMIN'),
('admin20@test.com', 'password1234', 'Admin 20', '010-2020-0020', 'ADMIN'),
('admin21@test.com', 'password1234', 'Admin 21', '010-2021-0021', 'ADMIN'),
('admin22@test.com', 'password1234', 'Admin 22', '010-2022-0022', 'ADMIN'),
('admin23@test.com', 'password1234', 'Admin 23', '010-2023-0023', 'ADMIN'),
('admin24@test.com', 'password1234', 'Admin 24', '010-2024-0024', 'ADMIN'),
('admin25@test.com', 'password1234', 'Admin 25', '010-2025-0025', 'ADMIN'),
('admin26@test.com', 'password1234', 'Admin 26', '010-2026-0026', 'ADMIN'),
('admin27@test.com', 'password1234', 'Admin 27', '010-2027-0027', 'ADMIN'),
('admin28@test.com', 'password1234', 'Admin 28', '010-2028-0028', 'ADMIN'),
('admin29@test.com', 'password1234', 'Admin 29', '010-2029-0029', 'ADMIN'),
('admin30@test.com', 'password1234', 'Admin 30', '010-2030-0030', 'ADMIN'),
('admin31@test.com', 'password1234', 'Admin 31', '010-2031-0031', 'ADMIN'),
('admin32@test.com', 'password1234', 'Admin 32', '010-2032-0032', 'ADMIN'),
('admin33@test.com', 'password1234', 'Admin 33', '010-2033-0033', 'ADMIN'),
('admin34@test.com', 'password1234', 'Admin 34', '010-2034-0034', 'ADMIN'),
('admin35@test.com', 'password1234', 'Admin 35', '010-2035-0035', 'ADMIN'),
('admin36@test.com', 'password1234', 'Admin 36', '010-2036-0036', 'ADMIN'),
('admin37@test.com', 'password1234', 'Admin 37', '010-2037-0037', 'ADMIN'),
('admin38@test.com', 'password1234', 'Admin 38', '010-2038-0038', 'ADMIN'),
('admin39@test.com', 'password1234', 'Admin 39', '010-2039-0039', 'ADMIN'),
('admin40@test.com', 'password1234', 'Admin 40', '010-2040-0040', 'ADMIN'),
('admin41@test.com', 'password1234', 'Admin 41', '010-2041-0041', 'ADMIN'),
('admin42@test.com', 'password1234', 'Admin 42', '010-2042-0042', 'ADMIN'),
('admin43@test.com', 'password1234', 'Admin 43', '010-2043-0043', 'ADMIN'),
('admin44@test.com', 'password1234', 'Admin 44', '010-2044-0044', 'ADMIN'),
('admin45@test.com', 'password1234', 'Admin 45', '010-2045-0045', 'ADMIN'),
('admin46@test.com', 'password1234', 'Admin 46', '010-2046-0046', 'ADMIN'),
('admin47@test.com', 'password1234', 'Admin 47', '010-2047-0047', 'ADMIN'),
('admin48@test.com', 'password1234', 'Admin 48', '010-2048-0048', 'ADMIN'),
('admin49@test.com', 'password1234', 'Admin 49', '010-2049-0049', 'ADMIN'),
('admin50@test.com', 'password1234', 'Admin 50', '010-2050-0050', 'ADMIN'),
('admin51@test.com', 'password1234', 'Admin 51', '010-2051-0051', 'ADMIN'),
('admin52@test.com', 'password1234', 'Admin 52', '010-2052-0052', 'ADMIN'),
('admin53@test.com', 'password1234', 'Admin 53', '010-2053-0053', 'ADMIN'),
('admin54@test.com', 'password1234', 'Admin 54', '010-2054-0054', 'ADMIN'),
('admin55@test.com', 'password1234', 'Admin 55', '010-2055-0055', 'ADMIN'),
('admin56@test.com', 'password1234', 'Admin 56', '010-2056-0056', 'ADMIN'),
('admin57@test.com', 'password1234', 'Admin 57', '010-2057-0057', 'ADMIN'),
('admin58@test.com', 'password1234', 'Admin 58', '010-2058-0058', 'ADMIN'),
('admin59@test.com', 'password1234', 'Admin 59', '010-2059-0059', 'ADMIN'),
('admin60@test.com', 'password1234', 'Admin 60', '010-2060-0060', 'ADMIN'),
('admin61@test.com', 'password1234', 'Admin 61', '010-2061-0061', 'ADMIN'),
('admin62@test.com', 'password1234', 'Admin 62', '010-2062-0062', 'ADMIN'),
('admin63@test.com', 'password1234', 'Admin 63', '010-2063-0063', 'ADMIN'),
('admin64@test.com', 'password1234', 'Admin 64', '010-2064-0064', 'ADMIN'),
('admin65@test.com', 'password1234', 'Admin 65', '010-2065-0065', 'ADMIN'),
('admin66@test.com', 'password1234', 'Admin 66', '010-2066-0066', 'ADMIN'),
('admin67@test.com', 'password1234', 'Admin 67', '010-2067-0067', 'ADMIN'),
('admin68@test.com', 'password1234', 'Admin 68', '010-2068-0068', 'ADMIN'),
('admin69@test.com', 'password1234', 'Admin 69', '010-2069-0069', 'ADMIN'),
('admin70@test.com', 'password1234', 'Admin 70', '010-2070-0070', 'ADMIN'),
('admin71@test.com', 'password1234', 'Admin 71', '010-2071-0071', 'ADMIN'),
('admin72@test.com', 'password1234', 'Admin 72', '010-2072-0072', 'ADMIN'),
('admin73@test.com', 'password1234', 'Admin 73', '010-2073-0073', 'ADMIN'),
('admin74@test.com', 'password1234', 'Admin 74', '010-2074-0074', 'ADMIN'),
('admin75@test.com', 'password1234', 'Admin 75', '010-2075-0075', 'ADMIN'),
('admin76@test.com', 'password1234', 'Admin 76', '010-2076-0076', 'ADMIN'),
('admin77@test.com', 'password1234', 'Admin 77', '010-2077-0077', 'ADMIN'),
('admin78@test.com', 'password1234', 'Admin 78', '010-2078-0078', 'ADMIN'),
('admin79@test.com', 'password1234', 'Admin 79', '010-2079-0079', 'ADMIN'),
('admin80@test.com', 'password1234', 'Admin 80', '010-2080-0080', 'ADMIN'),
('admin81@test.com', 'password1234', 'Admin 81', '010-2081-0081', 'ADMIN'),
('admin82@test.com', 'password1234', 'Admin 82', '010-2082-0082', 'ADMIN'),
('admin83@test.com', 'password1234', 'Admin 83', '010-2083-0083', 'ADMIN'),
('admin84@test.com', 'password1234', 'Admin 84', '010-2084-0084', 'ADMIN'),
('admin85@test.com', 'password1234', 'Admin 85', '010-2085-0085', 'ADMIN'),
('admin86@test.com', 'password1234', 'Admin 86', '010-2086-0086', 'ADMIN'),
('admin87@test.com', 'password1234', 'Admin 87', '010-2087-0087', 'ADMIN'),
('admin88@test.com', 'password1234', 'Admin 88', '010-2088-0088', 'ADMIN'),
('admin89@test.com', 'password1234', 'Admin 89', '010-2089-0089', 'ADMIN'),
('admin90@test.com', 'password1234', 'Admin 90', '010-2090-0090', 'ADMIN'),
('admin91@test.com', 'password1234', 'Admin 91', '010-2091-0091', 'ADMIN'),
('admin92@test.com', 'password1234', 'Admin 92', '010-2092-0092', 'ADMIN'),
('admin93@test.com', 'password1234', 'Admin 93', '010-2093-0093', 'ADMIN'),
('admin94@test.com', 'password1234', 'Admin 94', '010-2094-0094', 'ADMIN'),
('admin95@test.com', 'password1234', 'Admin 95', '010-2095-0095', 'ADMIN'),
('admin96@test.com', 'password1234', 'Admin 96', '010-2096-0096', 'ADMIN'),
('admin97@test.com', 'password1234', 'Admin 97', '010-2097-0097', 'ADMIN'),
('admin98@test.com', 'password1234', 'Admin 98', '010-2098-0098', 'ADMIN'),
('admin99@test.com', 'password1234', 'Admin 99', '010-2099-0099', 'ADMIN'),
('admin100@test.com', 'password1234', 'Admin 100', '010-2100-0100', 'ADMIN');

-- 참고사항:
-- 비밀번호: 'password123' (모든 사용자 공통)
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
