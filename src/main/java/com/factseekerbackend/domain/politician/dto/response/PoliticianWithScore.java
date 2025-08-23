package com.factseekerbackend.domain.politician.dto.response;

import com.factseekerbackend.domain.politician.entity.PoliticianTrustScore;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "점수가 포함된 정치인 정보")
public record PoliticianWithScore(
    @Schema(description = "정치인 이름", example = "이재명")
    String name,
    
    @Schema(description = "정당", example = "더불어민주당")
    String party,
    
    @Schema(description = "Gemini AI 점수", example = "85")
    Integer geminiScore,
    
    @Schema(description = "GPT AI 점수", example = "82")
    Integer gptScore,
    
    @Schema(description = "종합 점수", example = "84")
    Integer overallScore
) {
  public static PoliticianWithScore from(PoliticianTrustScore trustScore){
    return new PoliticianWithScore(
        trustScore.getPolitician().getName(),
        trustScore.getPolitician().getParty(),
        trustScore.getGeminiScore(),
        trustScore.getGptScore(),
        trustScore.getOverallScore()
    );
  }

}
