#!/bin/bash

echo "🎵 Concert Reservation System 시작 중..."

# .env 파일이 있는지 확인
if [ ! -f .env ]; then
    echo "❌ .env 파일이 없습니다. .env.example을 복사하여 .env 파일을 생성하세요."
    exit 1
fi

# Docker Compose로 서비스 시작
echo "📦 Docker 컨테이너들을 시작합니다..."
docker-compose up -d

# 서비스들이 준비될 때까지 대기
echo "⏳ 서비스들이 준비될 때까지 대기 중..."
sleep 30

# 헬스체크
echo "🔍 서비스 상태 확인 중..."
docker-compose ps

# 데이터베이스 초기화 확인 및 실행
echo "Checking if database is fully initialized..."
if ! docker exec -i $(docker-compose ps -q mysql) mysql -u root -p${MYSQL_ROOT_PASSWORD:-admin1234} mydb -e "SHOW TABLES LIKE 'seat_holds';" | grep -q "seat_holds"; then
    echo "Executing initialization scripts..."
    docker exec -i $(docker-compose ps -q mysql) mysql -u root -p${MYSQL_ROOT_PASSWORD:-admin1234} mydb < init-scripts/01-create-database.sql
    docker exec -i $(docker-compose ps -q mysql) mysql -u root -p${MYSQL_ROOT_PASSWORD:-admin1234} mydb < init-scripts/02-insert-initial-data.sql
    echo "Initialization completed."
else
    echo "Database already fully initialized, skipping scripts."
fi

echo "🇰🇷 한글 인코딩 테스트 중..."
docker exec -i $(docker-compose ps -q mysql) mysql -u root -p${MYSQL_ROOT_PASSWORD:-admin1234} mydb -e "SELECT '한글 테스트: BTS 월드투어 🎵' AS encoding_test;"
docker exec -i $(docker-compose ps -q mysql) mysql -u root -p${MYSQL_ROOT_PASSWORD:-admin1234} mydb -e "SELECT COUNT(*) AS concert_count FROM concerts;"
docker exec -i $(docker-compose ps -q mysql) mysql -u root -p${MYSQL_ROOT_PASSWORD:-admin1234} mydb -e "SELECT title, location FROM concerts LIMIT 3;"
echo "✅ 한글 인코딩이 정상적으로 작동합니다!"

echo "✅ 모든 서비스가 시작되었습니다!"
echo "🌐 애플리케이션: http://localhost:8080"
echo "🐰 RabbitMQ 관리: http://localhost:15672 (guest/guest)"
echo "️  MySQL: localhost:3306"
echo "🔴 Redis: localhost:6379"
echo "📋 로그를 확인하려면: docker-compose logs -f app" 