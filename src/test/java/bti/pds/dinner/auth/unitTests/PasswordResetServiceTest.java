package bti.pds.dinner.auth.unitTests;

import bti.pds.dinner.auth.application.input.ConfirmPasswordResetInput;
import bti.pds.dinner.auth.application.input.InitiatePasswordResetInput;
import bti.pds.dinner.auth.application.input.VerifyPasswordResetTokenInput;
import bti.pds.dinner.auth.application.service.PasswordResetService;
import bti.pds.dinner.auth.domain.*;
import bti.pds.dinner.auth.domain.event.OnPasswordResetedEvent;
import bti.pds.dinner.auth.domain.exception.TokenNotValidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PasswordResetService passwordResetService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleUser = User.builder()
                .id(new UserId())
                .name("Pedro")
                .email("pedro@orderly.com")
                .password("encodedOldPassword")
                .roleId(new RoleId())
                .locked(false)
                .enabled(true)
                .failedLoginAttempts(0)
                .build();
    }

    @Test
    @DisplayName("Should initiate password reset: delete old tokens, save new token and publish event")
    void testInitiatePasswordResetSuccess() {
        when(userRepository.findByEmail("pedro@orderly.com")).thenReturn(Optional.of(sampleUser));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        passwordResetService.initiatePasswordReset(new InitiatePasswordResetInput("pedro@orderly.com"));

        verify(passwordResetTokenRepository).deleteByUserId(sampleUser.getId());

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());

        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertNotNull(savedToken.getToken());
        assertEquals(sampleUser.getId(), savedToken.getUserId());
        assertTrue(savedToken.getExpiryDate().isAfter(LocalDateTime.now()));

        ArgumentCaptor<OnPasswordResetedEvent> eventCaptor = ArgumentCaptor.forClass(OnPasswordResetedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(savedToken.getToken(), eventCaptor.getValue().token());
        assertEquals(sampleUser, eventCaptor.getValue().user());
    }

    @Test
    @DisplayName("Should not initiate password reset if user is not found")
    void testInitiatePasswordResetUserNotFound() {
        when(userRepository.findByEmail("naoexiste@orderly.com")).thenReturn(Optional.empty());

        passwordResetService.initiatePasswordReset(new InitiatePasswordResetInput("naoexiste@orderly.com"));

        verify(passwordResetTokenRepository, never()).deleteByUserId(any());
        verify(passwordResetTokenRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should verify valid token successfully")
    void testVerifyTokenSuccess() {
        String tokenStr = UUID.randomUUID().toString();
        PasswordResetToken validToken = new PasswordResetToken(
                tokenStr,
                sampleUser.getId(),
                LocalDateTime.now().plusHours(1)
        );

        when(passwordResetTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(validToken));

        assertDoesNotThrow(() -> passwordResetService.verifyToken(new VerifyPasswordResetTokenInput(tokenStr)));
    }

    @Test
    @DisplayName("Should throw TokenNotValidException when token does not exist in repository")
    void testVerifyTokenNotFound() {
        String tokenStr = "invalid-token";
        when(passwordResetTokenRepository.findByToken(tokenStr)).thenReturn(Optional.empty());

        assertThrows(TokenNotValidException.class, () ->
                passwordResetService.verifyToken(new VerifyPasswordResetTokenInput(tokenStr))
        );
    }

    @Test
    @DisplayName("Should throw TokenNotValidException when token has expired")
    void testVerifyTokenExpired() {
        String tokenStr = UUID.randomUUID().toString();
        PasswordResetToken expiredToken = new PasswordResetToken(
                tokenStr,
                sampleUser.getId(),
                LocalDateTime.now().minusMinutes(5)
        );

        when(passwordResetTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(expiredToken));

        assertThrows(TokenNotValidException.class, () ->
                passwordResetService.verifyToken(new VerifyPasswordResetTokenInput(tokenStr))
        );
    }

    @Test
    @DisplayName("Should confirm password reset, update user password and delete used token")
    void testConfirmPasswordResetSuccess() {
        String tokenStr = UUID.randomUUID().toString();
        PasswordResetToken validToken = new PasswordResetToken(
                tokenStr,
                sampleUser.getId(),
                LocalDateTime.now().plusHours(1)
        );

        when(passwordResetTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(validToken));
        when(userRepository.findById(sampleUser.getId())).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.encode("NovaSenha123")).thenReturn("encodedNewPassword");

        passwordResetService.confirmPasswordReset(new ConfirmPasswordResetInput(tokenStr, "NovaSenha123"));

        verify(passwordEncoder).encode("NovaSenha123");
        verify(userRepository).save(sampleUser);
        verify(passwordResetTokenRepository).delete(validToken);
    }

    @Test
    @DisplayName("Should throw TokenNotValidException during confirmation if token is expired")
    void testConfirmPasswordResetExpiredToken() {
        String tokenStr = UUID.randomUUID().toString();
        PasswordResetToken expiredToken = new PasswordResetToken(
                tokenStr,
                sampleUser.getId(),
                LocalDateTime.now().minusMinutes(1)
        );

        when(passwordResetTokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(expiredToken));

        assertThrows(TokenNotValidException.class, () ->
                passwordResetService.confirmPasswordReset(new ConfirmPasswordResetInput(tokenStr, "NovaSenha123"))
        );

        verify(userRepository, never()).save(any());
        verify(passwordResetTokenRepository, never()).delete(any());
    }
}
