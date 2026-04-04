package com.example.bossbot.chat.service;

import com.example.bossbot.ai.service.OpenAIService;
import com.example.bossbot.chat.dto.ChatWebSocketResponse;
import com.example.bossbot.chat.dto.ModerationResult;
import com.example.bossbot.message.dto.CreateMessageRequest;
import com.example.bossbot.message.dto.MessageResponse;
import com.example.bossbot.message.entity.MessageRole;
import com.example.bossbot.message.service.MessageService;
import com.example.bossbot.stampanswer.dto.StampAnswerResponse;
import com.example.bossbot.stampanswer.service.StampAnswerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final MessageService messageService;
    private final ContentModerationService contentModerationService;
    private final StampAnswerService stampAnswerService;
    private final OpenAIService openAIService;

    @Override
    public void processMessage(Long conversationId, String content, Consumer<ChatWebSocketResponse> send) {
        try {
            // 1. Save user message
            MessageResponse userMessage = messageService.create(
                    CreateMessageRequest.builder()
                            .conversationId(conversationId)
                            .role(MessageRole.USER)
                            .content(content)
                            .build()
            );
            send.accept(ChatWebSocketResponse.message(userMessage));

            // 2. Content moderation
            ModerationResult moderation = contentModerationService.check(content);

            if (moderation.getSeverity() == ModerationResult.Severity.HIGH) {
                log.warn("Message blocked for conversation {} (HIGH severity)", conversationId);
                send.accept(ChatWebSocketResponse.blocked("Your message was blocked due to inappropriate content."));
                return;
            }

            if (moderation.getSeverity() == ModerationResult.Severity.LOW) {
                log.info("Message warned for conversation {} (LOW severity)", conversationId);
                send.accept(ChatWebSocketResponse.warning("Your message contains inappropriate language, but it has been processed."));
            }

            // 3. Stamp answer lookup — exact match first
            StampAnswerResponse stampAnswer = stampAnswerService.getByQuestion(content);

            if (stampAnswer != null) {
                log.info("Stamp answer found (exact match) for conversation: {}", conversationId);
                MessageResponse botMessage = saveBotMessage(conversationId, stampAnswer.getAnswer());
                send.accept(ChatWebSocketResponse.message(botMessage));
                return;
            }

            // 4. Stamp answer lookup — fuzzy match
            List<StampAnswerResponse> searchResults = stampAnswerService.search(content);

            if (!searchResults.isEmpty()) {
                StampAnswerResponse bestMatch = searchResults.getFirst();
                log.info("Stamp answer found (fuzzy match) for conversation: {}", conversationId);
                stampAnswerService.recordUsage(bestMatch.getId());
                MessageResponse botMessage = saveBotMessage(conversationId, bestMatch.getAnswer());
                send.accept(ChatWebSocketResponse.message(botMessage));
                return;
            }

            // 5. AI fallback with streaming
            log.info("No stamp answer found, falling back to AI for conversation: {}", conversationId);
            send.accept(ChatWebSocketResponse.streamStart(conversationId));

            List<MessageResponse> history = messageService.getAll(conversationId);

            String fullResponse = openAIService.streamChat(history, content, token ->
                    send.accept(ChatWebSocketResponse.streamToken(token))
            );

            MessageResponse botMessage = saveBotMessage(conversationId, fullResponse);
            send.accept(ChatWebSocketResponse.streamEnd(botMessage));

        } catch (Exception e) {
            log.error("Error processing message for conversation {}: {}", conversationId, e.getMessage(), e);
            send.accept(ChatWebSocketResponse.error("An error occurred while processing your message."));
        }
    }

    private MessageResponse saveBotMessage(Long conversationId, String content) {
        return messageService.create(
                CreateMessageRequest.builder()
                        .conversationId(conversationId)
                        .role(MessageRole.BOT)
                        .content(content)
                        .build()
        );
    }
}
