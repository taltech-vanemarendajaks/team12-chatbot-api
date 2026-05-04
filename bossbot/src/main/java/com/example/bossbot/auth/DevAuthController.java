package com.example.bossbot.auth;

import com.example.bossbot.security.JwtService;
import com.example.bossbot.user.User;
import com.example.bossbot.user.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Development-only authentication controller used to simulate user login in a local environment.
 * Provides an endpoint to generate and set authentication tokens as cookies for specific users.
 *
 * This controller is active only in the "local" Spring profile.
 *
 * Dependencies:
 * - {@link UserRepository}: Provides access to user data from the database.
 * - {@link JwtService}: Responsible for generating JWT tokens.
 *
 * Endpoints:
 * - POST "/auth/dev/login-as/{userId}":
 *   Allows an administrator or developer to emulate a user login by their user ID.
 *   Generates a JWT token for the specified user and sets it as an HTTP-only cookie
 *   in the response. The token is valid for 24 hours and intended for local testing purposes.
 */

@Profile("local")
@RestController
@RequestMapping("/auth/dev")
@RequiredArgsConstructor
public class DevAuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping("/login-as/{userId}")
    public ResponseEntity<Void> loginAs(@PathVariable Long userId, HttpServletResponse response) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        String token = jwtService.generateToken(user.getEmail());

        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // local http
        cookie.setPath("/");
        cookie.setMaxAge(24 * 60 * 60);
        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }
}
