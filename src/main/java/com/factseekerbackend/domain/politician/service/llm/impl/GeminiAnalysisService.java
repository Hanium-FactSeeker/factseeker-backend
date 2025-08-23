package com.factseekerbackend.domain.politician.service.llm.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.factseekerbackend.domain.politician.dto.LLMAnalysisResult;
import com.factseekerbackend.domain.politician.entity.Politician;
import com.factseekerbackend.domain.politician.service.llm.LLMAnalysisService;
import com.factseekerbackend.domain.politician.service.llm.LLMServiceType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GeminiAnalysisService implements LLMAnalysisService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com/v1beta")
            .build();

    @Value("${llm.gemini.api-key:}")
    private String geminiApiKey;
    
    @Value("${llm.gemini.model:gemini-pro}")
    private String model;
    
    @Value("${llm.gemini.max-tokens:2000}")
    private Integer maxTokens;
    
    @Value("${llm.gemini.temperature:0.7}")
    private Double temperature;

    @Override
    public LLMAnalysisResult analyzeTrustScore(Politician politician, String analysisPeriod) {
        log.info("[GEMINI] {} 분석 시작", politician.getName());
        
        try {
            // API 키 검증
            if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
                log.warn("[GEMINI] Gemini API 키가 설정되지 않았습니다.");
                return LLMAnalysisResult.builder()
                        .politician(politician)
                        .isSuccess(false)
                        .errorMessage("Gemini API 키가 설정되지 않았습니다.")
                        .build();
            }
            
            // Gemini API 호출
            return callGeminiAPI(politician, analysisPeriod);
            
        } catch (Exception e) {
            log.error("[GEMINI] {} Gemini 분석 실패: {}", politician.getName(), e.getMessage());
            return LLMAnalysisResult.builder()
                    .politician(politician)
                    .isSuccess(false)
                    .errorMessage("Gemini 분석 중 오류 발생: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Google Gemini API를 호출하여 정치인 신뢰도를 분석합니다.
     */
    private LLMAnalysisResult callGeminiAPI(Politician politician, String analysisPeriod) {
        try {
            // 분석 프롬프트 구성
            String prompt = buildAnalysisPrompt(politician, analysisPeriod);
            
            // API 요청 본문 구성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", List.of(Map.of(
                "parts", List.of(Map.of("text", prompt))
            )));
            requestBody.put("generationConfig", Map.of(
                "temperature", temperature,
                "maxOutputTokens", maxTokens
            ));
            
            // API 호출
            log.info("[GEMINI] Gemini API 호출 시작 - 모델: {}", model);
            
            String response = webClient.post()
                    .uri("/models/" + model + ":generateContent?key=" + geminiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // 동기 호출
            
            log.info("[GEMINI] Gemini API 응답 수신: {}", response);
            
            // 응답 검증
            if (response == null || response.trim().isEmpty()) {
                log.warn("[GEMINI] {} 빈 응답 수신", politician.getName());
                return createFallbackResult(politician, "빈 응답 수신");
            }
            
            // 거부 응답 체크
            if (isRejectedResponse(response)) {
                log.warn("[GEMINI] {} 요청 거부됨: {}", politician.getName(), response.substring(0, Math.min(100, response.length())));
                return createFallbackResult(politician, "API 요청 거부됨");
            }
            
            // 응답 파싱 및 결과 생성
            return parseGeminiResponse(politician, response);
            
        } catch (Exception e) {
            log.error("[GEMINI] {} Gemini API 호출 중 오류: {}", politician.getName(), e.getMessage());
            return createFallbackResult(politician, "API 호출 중 오류: " + e.getMessage());
        }
    }

    /**
     * 정치인 분석을 위한 프롬프트를 구성합니다.
     */
    private String buildAnalysisPrompt(Politician politician, String analysisPeriod) {
        return String.format("""
            당신은 한국의 공직자와 정치인에 대한 객관적이고 중립적인 분석을 수행하는 전문가입니다.
            주어진 정보를 바탕으로 공직자로서의 일반적인 특성을 분석해주세요.
            
            분석 대상 정보:
            - 이름: %s
            - 소속 정당: %s
            - 분석 기간: %s
            
            다음 4개 항목에 대해 0-100점으로 평가하고, 각각의 평가 근거를 구체적으로 설명해주세요:
            
            1. 정직성 (Integrity): 
               - 공직자로서의 도덕적 원칙과 윤리적 행동
               - 진실성과 신뢰성
               - 부정부패나 비리와의 관련성
            
            2. 투명성 (Transparency): 
               - 의사결정 과정의 공개성
               - 정보 공개와 소통의 투명성
               - 이해관계자와의 소통 품질
            
            3. 일관성 (Consistency): 
               - 정치적 입장과 약속의 일관성
               - 정책 방향성의 일관성
               - 행동과 발언의 일관성
            
            4. 책임감 (Accountability): 
               - 자신의 행동과 결정에 대한 책임 인식
               - 공직자로서의 의무 수행
               - 국민에 대한 책임감
            
            평가 기준:
            - 90-100점: 매우 우수한 수준
            - 80-89점: 우수한 수준
            - 70-79점: 양호한 수준
            - 60-69점: 보통 수준
            - 50-59점: 미흡한 수준
            - 40-49점: 부족한 수준
            - 30-39점: 매우 부족한 수준
            - 0-29점: 심각한 문제가 있는 수준
            
            응답 형식:
            {
              "integrityScore": 점수,
              "transparencyScore": 점수,
              "consistencyScore": 점수,
              "accountabilityScore": 점수,
              "integrityReason": "구체적인 평가 근거 (한국어로 작성)",
              "transparencyReason": "구체적인 평가 근거 (한국어로 작성)",
              "consistencyReason": "구체적인 평가 근거 (한국어로 작성)",
              "accountabilityReason": "구체적인 평가 근거 (한국어로 작성)",
              "analysisSummary": "전체적인 종합 평가 요약 (한국어로 작성)"
            }
            
            중요사항:
            - 반드시 JSON 형식으로만 응답해주세요
            - 각 점수는 0-100 사이의 정수여야 합니다
            - 평가 근거는 구체적이고 객관적이어야 합니다
            - 정치적 편향 없이 중립적으로 평가해주세요
            - 공직자로서의 일반적인 특성을 중심으로 평가해주세요
            """, 
            politician.getName(),
            politician.getParty() != null ? politician.getParty() : "정보 없음",
            analysisPeriod
        );
    }

    /**
     * Gemini API 응답을 파싱하여 LLMAnalysisResult로 변환합니다.
     */
    private LLMAnalysisResult parseGeminiResponse(Politician politician, String response) {
        try {
            // Jackson ObjectMapper를 사용한 JSON 파싱
            return parseJSONResponse(politician, response);
        } catch (Exception e) {
            log.warn("[GEMINI] JSON 파싱 실패: {}", e.getMessage());
            return createFallbackResult(politician, "JSON 파싱 실패: " + e.getMessage());
        }
    }

    /**
     * Jackson ObjectMapper를 사용한 정확한 JSON 파싱
     */
    private LLMAnalysisResult parseJSONResponse(Politician politician, String response) {
        try {
            // Gemini API 응답에서 content 추출
            JsonNode responseNode = objectMapper.readTree(response);
            String content = responseNode.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
            
            // content에서 JSON 부분만 추출 (마크다운 코드 블록 제거)
            String jsonContent = extractJSONFromResponse(content);
            
            // JSON 파싱
            JsonNode jsonNode = objectMapper.readTree(jsonContent);
            
            // 점수 추출
            int integrityScore = jsonNode.get("integrityScore").asInt();
            int transparencyScore = jsonNode.get("transparencyScore").asInt();
            int consistencyScore = jsonNode.get("consistencyScore").asInt();
            int accountabilityScore = jsonNode.get("accountabilityScore").asInt();
            
            // 이유 추출
            String integrityReason = jsonNode.get("integrityReason").asText();
            String transparencyReason = jsonNode.get("transparencyReason").asText();
            String consistencyReason = jsonNode.get("consistencyReason").asText();
            String accountabilityReason = jsonNode.get("accountabilityReason").asText();
            String analysisSummary = jsonNode.get("analysisSummary").asText();
            
            // 종합 점수 계산
            int overallScore = (integrityScore + transparencyScore + consistencyScore + accountabilityScore) / 4;
            
            log.info("[GEMINI] {} JSON 파싱 성공 - 점수: {}", politician.getName(), overallScore);
            
            return LLMAnalysisResult.builder()
                    .politician(politician)
                    .overallScore(overallScore)
                    .integrityScore(integrityScore)
                    .transparencyScore(transparencyScore)
                    .consistencyScore(consistencyScore)
                    .accountabilityScore(accountabilityScore)
                    .integrityReason(integrityReason)
                    .transparencyReason(transparencyReason)
                    .consistencyReason(consistencyReason)
                    .accountabilityReason(accountabilityReason)
                    .analysisSummary(analysisSummary)
                    .isSuccess(true)
                    .build();
                    
        } catch (Exception e) {
            log.error("[GEMINI] JSON 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }

    /**
     * 응답에서 JSON 부분만 추출합니다.
     */
    private String extractJSONFromResponse(String response) {
        // 마크다운 코드 블록 제거
        if (response.contains("```json")) {
            int start = response.indexOf("```json") + 7;
            int end = response.indexOf("```", start);
            if (end > start) {
                return response.substring(start, end).trim();
            }
        }
        
        // 일반 JSON 응답인 경우
        if (response.trim().startsWith("{")) {
            return response.trim();
        }
        
        // JSON을 찾을 수 없는 경우
        throw new RuntimeException("응답에서 JSON을 찾을 수 없습니다: " + response);
    }

    @Override
    public LLMServiceType getServiceType() {
        return LLMServiceType.GEMINI;
    }
    
    /**
     * 임시 랜덤 점수 생성 메서드 (API 실패 시 폴백)
     */
    private LLMAnalysisResult generateRandomScore(Politician politician) {
        Random random = new Random();
        int integrityScore = 60 + random.nextInt(40); // 60-99
        int transparencyScore = 60 + random.nextInt(40);
        int consistencyScore = 60 + random.nextInt(40);
        int accountabilityScore = 60 + random.nextInt(40);
        
        int overallScore = (integrityScore + transparencyScore + consistencyScore + accountabilityScore) / 4;
        
        LLMAnalysisResult result = LLMAnalysisResult.builder()
                .politician(politician)
                .overallScore(overallScore)
                .integrityScore(integrityScore)
                .transparencyScore(transparencyScore)
                .consistencyScore(consistencyScore)
                .accountabilityScore(accountabilityScore)
                .integrityReason("Gemini 분석: " + politician.getName() + "의 정직성에 대한 분석 결과")
                .transparencyReason("Gemini 분석: " + politician.getName() + "의 투명성에 대한 분석 결과")
                .consistencyReason("Gemini 분석: " + politician.getName() + "의 일관성에 대한 분석 결과")
                .accountabilityReason("Gemini 분석: " + politician.getName() + "의 책임감에 대한 분석 결과")
                .analysisSummary("Gemini가 분석한 " + politician.getName() + "의 종합 신뢰도 평가")
                .isSuccess(true)
                .build();
        
        log.info("[GEMINI] {} Gemini 분석 완료 - 점수: {}", politician.getName(), overallScore);
        return result;
    }

    private boolean isRejectedResponse(String response) {
        String lowerResponse = response.toLowerCase();
        return lowerResponse.contains("i'm sorry") || 
               lowerResponse.contains("can't assist") ||
               lowerResponse.contains("cannot help") ||
               lowerResponse.contains("i cannot") ||
               lowerResponse.contains("unable to") ||
               lowerResponse.contains("not able to") ||
               lowerResponse.contains("i don't") ||
               lowerResponse.contains("i do not") ||
               lowerResponse.contains("policy") ||
               lowerResponse.contains("guidelines") ||
               lowerResponse.contains("restrictions") ||
               lowerResponse.contains("content policy") ||
               lowerResponse.contains("safety") ||
               lowerResponse.contains("inappropriate");
    }

    private LLMAnalysisResult createFallbackResult(Politician politician, String errorMessage) {
        log.warn("[GEMINI] {} 대체 점수 생성: {}", politician.getName(), errorMessage);
        
        // 기본 점수 생성 (중간값)
        return LLMAnalysisResult.builder()
            .politician(politician)
            .isSuccess(false)
            .errorMessage(errorMessage)
            .integrityScore(50)
            .transparencyScore(50)
            .consistencyScore(50)
            .accountabilityScore(50)
            .overallScore(50)
            .integrityReason("Gemini 분석 실패로 인한 기본값")
            .transparencyReason("Gemini 분석 실패로 인한 기본값")
            .consistencyReason("Gemini 분석 실패로 인한 기본값")
            .accountabilityReason("Gemini 분석 실패로 인한 기본값")
            .build();
    }
}
