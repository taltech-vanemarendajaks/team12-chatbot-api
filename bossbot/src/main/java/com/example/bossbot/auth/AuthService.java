package com.example.bossbot.auth;

import com.example.bossbot.role.Role;
import com.example.bossbot.role.RoleName;
import com.example.bossbot.role.RoleRepository;
import com.example.bossbot.security.JwtService;
import com.example.bossbot.user.User;
import com.example.bossbot.user.UserAuthDto;
import com.example.bossbot.user.UserMapper;
import com.example.bossbot.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import java.time.Instant;

import static com.example.bossbot.security.OAuth2AuthValidator.requireEmail;

 /**
  * Class: AuthService
  * Custom OAuth2 authentication success handler.
  * Generates JWT tokens upon successful OAuth2 authentication and sets them in HTTP-only cookies.
  * Redirects users to the frontend application after successful login.
  * References: https://github.com/taltech-vanemarendajaks/vanemarendaja-borsibaar
  */

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    @Value("${app.auth.admin-bootstrap-emails:}")
    private String adminBootstrapEmails;

    public record AuthResult(UserAuthDto dto) {
    }

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, JwtService jwtService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @Transactional
    public AuthResult processOAuthLogin(OAuth2AuthenticationToken auth) {
        String email = requireEmail(auth);
        String name = auth.getPrincipal().getAttribute("name");

        // Check if user exists or create a new one
        User dbUser = userRepository.findByEmail(email)
                .orElse(User
                        .builder()
                        .email(email)
                        .name(name)
                        .build()
                );

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        boolean isBootstrapAdmin = adminEmailSet().contains(normalizedEmail);

        if (dbUser.getRole() == null || dbUser.getRole().getRoleName() == null) {
            RoleName targetRole = isBootstrapAdmin ? RoleName.ADMIN : RoleName.USER;
            Role role = roleRepository.findByRoleName(targetRole)
                    .orElseGet(() -> roleRepository.save(Role.builder().roleName(targetRole).build()));
            dbUser.setRole(role);
        }

        // Update name in case it changed
        dbUser.setName(name);
        dbUser.setLastActiveAt(Instant.now());

        userRepository.save(dbUser);

        // Issue JWT
        String token = jwtService.generateToken(dbUser.getEmail());
        return new AuthResult(userMapper.toDto(dbUser, token));
    }


     // AuthService.java (add helper)
     private Set<String> adminEmailSet() {
         if (adminBootstrapEmails == null || adminBootstrapEmails.isBlank()) {
             return Set.of();
         }

         return Arrays.stream(adminBootstrapEmails.split(","))
                 .map(String::trim)
                 .map(s -> s.toLowerCase(Locale.ROOT))
                 .filter(s -> !s.isBlank())
                 .collect(Collectors.toSet());
     }
}

