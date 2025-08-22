package com.factseekerbackend.domain.politician.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "상위 12명 정치인 이름과 점수 응답")
public record TopNamesWithScoresResponse(
    @Schema(description = "정치인 정보 목록")
    List<PoliticianWithScore> politicians
) {}
