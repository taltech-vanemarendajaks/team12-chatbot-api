package com.example.bossbot.chat.dto;

import com.example.bossbot.message.dto.MessageResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatWebSocketResponse {

    private String type;
    private MessageResponse message;
    private String text;
    private String token;
    private Long conversationId;
    private Integer maxMessageLength;

    public static ChatWebSocketResponse config(int maxMessageLength) {
        return ChatWebSocketResponse.builder()
                .type("CONFIG")
                .maxMessageLength(maxMessageLength)
                .build();
    }

    public static ChatWebSocketResponse message(MessageResponse msg) {
        return ChatWebSocketResponse.builder()
                .type("MESSAGE")
                .message(msg)
                .build();
    }

    public static ChatWebSocketResponse warning(String text) {
        return ChatWebSocketResponse.builder()
                .type("WARNING")
                .text(text)
                .build();
    }

    public static ChatWebSocketResponse blocked(String text) {
        return ChatWebSocketResponse.builder()
                .type("BLOCKED")
                .text(text)
                .build();
    }

    public static ChatWebSocketResponse streamStart(Long conversationId) {
        return ChatWebSocketResponse.builder()
                .type("STREAM_START")
                .conversationId(conversationId)
                .build();
    }

    public static ChatWebSocketResponse streamToken(String token) {
        return ChatWebSocketResponse.builder()
                .type("STREAM_TOKEN")
                .token(token)
                .build();
    }

    public static ChatWebSocketResponse streamEnd(MessageResponse msg) {
        return ChatWebSocketResponse.builder()
                .type("STREAM_END")
                .message(msg)
                .build();
    }

    public static ChatWebSocketResponse error(String text) {
        return ChatWebSocketResponse.builder()
                .type("ERROR")
                .text(text)
                .build();
    }
}
