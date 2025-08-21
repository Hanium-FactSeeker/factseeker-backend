//package com.factseekerbackend.global.politicalTranding.controller;
//
//import com.factseekerbackend.global.common.ApiResponse;
//import com.factseekerbackend.global.politicalTranding.service.TrendService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api")
//@Tag(name = "정치 트렌드", description = "정치 관련 트렌드 정보 조회 API")
//public class TrendController {
//
//    private final TrendService trendService;
//
//    @Operation(
//        summary = "최신 정치 트렌드 조회",
//        description = "현재 가장 인기 있는 정치 관련 트렌드 키워드를 조회합니다."
//    )
//    @ApiResponses({
//        @io.swagger.v3.oas.annotations.responses.ApiResponse(
//            responseCode = "200",
//            description = "조회 성공",
//            content = @Content(schema = @Schema(implementation = String.class))
//        )
//    })
//    @GetMapping("/latest-trends")
//    public ResponseEntity<ApiResponse<List<String>>> getLatestTrends() {
//        log.info("[API] 최신 정치 트렌드 조회 요청");
//
//        try {
//            List<String> latestTrends = trendService.getLatestTrends();
//            return ResponseEntity.ok(ApiResponse.success("최신 트렌드 순위를 성공적으로 조회했습니다.", latestTrends));
//        } catch (Exception e) {
//            log.error("[API] 최신 정치 트렌드 조회 실패: {}", e.getMessage());
//            return ResponseEntity.ok(ApiResponse.error("트렌드 조회 중 오류가 발생했습니다: " + e.getMessage()));
//        }
//    }
//}
