#!/bin/bash

# 대기열 입장 기능 테스트 스크립트
# 예매 시작 전 10분부터 입장 가능한 기능을 테스트합니다.

BASE_URL="http://localhost:8080"
CONCERT_ID=1

echo "=== 대기열 입장 기능 테스트 ==="
echo "콘서트 ID: $CONCERT_ID"
echo ""

# 1. 대기열 참가
echo "1. 대기열 참가 테스트"
JOIN_RESPONSE=$(curl -s -X POST "$BASE_URL/queue/join" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d "{\"concertId\": $CONCERT_ID}")

echo "대기열 참가 응답: $JOIN_RESPONSE"
echo ""

# 2. 대기열 상태 조회
echo "2. 대기열 상태 조회 테스트"
STATUS_RESPONSE=$(curl -s -X GET "$BASE_URL/queue/status/$CONCERT_ID" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE")

echo "대기열 상태 응답: $STATUS_RESPONSE"
echo ""

# 3. 입장 가능 여부 확인
echo "3. 입장 가능 여부 확인"
CAN_ENTER=$(echo $STATUS_RESPONSE | jq -r '.data.canEnter')
STATUS=$(echo $STATUS_RESPONSE | jq -r '.data.status')

echo "입장 가능 여부: $CAN_ENTER"
echo "상태: $STATUS"
echo ""

# 4. 대기열 입장 시도 (조건부)
if [ "$CAN_ENTER" = "true" ]; then
    echo "4. 대기열 입장 시도 (입장 가능한 경우)"
    ENTER_RESPONSE=$(curl -s -X POST "$BASE_URL/queue/enter/$CONCERT_ID" \
      -H "Authorization: Bearer YOUR_TOKEN_HERE")
    
    echo "대기열 입장 응답: $ENTER_RESPONSE"
else
    echo "4. 대기열 입장 시도 (아직 입장 불가)"
    ENTER_RESPONSE=$(curl -s -X POST "$BASE_URL/queue/enter/$CONCERT_ID" \
      -H "Authorization: Bearer YOUR_TOKEN_HERE")
    
    echo "대기열 입장 응답: $ENTER_RESPONSE"
fi
echo ""

# 5. 환경변수 확인
echo "5. 현재 설정된 환경변수 확인"
echo "early-entry-minutes: $(grep 'early-entry-minutes' src/main/resources/application.yml | cut -d':' -f2 | tr -d ' ')"
echo ""

echo "=== 테스트 완료 ==="
echo ""
echo "참고사항:"
echo "- 예매 시작 전 4시간 전부터 대기열 참가 가능합니다 (환경변수로 설정 가능)"
echo "- 예매 시작 시간부터 순차적으로 입장 가능합니다"
echo "- 콘서트의 openTime을 기준으로 계산됩니다"
echo "- 실제 테스트 시에는 유효한 JWT 토큰을 사용해야 합니다" 