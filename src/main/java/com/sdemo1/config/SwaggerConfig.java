package com.sdemo1.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Swagger/OpenAPI 설정 클래스
 * 
 * @author Concert Reservation Team
 * @version 1.0
 * @since 2024-01-01
 */
@Configuration
public class SwaggerConfig {

    /**
     * OpenAPI 설정을 정의합니다.
     * 
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers());
    }

    /**
     * API 정보를 정의합니다.
     * 
     * @return API 정보 객체
     */
    private Info apiInfo() {
        return new Info()
                .title("콘서트 예매 시스템 API")
                .description("""
                        **콘서트 예매 시스템 API** | Spring Boot 3.2.3 + Java 21
                        
                        | 주요 기능 | 사용 기술 스택 | 설명 |
                        |---------|-----------|------|
                        | **콘서트 관리** | Spring Data JPA + MySQL | 콘서트 CRUD, 검색, 필터링 |
                        | **좌석 실시간 관리** | Redis + WebSocket(STOMP) | 실시간 좌석 상태 조회/점유/해제 |
                        | **예매/결제 시스템** | Spring Security + JWT | 예매 처리, 결제 검증, 보안 인증 |
                        | **대기열 시스템** | RabbitMQ + Redis | 실시간 대기열 관리, 순서 보장 |
                        | **회원 관리** | Spring Security + JWT | 회원가입, 로그인, 프로필 관리 |
                        | **실시간 통신** | WebSocket(STOMP) | 좌석 상태 브로드캐스트, 대기열 모니터링 |
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("콘서트 예매 시스템 깃헙 링크")
                        .url("https://github.com/jiyeongdev/concert-ticketing"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * 서버 정보를 정의합니다.
     * 
     * @return 서버 목록
     */
    private List<Server> servers() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("로컬 개발 서버"),
                new Server()
                        .url("https://api.concert-reservation.com")
                        .description("프로덕션 서버")
        );
    }
} 