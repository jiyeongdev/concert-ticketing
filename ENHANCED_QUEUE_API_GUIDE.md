# 🎯 Redis + RabbitMQ 대기열 시스템 API 가이드

## 📋 개요

이 문서는 콘서트 예매 대기열 시스템의 새로운 API (v2)를 설명합니다. Redis와 RabbitMQ를 조합하여 대용량 처리를 지원합니다.

## 🏗️ 시스템 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client App    │    │   Redis         │    │   RabbitMQ      │
│                 │    │                 │    │                 │
│ - 대기열 입장    │───▶│ - 대기열 관리    │    │ - 순차 처리      │
│ - 상태 조회      │    │ - 순번 관리      │    │ - 메시지 큐     │
│ - 토큰 발급      │    │ - 상태 관리      │    │ - 입장 처리     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 🔄 전체 동작 흐름

### 1️⃣ 대기열 입장 (10분 전부터)
```
POST /v2/waiting-room/enter
```
- Redis에 사용자 등록 (ZSet 사용)
- 대기 순번 및 예상 시간 계산
- 상태: `WAITING`

### 2️⃣ 대기열 상태 조회
```
GET /v2/waiting-room/status/{concertId}
```
- Redis에서 순번 및 대기 시간 조회
- 실시간 상태 업데이트

### 3️⃣ 예매 오픈 (관리자/자동)
```
POST /v2/reservation/open/{concertId}?batchSize=10
```
- Redis에서 상위 N명 추출
- RabbitMQ에 메시지 발송
- Redis에서 해당 사용자들 제거

### 4️⃣ 예매 입장 처리 (Consumer)
- RabbitMQ 메시지 소비
- Redis에 사용자 상태를 `READY`로 변경
- 5분 TTL 설정

### 5️⃣ 예매 토큰 발급
```
GET /v2/reservation/token/{concertId}
```
- Redis에서 상태 확인 (`READY`인 경우만)
- 상태를 `ENTERED`로 변경
- 예매 페이지 입장 허용

## 📡 API 엔드포인트

### 🚪 대기열 입장
```http
POST /v2/waiting-room/enter
Content-Type: application/json
Authorization: Bearer {token}

{
  "concertId": "123"
}
```

**응답:**
```json
{
  "message": "대기열 입장 성공",
  "data": {
    "concertId": "123",
    "concertTitle": "콘서트 제목",
    "queueNumber": 15,
    "totalWaitingCount": 150,
    "estimatedWaitTime": 7,
    "estimatedEnterTime": "14:30:00",
    "status": "WAITING",
    "canEnter": false
  },
  "status": 201
}
```

### 📊 대기열 상태 조회
```http
GET /v2/waiting-room/status/{concertId}
Authorization: Bearer {token}
```

**응답:**
```json
{
  "message": "대기열 상태 조회 성공",
  "data": {
    "concertId": "123",
    "concertTitle": "콘서트 제목",
    "queueNumber": 15,
    "totalWaitingCount": 150,
    "estimatedWaitTime": 7,
    "estimatedEnterTime": "14:30:00",
    "status": "WAITING",
    "canEnter": false
  },
  "status": 200
}
```

### 🚪 대기열 나가기
```http
POST /v2/waiting-room/exit/{concertId}
Authorization: Bearer {token}
```

### 🎫 예매 토큰 발급
```http
GET /v2/reservation/token/{concertId}
Authorization: Bearer {token}
```

**응답 (성공 시):**
```json
{
  "message": "예매 토큰 발급 성공",
  "data": {
    "concertId": "123",
    "concertTitle": "콘서트 제목",
    "status": "ENTERED",
    "canEnter": true
  },
  "status": 200
}
```

### 🔓 예매 오픈 (관리자용)
```http
POST /v2/reservation/open/{concertId}?batchSize=10
Authorization: Bearer {admin_token}
```

### 👨‍💼 관리자용 대기열 조회
```http
GET /v2/waiting-room/admin/{concertId}
Authorization: Bearer {admin_token}
```

### 🧹 만료된 대기열 정리 (관리자용)
```http
POST /v2/waiting-room/cleanup
Authorization: Bearer {admin_token}
```

## 🔧 설정 옵션

### application.yml
```yaml
queue:
  concert:
    # 예매 시작 전 대기열 입장 가능 시간 (시간)
    join-start-hours: 4
    # 예매 시작 후 입장 간격 (초)
    entry-interval-seconds: 30
    # 예매 시작 후 그룹당 동시 입장 가능한 인원 수
    group-size: 10
    # 최대 대기 시간 (분)
    max-wait-time-minutes: 120
    # 좌석 점유 유지 시간 (분)
    hold-duration-minutes: 10
```

## 🗄️ Redis 데이터 구조

### 대기열 관리 (ZSet)
```
Key: concert:{concertId}
Value: {memberId} -> {timestamp}
```

### 사용자 상태 관리 (String)
```
Key: user:{memberId}:{concertId}
Value: WAITING | READY | ENTERED
TTL: 5분 (WAITING/READY), 1분 (ENTERED)
```

## 📨 RabbitMQ 메시지 구조

### 예매 입장 메시지
```json
{
  "memberId": "123",
  "concertId": "456",
  "memberName": "사용자명",
  "concertTitle": "콘서트 제목",
  "timestamp": "2024-01-01T10:00:00",
  "messageType": "ENTER"
}
```

## 🚀 성능 특징

### Redis
- **대기열 조회**: O(log N) - ZSet 사용
- **순번 계산**: O(log N) - ZRANK 명령
- **상태 관리**: O(1) - String 사용
- **TTL 자동 만료**: 메모리 효율성

### RabbitMQ
- **순차 처리**: FIFO 큐로 정확한 순서 보장
- **확장성**: 다중 Consumer 지원
- **내구성**: 메시지 영속성
- **장애 복구**: 자동 재시도

## 🔍 상태 코드

| 상태 | 설명 | canEnter |
|------|------|----------|
| `WAITING` | 대기열 대기 중 | false |
| `READY` | 예매 입장 준비 완료 | true |
| `ENTERED` | 예매 페이지 입장 완료 | true |
| `NOT_IN_QUEUE` | 대기열에 없음 | false |
| `BOOKING_CLOSED` | 예매 종료 | false |

## ⚠️ 에러 코드

| HTTP 상태 | 설명 |
|-----------|------|
| 400 | 잘못된 요청 (콘서트 ID 오류 등) |
| 401 | 인증 실패 |
| 403 | 권한 없음 (예매 토큰 발급 실패) |
| 409 | 상태 오류 (이미 대기열에 있음 등) |
| 500 | 서버 내부 오류 |

## 🎯 사용 시나리오

### 1. 일반 사용자 플로우
1. 예매 시작 4시간 전부터 대기열 입장 가능
2. 실시간 대기 순번 및 예상 시간 확인
3. 예매 시작 시 자동으로 입장 대상자 선정
4. 입장 대상자는 예매 토큰 발급 후 예매 페이지 접근

### 2. 관리자 플로우
1. 예매 시작 시간에 맞춰 예매 오픈 API 호출
2. 배치 크기 설정으로 순차적 입장 처리
3. 관리자용 대기열 조회로 실시간 모니터링
4. 필요시 만료된 대기열 정리

## 🔧 개발 환경 설정

### Redis 설정
```bash
# Redis 서버 시작
redis-server

# Redis CLI 접속
redis-cli

# 대기열 확인
ZRANGE concert:123 0 -1 WITHSCORES
```

### RabbitMQ 설정
```bash
# RabbitMQ 서버 시작
rabbitmq-server

# 관리자 페이지 접속
http://localhost:15672
# username: guest, password: guest
```

## 📈 모니터링

### Redis 모니터링
```bash
# Redis 메모리 사용량 확인
redis-cli info memory

# 대기열 크기 확인
redis-cli ZCARD concert:123
```

### RabbitMQ 모니터링
- 관리자 페이지: http://localhost:15672
- 큐 상태, 메시지 처리량, Consumer 상태 확인

## 🚀 배포 고려사항

### Redis 클러스터
- 대용량 처리 시 Redis Cluster 구성
- 마스터-슬레이브 복제로 고가용성 확보

### RabbitMQ 클러스터
- 다중 노드로 메시지 처리량 확장
- 메시지 영속성 설정으로 데이터 손실 방지

### 로드 밸런싱
- API 서버 다중화
- Redis/RabbitMQ 연결 풀 설정 