package com.example.bossbot.stampanswer.service;

import com.example.bossbot.ai.service.OllamaEmbeddingService;
import com.example.bossbot.ai.service.OllamaException;
import com.example.bossbot.stampanswer.config.StampMatcherConfig;
import com.example.bossbot.stampanswer.dto.StampAnswerResponse;
import com.example.bossbot.stampanswer.entity.StampAnswer;
import com.example.bossbot.stampanswer.repository.StampAnswerRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ollama.enabled", havingValue = "true")
public class OllamaStampMatcherImpl implements StampMatcherService {

    private final StampAnswerRepository repository;
    private final OllamaEmbeddingService embeddingService;
    private final StampMatcherConfig config;

    private record StampAnswerEmbedding(StampAnswer stampAnswer, double[] embedding) {
    }

    private final AtomicReference<List<StampAnswerEmbedding>> cache = new AtomicReference<>(List.of());
    private final AtomicBoolean refreshing = new AtomicBoolean(false);

    @PostConstruct
    void init() {
        refreshCache();
    }

    @Override
    public void refreshCache() {
        List<StampAnswer> active = repository.findByIsActiveTrueOrderByCreatedAtDesc();

        if (active.isEmpty()) {
            cache.set(List.of());
            log.info("Ollama stamp matcher cache refreshed (0 entries)");
            return;
        }

        try {
            List<String> texts = active.stream()
                    .map(sa -> {
                        String text = sa.getQuestion();
                        if (sa.getKeywords() != null && !sa.getKeywords().isBlank()) {
                            text += " " + sa.getKeywords();
                        }
                        return text;
                    })
                    .toList();

            List<double[]> embeddings = embeddingService.embedBatch(texts);

            List<StampAnswerEmbedding> entries = new ArrayList<>();
            for (int i = 0; i < active.size(); i++) {
                entries.add(new StampAnswerEmbedding(active.get(i), embeddings.get(i)));
            }
            cache.set(List.copyOf(entries));
            log.info("Ollama stamp matcher cache refreshed ({} entries with embeddings)", entries.size());

        } catch (OllamaException e) {
            log.warn("Ollama unavailable during cache refresh, falling back to keyword matching: {}", e.getMessage());
            List<StampAnswerEmbedding> entries = active.stream()
                    .map(sa -> new StampAnswerEmbedding(sa, null))
                    .toList();
            cache.set(entries);
        }
    }

    @Override
    public Optional<StampAnswerResponse> findBestMatch(String userInput) {
        if (userInput == null || userInput.trim().length() < config.getMinInputLength()) {
            return Optional.empty();
        }

        List<StampAnswerEmbedding> entries = cache.get();
        if (entries.isEmpty()) {
            return Optional.empty();
        }

        try {
            // If cache has no embeddings (startup failed), try refreshing once
            boolean hasEmbeddings = entries.stream().anyMatch(e -> e.embedding() != null);
            if (!hasEmbeddings && refreshing.compareAndSet(false, true)) {
                try {
                    log.info("Cache has no embeddings, attempting refresh before matching");
                    refreshCache();
                    entries = cache.get();
                } finally {
                    refreshing.set(false);
                }
                if (entries.isEmpty()) {
                    return Optional.empty();
                }
            }

            double[] inputEmbedding = embeddingService.embed(userInput);

            StampAnswer bestMatch = null;
            double bestSimilarity = 0;

            for (StampAnswerEmbedding entry : entries) {
                if (entry.embedding() == null) continue;
                double similarity = cosineSimilarity(inputEmbedding, entry.embedding());
                if (similarity > bestSimilarity ||
                        (similarity == bestSimilarity && bestMatch != null)) {
                    bestSimilarity = similarity;
                    bestMatch = entry.stampAnswer();
                }
            }

            if (bestSimilarity >= config.getSimilarityThreshold() && bestMatch != null) {
                log.info("Semantic match found (similarity={}) for input: {}", bestSimilarity, userInput);
                return Optional.of(StampAnswerResponse.fromEntity(bestMatch));
            }

            log.debug("No semantic match above threshold {} for input: {}", config.getSimilarityThreshold(), userInput);
            return Optional.empty();

        } catch (OllamaException e) {
            log.warn("Ollama unavailable at runtime, falling back to keyword matching: {}", e.getMessage());
            List<StampAnswer> stampAnswers = entries.stream().map(StampAnswerEmbedding::stampAnswer).toList();
            return KeywordMatchUtils.findBestKeywordMatch(userInput, stampAnswers, config.getMinInputLength(), config.getKeywordThreshold())
                    .map(StampAnswerResponse::fromEntity);
        }
    }

    private static double cosineSimilarity(double[] a, double[] b) {
        if (a.length != b.length || a.length == 0) return 0.0;
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        double denom = Math.sqrt(normA) * Math.sqrt(normB);
        return denom == 0 ? 0.0 : dot / denom;
    }
}
