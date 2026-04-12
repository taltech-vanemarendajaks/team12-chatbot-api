package com.example.bossbot.stampanswer.service;

import com.example.bossbot.stampanswer.dto.CreateStampAnswerRequest;
import com.example.bossbot.stampanswer.dto.StampAnswerResponse;
import com.example.bossbot.stampanswer.dto.UpdateStampAnswerRequest;

import java.util.List;

public interface StampAnswerService {

    /**
     * Create a new stamp answer
     */
    StampAnswerResponse create(CreateStampAnswerRequest request);

    /**
     * Get stamp answer by ID
     */
    StampAnswerResponse getById(Long id);

    /**
     * Get all stamp answers
     */
    List<StampAnswerResponse> getAll();

    /**
     * Get all active stamp answers
     */
    List<StampAnswerResponse> getAllActive();

    /**
     * Search stamp answers by question or keywords
     */
    List<StampAnswerResponse> search(String searchTerm);

    /**
     * Get most used stamp answers
     */
    List<StampAnswerResponse> getMostUsed();

    /**
     * Update existing stamp answer
     */
    StampAnswerResponse update(Long id, UpdateStampAnswerRequest request);

    /**
     * Soft delete (deactivate) a stamp answer
     */
    void delete(Long id);

    /**
     * Record usage of a stamp answer (increments counter)
     */
    void recordUsage(Long id);

    /**
     * Get answer by exact question match (case-insensitive)
     * Returns null if no active stamp answer found
     */
    StampAnswerResponse getByQuestion(String question);
}
