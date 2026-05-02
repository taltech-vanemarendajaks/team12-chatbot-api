package com.example.bossbot.conversation.repository;

import com.example.bossbot.conversation.entity.Conversation;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface ConversationRepository extends JpaRepository<@NonNull Conversation, @NonNull Long> {


    List<Conversation> findByUserIdOrderByUpdatedAtAsc(Long id);

    List<Conversation> findByUserIdAndActiveTrueOrderByUpdatedAtAsc(Long id);

    Optional<Conversation> findByIdAndUserId(Long conversationId, Long userId);

}
