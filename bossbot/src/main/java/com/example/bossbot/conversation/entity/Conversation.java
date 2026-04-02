package com.example.bossbot.conversation.entity;

import com.example.bossbot.message.entity.Message;
import com.example.bossbot.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "conversations")
@NamedQueries({
        @NamedQuery(name = "Conversation.findByUser_IdOrderByUpdatedAtAsc", query = "select c from Conversation c where c.user.id = :id order by c.updatedAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private String title;

    @CreationTimestamp
    private LocalDateTime startedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime endedAt;

    private Boolean active;

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    private List<Message> messages;
}
