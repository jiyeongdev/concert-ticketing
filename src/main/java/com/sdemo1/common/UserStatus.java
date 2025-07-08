package com.sdemo1.common;

/**
 * 사용자 대기열 상태 Enum
 * 대기열 시스템에서 사용자의 현재 상태를 관리
 */
public enum UserStatus {
    
    /**
     * 대기열에 없음 (기본 상태)
     */
    NOT_IN_QUEUE("NOT_IN_QUEUE", "대기열에 없음"),
    
    /**
     * 대기열에서 대기 중
     */
    WAITING("WAITING", "대기 중"),
    
    /**
     * 예매 입장 준비 완료 (READY)
     */
    READY("READY", "예매 입장 준비 완료"),
    
    /**
     * 예매 페이지 입장 완료 (ENTERED)
     */
    ENTERED("ENTERED", "예매 페이지 입장 완료"),
    
    /**
     * 예매 종료
     */
    BOOKING_CLOSED("BOOKING_CLOSED", "예매 종료"),
    
    /**
     * 만료됨
     */
    EXPIRED("EXPIRED", "만료됨");

    private final String value;
    private final String description;

    UserStatus(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 문자열로부터 UserStatus 찾기
     */
    public static UserStatus fromString(String status) {
        if (status == null) {
            return NOT_IN_QUEUE;
        }
        
        for (UserStatus userStatus : values()) {
            if (userStatus.value.equals(status)) {
                return userStatus;
            }
        }
        
        return NOT_IN_QUEUE;
    }

    /**
     * 예매 가능한 상태인지 확인
     */
    public boolean isBookingEligible() {
        return this == READY || this == ENTERED;
    }

    /**
     * 대기열에 있는 상태인지 확인
     */
    public boolean isInQueue() {
        return this == WAITING || this == READY || this == ENTERED;
    }

    /**
     * 대기 중인 상태인지 확인
     */
    public boolean isWaiting() {
        return this == WAITING;
    }

    /**
     * 예매 준비 완료 상태인지 확인
     */
    public boolean isReady() {
        return this == READY;
    }

    /**
     * 예매 페이지 입장 완료 상태인지 확인
     */
    public boolean isEntered() {
        return this == ENTERED;
    }

    @Override
    public String toString() {
        return value;
    }
} 