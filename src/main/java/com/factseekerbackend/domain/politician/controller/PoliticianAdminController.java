package com.factseekerbackend.domain.politician.controller;

import com.factseekerbackend.domain.politician.entity.Politician;
import com.factseekerbackend.domain.politician.repository.PoliticianRepository;
import com.factseekerbackend.domain.politician.service.PoliticianTrustAnalysisService;
import com.factseekerbackend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/politicians")
@Tag(name = "정치인 관리 (Admin)", description = "관리자용 기능 API")
@RequiredArgsConstructor
public class PoliticianAdminController {

  private final PoliticianTrustAnalysisService analysisService;
  private final PoliticianRepository politicianRepository;

  @Operation(summary = "전체 정치인 신뢰도 분석 실행", description = "모든 정치인에 대한 신뢰도 분석을 수동으로 실행합니다. (비동기 처리)")
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
  @PostMapping("/analysis/execute-all")
  public ResponseEntity<ApiResponse<String>> executeAnalysis() {
    log.info("[API] 전체 신뢰도 분석 수동 실행 요청");
    analysisService.analyzeAllPoliticians(); // 비동기 호출
    return ResponseEntity.ok(ApiResponse.success("전체 정치인 신뢰도 분석이 시작되었습니다."));
  }

  @Operation(summary = "개별 정치인 신뢰도 분석 실행", description = "특정 정치인에 대한 신뢰도 분석을 수동으로 실행합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "분석 실행 성공",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "해당 ID 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "500",
          description = "분석 실행 실패",
          content = @Content(schema = @Schema(implementation = ApiResponse.class))
      )
  })
  @PostMapping("/{id}/analysis/execute")
  public ResponseEntity<ApiResponse<String>> analyzePolitician(
      @Parameter(description = "분석할 정치인 ID", example = "1")
      @PathVariable Long id) {
    log.info("[API] 정치인 {} 개별 분석 요청", id);

    try {
      Politician politician = politicianRepository.findById(id)
          .orElseThrow(() -> new EntityNotFoundException("해당 ID의 정치인을 찾을 수 없습니다: " + id));
      analysisService.analyzePoliticianTrustScore(politician, LocalDate.now(), "2020-2025");
      return ResponseEntity.ok(ApiResponse.success("정치인 분석이 성공적으로 실행되었습니다."));

    } catch (EntityNotFoundException e) {
      log.warn("[API] 정치인 개별 분석 실패 - 존재하지 않는 ID: {}", id);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponse.error(e.getMessage()));

    } catch (Exception e) {
      log.error("[API] 정치인 개별 분석 실패 - 서버 내부 오류: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("분석 실행 중 서버에서 오류가 발생했습니다."));
    }
  }

}