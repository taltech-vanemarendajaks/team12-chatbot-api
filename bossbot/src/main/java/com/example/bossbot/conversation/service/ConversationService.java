package com.example.bossbot.conversation.service;

import com.example.bossbot.conversation.dto.ConversationResponse;
import com.example.bossbot.conversation.dto.CreateConversationRequest;
import com.example.bossbot.conversation.dto.UpdateConversationRequest;

import java.util.List;

public interface ConversationService {

    /**
     * Create a new conversation
     */
    ConversationResponse create(CreateConversationRequest request);

    /**
     * Get a conversation by ID
     */
    ConversationResponse getById(Long id);

    /**
     * Get all user conversations, ordered latest updated first.
     */
    List<ConversationResponse> getAll(Long userId); // TODO: from auth

    /**
     * Get all active user conversations, ordered latest updated first.
     */
    List<ConversationResponse> getAllActive(Long userId); // TODO: from auth

    /**
     * Update a conversation
     */
    ConversationResponse update(Long id, UpdateConversationRequest request);

    /**
     * Soft delete a conversation
     */
    void delete(Long id);
}
