package com.example.bossbot.stampanswer.service;

import com.example.bossbot.stampanswer.config.StampMatcherConfig;
import com.example.bossbot.stampanswer.dto.StampAnswerResponse;
import com.example.bossbot.stampanswer.entity.StampAnswer;
import com.example.bossbot.stampanswer.repository.StampAnswerRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ollama.enabled", havingValue = "false", matchIfMissing = true)
public class KeywordStampMatcherImpl implements StampMatcherService {

    private final StampAnswerRepository repository;
    private final StampMatcherConfig config;
    private final AtomicReference<List<StampAnswer>> cache = new AtomicReference<>(List.of());

    @PostConstruct
    void init() {
        refreshCache();
    }

    @Override
    public void refreshCache() {
        cache.set(List.copyOf(repository.findByIsActiveTrueOrderByCreatedAtDesc()));
        log.info("Keyword stamp matcher cache refreshed ({} entries)", cache.get().size());
    }

    @Override
    public Optional<StampAnswerResponse> findBestMatch(String userInput) {
        return KeywordMatchUtils.findBestKeywordMatch(userInput, cache.get(), config.getMinInputLength(), config.getKeywordThreshold())
                .map(StampAnswerResponse::fromEntity);
    }
}
