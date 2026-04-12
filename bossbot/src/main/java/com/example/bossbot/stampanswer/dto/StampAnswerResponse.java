package com.example.bossbot.stampanswer.dto;

import com.example.bossbot.stampanswer.entity.StampAnswer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StampAnswerResponse {

    private Long id;
    private String question;
    private String keywords;
    private String answer;
    private Boolean isActive;
    private Integer usageCount;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;

    /**
     * Maps entity to response DTO
     */
    public static StampAnswerResponse fromEntity(StampAnswer entity) {
        return StampAnswerResponse.builder()
                .id(entity.getId())
                .question(entity.getQuestion())
                .keywords(entity.getKeywords())
                .answer(entity.getAnswer())
                .isActive(entity.getIsActive())
                .usageCount(entity.getUsageCount())
                .lastUsedAt(entity.getLastUsedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
