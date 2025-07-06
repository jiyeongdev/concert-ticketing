# 대기열 시스템 환경변수 설정 가이드

## 개요
콘서트 예매 시스템의 대기열 기능에서 사용되는 환경변수들을 설정할 수 있습니다.

## 환경변수 설정

### 1. 예매 시작 전 대기열 참가 가능 시간
```yaml
queue:
  concert:
    join-start-hours: 4  # 예매 시작 전 4시간 전부터 대기열 참가 가능
```

**설정 가능한 값**:
- `1`: 예매 시작 전 1시간 전부터 대기열 참가 가능
- `2`: 예매 시작 전 2시간 전부터 대기열 참가 가능
- `4`: 예매 시작 전 4시간 전부터 대기열 참가 가능 (기본값)
- `6`: 예매 시작 전 6시간 전부터 대기열 참가 가능

### 2. 대기열 관련 기타 설정
```yaml
queue:
  concert:
    waiting-prefix: "concert.waiting."      # 대기열 큐 접두사
    processing-prefix: "concert.processing." # 처리 큐 접두사
    entry-interval-seconds: 30              # 30초마다 한 명씩 입장
    max-wait-time-minutes: 120              # 최대 대기 시간 2시간
    hold-duration-minutes: 10               # 좌석 점유 시간 10분
    join-start-hours: 4                     # 예매 시작 전 4시간 전부터 대기열 참가 가능
```

## 동작 방식

### 1. 대기열 참가 (JOIN) - "예매 오픈 전 대기열 등록"
- **시점**: 예매 시작 전 `join-start-hours`시간 전부터만 가능
- **동작**: 순번을 받고 대기열에 등록
- **제한**: 시간 기반 (콘서트의 `openTime` 기준)

### 2. 대기열 입장 (ENTER) - "순차적 입장 처리"
- **시점**: 예매 시작 시간부터 순차적으로 입장
- **동작**: 대기열에서 제거되고 예매 처리 큐로 이동
- **제한**: 시간 기반 (콘서트의 `openTime` 기준)

### 3. 시간 계산 예시
```
콘서트 예매 시작 시간: 2024-01-15 14:00:00
join-start-hours: 4

→ 대기열 참가 시작 시간: 2024-01-15 10:00:00 (4시간 전)
→ 대기열 입장 시작 시간: 2024-01-15 14:00:00 (예매 시작 시간)
```

## 로그 확인

대기열 참가 및 입장 가능 여부는 다음과 같은 로그로 확인할 수 있습니다:
```
콘서트 1 대기열 참가 시작 시간: 2024-01-15T10:00:00 (현재: 2024-01-15T09:30:00)
콘서트 1 예매 시작 시간: 2024-01-15T14:00:00 (현재: 2024-01-15T13:45:00, 순번: 1)
```

## API 응답 예시

### 대기열 상태 조회 응답
```json
{
  "message": "대기열 상태 조회 성공",
  "data": {
    "concertId": 1,
    "concertTitle": "2024 봄 콘서트",
    "queueNumber": 1,
    "totalWaitingCount": 5,
    "estimatedWaitTime": 0,
    "estimatedEnterTime": "즉시 입장 가능",
    "status": "WAITING",
    "canEnter": false
  },
  "status": "OK"
}
```

- `canEnter`: `true` - 입장 가능
- `canEnter`: `false` - 아직 입장 불가 (시간이 되지 않음)
- `status`: `CAN_ENTER` - 입장 가능 상태
- `status`: `WAITING` - 대기 중 상태 