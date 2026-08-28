package bti.pds.dinner.auth.application.service;


import bti.pds.dinner.auth.application.input.LoginInput;
import bti.pds.dinner.auth.application.output.UserOutput;
import bti.pds.dinner.auth.domain.RoleRepository;
import bti.pds.dinner.auth.domain.User;
import bti.pds.dinner.auth.domain.UserRepository;
import bti.pds.dinner.auth.domain.exception.LoginNotValidException;
import bti.pds.dinner.auth.domain.exception.RoleNotFoundException;
import bti.pds.dinner.auth.domain.exception.UserNotVerifiedException;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;


@Service
public class LoginService {
    private static final int MAX_FAILED_ATTEMPTS = 10;
    private static final int MINUTES_OF_LOCKOUT = 10;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Transactional(noRollbackFor = LoginNotValidException.class) // don't roll back on ResponseStatusException to allow failed login attempts to be saved
    public UserOutput login(@NonNull LoginInput loginInput) {
        User user = fetchUserOrThrow(loginInput.email());

        var notLockedUser = user.verifyUserNotLocked(LocalDateTime.now(), MINUTES_OF_LOCKOUT);

        if(!notLockedUser.isEnabled()) {
            throw new UserNotVerifiedException("user with the email " +  user.getEmail() + " not verified, please check your email");
        }

        if (!isPasswordValid(notLockedUser, loginInput.password())) {
            handleFailedAttempt(notLockedUser);
        }

        return handleSuccessfulLogin(notLockedUser);
    }

    private @NonNull User fetchUserOrThrow(String email) throws ResponseStatusException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new LoginNotValidException("Invalid e-mail or password"));
    }



    private boolean isPasswordValid(@NonNull User user, String requestPassword) {
        String dbPassword = user.getPassword();
        return passwordEncoder.matches(requestPassword, dbPassword);
    }

    private void handleFailedAttempt(@NonNull User user) throws ResponseStatusException {
        userRepository.save(user.failedLoginAttempt(MAX_FAILED_ATTEMPTS));
        throw new LoginNotValidException("Invalid e-mail or password");
    }

    private @NonNull UserOutput handleSuccessfulLogin(@NonNull User user) {
        User savedUser = userRepository.save(user.successfulLogin());
        var role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new RoleNotFoundException("role of user " + user.getName() + " not found"));
        return new UserOutput(savedUser.getId().uuid(), savedUser.getName(), savedUser.getEmail(), role.getName());
    }
}
