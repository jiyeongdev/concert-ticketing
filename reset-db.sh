#!/bin/bash

echo "MySQL 컨테이너와 볼륨을 완전히 삭제합니다. (DB 완전 초기화)"
docker rm -f concert-mysql 2>/dev/null || true
docker volume rm mysql_data 2>/dev/null || true
echo "삭제 완료!" 