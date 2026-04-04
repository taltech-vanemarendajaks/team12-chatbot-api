package com.example.bossbot.conversation.dto;

import com.example.bossbot.conversation.entity.Conversation;
import com.example.bossbot.message.dto.MessageResponse;
import com.example.bossbot.message.entity.Message;
import com.example.bossbot.message.entity.MessageRole;
import com.example.bossbot.user.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

    private Long id;
    private User user;
    private String title;
    private LocalDateTime startedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime endedAt;
    private Boolean active;
    private List<Message> messages;

    /**
     * Maps entity to response DTO
     */
    public static ConversationResponse fromEntity(Conversation entity) {
        return ConversationResponse.builder()
                .id(entity.getId())
                .user(entity.getUser())
                .title(entity.getTitle())
                .startedAt(entity.getStartedAt())
                .updatedAt(entity.getUpdatedAt())
                .endedAt(entity.getEndedAt())
                .active(entity.getActive())
                .messages(entity.getMessages())
                .build();
    }

}
