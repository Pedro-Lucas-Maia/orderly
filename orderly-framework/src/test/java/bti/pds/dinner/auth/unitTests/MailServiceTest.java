package bti.pds.dinner.auth.unitTests;

import bti.pds.dinner.auth.application.service.MailService;
import bti.pds.dinner.auth.domain.Role;
import bti.pds.dinner.auth.domain.RoleId;
import bti.pds.dinner.auth.domain.User;
import bti.pds.dinner.auth.domain.UserId;
import bti.pds.dinner.auth.infrastructure.mail.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class MailServiceTest {

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private MailService mailService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleUser = User.builder()
                .id(new UserId())
                .name("Pedro")
                .email("pedro@orderly.com")
                .password("securePassword123")
                .roleId(new RoleId())
                .locked(false)
                .enabled(false)
                .failedLoginAttempts(0)
                .build();
    }

    @Test
    @DisplayName("Should send verification email with Orderly template and link")
    void testSendVerificationEmail() {
        String token = "verif-token-123";

        mailService.sendVerificationEmail(sampleUser, token);

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailSender).sendEmail(emailCaptor.capture(), subjectCaptor.capture(), contentCaptor.capture());

        assertEquals("pedro@orderly.com", emailCaptor.getValue());
        assertTrue(subjectCaptor.getValue().contains("Orderly"));
        String html = contentCaptor.getValue();
        assertTrue(html.contains("Pedro"));
        assertTrue(html.contains("http://localhost:3000/verificar?token=verif-token-123"));
        assertTrue(html.contains("Orderly"));
        assertTrue(html.contains("#673b26"));
        assertTrue(html.contains("Verificar Minha Conta"));
    }

    @Test
    @DisplayName("Should send reset password email with Orderly template and link")
    void testSendResetPasswordEmail() {
        String token = "reset-token-456";

        mailService.sendResetPasswordEmail(sampleUser, token);

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailSender).sendEmail(emailCaptor.capture(), subjectCaptor.capture(), contentCaptor.capture());

        assertEquals("pedro@orderly.com", emailCaptor.getValue());
        assertTrue(subjectCaptor.getValue().contains("Orderly"));
        String html = contentCaptor.getValue();
        assertTrue(html.contains("Pedro"));
        assertTrue(html.contains("http://localhost:3000/password-reset/validate?token=reset-token-456"));
        assertTrue(html.contains("Orderly"));
        assertTrue(html.contains("#673b26"));
        assertTrue(html.contains("Redefinir Minha Senha"));
    }
}
