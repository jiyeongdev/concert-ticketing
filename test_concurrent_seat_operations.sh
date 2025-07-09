#!/bin/bash

# 동시 좌석 점유/해제 테스트 스크립트
# 사용법: ./test_concurrent_seat_operations.sh [콘서트ID] [서버URL]

CONCERT_ID=${1:-1}
BASE_URL=${2:-"http://localhost:8080"}

echo "🎭 동시 좌석 점유/해제 테스트 시작"
echo "콘서트 ID: $CONCERT_ID"
echo "서버 URL: $BASE_URL"
echo "=================================="

# 테스트용 사용자 정보
declare -a users=(
    "user1@test.com:password123"
    "user2@test.com:password123"
    "user3@test.com:password123"
    "user4@test.com:password123"
    "user5@test.com:password123"
)

# 1. 각 사용자별 토큰 획득
echo "🔐 1. 사용자별 토큰 획득 중..."
declare -a tokens=()

for i in "${!users[@]}"; do
    IFS=':' read -r email password <<< "${users[$i]}"
    
    login_response=$(curl -s -X POST "$BASE_URL/auth/login" \
      -H "Content-Type: application/json" \
      -d "{
        \"email\": \"$email\",
        \"password\": \"$password\"
      }")
    
    if [[ $login_response == *"success"* ]]; then
        token=$(echo $login_response | jq -r '.data.token')
        tokens[$i]=$token
        echo "✅ 사용자 $((i+1)) 토큰 획득"
    else
        echo "❌ 사용자 $((i+1)) 로그인 실패"
        exit 1
    fi
done

# 2. 모든 사용자에게 예매 토큰 발급
echo "🎫 2. 예매 토큰 발급 중..."
for i in "${!tokens[@]}"; do
    token_response=$(curl -s -X POST "$BASE_URL/booking/token/$CONCERT_ID" \
      -H "Authorization: Bearer ${tokens[$i]}")
    
    if [[ $token_response == *"success"* ]]; then
        echo "✅ 사용자 $((i+1)) 예매 토큰 발급 성공"
    else
        echo "❌ 사용자 $((i+1)) 예매 토큰 발급 실패"
    fi
done

# 3. 초기 좌석 상태 조회
echo "📊 3. 초기 좌석 상태 조회..."
initial_status=$(curl -s -X GET "$BASE_URL/booking/seats/$CONCERT_ID" \
  -H "Authorization: Bearer ${tokens[0]}")

available_seats=$(echo $initial_status | jq -r '.data[] | select(.status == "AVAILABLE") | .id' | head -10)
echo "사용 가능한 좌석: $available_seats"

# 4. 동시 좌석 점유 테스트
echo "🔒 4. 동시 좌석 점유 테스트..."
declare -a seat_ids=($(echo $available_seats | tr '\n' ' '))

# 백그라운드에서 동시에 좌석 점유
for i in "${!tokens[@]}"; do
    if [ $i -lt ${#seat_ids[@]} ]; then
        seat_id=${seat_ids[$i]}
        
        (
            echo "사용자 $((i+1))가 좌석 $seat_id 점유 시도..."
            hold_response=$(curl -s -X POST "$BASE_URL/booking/hold" \
              -H "Authorization: Bearer ${tokens[$i]}" \
              -H "Content-Type: application/json" \
              -d "{\"seatId\": $seat_id}")
            
            if [[ $hold_response == *"success"* ]]; then
                echo "✅ 사용자 $((i+1)) 좌석 $seat_id 점유 성공"
            else
                echo "❌ 사용자 $((i+1)) 좌석 $seat_id 점유 실패"
            fi
        ) &
    fi
done

# 모든 백그라운드 작업 완료 대기
wait

# 5. 점유 후 상태 조회
echo "📊 5. 점유 후 좌석 상태 조회..."
after_hold_status=$(curl -s -X GET "$BASE_URL/booking/seats/$CONCERT_ID" \
  -H "Authorization: Bearer ${tokens[0]}")

echo "점유 후 상태:"
echo $after_hold_status | jq '.data[] | {id, status, heldBy, remainingHoldTime}' | head -10

# 6. 동시 좌석 해제 테스트
echo "🔓 6. 동시 좌석 해제 테스트..."
for i in "${!tokens[@]}"; do
    if [ $i -lt ${#seat_ids[@]} ]; then
        seat_id=${seat_ids[$i]}
        
        (
            echo "사용자 $((i+1))가 좌석 $seat_id 해제 시도..."
            release_response=$(curl -s -X DELETE "$BASE_URL/booking/hold/$seat_id" \
              -H "Authorization: Bearer ${tokens[$i]}")
            
            if [[ $release_response == *"success"* ]]; then
                echo "✅ 사용자 $((i+1)) 좌석 $seat_id 해제 성공"
            else
                echo "❌ 사용자 $((i+1)) 좌석 $seat_id 해제 실패"
            fi
        ) &
    fi
done

# 모든 백그라운드 작업 완료 대기
wait

# 7. 해제 후 상태 조회
echo "📊 7. 해제 후 좌석 상태 조회..."
after_release_status=$(curl -s -X GET "$BASE_URL/booking/seats/$CONCERT_ID" \
  -H "Authorization: Bearer ${tokens[0]}")

echo "해제 후 상태:"
echo $after_release_status | jq '.data[] | {id, status, heldBy, remainingHoldTime}' | head -10

# 8. 실시간 상태 변화 모니터링
echo "🔄 8. 실시간 상태 변화 모니터링 (15초간)..."
echo "좌석 상태 변화를 모니터링합니다..."

for i in {1..15}; do
    current_status=$(curl -s -X GET "$BASE_URL/booking/seats/$CONCERT_ID" \
      -H "Authorization: Bearer ${tokens[0]}")
    
    available_count=$(echo $current_status | jq '.data[] | select(.status == "AVAILABLE") | .id' | wc -l)
    held_count=$(echo $current_status | jq '.data[] | select(.status == "HELD") | .id' | wc -l)
    booked_count=$(echo $current_status | jq '.data[] | select(.status == "BOOKED") | .id' | wc -l)
    
    echo "[$i/15] $(date '+%H:%M:%S') - AVAILABLE: $available_count, HELD: $held_count, BOOKED: $booked_count"
    
    sleep 1
done

echo "✅ 동시 좌석 점유/해제 테스트 완료!" 