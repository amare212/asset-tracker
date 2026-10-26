package com.example.Demo.service;

import com.example.Demo.dto.AuthRequest;
import com.example.Demo.dto.UserResponse;
import com.example.Demo.entity.Role;
import com.example.Demo.entity.User;
import com.example.Demo.exception.ConflictException;
import com.example.Demo.repository.UserRepository;
import com.example.Demo.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public UserResponse register(AuthRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("Username already taken: " + request.username());
        }
        User user = new User(
                request.username(),
                passwordEncoder.encode(request.password()),   // BCrypt hash
                Role.ROLE_STAFF);                              // self-registration is always STAFF
        User saved = userRepository.save(user);
        return new UserResponse(saved.getId(), saved.getUsername(), saved.getRole());
    }

    public String login(AuthRequest request) {
        // throws BadCredentialsException (-> 401) if username/password are wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        User user = userRepository.findByUsername(request.username()).orElseThrow();
        return jwtService.generateToken(user.getUsername(), user.getRole().name());
    }
}
