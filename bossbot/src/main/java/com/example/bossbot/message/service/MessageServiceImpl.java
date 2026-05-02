package com.example.bossbot.message.service;

import com.example.bossbot.common.ResourceNotFoundException;
import com.example.bossbot.conversation.repository.ConversationRepository;
import com.example.bossbot.message.dto.CreateMessageRequest;
import com.example.bossbot.message.dto.MessageResponse;
import com.example.bossbot.message.entity.Message;
import com.example.bossbot.message.repository.MessageRepository;
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
public class MessageServiceImpl implements MessageService {

    // Use generic "Message not found" for security reasons (even when conversation not found)
    private static final String MESSAGE_NOT_FOUND = "Message not found";

    private final MessageRepository repository;
    private final ConversationRepository conversationRepository;

    @Override
    @Transactional
    public MessageResponse create(CreateMessageRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();

        requireConversationOwnedByCurrentUser(request.getConversationId(), currentUser.getId());
        log.info("Creating new message in conversation: {}", request.getConversationId());

        // Authenticated user ID from Spring Security context
        Long currentUserId = currentUser.getId();

        Message entity = Message.builder()
                .conversationId(request.getConversationId())
                .role(request.getRole())
                .content(request.getContent())
                .isActive(true)
                .createdBy(currentUserId)
                .build();

        Message saved = repository.save(entity);
        log.info("Created message with ID: {}", saved.getId());

        return MessageResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MessageResponse getById(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        log.info("Fetching message with ID: {}", id);

        Message entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_NOT_FOUND));
        requireConversationOwnedByCurrentUser(entity.getConversationId(), currentUser.getId());

        return MessageResponse.fromEntity(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getAll(Long conversationId) {
        User currentUser = SecurityUtils.getCurrentUser();

        requireConversationOwnedByCurrentUser(conversationId, currentUser.getId());

        log.info("Fetching messages for conversation ID: {}", conversationId);
        return repository.findByConversationIdAndIsActiveTrueOrderByCreatedAtAscIdAsc(conversationId).stream()
                .map(MessageResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();

        log.info("Soft deleting message with ID: {}", id);

        Message entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_NOT_FOUND));

        requireConversationOwnedByCurrentUser(entity.getConversationId(), currentUser.getId());

        entity.setIsActive(false);
        repository.save(entity);

        log.info("Soft deleted message with ID: {}", id);
    }

    private void requireConversationOwnedByCurrentUser(Long conversationId, Long currentUserId) {
        conversationRepository.findByIdAndUserId(conversationId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(MESSAGE_NOT_FOUND));
    }
}
