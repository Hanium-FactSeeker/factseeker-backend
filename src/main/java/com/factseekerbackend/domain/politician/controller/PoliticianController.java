package com.factseekerbackend.domain.politician.controller;

import com.factseekerbackend.domain.politician.controller.dto.response.TrustScoreResponse;
import com.factseekerbackend.domain.politician.dto.request.PoliticianNameRequest;
import com.factseekerbackend.domain.politician.dto.response.PoliticianResponse;
import com.factseekerbackend.domain.politician.entity.Politician;
import com.factseekerbackend.domain.politician.entity.PoliticianTrustScore;
import com.factseekerbackend.domain.politician.repository.PoliticianRepository;
import com.factseekerbackend.domain.politician.repository.PoliticianTrustScoreRepository;
import com.factseekerbackend.domain.politician.service.PoliticianBatchService;
import com.factseekerbackend.domain.politician.service.PoliticianService;
import com.factseekerbackend.domain.politician.service.PoliticianTrustAnalysisService;
import com.factseekerbackend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/politicians")
@Tag(name = "정치인 관리", description = "정치인 정보 조회 및 신뢰도 분석 API")
@Validated
public class PoliticianController {

  private final PoliticianRepository politicianRepository;
  private final PoliticianTrustScoreRepository trustScoreRepository;
  private final PoliticianTrustAnalysisService analysisService;
  private final PoliticianBatchService batchService;
  private final PoliticianService service;

  @Operation(
      summary = "상위 12명 정치인 이름 조회",
      description = "신뢰도 점수가 높은 상위 12명 정치인의 이름을 조회합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "조회 성공",
          content = @Content(schema = @Schema(implementation = TopNamesResponse.class))
      )
  })
  @GetMapping("/top12-names")
  public ResponseEntity<ApiResponse<TopNamesResponse>> top12Names() {
    List<String> names = service.getTop12Names();
    TopNamesResponse response = new TopNamesResponse(names);
    return ResponseEntity.ok(ApiResponse.success("상위 12명 정치인 이름을 성공적으로 조회했습니다.", response));
  }

  @Operation(
      summary = "ID로 정치인 조회",
      description = "정치인 ID를 사용하여 정치인 정보를 조회합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "조회 성공",
          content = @Content(schema = @Schema(implementation = PoliticianResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "정치인을 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      )
  })
  @GetMapping("/{id:\\d+}")
  public ResponseEntity<ApiResponse<PoliticianResponse>> getById(
      @Parameter(description = "정치인 ID", example = "1")
      @PathVariable Long id) {
    PoliticianResponse politician = service.getById(id);
    return ResponseEntity.ok(ApiResponse.success("정치인 정보를 성공적으로 조회했습니다.", politician));
  }

  @Operation(
      summary = "이름으로 정치인 조회",
      description = "정치인 이름을 사용하여 정치인 정보를 조회합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "조회 성공",
          content = @Content(schema = @Schema(implementation = PoliticianResponse.class))
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
  @PostMapping("/by-name")
  public ResponseEntity<ApiResponse<PoliticianResponse>> getByNameBody(
      @Parameter(description = "정치인 이름", example = "윤석열")
      @Valid @RequestBody PoliticianNameRequest request) {
    PoliticianResponse politician = service.getByName(request.name());
    return ResponseEntity.ok(ApiResponse.success("정치인 정보를 성공적으로 조회했습니다.", politician));
  }

  @Operation(
      summary = "전체 정치인 조회",
      description = "활성화된 모든 정치인의 정보를 조회합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "조회 성공",
          content = @Content(schema = @Schema(implementation = PoliticianResponse.class))
      )
  })
  @GetMapping
  public ResponseEntity<ApiResponse<List<PoliticianResponse>>> getAllPoliticians() {
    List<Politician> politicians = politicianRepository.findAllActiveOrderByName();
    List<PoliticianResponse> responses = politicians.stream()
        .map(PoliticianResponse::from)
        .collect(Collectors.toList());

    return ResponseEntity.ok(ApiResponse.success("정치인 목록을 성공적으로 조회했습니다.", responses));
  }

  @Operation(
      summary = "정치인 신뢰도 조회",
      description = "특정 정치인의 최신 신뢰도 분석 결과를 조회합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "조회 성공",
          content = @Content(schema = @Schema(implementation = TrustScoreResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "신뢰도 분석 결과를 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      )
  })
  @GetMapping("/{id}/trust-score")
  public ResponseEntity<ApiResponse<TrustScoreResponse>> getPoliticianTrustScore(
      @Parameter(description = "정치인 ID", example = "1")
      @PathVariable Long id) {
    PoliticianTrustScore trustScore = trustScoreRepository.findLatestCompletedScore(id)
        .orElseThrow(() -> new RuntimeException("신뢰도 분석 결과를 찾을 수 없습니다: " + id));

    return ResponseEntity.ok(ApiResponse.success("정치인 신뢰도 정보를 성공적으로 조회했습니다.", TrustScoreResponse.from(trustScore)));
  }

  @Operation(
      summary = "전체 신뢰도 조회",
      description = "특정 날짜의 모든 정치인 신뢰도 분석 결과를 조회합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "조회 성공",
          content = @Content(schema = @Schema(implementation = TrustScoreResponse.class))
      )
  })
  @GetMapping("/trust-scores")
  public ResponseEntity<ApiResponse<List<TrustScoreResponse>>> getAllTrustScores(
      @Parameter(description = "분석 날짜 (ISO 형식: YYYY-MM-DD)", example = "2024-01-01")
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate analysisDate) {

    LocalDate targetDate = analysisDate != null ? analysisDate : LocalDate.now();
    List<PoliticianTrustScore> trustScores = trustScoreRepository.findCompletedScoresByDate(targetDate);

    List<TrustScoreResponse> responses = trustScores.stream()
        .map(TrustScoreResponse::from)
        .collect(Collectors.toList());

    return ResponseEntity.ok(ApiResponse.success("정치인 신뢰도 목록을 성공적으로 조회했습니다.", responses));
  }

  @Operation(
      summary = "전체 정치인 신뢰도 분석 실행",
      description = "모든 정치인에 대한 신뢰도 분석을 수동으로 실행합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "분석 실행 성공",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "500",
          description = "분석 실행 실패",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      )
  })
  @PostMapping("/analysis/execute")
  public ResponseEntity<ApiResponse<String>> executeAnalysis() {
    log.info("[API] 수동 분석 실행 요청");

    try {
      analysisService.analyzeAllPoliticians();
      return ResponseEntity.ok(ApiResponse.success("분석이 성공적으로 실행되었습니다."));
    } catch (Exception e) {
      log.error("[API] 수동 분석 실행 실패: {}", e.getMessage());
      return ResponseEntity.ok(ApiResponse.error("분석 실행 중 오류가 발생했습니다: " + e.getMessage()));
    }
  }

  @Operation(
      summary = "정치인 초기 데이터 설정",
      description = "애플리케이션 시작 시 정치인 초기 데이터를 설정합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "초기 데이터 설정 성공",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "500",
          description = "초기 데이터 설정 실패",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      )
  })
  @PostMapping("/init")
  public ResponseEntity<ApiResponse<String>> initializePoliticians() {
    log.info("[API] 정치인 초기 데이터 설정 요청");

    try {
      batchService.initializeOnStartup();
      return ResponseEntity.ok(ApiResponse.success("정치인 초기 데이터가 성공적으로 설정되었습니다."));
    } catch (Exception e) {
      log.error("[API] 정치인 초기 데이터 설정 실패: {}", e.getMessage());
      return ResponseEntity.ok(ApiResponse.error("초기 데이터 설정 중 오류가 발생했습니다: " + e.getMessage()));
    }
  }

  @Operation(
      summary = "개별 정치인 신뢰도 분석 실행",
      description = "특정 정치인에 대한 신뢰도 분석을 수동으로 실행합니다."
  )
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "분석 실행 성공",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "정치인을 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "500",
          description = "분석 실행 실패",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      )
  })
  @PostMapping("/{id}/analyze")
  public ResponseEntity<ApiResponse<String>> analyzePolitician(
      @Parameter(description = "정치인 ID", example = "1")
      @PathVariable Long id) {
    log.info("[API] 정치인 {} 개별 분석 요청", id);

    try {
      Politician politician = politicianRepository.findById(id)
          .orElseThrow(() -> new RuntimeException("정치인을 찾을 수 없습니다: " + id));

      analysisService.analyzePoliticianTrustScore(politician, LocalDate.now(), "2020-2025");
      return ResponseEntity.ok(ApiResponse.success("정치인 분석이 성공적으로 실행되었습니다."));
    } catch (Exception e) {
      log.error("[API] 정치인 개별 분석 실패: {}", e.getMessage());
      return ResponseEntity.ok(ApiResponse.error("분석 실행 중 오류가 발생했습니다: " + e.getMessage()));
    }
  }

  @Schema(description = "상위 12명 정치인 이름 응답")
  public record TopNamesResponse(
      @Schema(description = "정치인 이름 목록", example = "[\"윤석열\", \"이재명\", \"안철수\"]")
      List<String> names) {}
}
