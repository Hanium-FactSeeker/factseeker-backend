package com.factseekerbackend.domain.politician.dto;

public enum SortType {
    LATEST("최신순"),
    TRUST_SCORE("신뢰도순");
    
    private final String description;
    
    SortType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
