package com.example.bossbot.ai.service;

import com.example.bossbot.message.dto.MessageResponse;

import java.util.List;
import java.util.function.Consumer;

public interface OpenAIService {

    /**
     * Streams a chat response based on conversation history and the latest user message.
     *
     * @param conversationHistory previous messages in the conversation
     * @param userMessage         the latest user message
     * @param tokenCallback       called for each token as it arrives
     * @return the full concatenated response
     */
    String streamChat(List<MessageResponse> conversationHistory, String userMessage, Consumer<String> tokenCallback);
}
