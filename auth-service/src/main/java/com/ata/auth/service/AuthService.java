package com.ata.auth.service;

import com.ata.auth.dto.AuthDtos.AuthResponse;
import com.ata.auth.dto.AuthDtos.ChangePasswordRequest;
import com.ata.auth.dto.AuthDtos.ForgotPasswordRequest;
import com.ata.auth.dto.AuthDtos.LoginRequest;
import com.ata.auth.dto.AuthDtos.RegisterRequest;
import com.ata.auth.entity.User;
import com.ata.auth.exception.AuthException;
import com.ata.auth.repository.UserRepository;
import com.ata.auth.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering user: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email already registered: " + request.getEmail());
        }
        User.Role role;
        try {
            role = User.Role.valueOf(request.getRole().toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e) {
            throw new AuthException("Invalid role. Must be ADMIN or CUSTOMER", e);
        }
        User user = new User(request.getEmail(), passwordEncoder.encode(request.getPassword()), role);
        userRepository.save(user);
        logger.info("User registered successfully: {}", request.getEmail());
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        return new AuthResponse(token, refreshToken, user.getRole().name(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        logger.info("Login attempt for: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Invalid email or password"));
        if (!user.isActive()) {
            throw new AuthException("Account is deactivated");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("Invalid email or password");
        }
        logger.info("Login successful for: {}", request.getEmail());
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        return new AuthResponse(token, refreshToken, user.getRole().name(), user.getEmail());
    }

    public void logout(String email, String token) {
        logger.info("User logged out: {}", email);
        if (token != null && !token.isBlank()) {
            jwtUtil.blacklistToken(token);
        }
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        logger.info("Changing password for: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found"));
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AuthException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        logger.info("Password changed for: {}", email);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        logger.info("Forgot password reset for: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("No account found with this email"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new AuthException("Invalid or expired refresh token");
        }
        String email = jwtUtil.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found"));
        String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        return new AuthResponse(newAccessToken, newRefreshToken, user.getRole().name(), user.getEmail());
    }
}
