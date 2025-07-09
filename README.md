# 🎵 Concert Reservation System

실시간 콘서트 예매 시스템으로, Spring Boot + Docker + Redis + RabbitMQ를 활용한 대기열 기반 예매 시스템입니다.

## 🌟 주요 기능

- **실시간 콘서트 예매**: WebSocket을 통한 실시간 좌석 상태 업데이트
- **대기열 시스템**: RabbitMQ 기반의 공정한 대기열 관리
- **사용자 인증**: JWT 
- **좌석 관리**: Redis 기반 실시간 좌석 점유 시스템
- **결제 시스템**: 안전한 결제 처리 및 검증
- **실시간 모니터링**: WebSocket을 통한 실시간 알림

## 🏗️ 기술 스택

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

### 2. 프로젝트 클론

```bash
git clone <repository-url>
cd concert-reservation
```

### 3. 환경 설정

```bash
# 환경변수 파일 생성
.env 포함됨
```

### 4. 애플리케이션 실행

> ⚠️ **중요**: 프로젝트를 처음 실행하거나 안정적으로 실행하려면 아래 방법 중 하나를 선택하세요!

#### 🎯 방법 1: 한번에 모든 서비스 실행 (⭐ 가장 추천)

```bash
# 모든 서비스 자동 빌드 및 시작 (MySQL, Redis, RabbitMQ, Spring Boot)
./start.sh
```
**✅ 이 방법이 가장 안전하고 확실합니다!**

#### 🔧 방법 2: 단계별 실행

```bash
# 앱만 빌드
docker-compose build app

# 모든 서비스 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f app
```

#### ⚙️ 방법 3: 특정 서비스만 먼저 실행

```bash
# 의존성 서비스 먼저 시작
docker-compose up mysql redis rabbitmq -d

# 앱 빌드
docker-compose build app

# 앱 실행
docker-compose up app -d
```

#### 🛑 서비스 중지

```bash
# 모든 서비스 중지
./stop.sh

# 또는
docker-compose down
```

### 5. 서비스 접속

애플리케이션이 시작되면 다음 URL로 접속할 수 있습니다:

| 서비스 | URL | 설명 |
|--------|-----|------|
| **메인 애플리케이션** | http://localhost:8080 | 콘서트 예매 시스템 |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | API 문서 및 테스트 |
| **API Docs** | http://localhost:8080/v3/api-docs | OpenAPI 스펙 (JSON) |
| **RabbitMQ 관리** | http://localhost:15672 | 메시지 큐 관리 (guest/guest) |
| **MySQL** | localhost:3306 | 데이터베이스 (concert_user/concert_pass) |
| **Redis** | localhost:6379 | 캐시 서버 |

## 📖 API 문서

### Swagger UI 사용법

1. 브라우저에서 http://localhost:8080/swagger-ui.html 접속
2. 상단에서 API 태그별로 정리된 엔드포인트 확인
3. 각 API를 클릭하여 상세 정보 확인
4. **"Try it out"** 버튼으로 직접 API 테스트 가능

## 🛠️ 개발 및 관리

### Docker 이미지 빌드 및 컨테이너 관리

#### 🏗️ Docker 이미지 빌드

```bash
# 1. Spring Boot 애플리케이션 이미지 빌드
docker-compose build app

# 2. 캐시 무시하고 새로 빌드 (소스 변경 시)
docker-compose build --no-cache app

# 3. 모든 서비스 이미지 빌드
docker-compose build

# 4. 개별 이미지 직접 빌드
docker build -t concert-reservation-app .
```

#### 📦 생성되는 컨테이너 정보

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