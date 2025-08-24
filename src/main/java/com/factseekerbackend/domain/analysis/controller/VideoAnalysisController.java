package com.factseekerbackend.domain.analysis.controller;

import com.factseekerbackend.domain.analysis.controller.dto.request.VideoUrlRequest;
import com.factseekerbackend.domain.analysis.controller.dto.request.VideoIdsRequest;
import com.factseekerbackend.domain.analysis.controller.dto.response.*;
import com.factseekerbackend.domain.analysis.controller.dto.response.fastapi.ClaimDto;
import com.factseekerbackend.domain.analysis.entity.Top10VideoAnalysis;
import com.factseekerbackend.domain.analysis.entity.VideoAnalysisStatus;
import com.factseekerbackend.domain.analysis.service.VideoAnalysisService;
import com.factseekerbackend.domain.analysis.service.fastapi.FactCheckTriggerService;
import com.factseekerbackend.domain.user.entity.CustomUserDetails;
import com.factseekerbackend.global.common.ApiResponse;
import com.factseekerbackend.global.exception.BusinessException;
import com.factseekerbackend.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.factseekerbackend.domain.analysis.repository.Top10VideoAnalysisRepository;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
@Tag(name = "비디오 분석", description = "유튜브 비디오 분석 및 팩트체크 API")
@Validated
public class VideoAnalysisController {

    private final FactCheckTriggerService factCheckTriggerService;
    private final VideoAnalysisService videoAnalysisService;
    private final Top10VideoAnalysisRepository top10VideoAnalysisRepository;
    private final ObjectMapper om;

    @Operation(
            summary = "비디오 분석 결과 조회",
            description = "특정 비디오 분석 ID에 대한 분석 결과를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "분석 결과 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(name = "성공 예시 (claims 포함)", value = "{\n  \"success\": true,\n  \"message\": \"비디오 분석 결과\",\n  \"data\": {\n    \"videoId\": \"4zB4rPMgCuQ\",\n    \"videoUrl\": \"https://youtu.be/4zB4rPMgCuQ?si=Z_aG4sqMyymmfiIH\",\n    \"totalConfidenceScore\": 38,\n    \"summary\": \"증거 확보된 주장 비율: 80.0%\",\n    \"channelType\": \"논평형\",\n    \"channelTypeReason\": \"개인 의견과 가치 판단이 주를 이루며, 특정 인물에 대한 비판과 해석이 중심이 되고 있음.\",\n    \"claims\": [\n      {\n        \"claim\": \"내란 특검팀은 이상민 전 행정안전부 장관을 내란 중요임무 종사 및 직권남용 위증 혐의로 구속상태에서 재판에 넘겼다.\",\n        \"result\": \"likely_true\",\n        \"confidence_score\": 20,\n        \"evidence\": [\n          {\n            \"url\": \"https://www.khan.co.kr/article/202501132125025\",\n            \"relevance\": \"yes\",\n            \"fact_check_result\": \"아니오\",\n            \"justification\": \"기사는 이상민 전 행정안전부 장관이 특정 언론사에 대한 단전·단수 지시를 내렸다는 내용을 다루고 있지만, 내란 특검팀이 그를 내란 중요임무 종사 및 직권남용 위증 혐의로 구속상태에서 재판에 넘겼다는 주장에 대한 구체적인 사실 설명은 포함되어 있지 않습니다.\",\n            \"snippet\": \"허석곤 소방청장은 12·3 비상계엄이 선포된 직후 이상민 당시 행정안전부 장관으로부터 “특정 언론사에 대한 단전·단수에 협조하라”는 지시를 받았다고 밝혔다. 대상이 된 언론사는 경향신문과 한겨레, MBC 등이었다. 비상계엄 직후 정부가 정권에 비판적인 언론사에 대한 단전·단수를 기도했다는 사실이 처음 드러났다.\"\n          }\n        ]\n      },\n      {\n        \"claim\": \"특검팀은 한덕수 전 총리에 대한 구속영장 청구를 검토하고 있다.\",\n        \"result\": \"likely_true\",\n        \"confidence_score\": 70,\n        \"evidence\": [\n          {\n            \"url\": \"http://www.segye.com/content/html/2025/07/03/20250703501268.html\",\n            \"relevance\": \"yes\",\n            \"fact_check_result\": \"아니오\",\n            \"justification\": \"기사는 한덕수 전 총리가 특검팀에 소환되어 조사를 받았다는 사실을 다루고 있지만, 구속영장 청구에 대한 구체적인 언급이나 설명은 포함되어 있지 않습니다.\",\n            \"snippet\": \"2일 내란 특별검사팀에 소환된 한덕수 전 국무총리가 밤 11시 40분쯤 조사를 마치고 귀가했다. 이날 오전 10시에 출석한 지 13시간 40분 만이다. 한 전 총리는 2일 오전 10시쯤 서울고검에 출석해 같은 날 오후 11시 43분쯤 청사를 나섰다.**\"\n          },\n          {\n            \"url\": \"https://www.chosun.com/national/court_law/2025/07/07/WTXEKDPYD5H5NFNRWBMFXJKCIM/?utm_source=bigkinds&utm_medium=original&utm_campaign=news\",\n            \"relevance\": \"yes\",\n            \"fact_check_result\": \"예\",\n            \"justification\": \"기사는 특검팀이 한덕수 전 총리에 대한 구속영장 청구를 검토 중이라는 내용을 명확히 언급하고 있으며, 관련 사건과 인물에 대한 구체적인 설명을 제공하고 있다.\",\n            \"snippet\": \"특검은 한 전 총리에 대해서도 구속영장 청구를 검토 중인 것으로 알려졌다. 특검이 청구한 윤 전 대통령 구속영장을 보면, 강의구 전 대통령실 부속실장은 계엄 해제 이튿날인 지난해 12월 5일 낮 김주현 당시 민정수석으로부터 “대통령의 국법상 행위는 문서로 해야 하며, 국무총리와 관계 국무위원이 부서를 해야 한다”는 말을 듣고, 새로 계엄 선포문을 작성해 한 전 총리와 김용현 전 국방부 장관에게 서명을 받은 것으로 조사됐다. 이 선포문에는 대통령·국무총리·국방장관 서명란이 포함돼 있다.**\"\n          },\n          {\n            \"url\": \"https://www.khan.co.kr/article/202507072137025\",\n            \"relevance\": \"yes\",\n            \"fact_check_result\": \"예\",\n            \"justification\": \"기사는 한덕수 전 총리에 대한 구속영장 청구 검토와 관련된 내용을 다루고 있으며, 특검팀이 한덕수 전 총리를 공범으로 적시하고 구속영장 청구를 검토 중이라는 구체적인 사실을 설명하고 있습니다.\",\n            \"snippet\": \"12·3 불법계엄을 수사하는 조은석 특별검사팀이 윤석열 전 대통령에 대한 구속영장 청구서에 한덕수 전 국무총리를 ‘공범’으로 적시한 것으로 나타났다. ‘사후 계엄 선포문’ 작성 및 폐기에 관여했다는 이유에서다. 특검은 한 전 총리에 대한 구속영장 청구도 검토 중인 것으로 알려졌다.**\"\n          }\n        ]\n      },\n      {\n        \"claim\": \"홍준표 전 대구시장은 한덕수 전 총리를 향해 대통령 직무대행을 하면서 대선을 중립적인 입장에서 관리하는 것이 50여 년 관류 생활을 아름답게 끝낼 수 있을 거라고 경고했다.\",\n        \"result\": \"likely_true\",\n        \"confidence_score\": 80,\n        \"evidence\": [\n          {\n            \"url\": \"http://www.hani.co.kr/arti/politics/assembly/1192342.html\",\n            \"relevance\": \"yes\",\n            \"fact_check_result\": \"예\",\n            \"justification\": \"기사에서 홍준표 전 대구시장이 한덕수 전 총리에 대해 대선을 공정하게 관리해야 한다고 언급하며, 대선 출마설을 비판하는 내용이 포함되어 있어 주장을 뒷받침합니다.\",\n            \"snippet\": \"홍 전 시장은 14일 오후 서울 여의도 국회 근처 대하빌딩에 마련된 경선 캠프에서 출마 선언을 한 뒤 기자들과 만나 “(한덕수 대통령 권한대행은) 대선을 공정하게 이끌기 위해 관리할 직무 대행이다. 그런 사람이 대선에 나오겠다고 하는 것은 비상식”이라고 했다. 이어 “윤석열 정부가 탄핵이 됐다. 총리가 첫 번째 책임자”라며 “(한덕수 대행 추대를) 추진하는 것 자체가 몰상식”이라고 덧붙였다.**\"\n          },\n          {\n            \"url\": \"https://www.chosun.com/politics/assembly/2025/04/08/BTWLP7POKNCNXOJDA3Z7YSUULU/?utm_source=bigkinds&utm_medium=original&utm_campaign=news\",\n            \"relevance\": \"yes\",\n            \"fact_check_result\": \"아니오\",\n            \"justification\": \"기사는 홍준표 전 대구시장이 대선 출마를 선언했다는 내용을 포함하고 있지만, 한덕수 전 총리를 향해 대통령 직무대행을 중립적으로 관리하라는 경고에 대한 구체적인 사실 설명은 포함되어 있지 않습니다.\",\n            \"snippet\": \"홍준표 대구시장도 오는 14일 서울 여의도 대하빌딩에서 대선 출마를 선언한다. 홍 시장은 시장직을 사퇴하고 경선에 나설 방침이다. 이날 그는 헌법재판소 폐지, 수능 연 2회 실시 등을 제안하기도 했다.**\"\n          },\n          {\n            \"url\": \"http://www.segye.com/content/html/2025/05/07/20250507505075.html\",\n            \"relevance\": \"yes\",\n            \"fact_check_result\": \"아니오\",\n            \"justification\": \"기사에는 홍준표 전 대구시장이 한덕수 전 총리에 대해 언급한 내용이 포함되어 있으나, 그가 한덕수 전 총리를 향해 대통령 직무대행을 하면서 대선을 중립적으로 관리하라는 경고를 했다는 구체적인 사실 설명은 포함되어 있지 않습니다.\",\n            \"snippet\": \"이어 “그러나 용산과 당 지도부가 합작하여 느닷없이 한덕수를 띄우며 탄핵 대선을 윤석열 재신임 투표로 몰고 가려고 했을 때, 나는 설마 대선 패배가 불 보듯 뻔한 그런 짓을 자행하겠냐는 의구심이 들었다”고 했다. 그러면서 “그게 현실화되면서 김문수는 김덕수라고 자칭하고 다녔고, 용산과 당 지도부도 김문수는 만만하니 김문수를 밀어 한덕수의 장애가 되는 홍준표는 떨어트리자는 공작을 꾸미고 있었다”고 주장했다. 그는 “나를 지지하던 사람들은 순식간에 김문수 지지로 돌아섰고 한순간 김문수가 당원 지지 1위로 올라섰다”며 “김문수로서는 이들의 음험한 공작을 역이용했고 그 때부터 나는 이 더러운 판에 더이상 있기 싫어 졌다”고 했다.**\"\n          }\n        ]\n      },\n      {\n        \"claim\": \"홍준표 전 대구시장은 이상민 전 장관이 내란 연루로 구속되었다고 말했다.\",\n        \"result\": \"insufficient_evidence\",\n        \"confidence_score\": 0,\n        \"evidence\": []\n      },\n      {\n        \"claim\": \"홍준표 전 대구시장은 윤석열 전 대통령 옆에서 인생을 망쳤다는 취지로 한덕수 전 총리와 이상민 전 장관을 비판했다.\",\n        \"result\": \"likely_true\",\n        \"confidence_score\": 20,\n        \"evidence\": [\n          {\n            \"url\": \"http://www.segye.com/content/html/2025/05/07/20250507505887.html\",\n            \"relevance\": \"yes\",\n            \"fact_check_result\": \"아니오\",\n            \"justification\": \"기사는 홍준표 전 대구시장이 윤석열 전 대통령과 관련된 비판을 하고 있지만, 한덕수 전 총리와 이상민 전 장관에 대한 직접적인 비판이나 그들이 인생을 망쳤다는 취지의 발언은 포함되어 있지 않습니다.\",\n            \"snippet\": \"홍 전 시장은 \\\"용병 하나 잘못 들여 나라가 멍들고 당도 멍들고 있다\\\"며 \\\"3년 전 당원들이 나를 선택했으면 나라와 당이 이 꼴이 됐겠나. '오호통재라'라는 말은 이때 하는 말\\\"이라고 비꼬았다. <연합> 바이라인 [ⓒ 세계일보 & Segye.com, 무단전재 및 재배포 금지]**\"\n          }\n        ]\n      }\n    ],\n    \"keywords\": \"내란 특검팀, 불법 비상계연방조, 한덕수 전 국무총리, 이상민 전 행정안전부 장관, 구속영장 청구, 홍준표 전 대구시장, 윤석열 전 대통령, 대통령 직무대행, 이태원 참사\",\n    \"threeLineSummary\": \"내란 특검팀은 한덕수 전 국무총리를 불법 비상계연방조 가담 의혹으로 16시간 조사하고, 이상민 전 행정안전부 장관을 내란 중요임무 종사 및 직권남용 위증 혐의로 구속 상태에서 재판에 넘겼습니다. 홍준표 전 대구시장은 한덕수 전 총리가 대통령 직무대행을 중립적으로 수행하지 못하고 윤석열 전 대통령과 그 추종 세력에 휘둘렸다고 비판했습니다. 또한, 홍 전 시장은 이상민 전 장관이 이태원 참사 당시 퇴진 요구를 무시하다가 내란 연루로 구속되는 수모를 당했다고 지적했습니다.\",\n    \"createdAt\": \"2025-08-24T10:42:39.271894\",\n    \"status\": \"COMPLETED\"\n  }\n}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "분석 결과를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(name = "에러 예시", value = "{\n  \"success\": false,\n  \"message\": \"분석 결과를 찾을 수 없습니다.\"\n}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(name = "에러 예시", value = "{\n  \"success\": false,\n  \"message\": \"인증이 필요합니다.\"\n}")
                    )
            )
    })
    @GetMapping("/{videoAnalysisId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VideoAnalysisResponse>> getVideoAnalysis(
            @Parameter(description = "비디오 분석 ID", example = "1")
            @PathVariable("videoAnalysisId") Long videoAnalysisId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = userDetails.getId();
            VideoAnalysisResponse videoAnalysis = videoAnalysisService.getVideoAnalysis(userId, videoAnalysisId);
            if (videoAnalysis.getStatus() == VideoAnalysisStatus.FAILED) {
                return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                        .body(ApiResponse.error("비디오 분석에 실패했습니다."));
            }
            if (videoAnalysis.getStatus() == VideoAnalysisStatus.PENDING) {
                return ResponseEntity.status(org.springframework.http.HttpStatus.ACCEPTED)
                        .body(ApiResponse.success("분석 진행 중입니다.", videoAnalysis));
            }
            return ResponseEntity.ok(ApiResponse.success("비디오 분석 결과를 성공적으로 조회했습니다.", videoAnalysis));
        } catch (BusinessException e) {
            throw e;
        } catch (NullPointerException e) {
            log.warn("[API] 비디오 분석 결과를 찾을 수 없음: {}", e.getMessage());
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("분석 결과를 찾을 수 없습니다."));
        } catch (Exception e) {
            log.error("[API] 비디오 분석 결과 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                    .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
        }
    }

    @Operation(
            summary = "비디오 분석 요청",
            description = "유튜브 URL을 입력받아 비디오 분석을 요청합니다. 로그인 여부에 따라 다른 처리를 수행합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "분석 요청 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "로그인 사용자 - 접수 응답",
                                            value = "{\n  \"success\": true,\n  \"message\": \"비디오 분석이 성공적으로 요청되었습니다.\",\n  \"data\": {\n    \"analysisId\": 123,\n    \"status\": \"PENDING\"\n  }\n}"
                                    ),
                                    @ExampleObject(
                                            name = "비로그인 사용자 - 즉시 결과",
                                            value = "{\n  \"success\": true,\n  \"message\": \"비디오 분석 결과\",\n  \"data\": {\n    \"videoId\": \"abc123\",\n    \"videoUrl\": \"https://www.youtube.com/watch?v=abc123\",\n    \"totalConfidenceScore\": 78,\n    \"summary\": \"영상 요약...\",\n    \"channelType\": \"뉴스\",\n    \"channelTypeReason\": \"설명...\",\n    \"claims\": [],\n    \"keywords\": \"선거,경제,국회\",\n    \"threeLineSummary\": \"1) ... 2) ... 3) ...\",\n    \"createdAt\": \"2025-01-01T12:34:56\",\n    \"status\": \"COMPLETED\"\n  }\n}"
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\n  \"success\": false,\n  \"message\": \"잘못된 입력값입니다.\"\n}")
                    )
            )
    })
    @PostMapping
    public CompletableFuture<ResponseEntity<ApiResponse<?>>> saveVideoAnalysis(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "분석할 유튜브 URL",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VideoUrlRequest.class),
                            examples = @ExampleObject(
                                    name = "요청 예시",
                                    value = "{\"youtube_url\": \"https://www.youtube.com/watch?v=H-D2LfzB1wM\"}"
                            )
                    )
            )
            @Valid @RequestBody VideoUrlRequest videoUrlRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[API] 비디오 분석 요청: {}", videoUrlRequest.youtubeUrl());
        String youtubeUrl = videoUrlRequest.youtubeUrl();
        try {
            if (userDetails != null) {
                Long userId = userDetails.getId();
                log.info("[API] 로그인 사용자 분석 요청 - User ID: {}", userId);
                Long analysisId = factCheckTriggerService.triggerAndReturnId(youtubeUrl, userId);
                return CompletableFuture.completedFuture(
                        ResponseEntity.ok(
                                ApiResponse.success(
                                        "비디오 분석이 성공적으로 요청되었습니다.",
                                        new AnalysisStartResponse(analysisId, VideoAnalysisStatus.PENDING)
                                )
                        )
                );
            } else {
                log.info("[API] 비로그인 사용자 분석 요청");
                return factCheckTriggerService.triggerSingleToRdsToNotLogin(youtubeUrl)
                        .thenApply(result -> {
                            if (result == null) {
                                return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.getStatus())
                                        .body(ApiResponse.error(ErrorCode.INVALID_INPUT_VALUE.getMessage()));
                            }
                            if (result.getStatus() == VideoAnalysisStatus.FAILED) {
                                return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                                        .body(ApiResponse.error("비디오 분석에 실패했습니다."));
                            }
                            return ResponseEntity.ok(ApiResponse.success("비디오 분석 결과", result));
                        });
            }
        } catch (BusinessException e) {
            throw e; // 전역 예외 처리기로 위임
        } catch (Exception e) {
            log.error("[API] 비디오 분석 요청 실패: {}", e.getMessage(), e);
            return CompletableFuture.completedFuture(
                    ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                            .body(ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()))
            );
        }
    }



    @Operation(
            summary = "Top10 특정 비디오 분석 조회",
            description = "특정 비디오 ID의 Top10 분석 결과를 조회합니다. 데이터가 아직 준비되지 않은 경우 202(ACCEPTED)로 대기 상태를 반환합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "분석 결과 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VideoAnalysisResponse.class),
                            examples = @ExampleObject(name = "성공 예시", value = "{\n  \"videoId\": \"abc123\",\n  \"videoUrl\": \"https://www.youtube.com/watch?v=abc123\",\n  \"totalConfidenceScore\": 78,\n  \"summary\": \"영상 요약...\",\n  \"channelType\": \"뉴스\",\n  \"channelTypeReason\": \"설명...\",\n  \"claims\": [],\n  \"keywords\": \"선거,경제,국회\",\n  \"threeLineSummary\": \"1) ... 2) ... 3) ...\",\n  \"createdAt\": \"2025-01-01T12:34:56\",\n  \"status\": \"COMPLETED\"\n}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "해당 ID는 Top 10 목록에 없음",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "202",
                    description = "분석 대기 상태 (PENDING)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VideoAnalysisResponse.class),
                            examples = @ExampleObject(name = "대기 예시", value = "{\n  \"videoId\": \"abc123\",\n  \"status\": \"PENDING\"\n}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\n  \"success\": false,\n  \"message\": \"잘못된 입력값입니다.\"\n}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "분석 실패 (FAILED)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VideoAnalysisResponse.class),
                            examples = @ExampleObject(value = "{\n  \\\"videoId\\\": \\\"abc123\\\",\n  \\\"status\\\": \\\"FAILED\\\"\n}")
                    )
            )
    })
    @GetMapping("/top10/{videoId}")
    public ResponseEntity<VideoAnalysisResponse> getTop10VideoAnalysis(
            @Parameter(description = "비디오 ID", example = "exampleVideoId")
            @PathVariable("videoId") String videoId) {
        return top10VideoAnalysisRepository.findById(videoId)
                .map(analysis -> {
                    if (analysis.getStatus() == VideoAnalysisStatus.FAILED) {
                        return ResponseEntity.status(500)
                                .body(VideoAnalysisResponse.builder()
                                        .videoId(analysis.getVideoId())
                                        .status(VideoAnalysisStatus.FAILED)
                                        .build());
                    }
                    Object claims = parseClaims(analysis.getClaims());
                    return ResponseEntity.ok(VideoAnalysisResponse.from(analysis, claims));
                })
                .orElseGet(() -> {
                    // DB에 없을 때 Redis Top10에도 없으면 404
                    if (!videoAnalysisService.isInTop10(videoId)) {
                        return ResponseEntity.notFound().build();
                    }
                    // Top10에 있으면 PENDING(202)
                    return ResponseEntity.accepted().body(
                            VideoAnalysisResponse.builder()
                                    .videoId(videoId)
                                    .status(VideoAnalysisStatus.PENDING)
                                    .build()
                    );
                });
    }

    @Operation(
            summary = "Top 10 유튜브 키워드 조회",
            description = "특정 Top 10 비디오 ID에 대한 키워드를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "키워드 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = KeywordsResponse.class),
                            examples = @ExampleObject(value = "{\n  \"keywords\": [\"정책\", \"경제\", \"토론\"]\n}")
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\n  \"success\": false,\n  \"message\": \"잘못된 입력값입니다.\"\n}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\n  \"success\": false,\n  \"message\": \"서버 내부 오류가 발생했습니다.\"\n}"))
            )
    })
    @GetMapping("/top10/{videoId}/keywords")
    public ResponseEntity<ApiResponse<KeywordsResponse>> getTop10YoutubeKeywords(
            @Parameter(description = "비디오 ID", example = "exampleVideoId")
            @PathVariable String videoId) {
        return ResponseEntity.ok(ApiResponse.success("조회에 성공했습니다.", videoAnalysisService.getTop10YoutubeKeywords(videoId)));
    }

    private Object parseClaims(String claimsJson) {
        if (claimsJson == null || claimsJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return om.readValue(claimsJson, new TypeReference<List<ClaimDto>>() {
            });
        } catch (JsonProcessingException e) {
            log.error("json 파싱에 실패했습니다.: {}", claimsJson, e);
            return Collections.emptyList();
        }
    }

    @Operation(
            summary = "단일 비디오 진행률/상태 조회",
            description = "특정 비디오 ID에 대한 분석 진행률 및 상태를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\n  \"success\": true,\n  \"message\": \"조회에 성공했습니다.\",\n  \"data\": {\n    \"requested\": 1,\n    \"completed\": 1,\n    \"pending\": 0,\n    \"failed\": 0,\n    \"notFound\": 0,\n    \"results\": [\n      {\n        \"videoId\": \"abc123\",\n        \"status\": \"COMPLETED\",\n        \"totalConfidenceScore\": 78\n      }\n    ]\n  }\n}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\n  \"success\": false,\n  \"message\": \"잘못된 입력값입니다.\"\n}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\n  \"success\": false,\n  \"message\": \"서버 내부 오류가 발생했습니다.\"\n}"))
            )
    })
    @GetMapping("/top10/{videoId}/percents")
    public ResponseEntity<ApiResponse<PercentStatusData>> getVideoPercent(
            @PathVariable("videoId") String videoId
    ) {
        VideoIdsRequest request = new VideoIdsRequest(List.of(videoId));
        PercentStatusData statusData = videoAnalysisService.getTop10VideosPercent(request);
        return ResponseEntity.ok(ApiResponse.success("조회에 성공했습니다.", statusData));
    }

    @Operation(
            summary = "복수 비디오 진행률/상태 조회",
            description = "여러 비디오 ID에 대한 분석 진행률 및 상태를 일괄 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class),
                            examples = @ExampleObject(value = "{\n  \"success\": true,\n  \"message\": \"조회 성공(일부 불가 포함)\",\n  \"data\": {\n    \"requested\": 2,\n    \"completed\": 1,\n    \"pending\": 1,\n    \"failed\": 0,\n    \"notFound\": 0,\n    \"results\": [\n      {\n        \"videoId\": \"abc123\",\n        \"status\": \"COMPLETED\",\n        \"totalConfidenceScore\": 78\n      },\n      {\n        \"videoId\": \"def456\",\n        \"status\": \"PENDING\"\n      }\n    ]\n  }\n}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (요청 본문 검증 실패 등)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\n  \"success\": false,\n  \"message\": \"잘못된 입력값입니다.\"\n}"))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class), examples = @ExampleObject(value = "{\n  \"success\": false,\n  \"message\": \"서버 내부 오류가 발생했습니다.\"\n}"))
            )
    })
    @GetMapping("/top10/percents")
    public ResponseEntity<ApiResponse<PercentStatusData>> getVideosPercent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "조회할 비디오 ID 목록",
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = VideoIdsRequest.class),
                            examples = @ExampleObject(value = "{\n  \"videoIds\": [\"abc123\", \"def456\"]\n}"))
            )
            @RequestBody VideoIdsRequest request
    ) {
        PercentStatusData statusData = videoAnalysisService.getTop10VideosPercent(request);
        return ResponseEntity.ok(ApiResponse.success("조회 성공(일부 불가 포함)", statusData));
    }
}
