package com.example.bossbot.security;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for managing application status endpoints.
 *
 * Serves as a REST API to provide status-related information about the application
 * when running in a "local" or "docker" environment profile.
 *
 * The controller includes an endpoint to check the security configuration of the application
 * to enable FE run locally w/o security for easier development
 */
@Profile({"local", "docker"})
@RestController
@RequestMapping("api/v1")
public class StatusController {
    @Value("${app.security.permit-all:false}") // by default false
    private boolean permitAll;

    @Operation(summary = "Get security status", description = "Returns whether security is enabled or not.")
    @GetMapping("/security-status")
    public Map<String, Boolean> securityStatus(){
        return Map.of("isSecurityEnabled", !permitAll);
    }
}
