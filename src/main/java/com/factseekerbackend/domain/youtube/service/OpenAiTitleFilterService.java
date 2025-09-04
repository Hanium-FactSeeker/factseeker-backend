package com.factseekerbackend.domain.youtube.service;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.StructuredResponse;
import com.openai.models.responses.StructuredResponseCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
@Slf4j
public class OpenAiTitleFilterService {
    private final OpenAIClient client;
    private static final ChatModel MODEL = ChatModel.GPT_4O; // 통일된 모델 선택

    // 공통 가이드(한국어): 단건/배치 모두 동일한 분류 기준을 사용
    private static final String GUIDE_KO = """
        너는 매우 엄격한 이진 분류기다.
        입력으로 주어지는 ‘한국어 유튜브 제목’이 정치(POLITICS) 주제인지 판별하라.

        [정치 범위 예시]
        - 국가 권력/공공 의사결정/선거/정당/국회/청문회/입법/정부 조직(대통령·총리·장관·지자체장)
        - 사법·검찰의 권력행사(영장·수사·탄핵·특검)
        - 외교·안보·남북/국방/대북
        - 재난에 대한 공권력 대응/공공정책(세법·부동산·의대정원·연금·노동·교육·규제)

        [강한 POLITICS 신호]
        - 인물/직함: 윤석열, 한동훈, 이재명, 이준석, 오세훈, 홍준표, 원희룡, 김건희, 대통령, 총리, 장관, 의원, 지사, 시장, 구청장 등
        - 정당/기구: 국민의힘, 민주당, 정의당, 국회, 비대위, 혁신위, 공천, 컷오프, 원내대표, 당대표, 최고위 등
        - 제도/행위: 청문회, 탄핵, 국정조사, 특검, 영장심사, 압수수색, 공약, 개각, 개헌, 선거(총선·대선·보선·전당대회)
        - 정책 키워드: 의대정원, 의료개혁, 종부세/취득세, 전월세, 재건축/재개발, 최저임금, 연금개혁, 방위비, 병역, 원전/에너지, 검찰개혁, 언론3법 등
        - 외교/안보/남북: 정상회담, 한미/한일/한중, 대북제재, 미사일, 군사훈련, NSC, 국방부 등

        [경계 규칙]
        - 연예·유튜버·기업 이슈라도 ‘정부/정당/선거/국회/공공정책/권력 행사’와 직접 연결되면 POLITICS.
        - 재난/사건 보도는 ‘정부 대응·책임 공방·정책’이 중심이면 POLITICS, 단순 사고면 NOT_POLITICS.
        - 경제/부동산/코인/주가 등은 ‘정부 정책·국회 입법·규제기관 결정’이 핵심이면 POLITICS, 일반 투자/리뷰면 NOT_POLITICS.
        - 판결/수사 보도는 ‘정치인·정치 관련 사건’이거나 권력구조 쟁점이면 POLITICS, 연예인/일반 형사사건은 대체로 NOT_POLITICS.

        [NOT_POLITICS의 흔한 패턴]
        - 순수 연예/게임/IT 리뷰, 일상 브이로그, 다이어트·운동, 반려동물
        - 일반 사건사고(정책 언급 無), 주식/코인 분석(정부정책 비중 無), 해외 토막상식, 순수 교육/학습 팁

        [출력 규칙]
        - 반드시 지정된 JSON만 출력하고 다른 텍스트를 포함하지 마라.
    """;

    // Structured Outputs는 'object' 타입 JSON 스키마만 허용하므로,
    // 단일 라벨을 위한 최소 DTO를 내부에 정의한다.
    public static class Decision {
        private String label;

        public Decision() {}
        public Decision(String label) { this.label = label; }
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
    }

    // 배치 응답용 DTO: results[index,label]
    public static class BatchDecision {
        private List<Item> results;
        public List<Item> getResults() { return results; }
        public void setResults(List<Item> results) { this.results = results; }
        public static class Item {
            private Integer index;
            private String label;
            public Integer getIndex() { return index; }
            public void setIndex(Integer index) { this.index = index; }
            public String getLabel() { return label; }
            public void setLabel(String label) { this.label = label; }
        }
    }

    public boolean isPoliticalTitle(String title) {
        try {
            String prompt = buildTitlePrompt(title);

            StructuredResponseCreateParams<Decision> params = ResponseCreateParams.builder()
                    .model(MODEL)
                    .input(prompt)
                    .text(Decision.class)
                    .build();

            StructuredResponse<Decision> res = client.responses().create(params);

            String decision = res.output().stream()
                    .map(item -> item.message().orElse(null))
                    .filter(Objects::nonNull)
                    .flatMap(msg -> msg.content().stream())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No content"))
                    .asOutputText()
                    .getLabel();

            return toBool(decision);
        } catch (Exception e) {
            log.error("OpenAI title gate error. title={}", title, e);
            // 실패 시 필터 아웃(보수적)
            return false;
        }
    }

    private String buildTitlePrompt(String title) {
        String t = Objects.requireNonNullElse(title, "");
        return """
            %s

            [단건 판단]
            아래 제목이 정치 주제이면 {"label":"POLITICS"}, 아니면 {"label":"NOT_POLITICS"}만 정확히 출력하라.

            Title:
            ---start---
            %s
            ---end---
            """.formatted(GUIDE_KO, t);
    }

    /**
     * 한 번의 요청으로 다수 제목을 분류한다. 입력 순서를 유지한다.
     */
    public List<Boolean> arePoliticalTitles(List<String> titles) {
        List<Boolean> out = new ArrayList<>();
        if (titles == null || titles.isEmpty()) return out;
        try {
            String prompt = buildBatchPrompt(titles);

            StructuredResponseCreateParams<BatchDecision> params = ResponseCreateParams.builder()
                    .model(MODEL)
                    .input(prompt)
                    .text(BatchDecision.class)
                    .build();

            StructuredResponse<BatchDecision> res = client.responses().create(params);

            BatchDecision bd = res.output().stream()
                    .map(item -> item.message().orElse(null))
                    .filter(Objects::nonNull)
                    .flatMap(msg -> msg.content().stream())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No content"))
                    .asOutputText();

            // 초기값 false로 채우고, 응답 인덱스에 따라 덮어쓰기
            out.addAll(Collections.nCopies(titles.size(), Boolean.FALSE));
            if (bd != null && bd.getResults() != null) {
                for (BatchDecision.Item it : bd.getResults()) {
                    if (it == null) continue;
                    Integer idx = it.getIndex();
                    if (idx != null && idx >= 0 && idx < out.size()) {
                        out.set(idx, toBool(it.getLabel()));
                    }
                }
            }
        } catch (Exception e) {
            log.error("OpenAI batch title gate error. count={}", (titles == null ? 0 : titles.size()), e);
            // 실패 시 모두 false로 반환해 보수적으로 필터 아웃
            out.clear();
            out.addAll(Collections.nCopies(titles == null ? 0 : titles.size(), Boolean.FALSE));
        }
        return out;
    }

    private String buildBatchPrompt(List<String> titles) {
        StringBuilder listBlock = new StringBuilder();
        for (int i = 0; i < titles.size(); i++) {
            String t = Objects.requireNonNullElse(titles.get(i), "");
            listBlock.append(i).append(") ---start---\n").append(t).append("\n---end---\n");
        }

        return """
            %s

            [배치 판단]
            각 제목에 대해 정치 주제이면 "POLITICS", 아니면 "NOT_POLITICS"로 분류하라.
            반드시 아래의 JSON 한 개만 출력하라(그 외 텍스트 금지):
            {"results":[{"index":number,"label":"POLITICS|NOT_POLITICS"}, ...]}

            Titles (zero-based index):
            %s
            """.formatted(GUIDE_KO, listBlock);
    }

    private boolean toBool(String label) {
        return "POLITICS".equalsIgnoreCase(label == null ? "" : label.trim());
    }

}
