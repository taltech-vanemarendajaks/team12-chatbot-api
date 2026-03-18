package com.example.bossbot.user;

import com.example.bossbot.conversation.Conversation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Table(name = "users")
@RequiredArgsConstructor
public class User {
    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long discordId;
    private String username;
    private enum Role {
        ADMIN,
        USER
    };
    private Instant createdAt;
    private Instant lastActiveAt;
    private Boolean isBlocked;
    private String blockedReason;

    public User(String username) {
        this.username = username;
    }


}
