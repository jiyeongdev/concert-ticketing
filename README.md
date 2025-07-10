# 🎵 Concert Reservation System

실시간 콘서트 예매 시스템으로, Spring Boot + Docker + Redis + RabbitMQ를 활용한 대기열 기반 예매 시스템입니다.

## 🌟 주요 기능

- **실시간 콘서트 예매**: WebSocket을 통한 실시간 좌석 상태 업데이트
- **대기열 시스템**: RabbitMQ 기반의 공정한 대기열 관리
- **사용자 인증**: JWT 
- **좌석 관리**: Redis 기반 실시간 좌석 점유 시스템
- **결제 시스템**: 안전한 결제 처리 및 검증
- **실시간 모니터링**: WebSocket을 통한 실시간 알림

##  기술 스택

### Backend
- **Spring Boot 3.2.3** (Java 21)
- **Spring Security** (JWT )
- **Spring Data JPA** (Hibernate)
- **Spring WebSocket** (실시간 통신)

### Database & Cache
- **MySQL 8.0** (메인 데이터베이스)
- **Redis 7** (캐시 + 좌석 상태 관리)

### Message Queue
- **RabbitMQ 3** (대기열 시스템)

### Container & DevOps
- **Docker** + **Docker Compose**
- **Gradle 8.5** (빌드 도구)

### API Documentation
- **SpringDoc OpenAPI 3** (Swagger UI)

## 🚀 빠른 시작

### 1. 사전 요구사항

시스템에 다음이 설치되어 있어야 합니다:
- **Docker** (20.10+)
- **Docker Compose** (2.0+)
- **Git**

### 2. 환경 설정

```bash
# 환경변수 파일 생성
로컬용 API 이므로 .env 까지 커밋.
```

### 3. 애플리케이션 환경 별 실행

> ⚠️ **중요**: 프로젝트를 실행 가이드
> 

#### 1. 도커 환경(배포/개발) 실행 (⭐ 가장 추천)

```bash
./start.sh
```
- docker-compose로 모든 서비스가 실행됩니다.
- Spring Boot는 application-dev.yml 설정이 자동 적용됩니다.

#### 2. 로컬 개발 환경 실행
  ##### 모든 서비스 자동 빌드 및 시작 (MySQL, Redis, RabbitMQ, Spring Boot)

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```
- application-local.yml의 설정이 적용됩니다.
- DB/Redis/RabbitMQ는 로컬에서 직접 실행되어야 합니다.


### 4. 서비스 접속

애플리케이션이 시작되면 다음 URL로 접속할 수 있습니다:

| 서비스 | URL | 설명 |
|--------|-----|------|
| **메인 애플리케이션** | http://localhost:8080 | 콘서트 예매 시스템 |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | API 문서 및 테스트 |
| **API Docs** | http://localhost:8080/v3/api-docs | OpenAPI 스펙 (JSON) |
| **RabbitMQ 관리** | http://localhost:15672 | 메시지 큐 관리 (guest/guest) |
| **MySQL** | localhost:3306 | 데이터베이스 (root/admin1234) |
| **Redis** | localhost:6379 | 캐시 서버 |

## WebSocket Reservation Test Page

- **WebSocket Reservation Test Page**: http://localhost:8080/websocket-reservation.html - 웹소켓을 통해 실시간 좌석 예약 기능을 테스트할 수 있는 HTML 페이지입니다.


## 콘서트 예매 대기열 시스템 - Redis와 RabbitMQ를 조합하여 대용량 처리를 지원합니다.

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

## 🎟️ 콘서트 예매 대기열 시스템
 - 대용량 트래픽을 위한 **Redis** + **RabbitMQ** 기반의 예매 대기열 시스템입니다.

---

### 주요 동작 및 엔드포인트

| 단계 | 엔드포인트/동작                        | 설명                                   | 상태값                | 사용 기술           |
|------|----------------------------------------|----------------------------------------|----------------------|--------------------|
| 1    | POST /waiting-room/enter               | 대기열 입장, 순번/예상시간 계산         | WAITING              | Spring, Redis      |
| 2    | GET /waiting-room/status/{concertId}   | 대기열 순번/상태/예상 대기시간 조회     | WAITING, READY, ENTERED | Spring, Redis      |
| 3    | (내부 처리: RabbitMQ/Redis)            | 상위 N명 추출, 메시지 발송, READY로 변경 | READY                | Redis, RabbitMQ    |
| 4    | POST /booking/token/{concertId}        | READY 확인 후 ENTERED로 변경, 토큰 발급  | ENTERED              | Spring, Redis      |

---

### 상태값 설명

| 상태값   | 의미                        |
|----------|-----------------------------|
| WAITING  | 대기열에 입장, 대기 중       |
| READY    | 예매 가능, N분 내 입장 가능   |
| ENTERED  | 예매 페이지 입장 완료        |
| (EXPIRED)| (선택) N분 내 미입장 시 만료 |

---

#### 예시 흐름

1. **대기열 입장** → WAITING  
2. **상태 조회** → WAITING / READY / ENTERED  
3. **대기열 퇴장** (선택)  
4. **예매 오픈** (내부적으로 READY로 변경)  
5. **예매 토큰 발급** → ENTERED

---



##  개발 및 관리

####  생성되는 컨테이너 정보

Docker Compose 실행 시 다음 컨테이너들이 생성됩니다:

| 컨테이너명 | 이미지 | 포트 | 설명 |
|------------|--------|------|------|
| `concert-app` | concert-reservation-app | 8080 | Spring Boot 메인 애플리케이션 |
| `concert-mysql` | mysql:8.0 | 3306 | MySQL 데이터베이스 |
| `concert-redis` | redis:7-alpine | 6379 | Redis 캐시 서버 |
| `concert-rabbitmq` | rabbitmq:3-management | 5672, 15672 | RabbitMQ 메시지 큐 |



### 로그 확인

```bash
# 전체 로그 확인
docker-compose logs

# 특정 서비스 로그 (실시간)
docker-compose logs -f app

# 최근 100줄만 확인
docker-compose logs --tail=100 app
```


## 📞 문의

jiyeong.dev@gmail.com

