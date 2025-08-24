package com.factseekerbackend.domain.politician.controller;

import com.factseekerbackend.domain.politician.dto.response.PagedPoliticiansResponse;
import com.factseekerbackend.domain.politician.dto.response.PoliticianResponse;
import com.factseekerbackend.domain.politician.service.PoliticianService;
import com.factseekerbackend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/politicians")
@Tag(name = "정치인 정보", description = "정치인 기본 정보 조회 API")
public class PoliticianController {

  private final PoliticianService service;

  @Operation(summary = "전체 정치인 목록 조회 (페이지네이션)", description = "모든 정치인의 기본 정보를 페이지별로 조회합니다. 이름순으로 정렬됩니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "조회 성공",
          content = @Content(schema = @Schema(implementation = PagedPoliticiansResponse.class))
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
  public ResponseEntity<ApiResponse<PagedPoliticiansResponse>> getAllPoliticiansPaged(
      @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
      @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "페이지 크기", example = "10")
      @RequestParam(defaultValue = "10") int size) {

    Page<PoliticianResponse> pagedPoliticians = service.getAllPoliticiansPaged(page, size);

    PagedPoliticiansResponse response = new PagedPoliticiansResponse(
        pagedPoliticians.getContent(),
        pagedPoliticians.getNumber(),
        pagedPoliticians.getSize(),
        pagedPoliticians.getTotalElements(),
        pagedPoliticians.getTotalPages(),
        pagedPoliticians.isFirst(),
        pagedPoliticians.isLast()
    );
    return ResponseEntity.ok(ApiResponse.success("정치인 페이지 정보를 성공적으로 조회했습니다.", response));
  }

  @Operation(summary = "이름으로 정치인 검색", description = "이름의 일부를 사용하여 일치하는 모든 정치인 목록을 조회합니다.")
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
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<PoliticianResponse>>> searchByName(
      @Parameter(description = "검색할 이름", example = "석열", required = true)
      @RequestParam String name) {
    List<PoliticianResponse> politicians = service.searchByName(name);
    return ResponseEntity.ok(ApiResponse.success("정치인 목록을 성공적으로 조회했습니다.", politicians));
  }

  @Operation(summary = "ID로 정치인 상세 조회", description = "정치인 ID를 사용하여 특정 정치인의 상세 정보를 조회합니다.")
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
  @GetMapping("/{id:\\d+}")
  public ResponseEntity<ApiResponse<PoliticianResponse>> getById(
      @Parameter(description = "정치인 ID", example = "1")
      @PathVariable Long id) {
    PoliticianResponse politician = service.getById(id);
    return ResponseEntity.ok(ApiResponse.success("정치인 정보를 성공적으로 조회했습니다.", politician));
  }
}