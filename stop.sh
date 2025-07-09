#!/bin/bash

echo "🛑 Concert Reservation System 중지 중..."

# Docker Compose로 서비스 중지
docker-compose down

# 모든 컨테이너와 볼륨 삭제
docker-compose down -v

echo "✅ 모든 서비스가 중지/삭제되었습니다." 