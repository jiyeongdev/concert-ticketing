package com.sdemo1.config;

/**
 * Swagger API 문서용 예시 데이터 클래스
 * API 요청/응답 예시를 중앙에서 관리
 */
public class SwaggerExamples {

    // ========== 1. Auth API Examples ==========
    
    public static final String SIGNUP_REQUEST = """
        {
            "email": "user@example.com",
            "password": "password123",
            "name": "홍길동",
            "phone": "010-1234-5678",
            "role": "USER"
        }
        """;
    
    public static final String SIGNUP_RESPONSE_SUCCESS = """
        {
            "message": "회원가입이 완료되었습니다.",
            "data": {
                "memberId": 1,
                "email": "user@example.com",
                "name": "홍길동",
                "role": "USER"
            },
            "status": "OK"
        }
        """;
    
    public static final String SIGNUP_RESPONSE_ERROR = """
        {
            "message": "이미 존재하는 이메일입니다.",
            "data": null,
            "status": "BAD_REQUEST"
        }
        """;
    
    public static final String LOGIN_REQUEST = """
        {
            "email": "user@example.com",
            "password": "password123"
        }
        """;
    
    public static final String LOGIN_RESPONSE_SUCCESS = """
        {
            "message": "로그인 성공",
            "data": {
                "memberId": 1,
                "email": "user@example.com",
                "name": "홍길동",
                "role": "USER",
                "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            },
            "status": "OK"
        }
        """;
    
    public static final String LOGIN_RESPONSE_ERROR = """
        {
            "message": "이메일 또는 비밀번호가 올바르지 않습니다.",
            "data": null,
            "status": "UNAUTHORIZED"
        }
        """;

    // ========== 2. Concert API Examples ==========
    
    public static final String CONCERT_LIST_RESPONSE = """
        {
            "message": "콘서트 목록 조회 성공",
            "data": [
                {
                    "concertId": 1,
                    "title": "2024 봄 콘서트",
                    "artist": "아티스트",
                    "venue": "올림픽공원",
                    "concertDate": "2024-03-15T19:00:00",
                    "price": 80000,
                    "totalSeats": 1000,
                    "availableSeats": 800
                }
            ],
            "status": "OK"
        }
        """;
    
    public static final String CONCERT_CREATE_REQUEST = """
        {
            "title": "2024 봄 콘서트",
            "artist": "아티스트",
            "venue": "올림픽공원",
            "concertDate": "2024-03-15T19:00:00",
            "price": 80000,
            "totalSeats": 1000
        }
        """;
    
    public static final String CONCERT_CREATE_RESPONSE = """
        {
            "message": "콘서트 생성 성공",
            "data": {
                "concertId": 1,
                "title": "2024 봄 콘서트",
                "artist": "아티스트",
                "venue": "올림픽공원",
                "concertDate": "2024-03-15T19:00:00",
                "price": 80000,
                "totalSeats": 1000,
                "availableSeats": 1000
            },
            "status": "CREATED"
        }
        """;

    // ========== 3. Queue API Examples ==========
    
    public static final String QUEUE_ENTER_REQUEST = """
        {
            "concertId": 1
        }
        """;
    
    public static final String QUEUE_ENTER_RESPONSE = """
        {
            "message": "대기열 입장 성공",
            "data": {
                "status": "WAITING",
                "position": 15,
                "estimatedWaitTime": 900,
                "concertId": 1
            },
            "status": "CREATED"
        }
        """;
    
    public static final String QUEUE_STATUS_RESPONSE = """
        {
            "message": "대기열 상태 조회 성공",
            "data": {
                "status": "READY",
                "position": 0,
                "estimatedWaitTime": 0,
                "concertId": 1
            },
            "status": "OK"
        }
        """;

    // ========== 4. Booking API Examples ==========
    
    public static final String BOOKING_TOKEN_RESPONSE = """
        {
            "message": "예매 토큰 발급 성공",
            "data": {
                "status": "ENTERED",
                "position": 0,
                "estimatedWaitTime": 0
            },
            "status": "OK"
        }
        """;
    
    public static final String SEAT_STATUS_RESPONSE = """
        [
            {
                "seatId": 1,
                "seatNumber": "A1",
                "grade": "VIP",
                "price": 120000,
                "status": "AVAILABLE"
            },
            {
                "seatId": 2,
                "seatNumber": "A2",
                "grade": "VIP",
                "price": 120000,
                "status": "OCCUPIED"
            }
        ]
        """;
    
    public static final String SEAT_HOLD_SUCCESS = """
        {
            "success": true,
            "message": "좌석이 성공적으로 점유되었습니다.",
            "seatId": 1,
            "memberId": 1
        }
        """;
    
    public static final String SEAT_HOLD_FAILURE = """
        {
            "success": false,
            "message": "이미 점유된 좌석입니다.",
            "seatId": 1
        }
        """;

    // ========== 5. Payment API Examples ==========
    
    public static final String PAYMENT_REQUEST = """
        {
            "memberId": 1,
            "concertId": 1,
            "seatId": 1,
            "amount": 120000,
            "paymentMethod": "CARD"
        }
        """;
    
    public static final String PAYMENT_SUCCESS = """
        {
            "message": "결제가 성공적으로 처리되었습니다.",
            "data": {
                "success": true,
                "paymentId": 1,
                "reservationId": 1,
                "amount": 120000,
                "status": "COMPLETED"
            },
            "status": "OK"
        }
        """;
    
    public static final String PAYMENT_FAILURE = """
        {
            "message": "좌석이 이미 예매되었습니다.",
            "data": {
                "success": false,
                "message": "좌석이 이미 예매되었습니다."
            },
            "status": "BAD_REQUEST"
        }
        """;

    // ========== Common Error Response Examples ==========
    
    public static final String ERROR_UNAUTHORIZED = """
        {
            "message": "인증이 필요합니다.",
            "data": null,
            "status": "UNAUTHORIZED"
        }
        """;
    
    public static final String ERROR_FORBIDDEN = """
        {
            "message": "권한이 없습니다.",
            "data": null,
            "status": "FORBIDDEN"
        }
        """;
    
    public static final String ERROR_NOT_FOUND = """
        {
            "message": "요청한 리소스를 찾을 수 없습니다.",
            "data": null,
            "status": "NOT_FOUND"
        }
        """;
    
    public static final String ERROR_INTERNAL_SERVER = """
        {
            "message": "서버 내부 오류가 발생했습니다.",
            "data": null,
            "status": "INTERNAL_SERVER_ERROR"
        }
        """;
} 