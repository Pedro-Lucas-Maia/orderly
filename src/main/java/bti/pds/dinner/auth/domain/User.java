package bti.pds.dinner.auth.domain;


import bti.pds.dinner.auth.domain.exception.UserLockedException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class User {
    private UserId id;
    private String name;
    private String email;
    private String password;
    private RoleId roleId;

    private boolean locked;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private int failedLoginAttempts;
    private LocalDateTime lockoutMoment;

    public User(String name, String email, String password, RoleId roleId) {
        this.id = new UserId();
        this.name = name;
        this.email = email;
        this.password = password;
        this.roleId = roleId;
        this.locked = false;
        this.enabled = false;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastLoginAt = null;
        this.failedLoginAttempts = 0;
        this.lockoutMoment = null;
    }
    public User enable() {
        this.enabled = true;
        return this;
    }

    public User verifyUserNotLocked(LocalDateTime reference, int minutesOfLockout) {
        if (this.isLocked()) {
            if (this.getLockoutMoment() != null && this.getLockoutMoment().plusMinutes(minutesOfLockout).isBefore(reference)) {
                this.locked = false;
                this.failedLoginAttempts = 0;
                this.lockoutMoment = null;

                return this;
            } else {
                throw new UserLockedException("user with email " + this.getEmail() + " is locked, please wait");
            }
        }
        return this;
    }

    public User failedLoginAttempt(int maxFailedAttempts) {
        this.failedLoginAttempts = this.getFailedLoginAttempts() + 1;
        if (this.getFailedLoginAttempts() >=maxFailedAttempts) {
            this.locked = true;
            this.lockoutMoment = LocalDateTime.now();
        }
        return this;
    }

    public User successfulLogin() {
        this.failedLoginAttempts = 0;
        this.lastLoginAt = LocalDateTime.now();
        return this;
    }

    public User updateName(String newName) {
        if (this.name.equals(newName)) {
            throw new bti.pds.dinner.auth.domain.exception.InvalidProfileUpdateException("New name cannot be the same as the current name");
        }
        this.name = newName;
        this.updatedAt = LocalDateTime.now();
        return this;
    }

    public User updatePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
        this.updatedAt = LocalDateTime.now();
        return this;
    }
}
