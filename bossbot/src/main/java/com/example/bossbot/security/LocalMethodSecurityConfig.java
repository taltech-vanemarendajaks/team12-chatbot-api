package com.example.bossbot.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Configuration class for enabling method security specifically for the "local" profile.
 *
 * This configuration class is activated when the application is running with the "local" Spring profile.
 * It controls the method security settings, disabling pre/post annotations for method security by
 * setting `prePostEnabled` to false.
 *
 * Annotations:
 * - {@code @Profile("local")}: Ensures that this configuration is only loaded for the "local" profile.
 * - {@code @Configuration}: Marks this class as a source of bean definitions for the application context.
 * - {@code @EnableMethodSecurity}: Configures method-level security with the specified settings.
 */
@Profile("local")
@Configuration
@EnableMethodSecurity(prePostEnabled = false)
public class LocalMethodSecurityConfig {
}
