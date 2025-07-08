package com.sdemo1.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CategoryService {
    
    public String getCacheStatus() {
        // 임시 구현 - 실제로는 캐시 상태를 확인하는 로직이 들어갈 수 있습니다
        return "캐시 상태: 정상";
    }
    
    public void refreshCategoryMap() {
        // 임시 구현 - 실제로는 카테고리 캐시를 갱신하는 로직이 들어갈 수 있습니다
        log.info("카테고리 캐시가 갱신되었습니다.");
    }
} 