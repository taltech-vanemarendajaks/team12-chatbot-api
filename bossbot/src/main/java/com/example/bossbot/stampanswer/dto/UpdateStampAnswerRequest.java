package com.example.bossbot.stampanswer.dto;

// import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStampAnswerRequest {

    private String question;

    private String keywords;

    private String answer;

    private Boolean isActive;

    // TODO: Remove this field when Spring Security is implemented
    // updatedBy should be automatically set from authenticated user context
    // For now, service layer will use a default system user ID
    // @NotNull(message = "Updated by user ID is required")
    // private Long updatedBy;
}
