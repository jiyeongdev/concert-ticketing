package com.sdemo1.config;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

/**
 * Swagger/OpenAPI 설정 클래스
 * JWT 인증을 포함한 API 문서 설정
 * 
 * @author Concert Reservation Team
 * @version 1.0
 * @since 2024-01-01
 */
@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(servers())
                .tags(getTags())
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, createSecurityScheme()))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME));
    }

    /**
     * API 정보를 정의합니다.
     */
    private Info apiInfo() {
        return new Info()
                .title("콘서트 예매 시스템 API")
                .description("""
                        **콘서트 예매 시스템 API** | Spring Boot 3.2.3 + Java 21
                        
                        ### 🔐 인증 방법
                        1. `/auth/login` API를 사용하여 JWT 토큰을 받으세요
                        2. 우측 상단 **🔒 Authorize** 버튼을 클릭하세요
                        3. 받은 JWT 토큰을 입력하세요 (Bearer 접두사 제외)
                        4. **Authorize** 버튼을 클릭하여 인증을 완료하세요
                        5. 이제 모든 API에서 자동으로 JWT 토큰이 전송됩니다!
                        
                        ### 📋 주요 기능
                        | 기능 | 기술 스택 | 설명 |
                        |------|----------|------|
                        | **콘서트 관리** | Spring Data JPA + MySQL | 콘서트 CRUD, 검색, 필터링 |
                        | **좌석 실시간 관리** | Redis + WebSocket(STOMP) | 실시간 좌석 상태 조회/점유/해제 |
                        | **예매/결제 시스템** | Spring Security + JWT | 예매 처리, 결제 검증, 보안 인증 |
                        | **대기열 시스템** | RabbitMQ + Redis | 실시간 대기열 관리, 순서 보장 |
                        | **회원 관리** | Spring Security + JWT | 회원가입, 로그인, 프로필 관리 |
                        | **실시간 통신** | WebSocket(STOMP) | 좌석 상태 브로드캐스트, 대기열 모니터링 |

                        ### 🛡️ JWT Token 예시

                        | 권한  | 계정명   | AccessToken (만료 14일후) |
                        |-------|---------|------------------|
                        | ADMIN | admin3  | <br/>`eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyMyIsIm1lbWJlcklkIjoyMywibmFtZSI6IkFkbWluIDMiLCJyb2xlIjoiQURNSU4iLCJwaG9uZSI6IjAxMC0yMDAzLTAwMDMiLCJpYXQiOjE3NTIxMDg3NTgsImV4cCI6MTc1MzMxODM1OH0.VI8Mdzw1JUDfXDt51L1i6aX4qFvM_BnpH4UvTuhScAdTFyhX_9R3-WWItaBXtckH720cN9EhoGEh4pQIkk_9cg`<br/> |
                        | USER  | user3   | <br/>`eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIzIiwibWVtYmVySWQiOjMsIm5hbWUiOiJVc2VyIDMiLCJyb2xlIjoiVVNFUiIsInBob25lIjoiMDEwLTEwMDMtMDAwMyIsImlhdCI6MTc1MjEwODg1NywiZXhwIjoxNzUzMzE4NDU3fQ.lAsK4A_CIxpGeOnKhNUq2_L-t0oCp5ovzZoYbB8q4ynw3XjEuMoH_f9poidV__fCa35AnVMoeUnj1ycTz8ioow`<br/> |

                        - 위 토큰을 복사해서 Authorize 버튼에 붙여넣으면, 각 권한별 API 테스트가 가능합니다.
                        - Bearer 접두사는 자동으로 붙으니, 토큰 값만 입력하세요.

                        ### 🔄 좌석 점유/점유 해제 테스트 방법

                        - 좌석 점유/점유 해제 테스트는 브라우저 두 개를 띄우고, 아래 주소로 접속하세요:
                          ```
                          http://localhost:8080/websocket-reservation.html
                          ```
                        - 위 토큰의 memberId, seatId, concertId를 입력하고 점유/해제 버튼을 클릭하면 됩니다.

                        | 계정명 |  콘서트Id | 사용자(멤버)Id | 좌석 ID  |
                        |--------|-----------|----------|--------|
                        | admin3 | 2         | 23       | 33     |
                        | user3  | 2         | 3        | 33     |

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

    /**
     * JWT 인증을 위한 Security Scheme을 생성합니다.
     */
    private SecurityScheme createSecurityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("JWT 토큰을 입력하세요 (Bearer 접두사는 자동으로 추가됩니다)");
    }

    /**
     * API 태그들의 순서를 정의합니다.
     */
    private List<Tag> getTags() {
        return List.of(
                new Tag().name("1. 인증 관리").description("회원가입, 로그인, 토큰 갱신, 로그아웃 API"),
                new Tag().name("2. 회원 관리").description("회원 정보 조회, 프로필 수정 API"),
                new Tag().name("3. 좌석 등급 관리").description("좌석 등급 조회, 생성, 수정, 삭제 API"),
                new Tag().name("4. 좌석 관리").description("좌석 조회, 생성, 수정, 삭제 API"),
                new Tag().name("5. 콘서트 관리").description("콘서트 조회, 검색, 관리 API"),
                new Tag().name("6. 대기열 관리").description("대기열 입장/퇴장, 상태 조회 API"),
                new Tag().name("7. 예매 관리").description("예매 토큰 발급, 좌석 점유/해제, 실시간 좌석 상태 조회 API"),
                new Tag().name("8. 예매/결제 관리").description("결제 처리, 결제 취소 API"),
                new Tag().name("9. 관리자 전용").description("시스템 관리 및 캐시 관리 API (ADMIN 권한 필요)")
        );
    }
} 