package com.example.bossbot.stampanswer.service;

import com.example.bossbot.stampanswer.dto.CreateStampAnswerRequest;
import com.example.bossbot.stampanswer.dto.StampAnswerResponse;
import com.example.bossbot.stampanswer.dto.UpdateStampAnswerRequest;
import com.example.bossbot.stampanswer.entity.StampAnswer;
import com.example.bossbot.stampanswer.repository.StampAnswerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
@DisplayName("StampAnswerService Tests")
class StampAnswerServiceImplTest {

    @Mock
    private StampAnswerRepository repository;

    @InjectMocks
    private StampAnswerServiceImpl service;

    private StampAnswer testEntity;
    private CreateStampAnswerRequest createRequest;
    private UpdateStampAnswerRequest updateRequest;

    @BeforeEach
    void setUp() {
        testEntity = StampAnswer.builder()
                .id(1L)
                .question("What is Spring Boot?")
                .keywords("spring, boot, framework")
                .answer("Spring Boot is a Java framework")
                .isActive(true)
                .usageCount(10)
                .createdAt(LocalDateTime.now())
                .createdBy(1L)
                .updatedBy(1L)
                .build();

        createRequest = CreateStampAnswerRequest.builder()
                .question("What is Spring Boot?")
                .keywords("spring, boot, framework")
                .answer("Spring Boot is a Java framework")
                .isActive(true)
                .build();

        updateRequest = UpdateStampAnswerRequest.builder()
                .question("Updated question")
                .build();
    }

    @Test
    @DisplayName("Should create stamp answer successfully")
    void testCreate_Success() {
        // Given
        when(repository.existsByQuestionIgnoreCase(anyString())).thenReturn(false);
        when(repository.save(any(StampAnswer.class))).thenReturn(testEntity);

        // When
        StampAnswerResponse response = service.create(createRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getQuestion()).isEqualTo(testEntity.getQuestion());
        verify(repository).existsByQuestionIgnoreCase(createRequest.getQuestion());
        verify(repository).save(any(StampAnswer.class));
    }

    @Test
    @DisplayName("Should throw exception when question already exists")
    void testCreate_QuestionExists() {
        // Given
        when(repository.existsByQuestionIgnoreCase(anyString())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> service.create(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
        
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should get stamp answer by ID")
    void testGetById_Success() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(testEntity));

        // When
        StampAnswerResponse response = service.getById(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getQuestion()).isEqualTo(testEntity.getQuestion());
        verify(repository).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when stamp answer not found")
    void testGetById_NotFound() {
        // Given
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.getById(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should get all active stamp answers")
    void testGetAllActive() {
        // Given
        List<StampAnswer> entities = Arrays.asList(testEntity);
        when(repository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(entities);

        // When
        List<StampAnswerResponse> responses = service.getAllActive();

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        verify(repository).findByIsActiveTrueOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("Should search by question or keywords")
    void testSearch() {
        // Given
        List<StampAnswer> entities = Arrays.asList(testEntity);
        when(repository.searchByQuestionOrKeywords("spring")).thenReturn(entities);

        // When
        List<StampAnswerResponse> responses = service.search("spring");

        // Then
        assertThat(responses).hasSize(1);
        verify(repository).searchByQuestionOrKeywords("spring");
    }

    @Test
    @DisplayName("Should update stamp answer successfully")
    void testUpdate_Success() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(repository.save(any(StampAnswer.class))).thenReturn(testEntity);

        // When
        StampAnswerResponse response = service.update(1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        verify(repository).findById(1L);
        verify(repository).save(any(StampAnswer.class));
    }

    @Test
    @DisplayName("Should soft delete stamp answer")
    void testDelete_Success() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(repository.save(any(StampAnswer.class))).thenReturn(testEntity);

        // When
        service.delete(1L);

        // Then
        verify(repository).findById(1L);
        verify(repository).save(any(StampAnswer.class));
    }

    @Test
    @DisplayName("Should record usage successfully")
    void testRecordUsage_Success() {
        // Given
        when(repository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(repository.save(any(StampAnswer.class))).thenReturn(testEntity);

        // When
        service.recordUsage(1L);

        // Then
        verify(repository).findById(1L);
        verify(repository).save(any(StampAnswer.class));
    }

    @Test
    @DisplayName("Should get most used stamp answers")
    void testGetMostUsed() {
        // Given
        List<StampAnswer> entities = Arrays.asList(testEntity);
        when(repository.findTop10ByIsActiveTrueOrderByUsageCountDesc()).thenReturn(entities);

        // When
        List<StampAnswerResponse> responses = service.getMostUsed();

        // Then
        assertThat(responses).hasSize(1);
        verify(repository).findTop10ByIsActiveTrueOrderByUsageCountDesc();
    }
}
