package com.example.bossbot.security;

import com.example.bossbot.user.User;
import com.example.bossbot.user.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Configuration class for Spring Security.
 * Defines authentication and authorization rules, configures JWT-based stateless sessions,
 * and sets up CORS, CSRF protection, and OAuth2 login flow.
 * <p>
 * References:
 * <p>
 * - https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html
 * <p>
 * - https://www.baeldung.com/spring-security-sign-jwt-token
 * <p>
 * - https://github.com/taltech-vanemarendajaks/vanemarendaja-borsibaar
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final ClientRegistrationRepository clientRegistrationRepository;
    @Value("${app.security.permit-all:false}") // by default false
    private boolean permitAll;

    @Bean
    public JwtDecoder jwtDecoder(JwtService jwtService) {
        return NimbusJwtDecoder.withSecretKey(jwtService.getKey()).build();
    }

    @Bean
    public BearerTokenResolver cookieBearerTokenResolver() {
        return request -> {
            if (request.getCookies() == null) return null;
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
            return null;
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorsConfigurationSource corsConfigurationSource,
                                                   UserRepository userRepository,
                                                   JwtDecoder jwtDecoder) throws Exception {

        DefaultOAuth2AuthorizationRequestResolver defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");

        OAuth2AuthorizationRequestResolver customResolver = new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                var req = defaultResolver.resolve(request);
                if (req == null)
                    return null;
                return OAuth2AuthorizationRequest.from(req)
                        .additionalParameters(p -> p.put("prompt", "select_account"))
                        .build();
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                var req = defaultResolver.resolve(request, clientRegistrationId);
                if (req == null)
                    return null;
                return OAuth2AuthorizationRequest.from(req)
                        .additionalParameters(p -> p.put("prompt", "select_account"))
                        .build();
            }
        };

        return http
                .csrf(AbstractHttpConfigurer::disable)
                // TODO:: test csrf with POST, PUT, DELETE before applying it:
                // .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                // Let Spring Security add CORS headers on 401/403/preflight too
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .requestCache(RequestCacheConfigurer::disable)
                // Use IF_REQUIRED session management (stateless for API, sessions for OAuth2)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> {
                    if (permitAll) {
                        auth.anyRequest().permitAll();
                    } else {
                        auth
                                // Allow OPTIONS for CORS preflight
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                // Allow OAuth2 endpoints and public routes
                                .requestMatchers("/", "/error", "/oauth2/**", "/login/oauth2/code/**", "/auth/login/success")
                                .permitAll()
                                // TODO: add other public API EP if any
                                // All other API requests require authentication
                                .anyRequest().authenticated();
                    }
                })
                // With permitAll=false: Return 401 JSON for /api/** so FE can handle auth; non-API paths still redirect to Google login
                .exceptionHandling(ex -> ex                                                                                                                                                                                                                          .defaultAuthenticationEntryPointFor(
                                (request, response, authException) -> {                                                                                                                                                                                                          response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                                },
                                request -> request.getRequestURI().startsWith("/api/")
                        )
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("/auth/login/success", true)
                        .authorizationEndpoint(auth -> auth.authorizationRequestResolver(customResolver)))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter(userRepository)))
                        .bearerTokenResolver(cookieBearerTokenResolver()))
                .build();
    }


    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(allowedOrigins));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true); // since sending cookies
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    // Creates a converter that takes a JWT and turns it into a Spring Security authentication object
    private Converter <Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter(UserRepository userRepository) {
        return token -> {
            String email = token.getSubject();
            User user = userRepository.findByEmailWithRole(email)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED, "User not found"));
            return new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getRole() != null
                            ? List.of(new SimpleGrantedAuthority(
                            "ROLE_" + user.getRole().getRoleName()))
                            : List.of());
        };
    }
}


