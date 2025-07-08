# 콘서트 좌석 관리 API 가이드

## 개요
이 API는 콘서트 정보와 좌석 정보를 분리된 컨트롤러로 관리하는 RESTful API입니다. 인터파크의 실제 좌석등급과 유사하게 구성되어 있으며, 좌석의 위치 정보(x, y 좌표)를 포함합니다.

## API 구조

### 1. ConcertController - 콘서트 정보 관리
### 2. SeatController - 좌석 정보 관리  
### 3. SeatGradeController - 좌석등급 관리

## 콘서트 CRUD API

### 1. 모든 콘서트 조회
```
GET /concerts
```

### 2. 콘서트 ID로 조회
```
GET /concerts/{id}
```

### 3. 콘서트 제목으로 검색
```
GET /concerts/search?title={title}
```

### 4. 콘서트 생성 (ADMIN만)
```
POST /concerts
```

### 5. 콘서트 수정 (ADMIN만)
```
PUT /concerts/{id}
```

### 6. 콘서트 삭제 (ADMIN만)
```
DELETE /concerts/{id}
```

## 좌석등급 CRUD API

### 1. 콘서트의 모든 좌석등급 조회
```
GET /seat-grades/concert/{concertId}
```

### 2. 좌석등급 ID로 조회
```
GET /seat-grades/{id}
```

### 3. 좌석등급 생성 (ADMIN만)
```
POST /seat-grades
```
**요청 본문**:
```json
{
  "concertId": 1,
  "gradeName": "VIP",
  "price": 150000
}
```

### 4. 좌석등급 수정 (ADMIN만)
```
PUT /seat-grades/{id}
```

### 5. 좌석등급 삭제 (ADMIN만)
```
DELETE /seat-grades/{id}
```

## 대기열 API

### 1. 대기열 참가
```
POST /queue/join
```
**요청 본문**:
```json
{
  "concertId": 1
}
```

### 2. 대기열 상태 조회
```
GET /queue/status/{concertId}
```

### 3. 대기열 입장 처리
```
POST /queue/enter/{concertId}
```
**주의**: 예매 시작 시간부터 순차적으로 입장 가능

### 4. 대기열 나가기
```
DELETE /queue/leave/{concertId}
```

### 5. 콘서트 대기열 조회 (관리자용)
```
GET /queue/admin/{concertId}
```

## 좌석 CRUD API

### 1. 콘서트의 모든 좌석 조회
```
GET /seats/concert/{concertId}
```

### 2. 콘서트의 특정 등급 좌석 조회
```
GET /seats/concert/{concertId}/grade/{seatGradeId}
```

### 3. 콘서트의 특정 상태 좌석 조회
```
GET /seats/concert/{concertId}/status/{status}
```
**상태**: AVAILABLE, HELD, BOOKED

### 4. 콘서트의 사용 가능한 좌석 수 조회
```
GET /seats/concert/{concertId}/available-count
```

### 5. 좌석 ID로 조회
```
GET /seats/{seatId}
```

### 6. 좌석 생성 (ADMIN만) - 배열 형태로 1개 이상 생성 가능
```
POST /seats
```
**요청 본문** (단일 좌석):
```json
[
  {
    "concertId": 1,
    "seatGradeId": 1,
    "seatRow": "A",
    "seatNumber": "1",
    "positionX": 100,
    "positionY": 200,
    "status": "AVAILABLE"
  }
]
```

**요청 본문** (여러 좌석):
```json
[
  {
    "concertId": 1,
    "seatGradeId": 1,
    "seatRow": "A",
    "seatNumber": "1",
    "positionX": 100,
    "positionY": 200,
    "status": "AVAILABLE"
  },
  {
    "concertId": 1,
    "seatGradeId": 1,
    "seatRow": "A",
    "seatNumber": "2",
    "positionX": 130,
    "positionY": 200,
    "status": "AVAILABLE"
  },
  {
    "concertId": 1,
    "seatGradeId": 1,
    "seatRow": "B",
    "seatNumber": "1",
    "positionX": 100,
    "positionY": 250,
    "status": "AVAILABLE"
  }
]
```

### 6-1. 콘서트별 좌석 생성 (ADMIN만) - 콘서트 ID로 좌석들 생성
```
POST /seats/concert/{concertId}
```
**요청 본문**:
```json
[
  {
    "concertId": 1,
    "seatGradeId": 1,
    "seatRow": "A",
    "seatNumber": "1",
    "positionX": 100,
    "positionY": 200,
    "status": "AVAILABLE"
  },
  {
    "concertId": 1,
    "seatGradeId": 1,
    "seatRow": "A",
    "seatNumber": "2",
    "positionX": 130,
    "positionY": 200,
    "status": "AVAILABLE"
  }
]
```

### 7. 좌석 수정 (ADMIN만)
```
PUT /seats/{seatId}
```

### 7-1. 콘서트별 좌석 수정 (ADMIN만) - 콘서트의 모든 좌석을 한번에 수정
```
PUT /seats/concert/{concertId}
```
**요청 본문**:
```json
[
  {
    "concertId": 1,
    "seatGradeId": 1,
    "seatRow": "A",
    "seatNumber": "1",
    "positionX": 100,
    "positionY": 200,
    "status": "AVAILABLE"
  },
  {
    "concertId": 1,
    "seatGradeId": 1,
    "seatRow": "A",
    "seatNumber": "2",
    "positionX": 130,
    "positionY": 200,
    "status": "AVAILABLE"
  }
]
```

### 8. 좌석 삭제 (ADMIN만)
```
DELETE /seats/{seatId}
```

### 8-1. 콘서트별 좌석 삭제 (ADMIN만) - 콘서트의 모든 좌석 삭제
```
DELETE /seats/concert/{concertId}
```

### 9. 좌석 상태 변경 (ADMIN만)
```
PATCH /seats/{seatId}/status/{status}
```

## 좌석등급 예시

### 인터파크 스타일 좌석등급
1. **VIP**: 150,000원 (최고 등급)
2. **R**: 120,000원 (프리미엄 등급)
3. **S**: 100,000원 (스탠다드 등급)
4. **A**: 80,000원 (일반 등급)
5. **B**: 60,000원 (경제 등급)

## 좌석 상태

- **AVAILABLE**: 예약 가능
- **HELD**: 임시 보유 (예약 진행 중)
- **BOOKED**: 예약 완료

## 좌석 좌표 정보

좌석의 위치 정보는 다음과 같이 구성됩니다:

- **positionX**: 좌석의 X 좌표 (가로 위치)
- **positionY**: 좌석의 Y 좌표 (세로 위치)

### 좌표 사용 예시
```json
{
  "id": 1,
  "concertId": 1,
  "seatGradeId": 1,
  "seatRow": "A",
  "seatNumber": "1",
  "positionX": 100,
  "positionY": 200,
  "status": "AVAILABLE",
  "gradeName": "VIP",
  "price": 150000
}
```

### 좌표 활용 방안
1. **좌석 배치도 시각화**: 프론트엔드에서 좌석 배치를 그래픽으로 표시
2. **좌석 선택 UI**: 사용자가 마우스로 좌석을 클릭하여 선택
3. **좌석 간격 계산**: 좌석 간의 거리 계산으로 사회적 거리두기 적용
4. **좌석 그룹화**: 가까운 좌석들을 그룹으로 묶어서 관리

## 캐시 관리

### 캐시 무효화
```
POST /concerts/cache/clear      # 콘서트 캐시 무효화
POST /seat-grades/cache/clear   # 좌석등급 캐시 무효화
POST /seats/cache/clear         # 좌석 캐시 무효화
```
- **권한**: ADMIN만

## 응답 형식

모든 API는 다음과 같은 형식으로 응답합니다:

```json
{
  "message": "성공 메시지",
  "data": {
    // 실제 데이터
  },
  "status": "OK"
}
```

## 에러 처리

- **400**: 잘못된 요청
- **401**: 인증 실패
- **403**: 권한 없음 (ADMIN 권한 필요)
- **404**: 리소스를 찾을 수 없음
- **500**: 서버 내부 오류

## 사용 예시

### 1. 콘서트 생성 후 좌석등급 설정
```bash
# 1. 콘서트 생성
POST /concerts
{
  "title": "2024 봄 콘서트",
  "location": "올림픽공원",
  "concertDate": "2024-03-15T19:00:00",
  "openTime": "2024-03-15T18:00:00",
  "closeTime": "2024-03-15T22:00:00"
}

# 2. 좌석등급 생성
POST /seat-grades
{
  "concertId": 1,
  "gradeName": "VIP",
  "price": 150000
}

# 3. 좌석 생성 (좌표 포함)
POST /seats
{
  "concertId": 1,
  "seatGradeId": 1,
  "seatRow": "A",
  "seatNumber": "1",
  "positionX": 100,
  "positionY": 200,
  "status": "AVAILABLE"
}
```

### 2. 좌석 정보 조회
```bash
# 모든 좌석 조회 (좌표 정보 포함)
GET /seats/concert/1

# VIP 등급 좌석만 조회
GET /seats/concert/1/grade/1

# 사용 가능한 좌석만 조회
GET /seats/concert/1/status/AVAILABLE

# 사용 가능한 좌석 수 조회
GET /seats/concert/1/available-count
```

### 3. 좌석등급 정보 조회
```bash
# 콘서트의 모든 좌석등급 조회
GET /seat-grades/concert/1

# 특정 좌석등급 조회
GET /seat-grades/1
```

## API 구조의 장점

### 1. 명확한 책임 분리
- **ConcertController**: 콘서트 정보만 관리
- **SeatController**: 좌석 정보만 관리
- **SeatGradeController**: 좌석등급 정보만 관리

### 2. 독립적인 개발 및 유지보수
- 각 컨트롤러가 독립적으로 개발/수정 가능
- 기능별로 명확한 분리로 코드 가독성 향상

### 3. 확장성
- 새로운 기능 추가 시 해당 컨트롤러만 수정
- 다른 컨트롤러에 영향 없음

### 4. 좌석 시각화 지원
- 좌표 정보로 인해 프론트엔드에서 좌석 배치도 구현 가능
- 사용자 친화적인 좌석 선택 UI 구현 가능 