#!/bin/bash

# TTL 만료 자동 좌석 해제 테스트 스크립트
# 사용법: ./test_ttl_expiration.sh [콘서트ID] [서버URL]

CONCERT_ID=${1:-1}
BASE_URL=${2:-"http://localhost:8080"}
AUTH_TOKEN=""

echo "⏰ TTL 만료 자동 좌석 해제 테스트 시작"
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

available_seats=$(echo $initial_status | jq -r '.data[] | select(.status == "AVAILABLE") | .id' | head -5)
echo "사용 가능한 좌석: $available_seats"

# 4. 좌석 점유
echo "🔒 4. 좌석 점유 중..."
seat_id=$(echo $available_seats | head -1)

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
    exit 1
fi

# 5. 점유 후 상태 조회
echo "📊 5. 점유 후 좌석 상태 조회..."
after_hold_status=$(curl -s -X GET "$BASE_URL/booking/seats/$CONCERT_ID" \
  -H "Authorization: Bearer $AUTH_TOKEN")

echo "점유 후 상태:"
echo $after_hold_status | jq ".data[] | select(.id == $seat_id) | {id, status, heldBy, remainingHoldTime}"

# 6. TTL 만료 모니터링 (10분 + 30초)
echo "⏰ 6. TTL 만료 모니터링 (10분 30초간)..."
echo "좌석 $seat_id의 TTL 만료를 모니터링합니다..."
echo "예상 만료 시간: $(date -d '+10 minutes' '+%H:%M:%S')"

for i in {1..630}; do  # 10분 30초 = 630초
    current_status=$(curl -s -X GET "$BASE_URL/booking/seats/$CONCERT_ID" \
      -H "Authorization: Bearer $AUTH_TOKEN")
    
    seat_status=$(echo $current_status | jq ".data[] | select(.id == $seat_id) | .status" -r)
    held_by=$(echo $current_status | jq ".data[] | select(.id == $seat_id) | .heldBy" -r)
    remaining_time=$(echo $current_status | jq ".data[] | select(.id == $seat_id) | .remainingHoldTime" -r)
    
    # 10초마다 상태 출력
    if [ $((i % 10)) -eq 0 ]; then
        echo "[$i/630] $(date '+%H:%M:%S') - 좌석 $seat_id: $seat_status (점유자: ${held_by:-"없음"}, 남은시간: ${remaining_time:-"0"}초)"
    fi
    
    # TTL 만료 확인
    if [[ "$seat_status" == "AVAILABLE" && "$remaining_time" == "0" ]]; then
        echo "✅ TTL 만료 확인됨! 좌석 $seat_id가 자동으로 AVAILABLE 상태로 변경되었습니다."
        echo "만료 시간: $(date '+%H:%M:%S')"
        break
    fi
    
    sleep 1
done

# 7. 만료 후 상태 확인
echo "📊 7. 만료 후 상태 확인..."
final_status=$(curl -s -X GET "$BASE_URL/booking/seats/$CONCERT_ID" \
  -H "Authorization: Bearer $AUTH_TOKEN")

echo "최종 상태:"
echo $final_status | jq ".data[] | select(.id == $seat_id) | {id, status, heldBy, remainingHoldTime}"

# 8. 만료 후 다른 사용자 점유 테스트
echo "🔓 8. 만료 후 다른 사용자 점유 테스트..."

# 다른 사용자로 로그인
echo "다른 사용자로 로그인 중..."
other_login_response=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user2@test.com",
    "password": "password123"
  }')

if [[ $other_login_response == *"success"* ]]; then
    OTHER_TOKEN=$(echo $other_login_response | jq -r '.data.token')
    echo "✅ 다른 사용자 로그인 성공"
    
    # 예매 토큰 발급
    other_token_response=$(curl -s -X POST "$BASE_URL/booking/token/$CONCERT_ID" \
      -H "Authorization: Bearer $OTHER_TOKEN")
    
    if [[ $other_token_response == *"success"* ]]; then
        echo "✅ 다른 사용자 예매 토큰 발급 성공"
        
        # 만료된 좌석 점유 시도
        echo "만료된 좌석 $seat_id 점유 시도..."
        other_hold_response=$(curl -s -X POST "$BASE_URL/booking/hold" \
          -H "Authorization: Bearer $OTHER_TOKEN" \
          -H "Content-Type: application/json" \
          -d "{\"seatId\": $seat_id}")
        
        if [[ $other_hold_response == *"success"* ]]; then
            echo "✅ 다른 사용자 좌석 점유 성공 - TTL 만료 후 정상 작동"
        else
            echo "❌ 다른 사용자 좌석 점유 실패: $(echo $other_hold_response | jq -r '.message')"
        fi
        
        # 최종 상태 확인
        final_other_status=$(curl -s -X GET "$BASE_URL/booking/seats/$CONCERT_ID" \
          -H "Authorization: Bearer $OTHER_TOKEN")
        
        echo "다른 사용자 점유 후 상태:"
        echo $final_other_status | jq ".data[] | select(.id == $seat_id) | {id, status, heldBy, remainingHoldTime}"
        
    else
        echo "❌ 다른 사용자 예매 토큰 발급 실패"
    fi
else
    echo "❌ 다른 사용자 로그인 실패"
fi

echo "✅ TTL 만료 자동 좌석 해제 테스트 완료!"
echo ""
echo "💡 테스트 결과:"
echo "- TTL 만료 시간: 10분"
echo "- 자동 해제 확인: $(if [[ "$seat_status" == "AVAILABLE" ]]; then echo "성공"; else echo "실패"; fi)"
echo "- 다른 사용자 점유 가능: $(if [[ "$other_hold_response" == *"success"* ]]; then echo "성공"; else echo "실패"; fi)" 