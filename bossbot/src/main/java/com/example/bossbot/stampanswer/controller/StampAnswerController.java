package com.example.bossbot.stampanswer.controller;

import com.example.bossbot.stampanswer.dto.CreateStampAnswerRequest;
import com.example.bossbot.stampanswer.dto.StampAnswerResponse;
import com.example.bossbot.stampanswer.dto.UpdateStampAnswerRequest;
import com.example.bossbot.stampanswer.service.StampAnswerService;

import java.util.List;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/v1/stamp-answers")
@RequiredArgsConstructor
@Slf4j
public class StampAnswerController {

    private final StampAnswerService stampAnswerService;

    /**
     * Create a new stamp answer
     * POST /api/v1/stamp-answers
     */
    @PostMapping
    public ResponseEntity<StampAnswerResponse> create(@Valid @RequestBody CreateStampAnswerRequest request) {
        log.info("REST request to create stamp answer");
        StampAnswerResponse response = stampAnswerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get stamp answer by ID
     * GET /api/v1/stamp-answers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<StampAnswerResponse> getById(@PathVariable Long id) {
        log.info("REST request to get stamp answer by ID: {}", id);
        StampAnswerResponse response = stampAnswerService.getById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all stamp answers
     * GET /api/v1/stamp-answers
     */
    @GetMapping
    public ResponseEntity<List<StampAnswerResponse>> getAll(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        log.info("REST request to get all stamp answers. Active only: {}", activeOnly);
        List<StampAnswerResponse> responses = activeOnly 
                ? stampAnswerService.getAllActive() 
                : stampAnswerService.getAll();
        return ResponseEntity.ok(responses);
    }

    /**
     * Get stamp answers by category
     * GET /api/v1/stamp-answers/category/{category}
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<StampAnswerResponse>> getByCategory(@PathVariable String category) {
        log.info("REST request to get stamp answers by category: {}", category);
        List<StampAnswerResponse> responses = stampAnswerService.getByCategory(category);
        return ResponseEntity.ok(responses);
    }

    /**
     * Search stamp answers by question or keywords
     * GET /api/v1/stamp-answers/search?q={searchTerm}
     */
    @GetMapping("/search")
    public ResponseEntity<List<StampAnswerResponse>> search(@RequestParam("q") String searchTerm) {
        log.info("REST request to search stamp answers with term: {}", searchTerm);
        List<StampAnswerResponse> responses = stampAnswerService.search(searchTerm);
        return ResponseEntity.ok(responses);
    }

    /**
     * Get answer by exact question match (case-insensitive)
     * GET /api/v1/stamp-answers/by-question?q={question}
     * Returns 404 if no matching active stamp answer found
     */
    @GetMapping("/by-question")
    public ResponseEntity<StampAnswerResponse> getByQuestion(@RequestParam("q") String question) {
        log.info("REST request to get answer for question: {}", question);
        StampAnswerResponse response = stampAnswerService.getByQuestion(question);
        if (response == null) {
            log.info("No stamp answer found for question: {}", question);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Get most used stamp answers
     * GET /api/v1/stamp-answers/most-used
     */
    @GetMapping("/most-used")
    public ResponseEntity<List<StampAnswerResponse>> getMostUsed() {
        log.info("REST request to get most used stamp answers");
        List<StampAnswerResponse> responses = stampAnswerService.getMostUsed();
        return ResponseEntity.ok(responses);
    }

    /**
     * Update stamp answer
     * PUT /api/v1/stamp-answers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<StampAnswerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStampAnswerRequest request) {
        log.info("REST request to update stamp answer with ID: {}", id);
        StampAnswerResponse response = stampAnswerService.update(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete (deactivate) stamp answer
     * DELETE /api/v1/stamp-answers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST request to delete stamp answer with ID: {}", id);
        stampAnswerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Record usage of a stamp answer
     * POST /api/v1/stamp-answers/{id}/usage
     */
    @PostMapping("/{id}/usage")
    public ResponseEntity<Void> recordUsage(@PathVariable Long id) {
        log.info("REST request to record usage for stamp answer ID: {}", id);
        stampAnswerService.recordUsage(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Exception handler for IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Validation error: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    /**
     * Simple error response DTO
     */
    public record ErrorResponse(String message) {}
}
