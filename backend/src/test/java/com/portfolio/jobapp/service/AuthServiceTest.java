package com.portfolio.jobapp.service;

import com.portfolio.jobapp.dto.request.LoginRequest;
import com.portfolio.jobapp.dto.request.RegisterRequest;
import com.portfolio.jobapp.dto.request.UpdateProfileRequest;
import com.portfolio.jobapp.dto.response.AuthResponse;
import com.portfolio.jobapp.entity.User;
import com.portfolio.jobapp.exception.DuplicateResourceException;
import com.portfolio.jobapp.repository.UserRepository;
import com.portfolio.jobapp.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Alice Developer", "alice@example.com", "encoded-secret");
    }

    @Test
    @DisplayName("Should successfully register a new user and return JWT token")
    void register_Success() {
        RegisterRequest request = new RegisterRequest("Alice Developer", "alice@example.com", "plainSecret123");

        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainSecret123")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(tokenProvider.generateToken("alice@example.com")).thenReturn("mocked-jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mocked-jwt-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUser().getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getUser().getName()).isEqualTo("Alice Developer");

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when registering existing email")
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest request = new RegisterRequest("Alice", "alice@example.com", "plainSecret123");
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully authenticate user and return JWT")
    void login_Success() {
        LoginRequest request = new LoginRequest("alice@example.com", "plainSecret123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));
        when(tokenProvider.generateToken("alice@example.com")).thenReturn("mocked-jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("mocked-jwt-token");
        assertThat(response.getUser().getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    @DisplayName("Should propagate BadCredentialsException when credentials fail")
    void login_InvalidCredentials_ThrowsException() {
        LoginRequest request = new LoginRequest("alice@example.com", "wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void getCurrentUserMapsSafeProfileFields() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));

        assertThat(authService.getCurrentUser(" ALICE@example.com ").getName()).isEqualTo("Alice Developer");
    }

    @Test
    void updateProfileNormalizesEmailAndName() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));
        when(userRepository.existsByEmail("alice.new@example.com")).thenReturn(false);
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);

        authService.updateProfile("alice@example.com",
                new UpdateProfileRequest(" Alice Smith ", "ALICE.NEW@example.com", null, null));

        assertThat(sampleUser.getName()).isEqualTo("Alice Smith");
        assertThat(sampleUser.getEmail()).isEqualTo("alice.new@example.com");
    }

    @Test
    void updateProfileRejectsEmailAlreadyUsedByAnotherAccount() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));
        when(userRepository.existsByEmail("other@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.updateProfile("alice@example.com",
                new UpdateProfileRequest("Alice", "other@example.com", null, null)))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfileRequiresCurrentPasswordToChangePassword() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> authService.updateProfile("alice@example.com",
                new UpdateProfileRequest("Alice", "alice@example.com", null, "new-password")))
                .hasMessageContaining("Current password is required");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfileChangesPasswordAndReturnsFreshToken() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("old-password", "encoded-secret")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-encoded-secret");
        when(userRepository.save(sampleUser)).thenReturn(sampleUser);
        when(tokenProvider.generateToken("alice@example.com")).thenReturn("fresh-token");

        AuthResponse result = authService.updateProfile("alice@example.com",
                new UpdateProfileRequest("Alice Developer", "alice@example.com", "old-password", "new-password"));

        assertThat(sampleUser.getPassword()).isEqualTo("new-encoded-secret");
        assertThat(result.getToken()).isEqualTo("fresh-token");
    }

    @Test
    void updateProfileRejectsIncorrectCurrentPassword() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrong-password", "encoded-secret")).thenReturn(false);

        assertThatThrownBy(() -> authService.updateProfile("alice@example.com",
                new UpdateProfileRequest("Alice Developer", "alice@example.com", "wrong-password", "new-password")))
                .hasMessageContaining("Current password does not match");
        verify(userRepository, never()).save(any());
    }
}

