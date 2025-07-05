package com.sdemo1.service.impl;

import org.springframework.stereotype.Service;
import com.sdemo1.service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {
    
    @Override
    public String getCacheStatus() {
        // 임시 구현 - 실제로는 캐시 상태를 확인하는 로직이 들어갈 수 있습니다
        return "캐시 상태: 정상";
    }
    
    @Override
    public void refreshCategoryMap() {
        // 임시 구현 - 실제로는 카테고리 캐시를 갱신하는 로직이 들어갈 수 있습니다
        System.out.println("카테고리 캐시가 갱신되었습니다.");
    }
} 