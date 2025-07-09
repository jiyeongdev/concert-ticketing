#!/bin/bash

# 실시간 좌석 상태 조회 테스트 스크립트
# 사용법: ./test_realtime_seat_status.sh [콘서트ID] [서버URL]

CONCERT_ID=${1:-1}
BASE_URL=${2:-"http://localhost:8080"}
AUTH_TOKEN=""

echo "🎭 실시간 좌석 상태 조회 테스트 시작"
echo "콘서트 ID: $CONCERT_ID"
echo "서버 URL: $BASE_URL"
echo "=================================="

# 1. 로그인 및 토큰 획득
echo "🔐 1. 로그인 중..."
login_response=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user1@test.com",
    "password": "password123"
  }')

if [[ $login_response == *"success"* ]]; then
    AUTH_TOKEN=$(echo $login_response | jq -r '.data.token')
    echo "✅ 로그인 성공, 토큰 획득"
else
    echo "❌ 로그인 실패: $login_response"
    exit 1
fi

# 2. 예매 토큰 발급
echo "🎫 2. 예매 토큰 발급 중..."
token_response=$(curl -s -X POST "$BASE_URL/booking/token/$CONCERT_ID" \
  -H "Authorization: Bearer $AUTH_TOKEN")

if [[ $token_response == *"success"* ]]; then
    echo "✅ 예매 토큰 발급 성공"
else
    echo "❌ 예매 토큰 발급 실패: $token_response"
    exit 1
fi

# 3. 초기 좌석 상태 조회
echo "📊 3. 초기 좌석 상태 조회..."
initial_status=$(curl -s -X GET "$BASE_URL/booking/seats/$CONCERT_ID" \
  -H "Authorization: Bearer $AUTH_TOKEN")

echo "초기 상태:"
echo $initial_status | jq '.data[] | {id, status, heldBy, remainingHoldTime}' | head -10

# 4. 좌석 점유 테스트
echo "🔒 4. 좌석 점유 테스트..."
seat_id=$(echo $initial_status | jq -r '.data[0].id')

hold_response=$(curl -s -X POST "$BASE_URL/booking/hold" \
  -H "Authorization: Bearer $AUTH_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"seatId\": $seat_id
  }")

if [[ $hold_response == *"success"* ]]; then
    echo "✅ 좌석 점유 성공: 좌석 $seat_id"
else
    echo "❌ 좌석 점유 실패: $hold_response"
fi

# 5. 점유 후 상태 조회
echo "📊 5. 점유 후 좌석 상태 조회..."
after_hold_status=$(curl -s -X GET "$BASE_URL/booking/seats/$CONCERT_ID" \
  -H "Authorization: Bearer $AUTH_TOKEN")

echo "점유 후 상태:"
echo $after_hold_status | jq ".data[] | select(.id == $seat_id) | {id, status, heldBy, remainingHoldTime}"

# 6. 좌석 해제 테스트
echo "🔓 6. 좌석 해제 테스트..."
release_response=$(curl -s -X DELETE "$BASE_URL/booking/hold/$seat_id" \
  -H "Authorization: Bearer $AUTH_TOKEN")

if [[ $release_response == *"success"* ]]; then
    echo "✅ 좌석 해제 성공: 좌석 $seat_id"
else
    echo "❌ 좌석 해제 실패: $release_response"
fi

# 7. 해제 후 상태 조회
echo "📊 7. 해제 후 좌석 상태 조회..."
after_release_status=$(curl -s -X GET "$BASE_URL/booking/seats/$CONCERT_ID" \
  -H "Authorization: Bearer $AUTH_TOKEN")

echo "해제 후 상태:"
echo $after_release_status | jq ".data[] | select(.id == $seat_id) | {id, status, heldBy, remainingHoldTime}"

# 8. 실시간 상태 변화 모니터링
echo "🔄 8. 실시간 상태 변화 모니터링 (10초간)..."
echo "좌석 $seat_id의 상태 변화를 모니터링합니다..."

for i in {1..10}; do
    current_status=$(curl -s -X GET "$BASE_URL/booking/seats/$CONCERT_ID" \
      -H "Authorization: Bearer $AUTH_TOKEN")
    
    seat_status=$(echo $current_status | jq ".data[] | select(.id == $seat_id) | .status" -r)
    remaining_time=$(echo $current_status | jq ".data[] | select(.id == $seat_id) | .remainingHoldTime" -r)
    
    echo "[$i/10] $(date '+%H:%M:%S') - 좌석 $seat_id: $seat_status (남은시간: ${remaining_time}s)"
    
    sleep 1
done

echo "✅ 실시간 좌석 상태 조회 테스트 완료!" 