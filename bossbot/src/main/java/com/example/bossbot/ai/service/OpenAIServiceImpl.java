package com.example.bossbot.ai.service;

import com.example.bossbot.ai.config.OpenAIConfig;
import com.example.bossbot.message.dto.MessageResponse;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.StreamResponse;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletionChunk;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
@Slf4j
@ConditionalOnExpression("'${openai.api-key:mock}' != 'mock'")
public class OpenAIServiceImpl implements OpenAIService {

    private final OpenAIClient client;
    private final OpenAIConfig config;
    private final PromptBuilder promptBuilder;

    public OpenAIServiceImpl(OpenAIConfig config, PromptBuilder promptBuilder) {
        this.config = config;
        this.promptBuilder = promptBuilder;
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(config.getApiKey())
                .build();
        log.info("OpenAI service initialized with model: {}", config.getModel());
    }

    @Override
    public String streamChat(List<MessageResponse> conversationHistory, String userMessage, Consumer<String> tokenCallback) {
        log.info("Calling OpenAI API with model: {}", config.getModel());

        List<PromptBuilder.ChatMessage> messages = promptBuilder.buildMessages(conversationHistory, userMessage);

        // Build the params with messages
        ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                .model(ChatModel.of(config.getModel()))
                .maxCompletionTokens(config.getMaxTokens())
                .temperature(config.getTemperature());

        for (PromptBuilder.ChatMessage msg : messages) {
            switch (msg.role()) {
                case "system" -> paramsBuilder.addSystemMessage(msg.content());
                case "user" -> paramsBuilder.addUserMessage(msg.content());
                case "assistant" -> paramsBuilder.addAssistantMessage(msg.content());
            }
        }

        StringBuilder fullResponse = new StringBuilder();

        try (StreamResponse<ChatCompletionChunk> streamResponse = client.chat().completions().createStreaming(paramsBuilder.build())) {
            streamResponse.stream().forEach(chunk -> {
                List<ChatCompletionChunk.Choice> choices = chunk.choices();

                if (!choices.isEmpty()) {
                    String content = choices.getFirst().delta().content().orElse("");

                    if (!content.isEmpty()) {
                        tokenCallback.accept(content);
                        fullResponse.append(content);
                    }
                }
            });
        }

        log.info("OpenAI response completed. Total length: {}", fullResponse.length());

        return fullResponse.toString();
    }
}
