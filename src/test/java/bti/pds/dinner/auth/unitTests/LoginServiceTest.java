package bti.pds.dinner.auth.unitTests;

import bti.pds.dinner.auth.application.input.LoginInput;
import bti.pds.dinner.auth.application.output.UserOutput;
import bti.pds.dinner.auth.application.service.LoginService;
import bti.pds.dinner.auth.domain.*;
import bti.pds.dinner.auth.domain.exception.LoginNotValidException;
import bti.pds.dinner.auth.domain.exception.UserLockedException;
import bti.pds.dinner.auth.domain.exception.UserNotVerifiedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LoginServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginService loginService;

    private LoginInput loginInput;
    private Role role;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        this.loginInput = new LoginInput("joaozinho@gmail.com", "12345678");
        this.role = new Role("ROLE_USER");
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    public void testValidLogin() {
        User user = createDefaultUser();

        when(userRepository.findByEmail("joaozinho@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), eq("12345678"))).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(role));

        UserOutput userOutput = loginService.login(loginInput);

        verify(userRepository, times(1)).findByEmail(loginInput.email());
        verify(passwordEncoder, times(1)).matches(any(), eq(loginInput.password()));
        verify(userRepository, times(1)).save(any(User.class));
        verify(roleRepository, times(1)).findById(user.getRoleId());
        Assertions.assertEquals(0, user.getFailedLoginAttempts());
        Assertions.assertNotNull(user.getLastLoginAt());
        Assertions.assertEquals(user.getEmail(), userOutput.email());
        Assertions.assertEquals(user.getName(), userOutput.name());
        Assertions.assertEquals(role.getName(), userOutput.role());
    }

    @Test
    @DisplayName("Should throw if user don't exist in the database")
    public void testUserNotFound() {
        when(userRepository.findByEmail(loginInput.email())).thenReturn(Optional.empty());

        Assertions.assertThrows(LoginNotValidException.class, () -> loginService.login(loginInput));

        verify(userRepository, times(1)).findByEmail(loginInput.email());
    }

    @Test
    @DisplayName("Should throw if password is invalid")
    public void testInvalidPassword() {
        User user = createDefaultUser();

        when(userRepository.findByEmail(loginInput.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), eq("12345678"))).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        Assertions.assertThrows(LoginNotValidException.class, () -> loginService.login(loginInput));
    }

    @Test
    @DisplayName("Should throw if account is not verified")
    public void testAccountNotVerified() {
        User user = createDefaultUser();
        User unverifiedUser = User.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .roleId(user.getRoleId())
                .locked(false)
                .enabled(false)
                .build();

        when(userRepository.findByEmail(loginInput.email())).thenReturn(Optional.of(unverifiedUser));
        when(passwordEncoder.matches(any(), eq("12345678"))).thenReturn(true);

        Assertions.assertThrows(UserNotVerifiedException.class, () -> loginService.login(loginInput));
    }

    @Test
    @DisplayName("Should throw if account is locked")
    public void testAccountLocked() {
        User user = createDefaultUser();
        User lockedUser = User.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .roleId(user.getRoleId())
                .locked(true)
                .enabled(true)
                .lockoutMoment(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail(loginInput.email())).thenReturn(Optional.of(lockedUser));
        when(passwordEncoder.matches(any(), eq("12345678"))).thenReturn(true);

        Assertions.assertThrows(UserLockedException.class, () -> loginService.login(loginInput));
    }

    @Test
    @DisplayName("Should lock account after 10 failed login attempts")
    public void testAccountLockAfterFailedAttempts() {
        User user = createDefaultUser();
        User nearLockedUser = User.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .roleId(user.getRoleId())
                .locked(false)
                .enabled(true)
                .failedLoginAttempts(9)
                .build();

        when(userRepository.findByEmail(loginInput.email())).thenReturn(Optional.of(nearLockedUser));
        when(passwordEncoder.matches(any(), eq("12345678"))).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        Assertions.assertThrows(LoginNotValidException.class, () -> loginService.login(loginInput));
        Assertions.assertTrue(nearLockedUser.isLocked(), "User should be locked after 10 failed attempts");
        Assertions.assertEquals(10, nearLockedUser.getFailedLoginAttempts());
        Assertions.assertNotNull(nearLockedUser.getLockoutMoment());
        verify(userRepository, times(1)).save(nearLockedUser);
    }

    @Test
    @DisplayName("Should unlock account and login successfully if lockout time has expired")
    public void testLockoutExpired() {
        User user = createDefaultUser();
        User lockedButExpiredUser = User.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .roleId(user.getRoleId())
                .locked(true)
                .enabled(true)
                .lockoutMoment(LocalDateTime.now().minusMinutes(20))
                .build();

        when(userRepository.findByEmail(loginInput.email())).thenReturn(Optional.of(lockedButExpiredUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        when(roleRepository.findById(any(RoleId.class))).thenReturn(Optional.of(role));

        loginService.login(loginInput);

        Assertions.assertFalse(lockedButExpiredUser.isLocked());
        Assertions.assertNull(lockedButExpiredUser.getLockoutMoment());

        verify(userRepository, times(1)).save(lockedButExpiredUser);
    }


    private User createDefaultUser() {
        return User.builder()
                .id(new UserId())
                .name("Joãozinho")
                .email("joaozinho@gmail.com")
                .password("12345678")
                .roleId(new RoleId())
                .locked(false)
                .enabled(true)
                .failedLoginAttempts(0)
                .build();
    }
}
