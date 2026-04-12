package com.example.bossbot.stampanswer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStampAnswerRequest {

    @NotBlank(message = "Question is required")
    private String question;

    private String keywords;

    private String answer;

    @Builder.Default
    private Boolean isActive = true;

    // TODO: Remove this field when Spring Security is implemented
    // createdBy should be automatically set from authenticated user context
    // For now, service layer will use a default system user ID
    // @NotNull(message = "Created by user ID is required")
    // private Long createdBy;
}
