package com.example.bossbot.ai.config;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAIConfig {

    @ToString.Exclude
    private String apiKey = "mock";
    private String model = "gpt-4o-mini";
    private Integer maxTokens = 1024;
    private Double temperature = 0.7;
}
