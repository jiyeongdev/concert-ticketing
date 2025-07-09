# 콘서트 예매 시스템 API 문서

## 개요
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **웹소켓 테스트**: http://localhost:8080/websocket-reservation.html
- **API 버전**: v1.0
- **기본 URL**: http://localhost:8080

## 좌석 관리 API

### 1. 콘서트 좌석 목록 조회
- **엔드포인트**: `GET /seats/concert/{concertId}`
- **설명**: 특정 콘서트의 모든 좌석 정보를 조회합니다.
- **Path Variable**:
  - `concertId` (필수, Long): 콘서트 고유 ID
- **Request Body**: 없음
- **Response 예시**:
```json
{
  "message": "좌석 목록 조회 성공",
  "data": [
    {
      "id": 33,
      "concertId": 2,
      "seatGradeId": 1,
      "seatRow": "A",
      "seatNumber": "1",
      "positionX": 222,
      "positionY": 200,
      "status": "BOOKED",
      "gradeName": "VIP",
      "price": 150000
    },
    {
      "id": 34,
      "concertId": 2,
      "seatGradeId": 1,
      "seatRow": "B",
      "seatNumber": "2",
      "positionX": 130,
      "positionY": 200,
      "status": "AVAILABLE",
      "gradeName": "VIP",
      "price": 150000
    }
  ],
  "statusCode": 200
}
```

### 2. 콘서트 사용 가능한 좌석 수 조회
- **엔드포인트**: `GET /seats/concert/{concertId}/available-count`
- **설명**: 특정 콘서트의 예매 가능한 좌석 수를 조회합니다.
- **Path Variable**:
  - `concertId` (필수, Long): 콘서트 고유 ID
- **Request Body**: 없음
- **Response 예시**:
```json
{
  "message": "사용 가능한 좌석 수 조회 성공",
  "data": 850,
  "statusCode": 200
}
```

### 3. 좌석 생성
- **엔드포인트**: `POST /seats`
- **설명**: 새로운 좌석을 생성합니다. ADMIN 권한이 필요합니다.
- **Path Variable**: 없음
- **Request Body** (배열 형태):
```json
[
  {
    "concertId": 2,
    "seatGradeId": 1,
    "seatRow": "A",
    "seatNumber": "1",
    "positionX": 222,
    "positionY": 200,
    "status": "AVAILABLE"
  },
  {
    "concertId": 2,
    "seatGradeId": 1,
    "seatRow": "A",
    "seatNumber": "2",
    "positionX": 130,
    "positionY": 200,
    "status": "AVAILABLE"
  }
]
```
- **Response 예시**:
```json
{
  "message": "좌석 생성 성공",
  "data": [
    {
      "id": 33,
      "concertId": 2,
      "seatGradeId": 1,
      "seatRow": "A",
      "seatNumber": "1",
      "positionX": 222,
      "positionY": 200,
      "status": "AVAILABLE",
      "gradeName": "VIP",
      "price": 150000
    }
  ],
  "statusCode": 201
}
```

### 4. 콘서트 좌석등급별 좌석 조회
- **엔드포인트**: `GET /seats/concert/{concertId}/grade/{seatGradeId}`
- **설명**: 특정 콘서트의 특정 등급 좌석만 조회합니다.
- **Path Variable**:
  - `concertId` (필수, Long): 콘서트 고유 ID
  - `seatGradeId` (필수, Long): 좌석 등급 ID
- **Request Body**: 없음

### 5. 콘서트 상태별 좌석 조회
- **엔드포인트**: `GET /seats/concert/{concertId}/status/{status}`
- **설명**: 특정 콘서트의 특정 상태 좌석만 조회합니다.
- **Path Variable**:
  - `concertId` (필수, Long): 콘서트 고유 ID
  - `status` (필수, String): 좌석 상태 (AVAILABLE, OCCUPIED, RESERVED, SOLD, BOOKED)
- **Request Body**: 없음

### 6. 좌석 상세 조회
- **엔드포인트**: `GET /seats/{seatId}`
- **설명**: 좌석 ID로 특정 좌석의 상세 정보를 조회합니다.
- **Path Variable**:
  - `seatId` (필수, Long): 좌석 고유 ID
- **Request Body**: 없음
- **Response 예시**:
```json
{
  "message": "좌석 조회 성공",
  "data": {
    "id": 33,
    "concertId": 2,
    "seatGradeId": 1,
    "seatRow": "A",
    "seatNumber": "1",
    "positionX": 222,
    "positionY": 200,
    "status": "AVAILABLE",
    "gradeName": "VIP",
    "price": 150000
  },
  "statusCode": 200
}
```

## 공통 응답 형식

### 성공 응답
```json
{
  "message": "작업 성공 메시지",
  "data": "응답 데이터",
  "statusCode": 200
}
```

### 오류 응답
```json
{
  "message": "오류 메시지",
  "data": null,
  "statusCode": 400
}
```

## HTTP 상태 코드
- **200**: 성공
- **201**: 생성 성공
- **400**: 잘못된 요청
- **403**: 권한 없음
- **404**: 리소스를 찾을 수 없음
- **500**: 서버 내부 오류

## 인증
- **JWT 토큰**: Bearer 토큰 방식 사용
- **ADMIN 권한**: 좌석 생성, 수정, 삭제 시 필요

## 실시간 기능
- **WebSocket**: 실시간 좌석 상태 업데이트
- **테스트 페이지**: http://localhost:8080/websocket-reservation.html 