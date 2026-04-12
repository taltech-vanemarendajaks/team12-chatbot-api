package com.example.bossbot.stampanswer.service;

import com.example.bossbot.stampanswer.dto.CreateStampAnswerRequest;
import com.example.bossbot.stampanswer.dto.StampAnswerResponse;
import com.example.bossbot.stampanswer.dto.UpdateStampAnswerRequest;
import com.example.bossbot.stampanswer.entity.StampAnswer;
import com.example.bossbot.stampanswer.repository.StampAnswerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StampAnswerServiceImpl implements StampAnswerService {

    private static final String STAMP_ANSWER_NOT_FOUND = "Stamp answer not found with ID: ";

    private final StampAnswerRepository repository;

    @Override
    @Transactional
    public StampAnswerResponse create(CreateStampAnswerRequest request) {
        log.info("Creating new stamp answer with question: {}", request.getQuestion());

        // Check if question already exists
        if (repository.existsByQuestionIgnoreCase(request.getQuestion())) {
            throw new IllegalArgumentException("Stamp answer with this question already exists");
        }

        // TODO: Replace with authenticated user ID from Spring Security context
        Long currentUserId = 1L; // System user placeholder

        StampAnswer entity = StampAnswer.builder()
                .question(request.getQuestion())
                .keywords(request.getKeywords())
                .answer(request.getAnswer())
                .isActive(request.getIsActive() != null ? request.getIsActive() : Boolean.TRUE)
                .usageCount(0)
                .createdBy(currentUserId)
                .build();

        StampAnswer saved = repository.save(entity);
        log.info("Created stamp answer with ID: {}", saved.getId());

        return StampAnswerResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StampAnswerResponse getById(Long id) {
        log.info("Fetching stamp answer with ID: {}", id);

        StampAnswer entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(STAMP_ANSWER_NOT_FOUND + id));

        return StampAnswerResponse.fromEntity(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StampAnswerResponse> getAll() {
        log.info("Fetching all stamp answers");

        return repository.findAll().stream()
                .map(StampAnswerResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StampAnswerResponse> getAllActive() {
        log.info("Fetching all active stamp answers");

        return repository.findByIsActiveTrueOrderByCreatedAtDesc().stream()
                .map(StampAnswerResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StampAnswerResponse> search(String searchTerm) {
        log.info("Searching stamp answers with term: {}", searchTerm);

        return repository.searchByQuestionOrKeywords(searchTerm).stream()
                .map(StampAnswerResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StampAnswerResponse> getMostUsed() {
        log.info("Fetching most used stamp answers");

        return repository.findTop10ByIsActiveTrueOrderByUsageCountDesc().stream()
                .map(StampAnswerResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public StampAnswerResponse update(Long id, UpdateStampAnswerRequest request) {
        log.info("Updating stamp answer with ID: {}", id);

        StampAnswer entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(STAMP_ANSWER_NOT_FOUND + id));

        // TODO: Replace with authenticated user ID from Spring Security context
        Long currentUserId = 1L; // System user placeholder

        // Update only non-null fields
        if (request.getQuestion() != null) {
            entity.setQuestion(request.getQuestion());
        }
        if (request.getKeywords() != null) {
            entity.setKeywords(request.getKeywords());
        }
        if (request.getAnswer() != null) {
            entity.setAnswer(request.getAnswer());
        }
        if (request.getIsActive() != null) {
            entity.setIsActive(request.getIsActive());
        }
        entity.setUpdatedBy(currentUserId);

        StampAnswer updated = repository.save(entity);
        log.info("Updated stamp answer with ID: {}", id);

        return StampAnswerResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Soft deleting stamp answer with ID: {}", id);

        StampAnswer entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(STAMP_ANSWER_NOT_FOUND + id));

        entity.setIsActive(false);
        repository.save(entity);

        log.info("Soft deleted stamp answer with ID: {}", id);
    }

    @Override
    @Transactional
    public void recordUsage(Long id) {
        log.info("Recording usage for stamp answer ID: {}", id);

        StampAnswer entity = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(STAMP_ANSWER_NOT_FOUND + id));

        entity.recordUsage();
        repository.save(entity);

        log.info("Recorded usage for stamp answer ID: {}. New count: {}", id, entity.getUsageCount());
    }

    @Override
    @Transactional
    public StampAnswerResponse getByQuestion(String question) {
        log.info("Looking up stamp answer for question: {}", question);

        return repository.findByQuestionIgnoreCaseAndIsActiveTrue(question)
                .map(entity -> {
                    // Record usage when answer is retrieved by question
                    entity.recordUsage();
                    repository.save(entity);
                    log.info("Found and recorded usage for stamp answer ID: {}", entity.getId());
                    return StampAnswerResponse.fromEntity(entity);
                })
                .orElse(null);
    }
}
