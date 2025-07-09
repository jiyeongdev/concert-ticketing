#!/bin/bash

echo "🧹 Concert Reservation System 정리 중..."

# 컨테이너, 볼륨, 네트워크 모두 삭제
docker-compose down -v --remove-orphans

# 빌드 캐시 삭제
docker system prune -f

echo "✅ 모든 리소스가 정리되었습니다." 