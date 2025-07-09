# Concert Reservation System - Docker 실행 가이드

## 🚀 빠른 시작

### 1. 사전 요구사항
- Docker
- Docker Compose

### 2. 실행 방법

```bash
# 프로젝트 클론
git clone <repository-url>
cd concert-reservation

# 환경변수 파일 설정
cp env.example .env
# .env 파일을 편집하여 필요한 설정을 변경하세요

# 실행 권한 부여 (이미 되어있음)
chmod +x start.sh stop.sh clean.sh logs.sh

# 애플리케이션 시작
./start.sh
```

### 3. 서비스 접속 정보

- **애플리케이션**: http://localhost:8080
- **RabbitMQ 관리**: http://localhost:15672 (guest/guest)
- **MySQL**: localhost:3306
- **Redis**: localhost:6379

### 4. 유용한 명령어

```bash
# 로그 확인
./logs.sh app          # 애플리케이션 로그
./logs.sh mysql        # MySQL 로그
./logs.sh redis        # Redis 로그
./logs.sh rabbitmq     # RabbitMQ 로그
./logs.sh              # 모든 로그

# 서비스 중지
./stop.sh

# 모든 리소스 정리
./clean.sh

# 컨테이너 상태 확인
docker-compose ps

# 데이터베이스 접속
docker-compose exec mysql mysql -u concert_user -p mydb

# Redis CLI 접속
docker-compose exec redis redis-cli

# RabbitMQ 관리자 접속
docker-compose exec rabbitmq rabbitmqctl
```

### 5. 환경 변수 설정

`.env` 파일을 수정하여 환경 변수를 설정할 수 있습니다:

```env
# JWT 시크릿 키 (프로덕션에서는 반드시 변경)
JWT_SECRET=your-secret-key-here-change-in-production

# Google OAuth 설정 (필요시)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

### 6. 문제 해결

#### 포트 충돌
기본 포트가 사용 중인 경우 `.env` 파일에서 포트를 변경하세요:

```env
SERVER_PORT=8081  # 8080 대신 8081 사용
MYSQL_PORT=3307   # 3306 대신 3307 사용
```

#### 데이터베이스 연결 문제
MySQL이 완전히 시작될 때까지 기다리지 못하는 경우, `docker-compose.yml`의 `depends_on` 섹션을 확인하세요.

#### Redis Key Events 설정
Redis는 자동으로 Key Events가 활성화되도록 설정되어 있습니다 (`notify-keyspace-events Ex`).

### 7. 개발 모드

개발 중에는 다음 명령어로 실시간 로그를 확인할 수 있습니다:

```bash
# 실시간 로그 확인
./logs.sh

# 특정 서비스만 로그 확인
./logs.sh app
```

### 8. 프로덕션 배포

프로덕션 환경에서는 다음 사항을 고려하세요:

1. JWT_SECRET을 강력한 랜덤 문자열로 변경
2. 데이터베이스 비밀번호를 강력하게 설정
3. 방화벽 설정으로 포트 접근 제한
4. SSL/TLS 인증서 설정
5. 로그 로테이션 설정

## 📋 시스템 아키텍처

### 서비스 구성
1. **Spring Boot 애플리케이션** (포트: 8080)
   - REST API 서비스
   - WebSocket 지원
   - JWT 인증
   - OAuth2 지원

2. **MySQL 데이터베이스** (포트: 3306)
   - 사용자 정보
   - 콘서트 정보
   - 예매 정보
   - 좌석 정보

3. **Redis 캐시** (포트: 6379)
   - 세션 관리
   - 좌석 점유 상태
   - 대기열 관리
   - Key Events 알림

4. **RabbitMQ 메시지 큐** (포트: 5672, 관리: 15672)
   - 비동기 메시지 처리
   - 대기열 관리
   - 이벤트 발행/구독

### 주요 기능
- **사용자 인증**: JWT + OAuth2
- **콘서트 예매**: 실시간 좌석 예매
- **대기열 시스템**: RabbitMQ 기반 대기열
- **실시간 알림**: WebSocket + Redis Key Events
- **좌석 관리**: Redis 기반 실시간 좌석 상태
- **결제 시스템**: 결제 처리 및 검증 