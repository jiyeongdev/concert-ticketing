#!/bin/bash

# 여러 콘서트에 대한 대기열 테스트 스크립트

BASE_URL="http://localhost:8080"
TOTAL_USERS=40 

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m' # No Color

# 사용법 출력
if [ $# -eq 0 ]; then
    echo -e "${RED}사용법: $0 <콘서트ID1> [콘서트ID2] [콘서트ID3] ...${NC}"
    echo "예시: $0 8 11 15"
    echo "예시: $0 8"
    exit 1
fi

# 콘서트 ID 배열 생성
declare -a concert_ids
for arg in "$@"; do
    if [[ "$arg" =~ ^[0-9]+$ ]]; then
        concert_ids+=($arg)
    else
        echo -e "${RED}오류: '$arg'는 유효한 콘서트 ID가 아닙니다${NC}"
        exit 1
    fi
done

echo -e "${BLUE}=== 여러 콘서트 대기열 테스트 ===${NC}"
echo "테스트할 콘서트 ID: ${concert_ids[*]}"
echo "총 사용자 수: $TOTAL_USERS"
echo ""

# 각 콘서트에 대해 테스트 실행
for concert_id in "${concert_ids[@]}"; do
    echo -e "${PURPLE}=== 콘서트 ID: $concert_id 테스트 시작 ===${NC}"
    echo ""
    
    # 결과 저장용 배열
    declare -a user_tokens
    declare -a user_queue_numbers
    declare -a user_status

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

    # 2. 대기열 입장 (enter API 호출)
    echo -e "${YELLOW}2. 대기열 입장 API 호출 (콘서트 ID: $concert_id)...${NC}"

    for i in $(seq 1 $TOTAL_USERS); do
        if [ -n "${user_tokens[$i]}" ]; then
            echo -n "사용자 ${i} 대기열 입장 중... "
            
            # 대기열 입장 API 호출
            ENTER_RESPONSE=$(curl -s -X POST "$BASE_URL/ck/waiting-room/enter" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer ${user_tokens[$i]}" \
                -d "{\"concertId\": $concert_id}")
            
            # 응답 파싱
            QUEUE_NUMBER=$(echo $ENTER_RESPONSE | jq -r '.data.queueNumber')
            STATUS=$(echo $ENTER_RESPONSE | jq -r '.data.status')
            ESTIMATED_WAIT_TIME=$(echo $ENTER_RESPONSE | jq -r '.data.estimatedWaitTime')
            
            if [ "$STATUS" != "null" ] && [ "$STATUS" != "" ]; then
                user_queue_numbers[$i]=$QUEUE_NUMBER
                user_status[$i]=$STATUS
                echo -e "${GREEN}성공 (상태: $STATUS, 순번: $QUEUE_NUMBER, 예상대기: ${ESTIMATED_WAIT_TIME}분)${NC}"
            else
                echo -e "${RED}실패${NC}"
                echo "대기열 입장 응답: $ENTER_RESPONSE"
            fi
            
            # 0.2초 간격 (더 빠른 호출)
            sleep 0.2
        fi
    done

    echo ""
    echo -e "${GREEN}대기열 입장 완료 (콘서트 ID: $concert_id)${NC}"
    echo ""

    # 3. 대기열 상태 요약
    echo -e "${YELLOW}3. 대기열 상태 요약 (콘서트 ID: $concert_id)${NC}"
    echo "총 참가자 수: ${#user_queue_numbers[@]}"
    echo ""

    # 상태별 통계
    echo "상태별 분포:"
    declare -A status_count
    for i in $(seq 1 $TOTAL_USERS); do
        if [ -n "${user_status[$i]}" ]; then
            status=${user_status[$i]}
            if [ -z "${status_count[$status]}" ]; then
                status_count[$status]=0
            fi
            status_count[$status]=$((${status_count[$status]} + 1))
            echo "사용자 ${i}: 상태 ${user_status[$i]}, 순번 ${user_queue_numbers[$i]}"
        fi
    done

    echo ""
    echo "상태별 통계:"
    for status in "${!status_count[@]}"; do
        echo "  $status: ${status_count[$status]}명"
    done

    echo ""

    # 4. 대기열 상태 조회 (첫 번째 사용자)
    if [ -n "${user_tokens[1]}" ]; then
        echo -e "${YELLOW}4. 대기열 상태 조회 (첫 번째 사용자)${NC}"
        
        STATUS_RESPONSE=$(curl -s -X GET "$BASE_URL/ck/waiting-room/status/$concert_id" \
            -H "Authorization: Bearer ${user_tokens[1]}")
        
        echo "대기열 상태: $STATUS_RESPONSE"
        echo ""
    fi

    # 5. 대기열 상태 조회 (중간 사용자)
    if [ -n "${user_tokens[25]}" ]; then
        echo -e "${YELLOW}5. 대기열 상태 조회 (25번째 사용자)${NC}"
        
        STATUS_RESPONSE=$(curl -s -X GET "$BASE_URL/ck/waiting-room/status/$concert_id" \
            -H "Authorization: Bearer ${user_tokens[25]}")
        
        echo "대기열 상태: $STATUS_RESPONSE"
        echo ""
    fi

    # 6. 대기열 상태 조회 (마지막 사용자)
    if [ -n "${user_tokens[50]}" ]; then
        echo -e "${YELLOW}6. 대기열 상태 조회 (50번째 사용자)${NC}"
        
        STATUS_RESPONSE=$(curl -s -X GET "$BASE_URL/ck/waiting-room/status/$concert_id" \
            -H "Authorization: Bearer ${user_tokens[50]}")
        
        echo "대기열 상태: $STATUS_RESPONSE"
        echo ""
    fi

    echo -e "${PURPLE}=== 콘서트 ID: $concert_id 테스트 완료 ===${NC}"
    echo ""
    
    # 다음 콘서트 테스트 전 잠시 대기
    if [ ${#concert_ids[@]} -gt 1 ]; then
        echo -e "${BLUE}다음 콘서트 테스트를 위해 3초 대기...${NC}"
        sleep 3
        echo ""
    fi
done

echo -e "${BLUE}=== 모든 콘서트 테스트 완료 ===${NC}"
echo ""
echo "테스트 완료된 콘서트 ID: ${concert_ids[*]}"
echo ""
echo "참고사항:"
echo "- 실제 테스트 전에 콘서트의 openTime을 설정해야 합니다"
echo "- 예매 시작 전 4시간 전부터 대기열 입장 가능합니다"
echo "- 예매 시작 시간부터 순차적으로 입장 가능합니다"
echo "- 각 콘서트마다 $TOTAL_USERS명의 유저가 동시에 대기열에 입장하는 상황을 시뮬레이션합니다" 