package com.factseekerbackend.infrastructure.naver.controller;

import com.factseekerbackend.global.common.ApiResponse;
import com.factseekerbackend.infrastructure.naver.newsapi.NaverClient;
import com.factseekerbackend.infrastructure.naver.newsapi.dto.response.NewsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
 

@Tag(name = "뉴스 검색", description = "네이버 뉴스 검색 API")
@RequiredArgsConstructor
@RequestMapping("/api/news")
@RestController
public class NewsSearchController {
    private final NaverClient naverClient;

    @Operation(
        summary = "네이버 뉴스 검색",
        description = "키워드로 네이버 뉴스를 검색합니다. 정렬은 'sim'(정확도) 또는 'date'(최신순)을 지원합니다."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "뉴스 검색 성공",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "성공 예시",
                    summary = "성공적으로 뉴스 목록을 반환",
                    value = "{\n  \"success\": true,\n  \"message\": \"뉴스 정보 조회 성공.\",\n  \"data\": {\n    \"lastBuildDate\": \"Wed, 21 Aug 2024 12:34:56 +0900\",\n    \"total\": 1234,\n    \"start\": 1,\n    \"display\": 10,\n    \"items\": [\n      {\n        \"title\": \"<b>팩트시커</b> 출시… 뉴스 검증 돕는다\",\n        \"link\": \"https://news.naver.com/main/read.naver?oid=001&aid=0000000001\",\n        \"description\": \"AI 기반 팩트체커 서비스 팩트시커가 출시됐다…\",\n        \"pubDate\": \"Wed, 21 Aug 2024 12:30:00 +0900\"\n      },\n      {\n        \"title\": \"네이버 뉴스 검색 API 활용 사례\",\n        \"link\": \"http://example.com/article/2\",\n        \"description\": \"네이버 뉴스 API를 활용한 검색 예시입니다.\",\n        \"pubDate\": \"Wed, 21 Aug 2024 12:10:00 +0900\"\n      }\n    ]\n  }\n}"
                )
            )
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 파라미터",
            content = @Content(
                schema = @Schema(implementation = ApiResponse.class),
                examples = @ExampleObject(
                    name = "에러 예시",
                    summary = "유효하지 않은 파라미터",
                    value = "{\n  \"success\": false,\n  \"message\": \"sort must be 'sim' or 'date'\"\n}"
                )
            )
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<NewsResponse>> searchNews(
            @Parameter(description = "검색 키워드", example = "이재명")
            @RequestParam String query,
            @Parameter(description = "검색 시작 위치 (1~1000)", example = "1")
            @RequestParam(required = false, defaultValue = "1") int start,
            @Parameter(description = "한 번에 표시할 결과 수 (10~100)", example = "10")
            @RequestParam(required = false, defaultValue = "10") int display,
            @Parameter(
                description = "정렬 기준: sim(정확도), date(최신순)",
                example = "date",
                schema = @Schema(allowableValues = {"sim", "date"})
            )
            @RequestParam(required = false, defaultValue = "sim") String sort
    ) {

        NewsResponse response = naverClient.search(query, display, start, sort);

        return ResponseEntity.ok(ApiResponse.success("뉴스 정보 조회 성공.", response));
    }
}
