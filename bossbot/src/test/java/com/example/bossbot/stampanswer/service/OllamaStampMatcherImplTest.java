package com.example.bossbot.stampanswer.service;

import com.example.bossbot.ai.service.OllamaEmbeddingService;
import com.example.bossbot.ai.service.OllamaException;
import com.example.bossbot.stampanswer.config.StampMatcherConfig;
import com.example.bossbot.stampanswer.dto.StampAnswerResponse;
import com.example.bossbot.stampanswer.entity.StampAnswer;
import com.example.bossbot.stampanswer.repository.StampAnswerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OllamaStampMatcher Tests")
class OllamaStampMatcherImplTest {

    @Mock
    private StampAnswerRepository repository;

    @Mock
    private OllamaEmbeddingService embeddingService;

    private StampMatcherConfig config;
    private OllamaStampMatcherImpl matcher;

    private StampAnswer testStampAnswer;

    @BeforeEach
    void setUp() {
        config = new StampMatcherConfig();
        config.setSimilarityThreshold(0.7);
        config.setKeywordThreshold(0.3);
        config.setMinInputLength(3);
        matcher = new OllamaStampMatcherImpl(repository, embeddingService, config);

        testStampAnswer = StampAnswer.builder()
                .id(1L)
                .question("What is Spring Boot?")
                .keywords("spring, boot, framework")
                .answer("Spring Boot is a framework...")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should find match when similarity is above threshold")
    void testFindBestMatch_highSimilarity() {
        double[] embedding = {0.1, 0.2, 0.3};
        when(repository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(testStampAnswer));
        when(embeddingService.embedBatch(any())).thenReturn(List.of(embedding));
        when(embeddingService.embed("What is Spring Boot")).thenReturn(embedding);
        matcher.refreshCache();

        Optional<StampAnswerResponse> result = matcher.findBestMatch("What is Spring Boot");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return empty when similarity is below threshold")
    void testFindBestMatch_belowThreshold() {
        double[] docEmbedding = {1.0, 0.0};
        double[] queryEmbedding = {0.0, 1.0};
        when(repository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(testStampAnswer));
        when(embeddingService.embedBatch(any())).thenReturn(List.of(docEmbedding));
        when(embeddingService.embed("random gibberish text")).thenReturn(queryEmbedding);
        matcher.refreshCache();

        Optional<StampAnswerResponse> result = matcher.findBestMatch("random gibberish text");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should fall back to keyword matching when Ollama is down at query time")
    void testFindBestMatch_ollamaDown_fallsBackToKeywords() {
        double[] embedding = {0.1, 0.2, 0.3};
        when(repository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(testStampAnswer));
        when(embeddingService.embedBatch(any())).thenReturn(List.of(embedding));
        matcher.refreshCache();

        when(embeddingService.embed("What is Spring Boot")).thenThrow(new OllamaException("Connection refused"));

        Optional<StampAnswerResponse> result = matcher.findBestMatch("What is Spring Boot");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return empty for short input without calling Ollama")
    void testFindBestMatch_shortInput() {
        Optional<StampAnswerResponse> result = matcher.findBestMatch("ab");

        assertThat(result).isEmpty();
        verify(embeddingService, never()).embed(any());
    }

    @Test
    @DisplayName("Should recompute embeddings on cache refresh")
    void testRefreshCache_recomputesEmbeddings() {
        double[] embedding = {0.1, 0.2, 0.3};
        when(repository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(testStampAnswer));
        when(embeddingService.embedBatch(any())).thenReturn(List.of(embedding));

        matcher.refreshCache();

        verify(embeddingService).embedBatch(List.of("What is Spring Boot? spring, boot, framework"));
    }

    @Test
    @DisplayName("Should handle Ollama being down during cache refresh")
    void testRefreshCache_ollamaDown() {
        when(repository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(testStampAnswer));
        when(embeddingService.embedBatch(any())).thenThrow(new OllamaException("Connection refused"));

        matcher.refreshCache();

        when(embeddingService.embed("What is Spring Boot")).thenThrow(new OllamaException("Connection refused"));
        Optional<StampAnswerResponse> result = matcher.findBestMatch("What is Spring Boot");
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Should return first stamp answer when similarities are equal")
    void testFindBestMatch_returnsFirstWhenSimilaritiesAreEqual() {
        StampAnswer first = StampAnswer.builder()
                .id(2L).question("What is Spring Boot?").keywords("spring").isActive(true).build();
        StampAnswer second = StampAnswer.builder()
                .id(3L).question("What is Spring Boot?").keywords("spring").isActive(true).build();

        double[] embedding = {0.5, 0.5, 0.5};
        when(repository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(first,second));
        when(embeddingService.embedBatch(any())).thenReturn(List.of(embedding, embedding));
        when(embeddingService.embed("What is Spring Boot")).thenReturn(embedding);
        matcher.refreshCache();

        Optional<StampAnswerResponse> result = matcher.findBestMatch("What is Spring Boot");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(2L);
    }
}
