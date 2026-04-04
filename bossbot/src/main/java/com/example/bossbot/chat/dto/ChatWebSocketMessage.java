package com.example.bossbot.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatWebSocketMessage {

    private String type;
    private Long conversationId;
    private String content;
}
