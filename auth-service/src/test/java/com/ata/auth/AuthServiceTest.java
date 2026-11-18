package com.ata.auth;

import com.ata.auth.dto.AuthDtos.*;
import com.ata.auth.entity.User;
import com.ata.auth.exception.AuthException;
import com.ata.auth.repository.UserRepository;
import com.ata.auth.security.JwtUtil;
import com.ata.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("test@example.com", "encodedPassword", User.Role.CUSTOMER);
    }

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setRole("CUSTOMER");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_adminRole_success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("admin@example.com");
        request.setPassword("password123");
        request.setRole("ADMIN");

        User adminUser = new User("admin@example.com", "encoded", User.Role.ADMIN);
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(adminUser);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void register_duplicateEmail_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setRole("CUSTOMER");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(AuthException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_invalidRole_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setRole("INVALID");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        assertThrows(AuthException.class, () -> authService.register(request));
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
    }

    @Test
    void login_deactivatedUser_throwsException() {
        user.setActive(false);
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        assertThrows(AuthException.class, () -> authService.login(request));
    }

    @Test
    void login_wrongPassword_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        assertThrows(AuthException.class, () -> authService.login(request));
    }

    @Test
    void login_userNotFound_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("notfound@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(AuthException.class, () -> authService.login(request));
    }

    @Test
    void changePassword_success() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("newEncoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertDoesNotThrow(() -> authService.changePassword("test@example.com", request));
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_wrongOldPassword_throwsException() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongOld");
        request.setNewPassword("newPass123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOld", "encodedPassword")).thenReturn(false);

        assertThrows(AuthException.class, () -> authService.changePassword("test@example.com", request));
    }

    @Test
    void changePassword_userNotFound_throwsException() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPass");
        request.setNewPassword("newPass123");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(AuthException.class, () -> authService.changePassword("unknown@example.com", request));
    }

    @Test
    void forgotPassword_success() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");
        request.setNewPassword("newPass123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass123")).thenReturn("newEncoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        assertDoesNotThrow(() -> authService.forgotPassword(request));
        verify(userRepository).save(user);
    }

    @Test
    void forgotPassword_userNotFound_throwsException() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("notfound@example.com");
        request.setNewPassword("newPass123");

        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(AuthException.class, () -> authService.forgotPassword(request));
    }

    @Test
    void logout_doesNotThrow() {
        assertDoesNotThrow(() -> authService.logout("test@example.com", "some-token"));
    }
}
