package com.example.bossbot.chat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "chat")
public class ChatConfig {

    private int maxMessageLength;
    private int maxHistoryMessages;
    private String systemPrompt;
}
