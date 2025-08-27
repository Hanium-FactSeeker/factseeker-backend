package com.factseekerbackend.domain.apify.service;

import com.factseekerbackend.domain.apify.dto.request.ApifyRequest;
import com.factseekerbackend.domain.apify.entity.PoliticianSnsPost;
import com.factseekerbackend.domain.apify.repository.PoliticianSnsPostRepository;
import com.factseekerbackend.domain.politician.entity.Politician;
import com.factseekerbackend.domain.politician.repository.PoliticianRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApifyService {

    private final PoliticianRepository politicianRepository;
    private final PoliticianSnsPostRepository snsPostRepository;

    @Transactional
    public void saveAll(List<ApifyRequest> requests) {
        for (ApifyRequest request : requests) {
            Optional<Politician> optionalPolitician = politicianRepository.findByXUrlContaining(request.username());
            if (optionalPolitician.isEmpty()) continue;

            Politician politician = optionalPolitician.get();

            PoliticianSnsPost post = PoliticianSnsPost.builder()
                    .politician(politician)
                    .postText(request.full_text())
                    .postDate(LocalDateTime.parse(request.date()))
                    .trustScore(0.0) // 초기값
                    .build();

            snsPostRepository.save(post);
        }
    }
}