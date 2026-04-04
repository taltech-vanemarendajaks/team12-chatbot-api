package com.example.bossbot.conversation.controller;

import com.example.bossbot.conversation.dto.ConversationResponse;
import com.example.bossbot.conversation.dto.CreateConversationRequest;
import com.example.bossbot.conversation.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@CrossOrigin("http://localhost:5173") // TODO: Update with prod URL and/or put localhost to variable
@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Conversations", description = "Chat conversation operations")
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * Create a new conversation
     * POST /api/v1/conversations
     */
    @Operation(summary = "Create a new conversation")
    @ApiResponse(responseCode = "201", description = "Conversation created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @PostMapping
    public ResponseEntity<@NonNull ConversationResponse> create(@Valid @RequestBody CreateConversationRequest request,
        @RequestParam Long userId) {
        log.info("REST request to create conversation with title: {}", request.getTitle());
        ConversationResponse response = conversationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a conversation by ID
     * GET /api/v1/conversations/{id}
     */
    @Operation(summary = "Get a conversation by ID")
    @ApiResponse(responseCode = "200", description = "Conversation found")
    @ApiResponse(responseCode = "404", description = "Conversation not found")
    @GetMapping("/{id}")
    public ResponseEntity<@NonNull ConversationResponse> getById(@PathVariable Long id) {
        log.info("REST request to get conversation by ID: {}", id);
        ConversationResponse response = conversationService.getById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all conversations
     * GET /api/v1/conversations?userId={id}
     */
    @Operation(summary = "Get all conversations for user")
    @ApiResponse(responseCode = "200", description = "Conversations returned")
    @ApiResponse(responseCode = "400", description = "userId query parameter is required") // TODO: Temporary 400 status code as later from auth
    @GetMapping
    public ResponseEntity<@NonNull List<ConversationResponse>> getAll(
            @RequestParam Long userId) { // TODO: Later from auth
        log.info("REST request to get conversations");
        List<ConversationResponse> responses = conversationService.getAll(userId); // TODO: Later from auth
        return ResponseEntity.ok(responses);
    }

    /**
     * Get all active conversations
     * GET /api/v1/conversations?userId={id}
     */
    @Operation(summary = "Get all conversations for user")
    @ApiResponse(responseCode = "200", description = "Conversations returned")
    @ApiResponse(responseCode = "400", description = "userId query parameter is required") // TODO: Temporary 400 status code as later from auth
    @GetMapping("/active")
    public ResponseEntity<@NonNull List<ConversationResponse>> getAllActive(
            @RequestParam Long userId) { // TODO: Later from auth
        log.info("REST request to get active conversations");
        List<ConversationResponse> responses = conversationService.getAllActive(userId); // TODO: Later from auth
        return ResponseEntity.ok(responses);
    }

    /**
     * Soft delete a conversation
     * DELETE /api/v1/conversations/{id}
     */
    @Operation(summary = "Soft delete a conversation")
    @ApiResponse(responseCode = "204", description = "Conversation deleted successfully")
    @ApiResponse(responseCode = "404", description = "Conversation not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<@NonNull Void> delete(@PathVariable Long id) {
        log.info("REST request to delete conversation with ID: {}", id);
        conversationService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
