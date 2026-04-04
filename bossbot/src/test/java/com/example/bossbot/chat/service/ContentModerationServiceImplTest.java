package com.example.bossbot.chat.service;

import com.example.bossbot.bannedword.entity.BannedWord;
import com.example.bossbot.bannedword.repository.BannedWordRepository;
import com.example.bossbot.chat.dto.ModerationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContentModerationService Tests")
class ContentModerationServiceImplTest {

    @Mock
    private BannedWordRepository bannedWordRepository;

    private ContentModerationServiceImpl service;

    @BeforeEach
    void setUp() {
        List<BannedWord> bannedWords = List.of(
                BannedWord.builder().id(1L).word("badword").severity("HIGH").isActive(true).build(),
                BannedWord.builder().id(2L).word("mildword").severity("LOW").isActive(true).build()
        );
        when(bannedWordRepository.findByIsActiveTrue()).thenReturn(bannedWords);
        service = new ContentModerationServiceImpl(bannedWordRepository);
        service.init();
    }

    @Test
    @DisplayName("Should return NONE when content has no banned words")
    void testCheck_NoBannedWords() {
        // Given
        String content = "Hello, how are you?";

        // When
        ModerationResult result = service.check(content);

        // Then
        assertThat(result.getSeverity()).isEqualTo(ModerationResult.Severity.NONE);
        assertThat(result.getMatchedWord()).isNull();
    }

    @Test
    @DisplayName("Should return HIGH severity when content contains HIGH severity word")
    void testCheck_HighSeverity() {
        // Given
        String content = "This is a badword in a sentence";

        // When
        ModerationResult result = service.check(content);

        // Then
        assertThat(result.getSeverity()).isEqualTo(ModerationResult.Severity.HIGH);
        assertThat(result.getMatchedWord()).isEqualTo("badword");
    }

    @Test
    @DisplayName("Should return LOW severity when content contains LOW severity word")
    void testCheck_LowSeverity() {
        // Given
        String content = "This has a mildword here";

        // When
        ModerationResult result = service.check(content);

        // Then
        assertThat(result.getSeverity()).isEqualTo(ModerationResult.Severity.LOW);
        assertThat(result.getMatchedWord()).isEqualTo("mildword");
    }

    @Test
    @DisplayName("Should be case insensitive")
    void testCheck_CaseInsensitive() {
        // Given
        String content = "This has BADWORD in uppercase";

        // When
        ModerationResult result = service.check(content);

        // Then
        assertThat(result.getSeverity()).isEqualTo(ModerationResult.Severity.HIGH);
    }

    @Test
    @DisplayName("Should return highest severity when multiple banned words found")
    void testCheck_MultipleBannedWords() {
        // Given
        String content = "This has mildword and badword";

        // When
        ModerationResult result = service.check(content);

        // Then
        assertThat(result.getSeverity()).isEqualTo(ModerationResult.Severity.HIGH);
    }

    @Test
    @DisplayName("Should not match substrings - word boundaries are enforced")
    void testCheck_WordBoundaries() {
        // Given
        String content = "This is a badwordsuffix or prefixbadword";

        // When
        ModerationResult result = service.check(content);

        // Then
        assertThat(result.getSeverity()).isEqualTo(ModerationResult.Severity.NONE);
        assertThat(result.getMatchedWord()).isNull();
    }

    @Test
    @DisplayName("Should handle empty content")
    void testCheck_EmptyContent() {
        // Given
        String content = "";

        // When
        ModerationResult result = service.check(content);

        // Then
        assertThat(result.getSeverity()).isEqualTo(ModerationResult.Severity.NONE);
    }
}
