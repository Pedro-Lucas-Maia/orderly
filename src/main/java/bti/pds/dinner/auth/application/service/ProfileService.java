package bti.pds.dinner.auth.application.service;

import bti.pds.dinner.auth.application.input.DeleteProfileInput;
import bti.pds.dinner.auth.application.input.UpdateNameInput;
import bti.pds.dinner.auth.application.input.UpdatePasswordInput;
import bti.pds.dinner.auth.application.output.UserOutput;
import bti.pds.dinner.auth.domain.RoleRepository;
import bti.pds.dinner.auth.domain.User;
import bti.pds.dinner.auth.domain.UserRepository;
import bti.pds.dinner.auth.domain.exception.InvalidPasswordException;
import bti.pds.dinner.auth.domain.exception.RoleNotFoundException;
import bti.pds.dinner.auth.domain.exception.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserOutput getProfile(String email) {
        User user = fetchUserOrThrow(email);
        return toUserOutput(user);
    }

    @Transactional
    public UserOutput updateProfileName(UpdateNameInput input) {
        User user = fetchUserOrThrow(input.email());

        userRepository.save(user.updateName(input.name()));

        return toUserOutput(user);
    }

    @Transactional
    public UserOutput updateProfilePassword(UpdatePasswordInput input) {
        User user = fetchUserOrThrow(input.email());

        if (!passwordEncoder.matches(input.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Old password is incorrect");
        }
        if (passwordEncoder.matches(input.newPassword(), user.getPassword())) {
            throw new InvalidPasswordException("New password cannot be the same");
        }

        userRepository.save(user.updatePassword(passwordEncoder.encode(input.newPassword())));

        return toUserOutput(user);
    }

    @Transactional
    public void deleteProfile(DeleteProfileInput input) {
        User user = fetchUserOrThrow(input.email());

        if (!passwordEncoder.matches(input.password(), user.getPassword())) {
            throw new InvalidPasswordException("Password is incorrect");
        }

        userRepository.delete(user);
    }

    private User fetchUserOrThrow(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private UserOutput toUserOutput(User user) {
        var role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new RoleNotFoundException("role of user " + user.getName() + " not found"));
        return new UserOutput(user.getId().uuid(), user.getName(), user.getEmail(), role.getName());
    }
}
