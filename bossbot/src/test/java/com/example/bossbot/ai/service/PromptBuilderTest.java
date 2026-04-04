package com.example.bossbot.ai.service;

import com.example.bossbot.chat.config.ChatConfig;
import com.example.bossbot.message.dto.MessageResponse;
import com.example.bossbot.message.entity.MessageRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PromptBuilder Tests")
class PromptBuilderTest {

    @Mock
    private ChatConfig chatConfig;

    private PromptBuilder promptBuilder;

    @BeforeEach
    void setUp() {
        promptBuilder = new PromptBuilder(chatConfig);
    }

    @Test
    @DisplayName("Should build messages with system prompt and user message")
    void testBuildMessages_Basic() {
        // Given
        when(chatConfig.getSystemPrompt()).thenReturn("You are a helpful assistant.");
        when(chatConfig.getMaxHistoryMessages()).thenReturn(20);

        // When
        List<PromptBuilder.ChatMessage> messages = promptBuilder.buildMessages(List.of(), "Hello");

        // Then
        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).role()).isEqualTo("system");
        assertThat(messages.get(0).content()).isEqualTo("You are a helpful assistant.");
        assertThat(messages.get(1).role()).isEqualTo("user");
        assertThat(messages.get(1).content()).isEqualTo("Hello");
    }

    @Test
    @DisplayName("Should include conversation history with correct roles")
    void testBuildMessages_WithHistory() {
        // Given
        when(chatConfig.getSystemPrompt()).thenReturn("System prompt");
        when(chatConfig.getMaxHistoryMessages()).thenReturn(20);

        List<MessageResponse> history = List.of(
                MessageResponse.builder()
                        .id(1L).conversationId(1L).role(MessageRole.USER)
                        .content("Hi").createdAt(LocalDateTime.now()).createdBy(1L).isActive(true)
                        .build(),
                MessageResponse.builder()
                        .id(2L).conversationId(1L).role(MessageRole.BOT)
                        .content("Hello!").createdAt(LocalDateTime.now()).createdBy(1L).isActive(true)
                        .build()
        );

        // When
        List<PromptBuilder.ChatMessage> messages = promptBuilder.buildMessages(history, "How are you?");

        // Then
        assertThat(messages).hasSize(4);
        assertThat(messages.get(0).role()).isEqualTo("system");
        assertThat(messages.get(1).role()).isEqualTo("user");
        assertThat(messages.get(1).content()).isEqualTo("Hi");
        assertThat(messages.get(2).role()).isEqualTo("assistant");
        assertThat(messages.get(2).content()).isEqualTo("Hello!");
        assertThat(messages.get(3).role()).isEqualTo("user");
        assertThat(messages.get(3).content()).isEqualTo("How are you?");
    }

    @Test
    @DisplayName("Should limit history to maxHistoryMessages")
    void testBuildMessages_LimitsHistory() {
        // Given
        when(chatConfig.getSystemPrompt()).thenReturn("System prompt");
        when(chatConfig.getMaxHistoryMessages()).thenReturn(2);

        List<MessageResponse> history = List.of(
                MessageResponse.builder()
                        .id(1L).conversationId(1L).role(MessageRole.USER)
                        .content("Old message").createdAt(LocalDateTime.now()).createdBy(1L).isActive(true)
                        .build(),
                MessageResponse.builder()
                        .id(2L).conversationId(1L).role(MessageRole.BOT)
                        .content("Old reply").createdAt(LocalDateTime.now()).createdBy(1L).isActive(true)
                        .build(),
                MessageResponse.builder()
                        .id(3L).conversationId(1L).role(MessageRole.USER)
                        .content("Recent message").createdAt(LocalDateTime.now()).createdBy(1L).isActive(true)
                        .build(),
                MessageResponse.builder()
                        .id(4L).conversationId(1L).role(MessageRole.BOT)
                        .content("Recent reply").createdAt(LocalDateTime.now()).createdBy(1L).isActive(true)
                        .build()
        );

        // When
        List<PromptBuilder.ChatMessage> messages = promptBuilder.buildMessages(history, "New question");

        // Then
        // system + 2 history messages + new user message = 4
        assertThat(messages).hasSize(4);
        assertThat(messages.get(1).content()).isEqualTo("Recent message");
        assertThat(messages.get(2).content()).isEqualTo("Recent reply");
        assertThat(messages.get(3).content()).isEqualTo("New question");
    }

    @Test
    @DisplayName("Should not duplicate user message if it already exists in history")
    void testBuildMessages_NoDuplicateUserMessage() {
        // Given
        when(chatConfig.getSystemPrompt()).thenReturn("System prompt");
        when(chatConfig.getMaxHistoryMessages()).thenReturn(20);

        List<MessageResponse> history = List.of(
                MessageResponse.builder()
                        .id(1L).conversationId(1L).role(MessageRole.USER)
                        .content("Hello").createdAt(LocalDateTime.now()).createdBy(1L).isActive(true)
                        .build()
        );

        // When
        List<PromptBuilder.ChatMessage> messages = promptBuilder.buildMessages(history, "Hello");

        // Then
        assertThat(messages).hasSize(2);
        assertThat(messages.get(1).role()).isEqualTo("user");
        assertThat(messages.get(1).content()).isEqualTo("Hello");
    }
}
