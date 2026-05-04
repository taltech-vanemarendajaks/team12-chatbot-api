package com.example.bossbot.bannedword.controller;

import com.example.bossbot.bannedword.dto.BannedWordResponse;
import com.example.bossbot.bannedword.dto.CreateBannedWordRequest;
import com.example.bossbot.bannedword.dto.UpdateBannedWordRequest;
import com.example.bossbot.bannedword.service.BannedWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/banned-words")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Banned Words", description = "Banned word management operations")
public class BannedWordController {

    private final BannedWordService bannedWordService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new banned word")
    @ApiResponse(responseCode = "201", description = "Banned word created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping
    public ResponseEntity<BannedWordResponse> create(@Valid @RequestBody CreateBannedWordRequest request) {
        log.info("REST request to create banned word");
        BannedWordResponse response = bannedWordService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get banned word by ID")
    @ApiResponse(responseCode = "200", description = "Banned word found")
    @ApiResponse(responseCode = "404", description = "Banned word not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/{id}")
    public ResponseEntity<BannedWordResponse> getById(@PathVariable Long id) {
        log.info("REST request to get banned word by ID: {}", id);
        BannedWordResponse response = bannedWordService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all banned words")
    @ApiResponse(responseCode = "200", description = "Banned words returned")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping
    public ResponseEntity<List<BannedWordResponse>> getAll(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        log.info("REST request to get all banned words. Active only: {}", activeOnly);
        List<BannedWordResponse> responses = activeOnly
                ? bannedWordService.getAllActive()
                : bannedWordService.getAll();
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get banned words by category")
    @ApiResponse(responseCode = "200", description = "Banned words returned")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @GetMapping("/category/{category}")
    public ResponseEntity<List<BannedWordResponse>> getByCategory(@PathVariable String category) {
        log.info("REST request to get banned words by category: {}", category);
        List<BannedWordResponse> responses = bannedWordService.getByCategory(category);
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a banned word")
    @ApiResponse(responseCode = "200", description = "Banned word updated successfully")
    @ApiResponse(responseCode = "404", description = "Banned word not found")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PutMapping("/{id}")
    public ResponseEntity<BannedWordResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBannedWordRequest request) {
        log.info("REST request to update banned word with ID: {}", id);
        BannedWordResponse response = bannedWordService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete (deactivate) a banned word")
    @ApiResponse(responseCode = "204", description = "Banned word deleted successfully")
    @ApiResponse(responseCode = "404", description = "Banned word not found")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST request to delete banned word with ID: {}", id);
        bannedWordService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Validation error: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    public record ErrorResponse(String message) {}
}