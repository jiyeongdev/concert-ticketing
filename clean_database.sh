#!/bin/bash

echo "🗑️  로컬 데이터베이스 스키마 삭제 중..."

# Docker MySQL이 실행 중인지 확인
if docker-compose ps mysql | grep -q "Up"; then
    echo "📦 Docker MySQL에서 mydb 스키마를 삭제합니다..."
    
    # Docker MySQL에서 스키마 삭제
    docker-compose exec -T mysql mysql -u root -padmin1234 -e "
    DROP DATABASE IF EXISTS mydb;
    # CREATE DATABASE mydb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
    SHOW DATABASES;
    " 2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo "✅ Docker MySQL에서 mydb 스키마가 삭제되고 재생성되었습니다!"
    else
        echo "❌ Docker MySQL 접속에 실패했습니다."
    fi
else
    echo "⚠️  Docker MySQL이 실행되지 않고 있습니다."
fi

# 로컬 MySQL도 확인해서 삭제
echo "🔍 로컬 MySQL에서도 mydb 스키마를 확인합니다..."

# 로컬 MySQL에 접속 가능한지 확인
if command -v mysql &> /dev/null; then
    echo "💾 로컬 MySQL이 발견되었습니다. 수동으로 삭제하시겠습니까? (y/N)"
    read -r response
    
    if [[ "$response" =~ ^([yY][eE][sS]|[yY])$ ]]; then
        echo "다음 명령어를 실행하세요:"
        echo "mysql -u root -p"
        echo "그 다음 MySQL에서:"
        echo "DROP DATABASE IF EXISTS mydb;"
        echo "SHOW DATABASES;"
        echo "EXIT;"
    fi
else
    echo "💾 로컬 MySQL이 설치되지 않았거나 PATH에 없습니다."
fi

echo "🧹 완료! 이제 새로운 환경에서 시작할 수 있습니다."
echo "🚀 새로 시작하려면: ./start.sh" 