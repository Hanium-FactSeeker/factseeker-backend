package com.factseekerbackend.domain.politician.service.llm;

import com.factseekerbackend.domain.politician.dto.LLMAnalysisResult;
import com.factseekerbackend.domain.politician.entity.Politician;

public interface LLMAnalysisService {

    /**
     * 정치인 신뢰도를 분석합니다.
     * 
     * @param politician 분석할 정치인
     * @param analysisPeriod 분석 기간 (예: "2020-2025")
     * @return 분석 결과
     */
    LLMAnalysisResult analyzeTrustScore(Politician politician, String analysisPeriod);

    /**
     * 서비스 타입을 반환합니다.
     * 
     * @return 서비스 타입 (GPT, GEMINI)
     */
    LLMServiceType getServiceType();
}
