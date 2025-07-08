#!/bin/bash

# 테스트용 사용자 생성 스크립트

BASE_URL="http://localhost:8080"
TOTAL_USERS=50

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}=== 테스트용 사용자 생성 ===${NC}"
echo "총 생성할 사용자 수: $TOTAL_USERS"
echo ""

# 성공/실패 카운터
SUCCESS_COUNT=0
FAIL_COUNT=0

# 사용자 생성
for i in $(seq 1 $TOTAL_USERS); do
    email="user${i}@test.com"
    password="password123"
    name="사용자${i}"
    phone="010-1234-${i:0:4}"
    
    echo -n "사용자 ${i} 생성 중... "
    
    # 회원가입
    SIGNUP_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/signup" \
        -H "Content-Type: application/json" \
        -d "{
            \"email\": \"$email\",
            \"password\": \"$password\",
            \"name\": \"$name\",
            \"phone\": \"$phone\"
        }")
    
    # 응답 확인
    SUCCESS=$(echo $SIGNUP_RESPONSE | jq -r '.status')
    
    if [ "$SUCCESS" = "OK" ] || [ "$SUCCESS" = "CREATED" ]; then
        echo -e "${GREEN}성공${NC}"
        ((SUCCESS_COUNT++))
    else
        echo -e "${RED}실패${NC}"
        echo "응답: $SIGNUP_RESPONSE"
        ((FAIL_COUNT++))
    fi
    
    # 0.1초 간격
    sleep 0.1
done

echo ""
echo -e "${BLUE}=== 생성 완료 ===${NC}"
echo -e "${GREEN}성공: ${SUCCESS_COUNT}명${NC}"
echo -e "${RED}실패: ${FAIL_COUNT}명${NC}"
echo ""

echo "생성된 사용자 정보:"
echo "이메일: user1@test.com ~ user${TOTAL_USERS}@test.com"
echo "비밀번호: password123"
echo ""

echo "로그인 테스트:"
echo "curl -X POST $BASE_URL/auth/login \\"
echo "  -H \"Content-Type: application/json\" \\"
echo "  -d '{\"email\": \"user1@test.com\", \"password\": \"password123\"}'" 