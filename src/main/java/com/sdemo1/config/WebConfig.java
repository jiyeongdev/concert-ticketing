package com.sdemo1.config;

import com.sdemo1.interceptor.LoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;

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
        // 로깅 인터셉터만 등록 (인증은 Spring Security만 사용)
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")  // 모든 경로에 대해 로깅
                .excludePathPatterns("/static/**", "/error");  // 특정 경로는 제외
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 정적 리소스 핸들러 설정
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
} 