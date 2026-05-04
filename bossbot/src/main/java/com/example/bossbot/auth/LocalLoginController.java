package com.example.bossbot.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Controller for handling authentication-related requests in the local profile.
 *
 * This controller is active only when the application is running in the "local" profile,
 * as specified by the `@Profile("local")` annotation. It contains a single endpoint
 * for redirecting users to the frontend application when accessing the local login route.
 *
 * Endpoint:
 * - GET /auth/login: Redirects the user to the local frontend URL at "http://localhost:5173".
 */
@Profile("local")
@RestController
public class LocalLoginController {
    @GetMapping("/auth/login")
    public void login(HttpServletResponse response) throws IOException {
        // Redirect with local profile
        response.sendRedirect("http://localhost:5173");
    }
}
