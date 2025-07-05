package com.sdemo1.config;

import com.sdemo1.interceptor.LoggingInterceptor;
import com.sdemo1.interceptor.AuthenticationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;
    private final AuthenticationInterceptor authenticationInterceptor;

    // @Override
    // public void addCorsMappings(CorsRegistry registry) {
    //     registry.addMapping("/**")
    //             .allowedOrigins(
    //                 "http://localhost:3000",
    //                 "https://www.myrecipe.top"
    //             )
    //             .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
    //             .allowedHeaders("*")
    //             .allowCredentials(true);
    // }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 로깅 인터셉터 등록
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")  // 모든 경로에 대해 로깅
                .excludePathPatterns("/static/**", "/error");  // 특정 경로는 제외
        
        // 인증 인터셉터 등록
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/**")  // 모든 경로에 대해 인증 체크
                .excludePathPatterns(
                    "/ck/auth/**",      // 인증 관련 경로 제외
                    "/health-check",    // 헬스체크 제외
                    "/static/**",       // 정적 리소스 제외
                    "/error"            // 에러 페이지 제외
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 리소스 핸들러 설정
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
} 