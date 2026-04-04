package com.example.bossbot.chat.service;

import com.example.bossbot.chat.dto.ChatWebSocketResponse;

import java.util.function.Consumer;

public interface ChatService {

    void processMessage(Long conversationId, String content, Consumer<ChatWebSocketResponse> responseSender);
}
