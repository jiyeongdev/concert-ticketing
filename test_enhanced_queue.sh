#!/bin/bash

# Redis + RabbitMQ 대기열 시스템 테스트 스크립트
# 사용법: ./test_enhanced_queue.sh

BASE_URL="http://localhost:8080"
ADMIN_TOKEN="your-admin-token-here"
USER_TOKEN="your-user-token-here"
CONCERT_ID="1"

echo "🎯 Redis + RabbitMQ 대기열 시스템 테스트 시작"
echo "================================================"

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 함수: API 호출 및 결과 출력
test_api() {
    local method=$1
    local endpoint=$2
    local data=$3
    local token=$4
    
    echo -e "${BLUE}테스트: $method $endpoint${NC}"
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -H "Authorization: Bearer $token" \
            "$BASE_URL$endpoint")
    elif [ "$method" = "POST" ]; then
        if [ -n "$data" ]; then
            response=$(curl -s -w "\n%{http_code}" -H "Content-Type: application/json" \
                -H "Authorization: Bearer $token" -d "$data" \
                "$BASE_URL$endpoint")
        else
            response=$(curl -s -w "\n%{http_code}" -H "Authorization: Bearer $token" \
                "$BASE_URL$endpoint")
        fi
    fi
    
    # HTTP 상태 코드 추출
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | head -n -1)
    
    if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
        echo -e "${GREEN}✅ 성공 (HTTP $http_code)${NC}"
        echo "$body" | jq '.' 2>/dev/null || echo "$body"
    else
        echo -e "${RED}❌ 실패 (HTTP $http_code)${NC}"
        echo "$body"
    fi
    echo ""
}

# 1. 대기열 입장 테스트
echo -e "${YELLOW}1. 대기열 입장 테스트${NC}"
test_api "POST" "/v2/waiting-room/enter" \
    "{\"concertId\": \"$CONCERT_ID\"}" "$USER_TOKEN"

# 2. 대기열 상태 조회 테스트
echo -e "${YELLOW}2. 대기열 상태 조회 테스트${NC}"
test_api "GET" "/v2/waiting-room/status/$CONCERT_ID" "" "$USER_TOKEN"

# 3. 관리자용 대기열 조회 테스트
echo -e "${YELLOW}3. 관리자용 대기열 조회 테스트${NC}"
test_api "GET" "/v2/waiting-room/admin/$CONCERT_ID" "" "$ADMIN_TOKEN"

# 4. 예매 오픈 테스트 (관리자)
echo -e "${YELLOW}4. 예매 오픈 테스트 (관리자)${NC}"
test_api "POST" "/v2/reservation/open/$CONCERT_ID?batchSize=5" "" "$ADMIN_TOKEN"

# 5. 예매 토큰 발급 테스트
echo -e "${YELLOW}5. 예매 토큰 발급 테스트${NC}"
test_api "GET" "/v2/reservation/token/$CONCERT_ID" "" "$USER_TOKEN"

# 6. 대기열 나가기 테스트
echo -e "${YELLOW}6. 대기열 나가기 테스트${NC}"
test_api "POST" "/v2/waiting-room/exit/$CONCERT_ID" "" "$USER_TOKEN"

# 7. 만료된 대기열 정리 테스트 (관리자)
echo -e "${YELLOW}7. 만료된 대기열 정리 테스트 (관리자)${NC}"
test_api "POST" "/v2/waiting-room/cleanup" "" "$ADMIN_TOKEN"

echo -e "${GREEN}🎉 모든 테스트 완료!${NC}"

# Redis 상태 확인 (선택사항)
echo -e "${YELLOW}Redis 상태 확인:${NC}"
echo "Redis CLI에서 다음 명령어로 상태 확인:"
echo "  redis-cli ZCARD concert:$CONCERT_ID"
echo "  redis-cli ZRANGE concert:$CONCERT_ID 0 -1 WITHSCORES"
echo "  redis-cli KEYS 'user:*:$CONCERT_ID'"

# RabbitMQ 상태 확인 (선택사항)
echo -e "${YELLOW}RabbitMQ 상태 확인:${NC}"
echo "관리자 페이지: http://localhost:15672"
echo "  - Queues 탭에서 큐 상태 확인"
echo "  - Messages 탭에서 메시지 처리량 확인" 