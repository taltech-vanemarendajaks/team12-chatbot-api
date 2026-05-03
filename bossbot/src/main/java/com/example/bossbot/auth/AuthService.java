package com.example.bossbot.auth;

import com.example.bossbot.role.Role;
import com.example.bossbot.role.RoleName;
import com.example.bossbot.role.RoleRepository;
import com.example.bossbot.security.JwtService;
import com.example.bossbot.user.User;
import com.example.bossbot.user.UserAuthDto;
import com.example.bossbot.user.UserMapper;
import com.example.bossbot.user.UserRepository;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;

    public record AuthResult(UserAuthDto dto) {
    }

    public AuthService(UserRepository userRepository, JwtService jwtService, UserMapper userMapper,
                       RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public AuthResult processOAuthLogin(OAuth2AuthenticationToken auth) {
        String email = requireEmail(auth);
        String name = auth.getPrincipal().getAttribute("name");

        // Check if user exists or create a new one
        User dbUser = userRepository.findByEmail(email)
                .orElse(User.builder()
                        .email(email)
                        .name(name)
                        .build());

        if (dbUser.getRole() == null) {
            Role defaultRole = roleRepository.findByRoleName(RoleName.USER)
                    .orElseThrow(() -> new IllegalArgumentException("Default role USER not found"));
            dbUser.setRole(defaultRole);
        }

        // Update name in case it changed
        dbUser.setName(name);
        dbUser.setLastActiveAt(Instant.now());

        userRepository.save(dbUser);

        // Issue JWT
        String token = jwtService.generateToken(dbUser.getEmail());

        return new AuthResult(userMapper.toDto(dbUser, token));
    }
}

