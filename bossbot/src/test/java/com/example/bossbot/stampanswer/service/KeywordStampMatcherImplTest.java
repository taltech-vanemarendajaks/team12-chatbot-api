package com.example.bossbot.stampanswer.service;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("KeywordStampMatcher Tests")
class KeywordStampMatcherImplTest {

    @Mock
    private StampAnswerRepository repository;

    private StampMatcherConfig config;
    private KeywordStampMatcherImpl matcher;

    private StampAnswer testStampAnswer;

    @BeforeEach
    void setUp() {
        config = new StampMatcherConfig();
        config.setMinInputLength(3);
        config.setKeywordThreshold(0.3);
        matcher = new KeywordStampMatcherImpl(repository, config);

        testStampAnswer = StampAnswer.builder()
                .id(1L)
                .question("What is Spring Boot?")
                .keywords("spring, boot, framework")
                .answer("Spring Boot is a framework...")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should load cache on refresh")
    void testRefreshCache() {
        when(repository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(testStampAnswer));

        matcher.refreshCache();

        verify(repository).findByIsActiveTrueOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Should find match with good keyword overlap")
    void testFindBestMatch_goodOverlap() {
        when(repository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(testStampAnswer));
        matcher.refreshCache();

        Optional<StampAnswerResponse> result = matcher.findBestMatch("What is Spring Boot");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return empty for short input")
    void testFindBestMatch_shortInput() {
        when(repository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(testStampAnswer));
        matcher.refreshCache();

        Optional<StampAnswerResponse> result = matcher.findBestMatch("ab");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when cache is empty")
    void testFindBestMatch_emptyCache() {
        when(repository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of());
        matcher.refreshCache();

        Optional<StampAnswerResponse> result = matcher.findBestMatch("What is Spring Boot");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty for irrelevant input")
    void testFindBestMatch_noOverlap() {
        when(repository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(testStampAnswer));
        matcher.refreshCache();

        Optional<StampAnswerResponse> result = matcher.findBestMatch("hello world today");

        assertThat(result).isEmpty();
    }
}
