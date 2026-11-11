package bti.pds.dinner.auth.application.service;

import bti.pds.dinner.auth.application.input.RegisterInput;
import bti.pds.dinner.auth.application.output.UserOutput;
import bti.pds.dinner.auth.domain.*;
import bti.pds.dinner.auth.domain.event.OnUserRegisteredEvent;
import bti.pds.dinner.auth.domain.exception.CpfNotValidException;
import bti.pds.dinner.auth.domain.exception.EmailNotValidException;
import bti.pds.dinner.auth.domain.exception.RoleNotFoundException;
import bti.pds.dinner.auth.domain.exception.UserNotFoundException;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class RegisterService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AccountVerificationService accountVerificationService;

    public RegisterService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, ApplicationEventPublisher applicationEventPublisher, AccountVerificationService accountVerificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.accountVerificationService = accountVerificationService;
    }

    @Transactional
    @Observed(name = "auth.user.register")
    public UserOutput register(@NonNull RegisterInput registerInput) {
        checkEmailUniqueness(registerInput.email());
        checkCpfUniqueness(registerInput.cpf());

        Role role = findRoleOrThrow();

        User user = saveUserToDb(registerInput, role);

        publishRegisterEvent(user);

        log.info("User registered successfully. userId={}, role={}", user.getId().uuid(), role.getName());

        return new UserOutput(user.getId().uuid(), user.getName(), user.getEmail(), role.getName());
    }

    private void checkEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration rejected: email already in use. maskedEmail={}", LogMaskUtils.maskEmail(email));
            throw new EmailNotValidException("Email already in use");
        }
    }

    private void checkCpfUniqueness(String cpf) {
        if (userRepository.existsByCpf(cpf)) {
            log.warn("Registration rejected: CPF already exists. maskedCpf={}", LogMaskUtils.maskCpf(cpf));
            throw new CpfNotValidException("CPF already registered in the system");
        }
    }

    private @NonNull Role findRoleOrThrow() {
        return roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> {
                    log.error("Critical configuration error: Default role ROLE_USER not found in the database");
                    return new RoleNotFoundException("Default role not found");
                });
    }

    private User saveUserToDb(@NonNull RegisterInput registerInput, @NonNull Role role) {
        String encodedPassword = passwordEncoder.encode(registerInput.password());
        User newUser = new User(registerInput.name(), registerInput.cpf(), registerInput.email(), encodedPassword, role.getId());
        return userRepository.save(newUser);
    }

    private void publishRegisterEvent(User user) {
        String token = accountVerificationService.createVerificationToken(user);

        OnUserRegisteredEvent event = new OnUserRegisteredEvent(user, token);

        applicationEventPublisher.publishEvent(event);

        log.debug("Published OnUserRegisteredEvent for userId={}", user.getId().uuid());
    }

    @Transactional
    @Observed(name = "auth.user.verify_token")
    public UserOutput verifyToken(String token) {
        UUID userId = accountVerificationService.verifyToken(token);
        User user = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> {
                    log.error("Data inconsistency: Valid verification token points to non-existent user. userId={}", userId);
                    return new UserNotFoundException("User not found after token verification");
                });

        var enabledUser = user.enable();
        User savedUser = userRepository.save(enabledUser);
        var role = roleRepository.findById(savedUser.getRoleId())
                .orElseThrow(() -> {
                    log.error("Data inconsistency: User assigned to non-existent role. userId={}, roleId={}",
                            savedUser.getId().uuid(), savedUser.getRoleId().uuid());
                    return new RoleNotFoundException("Role with the id " + savedUser.getRoleId().uuid() + " not found");
                });

        log.info("User account verified and enabled. userId={}, role={}", user.getId().uuid(), role.getName());

        return new UserOutput(savedUser.getId().uuid(), savedUser.getName(), savedUser.getEmail(), role.getName());
    }

}
