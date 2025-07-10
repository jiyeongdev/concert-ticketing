#!/bin/bash

set -e

print_banner() {
  echo "🎵 Concert Reservation System 시작 중..."
}

check_env_file() {
  if [ ! -f .env ]; then
    echo "❌ .env 파일이 없습니다. .env.example을 복사하여 .env 파일을 생성하세요."
    exit 1
  fi
}

ask_clean() {
  read -p "concert-reservation_mysql_data 볼륨을 완전히 초기화하시겠습니까? (clean.sh 실행) [y/N]: " confirm
  if [[ "$confirm" =~ ^[Yy]$ ]]; then
    ./clean.sh
  else
    echo "DB 초기화를 건너뜁니다. (기존 데이터가 남아있을 수 있습니다)"
  fi
}

start_infra() {
  echo "📦 MySQL, Redis, RabbitMQ 컨테이너만 우선 시작합니다..."
  docker-compose up -d mysql redis rabbitmq
  echo "⏳ DB/캐시/큐 서비스들이 준비될 때까지 대기 중..."
  sleep 30
  echo "🔍 DB 상태 확인 중..."
  docker-compose ps
}

run_init_scripts() {
  echo "Executing initialization scripts... (DDL 포함)"
  echo "DDL(01-create-database.sql) 실행 중..."
  docker exec -i $(docker-compose ps -q mysql) mysql -u root -p${MYSQL_ROOT_PASSWORD:-admin1234} < init-scripts/01-create-database.sql
  echo "01번 완료, 10초 대기..."
  sleep 10
  echo "초기 데이터(02-insert-initial-data.sql) 실행 중..."
  docker exec -i $(docker-compose ps -q mysql) mysql -u root -p${MYSQL_ROOT_PASSWORD:-admin1234} mydb < init-scripts/02-insert-initial-data.sql
  echo "02번 완료"
  echo "Initialization completed."
}

check_and_init_db() {
  if ! docker exec -i $(docker-compose ps -q mysql) mysql -u root -p${MYSQL_ROOT_PASSWORD:-admin1234} mydb -e "SHOW TABLES LIKE 'seat_holds';" | grep -q "seat_holds"; then
    echo "seat_holds 테이블이 없으므로 초기화 필요"
    run_init_scripts
  else
    echo "이미 초기화됨"
  fi
}

start_app() {
  echo "🚀 Spring Boot 애플리케이션 컨테이너를 시작합니다..."
  docker-compose up -d app
}

print_summary() {
  echo "✅ 모든 서비스가 시작되었습니다!"
  echo "🌐 애플리케이션: http://localhost:8080"
  echo "🐰 RabbitMQ 관리: http://localhost:15672 (guest/guest)"
  echo "️  MySQL: localhost:3306"
  echo "🔴 Redis: localhost:6379"
  echo "📋 로그를 확인하려면: docker-compose logs -f app"
}

# 메인 실행 흐름
print_banner
check_env_file
ask_clean
start_infra
check_and_init_db
start_app
print_summary 