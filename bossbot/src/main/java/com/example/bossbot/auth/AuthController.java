package com.example.bossbot.auth;

import com.example.bossbot.security.OAuth2AuthValidator;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * REST controller for authentication endpoints.
 * Handles login success callbacks, logout, and token refresh operations.
 * Manages JWT tokens through HTTP-only cookies for secure session management.
 */

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    @Value("${app.frontend.url}")
    private String frontendUrl;
    @Value("${jwt.expiration.milliseconds}")
    private int jwtCookieMaxAgeMilliSeconds;
    private static final String POST_LOGIN_REDIRECT_PATH = "/";
    private static final String LOGIN_ERROR_REDIRECT_PATH = "/login?error=auth_failure";

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostConstruct
    void validateConfig() {
        // Hardcoded due to security: validate on startup, to keep app from running if urls get compromised
        List<String> allowedUrls = List.of("http://localhost:5173", "http://bossbot.mooo.com");
        if(!allowedUrls.contains(frontendUrl)){
            throw new IllegalStateException("Untrusted frontend URL: " + frontendUrl);
        }
    }

    @GetMapping("/login/success")
    public void success(HttpServletResponse response, OAuth2AuthenticationToken auth) throws IOException {
        // Basic validation
        if(rejectIfUnauthenticated(response, auth)) return;

        try {
            var result = authService.processOAuthLogin(auth);
            int jwtCookieMaxAgeSeconds = jwtCookieMaxAgeMilliSeconds / 1000;
            Cookie cookie = createOrClearJwtCookie(result.dto().token(), jwtCookieMaxAgeSeconds);
            response.addCookie(cookie);
            response.sendRedirect(frontendUrl + POST_LOGIN_REDIRECT_PATH);
        } catch (Exception e) {
            // TODO:: create FE error page
            log.error("OAuth login failed: ", e);
            response.sendRedirect(frontendUrl + LOGIN_ERROR_REDIRECT_PATH);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();

        Cookie cookie = createOrClearJwtCookie("", 0);
        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }

    private boolean rejectIfUnauthenticated(HttpServletResponse response, OAuth2AuthenticationToken auth)
            throws IOException {
        try {
            OAuth2AuthValidator.requireAuthenticatedUser(auth);
            // not rejected
            return false;
        } catch (AuthenticationCredentialsNotFoundException ex){
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
            // rejected
            return true;
        }
        catch (IllegalStateException ex){
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
        }
        //rejected
        return true;
    }

    private Cookie createOrClearJwtCookie(String token, Integer maxAgeSeconds){
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(frontendUrl.startsWith("https://"));
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeSeconds);

        return cookie;
    }
}
