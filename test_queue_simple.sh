#!/bin/bash

# 간단한 대기열 테스트 스크립트 (이미 사용자가 있다고 가정)

BASE_URL="http://localhost:8080"
CONCERT_ID=8
TOTAL_USERS=10  # 테스트용으로 10명만

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== 간단한 대기열 테스트 ===${NC}"
echo "콘서트 ID: $CONCERT_ID"
echo "총 사용자 수: $TOTAL_USERS"
echo ""

# 결과 저장용 배열
declare -a user_tokens
declare -a user_queue_numbers

# 1. 기존 사용자로 로그인하여 토큰 생성
echo -e "${YELLOW}1. 사용자 로그인 및 토큰 생성...${NC}"

for i in $(seq 1 $TOTAL_USERS); do
    email="user${i}@test.com"
    password="password123"
    
    echo -n "사용자 ${i} 로그인 중... "
    
    # 로그인
    LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/ck/auth/login" \
        -H "Content-Type: application/json" \
        -d "{
            \"email\": \"$email\",
            \"password\": \"$password\"
        }")
    
    # 토큰 추출
    TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.accessToken')
    
    if [ "$TOKEN" != "null" ] && [ "$TOKEN" != "" ]; then
        user_tokens[$i]=$TOKEN
        echo -e "${GREEN}성공${NC}"
    else
        echo -e "${RED}실패${NC}"
        echo "로그인 응답: $LOGIN_RESPONSE"
    fi
done

echo ""
echo -e "${GREEN}토큰 생성 완료: ${#user_tokens[@]}개${NC}"
echo ""

# 2. 대기열 참가
echo -e "${YELLOW}2. 대기열 순차 참가...${NC}"

for i in $(seq 1 $TOTAL_USERS); do
    if [ -n "${user_tokens[$i]}" ]; then
        echo -n "사용자 ${i} 대기열 참가 중... "
        
        # 대기열 참가
        JOIN_RESPONSE=$(curl -s -X POST "$BASE_URL/ck/queue/join" \
            -H "Content-Type: application/json" \
            -H "Authorization: Bearer ${user_tokens[$i]}" \
            -d "{\"concertId\": $CONCERT_ID}")
        
        # 응답 파싱
        QUEUE_NUMBER=$(echo $JOIN_RESPONSE | jq -r '.data.queueNumber')
        STATUS=$(echo $JOIN_RESPONSE | jq -r '.data.status')
        
        if [ "$QUEUE_NUMBER" != "null" ] && [ "$QUEUE_NUMBER" != "" ]; then
            user_queue_numbers[$i]=$QUEUE_NUMBER
            echo -e "${GREEN}성공 (순번: $QUEUE_NUMBER)${NC}"
        else
            echo -e "${RED}실패${NC}"
            echo "대기열 참가 응답: $JOIN_RESPONSE"
        fi
        
        # 0.5초 간격
        sleep 0.5
    fi
done

echo ""
echo -e "${GREEN}대기열 참가 완료${NC}"
echo ""

# 3. 대기열 상태 요약
echo -e "${YELLOW}3. 대기열 상태 요약${NC}"
echo "총 참가자 수: ${#user_queue_numbers[@]}"
echo ""

# 순번별 통계
echo "순번별 분포:"
for i in $(seq 1 $TOTAL_USERS); do
    if [ -n "${user_queue_numbers[$i]}" ]; then
        echo "사용자 ${i}: 순번 ${user_queue_numbers[$i]}"
    fi
done

echo ""

# 4. 대기열 상태 조회 (첫 번째 사용자)
if [ -n "${user_tokens[1]}" ]; then
    echo -e "${YELLOW}4. 대기열 상태 조회 (첫 번째 사용자)${NC}"
    
    STATUS_RESPONSE=$(curl -s -X GET "$BASE_URL/ck/queue/status/$CONCERT_ID" \
        -H "Authorization: Bearer ${user_tokens[1]}")
    
    echo "대기열 상태: $STATUS_RESPONSE"
    echo ""
fi

# 5. 대기열 입장 시도 (첫 번째 사용자)
if [ -n "${user_tokens[1]}" ]; then
    echo -e "${YELLOW}5. 대기열 입장 시도 (첫 번째 사용자)${NC}"
    
    ENTER_RESPONSE=$(curl -s -X POST "$BASE_URL/ck/queue/enter/$CONCERT_ID" \
        -H "Authorization: Bearer ${user_tokens[1]}")
    
    echo "입장 시도 결과: $ENTER_RESPONSE"
    echo ""
fi

echo -e "${BLUE}=== 테스트 완료 ===${NC}"
echo ""
echo "참고사항:"
echo "- 실제 테스트 전에 콘서트의 openTime을 설정해야 합니다"
echo "- 예매 시작 전 4시간 전부터 대기열 참가 가능합니다"
echo "- 예매 시작 시간부터 순차적으로 입장 가능합니다" 