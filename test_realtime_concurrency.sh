#!/bin/bash

# 실시간 좌석 현황 및 동시성 제어 테스트 스크립트
# 사용법: ./test_realtime_concurrency.sh [콘서트ID] [서버URL]

CONCERT_ID=${1:-1}
BASE_URL=${2:-"http://localhost:8080"}

echo "🎭 실시간 좌석 현황 및 동시성 제어 테스트 시작"
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

# 4. 동시성 제어 테스트 - 같은 좌석을 여러 사용자가 동시에 점유 시도
echo "🔒 4. 동시성 제어 테스트 - 같은 좌석 동시 점유 시도..."
declare -a seat_ids=($(echo $available_seats | tr '\n' ' '))
target_seat_id=${seat_ids[0]}

echo "테스트 좌석: $target_seat_id"

# 백그라운드에서 동시에 같은 좌석 점유 시도
for i in "${!tokens[@]}"; do
    (
        echo "사용자 $((i+1))가 좌석 $target_seat_id 점유 시도..."
        hold_response=$(curl -s -X POST "$BASE_URL/booking/hold" \
          -H "Authorization: Bearer ${tokens[$i]}" \
          -H "Content-Type: application/json" \
          -d "{\"seatId\": $target_seat_id}")
        
        if [[ $hold_response == *"success"* ]]; then
            echo "✅ 사용자 $((i+1)) 좌석 $target_seat_id 점유 성공"
        else
            echo "❌ 사용자 $((i+1)) 좌석 $target_seat_id 점유 실패: $(echo $hold_response | jq -r '.message')"
        fi
    ) &
done

# 모든 백그라운드 작업 완료 대기
wait

# 5. 점유 후 상태 조회
echo "📊 5. 점유 후 좌석 상태 조회..."
after_hold_status=$(curl -s -X GET "$BASE_URL/booking/seats/$CONCERT_ID" \
  -H "Authorization: Bearer ${tokens[0]}")

echo "점유 후 상태:"
echo $after_hold_status | jq ".data[] | select(.id == $target_seat_id) | {id, status, heldBy, remainingHoldTime}"

# 6. 실시간 상태 변화 모니터링 (30초간)
echo "🔄 6. 실시간 상태 변화 모니터링 (30초간)..."
echo "좌석 $target_seat_id의 상태 변화를 모니터링합니다..."

for i in {1..30}; do
    current_status=$(curl -s -X GET "$BASE_URL/booking/seats/$CONCERT_ID" \
      -H "Authorization: Bearer ${tokens[0]}")
    
    seat_status=$(echo $current_status | jq ".data[] | select(.id == $target_seat_id) | .status" -r)
    held_by=$(echo $current_status | jq ".data[] | select(.id == $target_seat_id) | .heldBy" -r)
    remaining_time=$(echo $current_status | jq ".data[] | select(.id == $target_seat_id) | .remainingHoldTime" -r)
    
    echo "[$i/30] $(date '+%H:%M:%S') - 좌석 $target_seat_id: $seat_status (점유자: ${held_by:-"없음"}, 남은시간: ${remaining_time:-"0"}초)"
    
    sleep 1
done

# 7. 좌석 해제 후 다른 사용자 점유 테스트
echo "🔓 7. 좌석 해제 후 다른 사용자 점유 테스트..."

# 현재 점유자 찾기
current_holder=$(echo $after_hold_status | jq ".data[] | select(.id == $target_seat_id) | .heldBy" -r)
if [[ "$current_holder" != "null" && -n "$current_holder" ]]; then
    # 점유자 ID 추출 (사용자_11 -> 11)
    holder_id=$(echo $current_holder | sed 's/사용자_//')
    holder_index=$((holder_id - 11))  # user1은 index 0
    
    echo "현재 점유자: $current_holder (사용자 $((holder_index + 1)))"
    
    # 점유자로부터 좌석 해제
    echo "점유자로부터 좌석 해제 시도..."
    release_response=$(curl -s -X DELETE "$BASE_URL/booking/hold/$target_seat_id" \
      -H "Authorization: Bearer ${tokens[$holder_index]}")
    
    if [[ $release_response == *"success"* ]]; then
        echo "✅ 좌석 해제 성공"
        
        # 다른 사용자가 해제된 좌석 점유 시도
        other_user_index=$(((holder_index + 1) % ${#tokens[@]}))
        echo "사용자 $((other_user_index + 1))가 해제된 좌석 점유 시도..."
        
        hold_response=$(curl -s -X POST "$BASE_URL/booking/hold" \
          -H "Authorization: Bearer ${tokens[$other_user_index]}" \
          -H "Content-Type: application/json" \
          -d "{\"seatId\": $target_seat_id}")
        
        if [[ $hold_response == *"success"* ]]; then
            echo "✅ 다른 사용자 좌석 점유 성공"
        else
            echo "❌ 다른 사용자 좌석 점유 실패: $(echo $hold_response | jq -r '.message')"
        fi
        
        # 최종 상태 조회
        final_status=$(curl -s -X GET "$BASE_URL/booking/seats/$CONCERT_ID" \
          -H "Authorization: Bearer ${tokens[0]}")
        
        echo "최종 상태:"
        echo $final_status | jq ".data[] | select(.id == $target_seat_id) | {id, status, heldBy, remainingHoldTime}"
        
    else
        echo "❌ 좌석 해제 실패: $(echo $release_response | jq -r '.message')"
    fi
else
    echo "현재 점유자가 없습니다."
fi

echo "✅ 실시간 좌석 현황 및 동시성 제어 테스트 완료!" 