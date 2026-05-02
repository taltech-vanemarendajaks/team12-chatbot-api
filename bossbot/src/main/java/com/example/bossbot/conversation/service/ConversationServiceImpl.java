package com.example.bossbot.conversation.service;

import com.example.bossbot.common.ResourceNotFoundException;
import com.example.bossbot.conversation.dto.ConversationResponse;
import com.example.bossbot.conversation.dto.CreateConversationRequest;
import com.example.bossbot.conversation.dto.UpdateConversationRequest;
import com.example.bossbot.conversation.entity.Conversation;
import com.example.bossbot.conversation.repository.ConversationRepository;
import com.example.bossbot.user.User;
import com.example.bossbot.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationServiceImpl implements ConversationService {

    private static final String CONVERSATION_NOT_FOUND = "Conversation not found with ID: ";

    private final ConversationRepository repository;

    @Override
    @Transactional
    public ConversationResponse create(CreateConversationRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        log.info("Creating new conversation: {}", request.getTitle());

        Conversation entity = Conversation.builder()
                .title(request.getTitle())
                .active(true)
                .user(currentUser)
                .build();

        Conversation saved = repository.save(entity);
        log.info("Created conversation with ID: {}", saved.getId());

        return ConversationResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getById(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUser().getId();
        log.info("Fetching conversation with ID: {}", id);

        Conversation entity = repository.findByIdAndUserId(id, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONVERSATION_NOT_FOUND + id));

        return ConversationResponse.fromEntity(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getAll() {
        Long currentUserId = SecurityUtils.getCurrentUser().getId();
        log.info("Fetching all conversations for user with ID: {}", currentUserId);

        return repository.findByUserIdOrderByUpdatedAtAsc(currentUserId).stream()
                .map(ConversationResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getAllActive() {
        Long currentUserId = SecurityUtils.getCurrentUser().getId();
        log.info("Fetching all active conversations");

        return repository.findByUserIdAndActiveTrueOrderByUpdatedAtAsc(currentUserId).stream()
                .map(ConversationResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public ConversationResponse update(Long id, UpdateConversationRequest request) {
        Long currentUserId = SecurityUtils.getCurrentUser().getId();
        log.info("Updating conversation with ID: {}", id);

        Conversation entity = repository.findByIdAndUserId(id, currentUserId)
                .orElseThrow(() -> new IllegalArgumentException(CONVERSATION_NOT_FOUND + id));

        if (request.getTitle() != null) {
            entity.setTitle(request.getTitle());
        }
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }

        Conversation updated = repository.save(entity);
        log.info("Updated conversation with ID: {}", id);

        return ConversationResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUser().getId();
        log.info("Soft deleting conversation with ID: {}", id);

        Conversation entity = repository.findByIdAndUserId(id, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found with ID: " + id));

        entity.setActive(false);
        repository.save(entity);

        log.info("Soft deleted conversation with ID: {}", id);
    }
}