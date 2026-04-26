package com.example.bossbot.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Service class for generating JSON Web Tokens (JWT) using the io.jsonwebtoken library.
 * Handles token creation with a configurable secret and expiration.
 *  <p>
 *  References:
 *  <p>
 *  - https://medium.com/@frankpythagore/upgrading-your-jwt-implementation-in-java-a-guide-to-the-new-io-jsonwebtoken-api-ab30234d5979
 *  <p>
 *  - https://stackoverflow.com/questions/73486900/how-to-fix-deprecated-parser-and-setsigningkeyjava-security-key-usage-in-jjw
 */

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;
    private SecretKey key;

    @PostConstruct
    void init(){
        // FIXME:: Consider using Base64.getDecoder().decode(secretKey)
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // Leave it package private, not to share outside!
    SecretKey getKey() {
        return key;
    }

    @Value("${jwt.expiration.milliseconds}")
    private int jwtCookieMaxAgeMilliSeconds;

    public String generateToken(String subject) {
        long expirationMs = jwtCookieMaxAgeMilliSeconds;
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key, io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();
    }
}
