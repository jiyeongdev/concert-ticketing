#!/bin/bash

echo "🧹 Concert Reservation System 정리 중..."

# 이 프로젝트의 컨테이너, 네트워크, 볼륨(named volume)만 삭제
docker-compose down -v --remove-orphans

# 볼륨 삭제 (존재할 때만)
if docker volume ls -q | grep -q "^concert-reservation_mysql_data$"; then
  echo "concert-reservation_mysql_data 볼륨 삭제 실패... 수동으로 삭제해주세요."
else
  echo "concert-reservation_mysql_data 볼륨이 존재하지 않습니다. 건너뜁니다."
fi

echo "✅ 모든 리소스가 정리되었습니다." 
