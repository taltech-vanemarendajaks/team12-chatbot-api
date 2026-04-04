package com.example.bossbot.ai.service;

import com.example.bossbot.ai.config.OpenAIConfig;
import com.example.bossbot.message.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "openai.api-key", havingValue = "mock", matchIfMissing = true)
public class MockOpenAIServiceImpl implements OpenAIService {

    private final OpenAIConfig config;

    @Override
    public String streamChat(List<MessageResponse> conversationHistory, String userMessage, Consumer<String> tokenCallback) {
        log.info("Using MOCK OpenAI service (api-key=mock). Model configured: {}", config.getModel());

        String mockResponse = "I'm BossBot! This is a mock response. " +
                "To enable real AI responses, set the openai.api-key property to a valid OpenAI API key.";

        // Simulate streaming by sending word by word with delays
        String[] words = mockResponse.split("(?<=\\s)");

        for (String word : words) {
            tokenCallback.accept(word);

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return mockResponse;
    }
}
