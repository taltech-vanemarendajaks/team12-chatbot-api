package com.example.bossbot.chat.handler;

import com.example.bossbot.chat.config.ChatConfig;
import com.example.bossbot.chat.dto.ChatWebSocketMessage;
import com.example.bossbot.chat.dto.ChatWebSocketResponse;
import com.example.bossbot.chat.service.ChatService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final ChatConfig chatConfig;
    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(ChatService chatService, ChatConfig chatConfig) {
        this.chatService = chatService;
        this.chatConfig = chatConfig;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        var safeSession = new ConcurrentWebSocketSessionDecorator(session, 10_000, 65_536);
        sessions.put(session.getId(), safeSession);
        log.info("WebSocket connected: {}", session.getId());
        sendResponse(safeSession, ChatWebSocketResponse.config(chatConfig.getMaxMessageLength()));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        var safeSession = sessions.getOrDefault(session.getId(), session);

        try {
            ChatWebSocketMessage message = objectMapper.readValue(
                    textMessage.getPayload(), ChatWebSocketMessage.class);

            if (!"SEND_MESSAGE".equals(message.getType())) {
                sendResponse(safeSession, ChatWebSocketResponse.error("Unknown message type: " + message.getType()));
                return;
            }

            if (message.getConversationId() == null || message.getContent() == null || message.getContent().isBlank()) {
                sendResponse(safeSession, ChatWebSocketResponse.error("conversationId and content are required"));
                return;
            }

            if (message.getContent().length() > chatConfig.getMaxMessageLength()) {
                sendResponse(safeSession, ChatWebSocketResponse.error("Message exceeds maximum length of " + chatConfig.getMaxMessageLength()));
                return;
            }

            chatService.processMessage(message.getConversationId(), message.getContent(),
                    response -> sendResponse(safeSession, response));

        } catch (Exception e) {
            log.error("Error handling WebSocket message: {}", e.getMessage(), e);
            sendResponse(safeSession, ChatWebSocketResponse.error("Failed to process message"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("WebSocket disconnected: {} (status: {})", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
    }

    private void sendResponse(WebSocketSession session, ChatWebSocketResponse response) {
        try {
            if (!session.isOpen()) {
                log.warn("Attempted to send to closed session: {}", session.getId());
                return;
            }

            String json = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(json));
        } catch (Exception e) {
            log.error("Failed to send WebSocket response: {}", e.getMessage(), e);
        }
    }
}
