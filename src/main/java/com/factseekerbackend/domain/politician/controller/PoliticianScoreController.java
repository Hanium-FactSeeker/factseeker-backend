package com.factseekerbackend.domain.politician.controller;

import com.factseekerbackend.domain.politician.dto.SortType;
import com.factseekerbackend.domain.politician.dto.response.PagedPoliticiansResponse;
import com.factseekerbackend.domain.politician.dto.response.PagedPoliticiansWithScoresResponse;
import com.factseekerbackend.domain.politician.dto.response.PoliticianWithScore;
import com.factseekerbackend.domain.politician.dto.response.TopNamesWithScoresResponse;
import com.factseekerbackend.domain.politician.service.PoliticianService;
import com.factseekerbackend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/politicians/scores")
@Tag(name = "정치인 신뢰도", description = "정치인 신뢰도 점수 관련 API")
class PoliticianScoreController {

  private final PoliticianService service;

  @Operation(summary = "신뢰도 점수 목록 조회 (페이지네이션, 정렬)", description = "모든 정치인의 최신 신뢰도 점수를 페이지별로 조회합니다. 신뢰도순 또는 최신순으로 정렬할 수 있습니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "조회 성공",
          content = @Content(schema = @Schema(implementation = PagedPoliticiansWithScoresResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 요청",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "정치인을 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      )
  })
  @GetMapping
  public ResponseEntity<ApiResponse<PagedPoliticiansWithScoresResponse>> getScoresPaged(
      @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "페이지 크기", example = "8")
      @RequestParam(defaultValue = "8") int size,
      @Parameter(description = "정렬 방식", schema = @io.swagger.v3.oas.annotations.media.Schema(allowableValues = {"LATEST", "TRUST_SCORE"}))
      @RequestParam(defaultValue = "TRUST_SCORE") SortType sortType) {

    Page<PoliticianWithScore> pagedScores = service.getTrustScoresPaged(page, size, sortType);

    PagedPoliticiansWithScoresResponse response = new PagedPoliticiansWithScoresResponse(
        pagedScores.getContent(),
        pagedScores.getNumber(),
        pagedScores.getSize(),
        pagedScores.getTotalElements(),
        pagedScores.getTotalPages(),
        pagedScores.isFirst(),
        pagedScores.isLast()
    );

    String message = String.format("정치인 신뢰도 페이지 정보를 %s으로 성공적으로 조회했습니다.", sortType.getDescription());
    return ResponseEntity.ok(ApiResponse.success(message, response));
  }

  @Operation(summary = "상위 12명 신뢰도 점수 요약 조회", description = "신뢰도 점수가 가장 높은 상위 12명의 정치인 정보를 요약하여 조회합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "조회 성공",
          content = @Content(schema = @Schema(implementation = TopNamesWithScoresResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "잘못된 요청",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "정치인을 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      )
  })
  @GetMapping("/top-summary")
  public ResponseEntity<ApiResponse<TopNamesWithScoresResponse>> getTop12Summary() {
    List<PoliticianWithScore> politicians = service.getTop12NamesWithScores();
    TopNamesWithScoresResponse response = new TopNamesWithScoresResponse(politicians);
    return ResponseEntity.ok(ApiResponse.success("상위 12명 정치인 정보를 성공적으로 조회했습니다.", response));
  }
}