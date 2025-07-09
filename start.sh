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

echo "✅ 모든 서비스가 시작되었습니다!"
echo "🌐 애플리케이션: http://localhost:${SERVER_PORT:-8080}"
echo "🐰 RabbitMQ 관리: http://localhost:${RABBITMQ_MANAGEMENT_PORT:-15672} (${RABBITMQ_USERNAME:-guest}/${RABBITMQ_PASSWORD:-guest})"
echo "️  MySQL: localhost:${MYSQL_PORT:-3306}"
echo "🔴 Redis: localhost:${REDIS_PORT:-6379}"

# 로그 확인
echo "📋 로그를 확인하려면: docker-compose logs -f app" 