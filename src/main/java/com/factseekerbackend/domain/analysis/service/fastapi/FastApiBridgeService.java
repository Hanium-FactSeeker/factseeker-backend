package com.factseekerbackend.domain.analysis.service.fastapi;

import com.factseekerbackend.domain.analysis.entity.AnalysisStatus;
import com.factseekerbackend.domain.analysis.entity.video.Top10VideoAnalysis;
import com.factseekerbackend.domain.analysis.repository.Top10VideoAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
@RequiredArgsConstructor
public class FastApiBridgeService {

    private final FactCheckTriggerService triggerService;      // FastAPI 호출 → RDS 저장
    private final RedisTemplate<String, Object> cacheRedis;    // Redis 읽기용
    private final Top10VideoAnalysisRepository top10VideoAnalysisRepository;

    @Qualifier("factApiExecutor")
    private final Executor factApiExecutor;                    // 전용 스레드풀 (동시성 3)

    private static final int SIZE = 10;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter LOCK_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    /**
     * 스케줄 B: 매 시 정각 + 10초에 FastAPI 호출 시작
     */
    @Scheduled(cron = "10 0 0 * * *", zone = "Asia/Seoul")
    public void callFastApiForTop10() {
        String tag = OffsetDateTime.now(KST).format(LOCK_FMT);

        // 분산락: 같은 분 중복 실행 방지
        String lockKey = "lock:callFastAPI:" + tag;
        Boolean locked = cacheRedis.opsForValue().setIfAbsent(lockKey, "1", Duration.ofMinutes(10));
        if (!Boolean.TRUE.equals(locked)) {
            log.info("스킵: 다른 인스턴스가 실행 중 tag={}", tag);
            return;
        }

        try {
            // 캐시 신선도 단일 체크(정각+10초 기준, 120s 이내면 최신)
            if (!isCacheFreshOnce(120)) {
                log.warn("캐시 갱신 확인 실패(120s 이내 갱신 아님). 진행합니다.");
            }

            // videoId 일괄 조회
            List<String> videoIds = fetchTopNVideoIdsPipeline(SIZE);
            if (videoIds.isEmpty()) {
                log.warn("대상 videoId 없음. 종료.");
                return;
            }
            log.info("대상 {}건: {}", videoIds.size(), videoIds);

            // PENDING 상태로 Top10VideoAnalysis 레코드 미리 생성/업데이트
            for (String videoId : videoIds) {
                top10VideoAnalysisRepository.findById(videoId).ifPresentOrElse(
                    analysis -> {
                        // 이미 존재하는 경우, PENDING이 아니면 업데이트 (재분석 필요 시)
                        if (analysis.getStatus() != AnalysisStatus.PENDING) {
                            top10VideoAnalysisRepository.save(analysis.toBuilder()
                                .status(AnalysisStatus.PENDING)
                                .createdAt(LocalDateTime.now()) // 생성 시간 업데이트
                                .build());
                        }
                    },
                    () -> {
                        // 존재하지 않는 경우, 새로 생성
                        top10VideoAnalysisRepository.save(Top10VideoAnalysis.builder()
                            .videoId(videoId)
                            .status(AnalysisStatus.PENDING)
                            .createdAt(LocalDateTime.now())
                            .build());
                    }
                );
            }
            log.info("Top10VideoAnalysis PENDING 상태로 {}건 미리 생성/업데이트 완료.", videoIds.size());

            // 전용 스레드풀로 동시 실행(풀 설정이 동시 3개로 제한)
            List<CompletableFuture<Void>> futures = new ArrayList<CompletableFuture<Void>>(videoIds.size());
            for (String id : videoIds) {
                CompletableFuture<Void> f = CompletableFuture.runAsync(() -> {
                    try {
                        triggerService.triggerSingleToRds(id); // 네가 가진 RDS 저장 메서드
                    } catch (Exception e) {
                        log.warn("FastAPI 처리 실패 id={}: {}", id, e.toString());
                    }
                }, factApiExecutor);
                futures.add(f);
            }
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            log.info("FastAPI 트리거 완료 ({}건)", videoIds.size());

        } catch (Exception e) {
            log.error("트리거 예외: {}", e.toString(), e);
        }
    }

    /** rank:1 updatedAt이 now-120s 이내면 최신으로 간주 (단일 체크) */
    private boolean isCacheFreshOnce(int freshnessSec) {
        Optional<String> tsOpt = getUpdatedAtFromRank(1);
        if (!tsOpt.isPresent()) return false;
        try {
            OffsetDateTime t = OffsetDateTime.parse(tsOpt.get());
            long age = Math.abs(java.time.Duration.between(t, OffsetDateTime.now(KST)).getSeconds());
            return age <= freshnessSec;
        } catch (Exception ignore) {
            return false;
        }
    }

    /** Redis: rank 키의 updatedAt 읽기 (triggerService 의존 제거) */
    private Optional<String> getUpdatedAtFromRank(int rank) {
        Object v = cacheRedis.opsForHash().get(rankKey(rank), "updatedAt");
        if (v instanceof String) return Optional.of((String) v);
        return Optional.empty();
    }

    /** Redis: rank:1..n 의 videoId를 파이프라인으로 일괄 HGET (중복 제거 + 순서 유지) */
    private List<String> fetchTopNVideoIdsPipeline(int n) {
        List<Object> raw = cacheRedis.executePipelined((RedisCallback<Object>) connection -> {
            for (int rank = 1; rank <= n; rank++) {
                byte[] key = cacheRedis.getStringSerializer().serialize(rankKey(rank));
                byte[] field = cacheRedis.getStringSerializer().serialize("videoId");
                connection.hashCommands().hGet(key, field);
            }
            return null;
        });

        List<String> out = new ArrayList<>(n);
        for (Object o : raw) {
            if (o instanceof byte[]) {
                String id =  cacheRedis.getStringSerializer().deserialize((byte[]) o);
                if (id != null && !id.isBlank()) out.add(id);
            } else if (o instanceof String) {
                String s = (String) o;
                if (!s.isBlank()) out.add(s);
            }
        }
        return new ArrayList<>(new LinkedHashSet<>(out));
    }

    private String rankKey(int rank) {
        return "popular:politics:KR:rank:" + rank;
    }
}
