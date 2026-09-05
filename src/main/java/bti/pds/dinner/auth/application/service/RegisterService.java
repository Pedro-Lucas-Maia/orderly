package bti.pds.dinner.auth.application.service;

import bti.pds.dinner.auth.application.input.RegisterInput;
import bti.pds.dinner.auth.application.output.UserOutput;
import bti.pds.dinner.auth.domain.*;
import bti.pds.dinner.auth.domain.event.OnUserRegisteredEvent;
import bti.pds.dinner.auth.domain.exception.EmailNotValidException;
import bti.pds.dinner.auth.domain.exception.RoleNotFoundException;
import bti.pds.dinner.auth.domain.exception.UserNotFoundException;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
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
    public UserOutput register(@NonNull RegisterInput registerInput) {
        checkEmailUniqueness(registerInput.email());

        Role role = findRoleOrThrow();

        User user = saveUserToDb(registerInput, role);

        publishRegisterEvent(user);

        return new UserOutput(user.getId().uuid(), user.getName(), user.getEmail(), role.getName());
    }

    private void checkEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailNotValidException("Email already in use");
        }
    }

    private @NonNull Role findRoleOrThrow() {
        return roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("Default role not found"));
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
    }

    @Transactional
    public UserOutput verifyToken(String token) {
        UUID userId = accountVerificationService.verifyToken(token);
        User user = userRepository.findById(new UserId(userId))
                .orElseThrow(() -> new UserNotFoundException("User not found after token verification"));

        var enabledUser = user.enable();
        User savedUser = userRepository.save(enabledUser);
        var role = roleRepository.findById(savedUser.getRoleId())
                .orElseThrow(() -> new RoleNotFoundException("Role with the id " + savedUser.getRoleId().uuid() + " not found"));

        return new UserOutput(savedUser.getId().uuid(), savedUser.getName(), savedUser.getEmail(), role.getName());
    }
}
