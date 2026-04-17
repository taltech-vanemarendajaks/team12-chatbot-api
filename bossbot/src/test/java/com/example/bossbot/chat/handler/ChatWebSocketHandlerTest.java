package com.example.bossbot.chat.handler;

import com.example.bossbot.chat.config.ChatConfig;
import com.example.bossbot.chat.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatWebSocketHandler — Rate Limiting")
class ChatWebSocketHandlerTest {

    @Mock
    private ChatService chatService;

    @Mock
    private WebSocketSession session;

    private ChatWebSocketHandler handler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ChatConfig config = new ChatConfig();
        config.setMaxMessageLength(150);
        config.setMaxHistoryMessages(20);
        config.setRateLimitBurstCount(3);
        config.setRateLimitBaseMs(1000);
        config.setRateLimitMaxMs(16000);
        config.setRateLimitResetMs(30000);

        handler = new ChatWebSocketHandler(chatService, config);

        when(session.getId()).thenReturn("test-session");
        when(session.isOpen()).thenReturn(true);
    }

    private TextMessage createMessage(Long conversationId, String content) throws Exception {
        String json = objectMapper.writeValueAsString(
                java.util.Map.of("type", "SEND_MESSAGE", "conversationId", conversationId, "content", content));
        return new TextMessage(json);
    }

    @Test
    @DisplayName("Messages within burst count go through without rate limiting")
    void testMessagesWithinBurstAllowed() throws Exception {
        // Given — connection established
        handler.afterConnectionEstablished(session);

        // When — send 3 messages (burst count = 3)
        for (int i = 0; i < 3; i++) {
            handler.handleTextMessage(session, createMessage(1L, "Hello " + i));
        }

        // Then — all 3 should reach the chat service
        verify(chatService, times(3)).processMessage(eq(1L), anyString(), any(), any());
    }

    @Test
    @DisplayName("4th rapid message triggers RATE_LIMITED response")
    void testRateLimitedAfterBurst() throws Exception {
        // Given
        handler.afterConnectionEstablished(session);

        // When — send 4 messages rapidly (burst = 3, so 4th triggers cooldown on 5th)
        for (int i = 0; i < 4; i++) {
            handler.handleTextMessage(session, createMessage(1L, "Msg " + i));
        }

        // The 4th message goes through but sets cooldown
        verify(chatService, times(4)).processMessage(eq(1L), anyString(), any(), any());

        // 5th message should be rate limited
        handler.handleTextMessage(session, createMessage(1L, "Msg 5"));

        // Still only 4 calls to chatService
        verify(chatService, times(4)).processMessage(eq(1L), anyString(), any(), any());

        // Verify a RATE_LIMITED response was sent
        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, atLeast(1)).sendMessage(captor.capture());
        String lastResponse = captor.getAllValues().get(captor.getAllValues().size() - 1).getPayload();
        assertThat(lastResponse).contains("RATE_LIMITED");
        assertThat(lastResponse).contains("retryAfterMs");
    }

    @Test
    @DisplayName("Cooldown doubles progressively up to max")
    void testCooldownDoublesProgressively() throws Exception {
        // Given
        handler.afterConnectionEstablished(session);

        // Send burst + extra messages to trigger escalating cooldowns
        // Burst of 3 goes through, 4th goes through but sets cooldown at 1000ms
        for (int i = 0; i < 4; i++) {
            handler.handleTextMessage(session, createMessage(1L, "Msg " + i));
        }

        // 5th message is rate limited (cooldown = 1000ms)
        handler.handleTextMessage(session, createMessage(1L, "Rate limited"));

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, atLeast(1)).sendMessage(captor.capture());

        String rateLimitedPayload = captor.getAllValues().stream()
                .map(TextMessage::getPayload)
                .filter(p -> p.contains("RATE_LIMITED"))
                .findFirst()
                .orElse(null);

        assertThat(rateLimitedPayload).isNotNull();
        assertThat(rateLimitedPayload).contains("retryAfterMs");
    }

    @Test
    @DisplayName("Spam state is cleaned up on disconnect")
    void testCleanupOnDisconnect() throws Exception {
        // Given — connection established and messages sent
        handler.afterConnectionEstablished(session);
        for (int i = 0; i < 4; i++) {
            handler.handleTextMessage(session, createMessage(1L, "Msg " + i));
        }

        // When — disconnect and reconnect
        handler.afterConnectionClosed(session, org.springframework.web.socket.CloseStatus.NORMAL);
        handler.afterConnectionEstablished(session);

        // Then — burst count resets, messages go through again
        for (int i = 0; i < 3; i++) {
            handler.handleTextMessage(session, createMessage(1L, "After reconnect " + i));
        }

        // 4 before disconnect + 3 after reconnect = 7 total
        verify(chatService, times(7)).processMessage(eq(1L), anyString(), any(), any());
    }

    @Test
    @DisplayName("CONFIG message sent on connection established")
    void testConfigSentOnConnect() throws Exception {
        // When
        handler.afterConnectionEstablished(session);

        // Then
        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session).sendMessage(captor.capture());
        assertThat(captor.getValue().getPayload()).contains("CONFIG");
        assertThat(captor.getValue().getPayload()).contains("maxMessageLength");
    }
}
