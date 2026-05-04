package com.example.bossbot.user;


import com.example.bossbot.role.Role;
import com.example.bossbot.role.RoleName;
import com.example.bossbot.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/**
 * REST controller for managing user account information.
 * Provides endpoints for retrieving details of the currently authenticated user.
 * SecurityUtils throws ResponseStatusException if occurs, ApiExceptionHandler catches it globally
 * If user does not have role yet, default to USER
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {
//    private final UserRepository userRepository;
    public record MeResponse(String email, String name, RoleName roleName, Long discordId) {
    }

//    public record RegisterRequest(boolean acceptTerms) {
//    }

    @GetMapping
    @Operation(summary = "Get current user account", description = "Returns the currently authenticated user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved account info")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    public ResponseEntity<MeResponse> me() {
        try {
            User user = SecurityUtils.getCurrentUser();
            return ResponseEntity.ok (new MeResponse(
                    user.getEmail(),
                    user.getName(),
                    user.getRole() != null ? user.getRole().getRoleName() : null,
                    user.getDiscordId()
            ));
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            // Log unexpected errors for debugging
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to retrieve account information: " + e.getMessage(),
                    e);
        }
    }
}
