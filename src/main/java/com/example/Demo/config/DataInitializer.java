package com.example.Demo.config;

import com.example.Demo.entity.Role;
import com.example.Demo.entity.User;
import com.example.Demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Registration always creates ROLE_STAFF, so the first ADMIN has to be
 * created some other way. This seeds one on startup (if it doesn't exist).
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedAdmin(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${app.admin.username}") String adminUsername,
                                @Value("${app.admin.password}") String adminPassword) {
        return args -> {
            if (!userRepository.existsByUsername(adminUsername)) {
                userRepository.save(new User(adminUsername,
                        passwordEncoder.encode(adminPassword), Role.ROLE_ADMIN));
            }
        };
    }
}
