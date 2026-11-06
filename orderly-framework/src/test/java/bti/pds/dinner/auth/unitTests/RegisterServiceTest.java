package bti.pds.dinner.auth.unitTests;

import bti.pds.dinner.auth.application.input.RegisterInput;
import bti.pds.dinner.auth.application.output.UserOutput;
import bti.pds.dinner.auth.application.service.AccountVerificationService;
import bti.pds.dinner.auth.application.service.RegisterService;
import bti.pds.dinner.auth.domain.*;
import bti.pds.dinner.auth.domain.event.OnUserRegisteredEvent;
import bti.pds.dinner.auth.domain.exception.EmailNotValidException;
import bti.pds.dinner.auth.domain.exception.RoleNotFoundException;
import bti.pds.dinner.auth.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

public class RegisterServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private AccountVerificationService accountVerificationService;

    @InjectMocks
    private RegisterService registerService;

    private RegisterInput registerInput;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.registerInput = new RegisterInput("Joãozinho", "123.456.789-09", "joaozinho@gmail.com", "12345678");
    }

    @Test
    @DisplayName("Should successfully register with valid credentials")
    public void testValidRegister() {
        User targetUser = createUser();
        Role role = new Role("ROLE_USER");
        
        when(userRepository.existsByEmail("joaozinho@gmail.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("12345678")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(targetUser);
        when(accountVerificationService.createVerificationToken(any(User.class))).thenReturn("MockedToken");

        UserOutput userOutput = registerService.register(registerInput);

        verify(userRepository, times(1)).existsByEmail(registerInput.email());
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(registerInput.password());
        verify(roleRepository, times(1)).findByName("ROLE_USER");
        verify(accountVerificationService, times(1)).createVerificationToken(any(User.class));
        verify(applicationEventPublisher, times(1)).publishEvent(any(OnUserRegisteredEvent.class));

        Assertions.assertEquals(registerInput.email(), userOutput.email());
        Assertions.assertEquals(registerInput.name(), userOutput.name());
        Assertions.assertEquals("ROLE_USER", userOutput.role());
    }

    @Test
    @DisplayName("Should throw if email is already in use")
    public void testEmailAlreadyInUse() {
        when(userRepository.existsByEmail(registerInput.email())).thenReturn(true);

        Assertions.assertThrows(EmailNotValidException.class, () -> registerService.register(registerInput));

        verify(userRepository, times(1)).existsByEmail(registerInput.email());
    }

    @Test
    @DisplayName("Should throw if default role is not found in the database")
    public void testDefaultRoleNotFound() {
        when(userRepository.existsByEmail(registerInput.email())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        Assertions.assertThrows(RoleNotFoundException.class, () -> registerService.register(registerInput));

        verify(roleRepository, times(1)).findByName("ROLE_USER");
    }

    @Test
    @DisplayName("Should validate token and enable user")
    public void testValidateTokenAndEnableUser() {
        String token = UUID.randomUUID().toString();
        User targetUser = createUser();
        Role role = new Role("ROLE_USER");

        when(accountVerificationService.verifyToken(token)).thenReturn(targetUser.getId().uuid());
        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(targetUser));
        when(userRepository.save(any(User.class))).thenReturn(targetUser);
        when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(role));

        registerService.verifyToken(token);

        verify(userRepository, times(1)).findById(any(UserId.class));
        verify(accountVerificationService, times(1)).verifyToken(token);
        verify(userRepository, times(1)).save(targetUser);
        verify(roleRepository, times(1)).findById(any(RoleId.class));

        Assertions.assertTrue(targetUser.isEnabled());
    }

    @Test
    @DisplayName("Should throw if can't find the user by it's id")
    public void testFindUserById() {
        String token = UUID.randomUUID().toString();
        when(accountVerificationService.verifyToken(token)).thenReturn(UUID.randomUUID());
        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.empty());

        Assertions.assertThrows(UserNotFoundException.class, () -> registerService.verifyToken(token));

        verify(userRepository, times(1)).findById(any(UserId.class));
        verify(accountVerificationService, times(1)).verifyToken(token);
    }

    private User createUser() {
        return User.builder()
                .id(new UserId())
                .name("Joãozinho")
                .email("joaozinho@gmail.com")
                .password("encodedPassword")
                .roleId(new RoleId())
                .locked(false)
                .enabled(false)
                .failedLoginAttempts(0)
                .build();
    }
}
