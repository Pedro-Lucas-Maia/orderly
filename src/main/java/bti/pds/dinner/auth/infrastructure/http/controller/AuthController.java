package bti.pds.dinner.auth.infrastructure.http.controller;

import bti.pds.dinner.auth.application.service.AuthFacadeService;
import bti.pds.dinner.auth.infrastructure.http.request.ForgotPasswordRequest;
import bti.pds.dinner.auth.infrastructure.http.request.LoginRequest;
import bti.pds.dinner.auth.infrastructure.http.request.RegisterRequest;
import bti.pds.dinner.auth.infrastructure.http.request.ResetPasswordRequest;
import bti.pds.dinner.auth.infrastructure.http.response.AuthResponse;
import bti.pds.dinner.auth.infrastructure.http.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthFacadeService authFacadeService;

    public AuthController(AuthFacadeService authFacadeService) {
        this.authFacadeService = authFacadeService;
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authFacadeService.login(LoginRequest.toInput(loginRequest));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authResponse.cookie())
                .body(new UserResponse(authResponse.id(), authResponse.name(), authResponse.email(), authResponse.role()));
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        String responseCookie = authFacadeService.getCleanCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie)
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserResponse userResponse = authFacadeService.register(RegisterRequest.toInput(registerRequest));

        return  ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    @GetMapping("/verify")
    public ResponseEntity<UserResponse> verify(@RequestParam("token") String token) {
      AuthResponse authResponse = authFacadeService.verifyToken(token);
      return ResponseEntity.ok()
              .header(HttpHeaders.SET_COOKIE, authResponse.cookie())
              .body(new UserResponse(authResponse.id(), authResponse.name(), authResponse.email(), authResponse.role()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        authFacadeService.initiatePasswordReset(ForgotPasswordRequest.toInput(forgotPasswordRequest));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/password-reset/validate")
    public ResponseEntity<UserResponse> validatePasswordReset(@RequestParam("token") String token) {
        authFacadeService.validatePasswordResetToken(new bti.pds.dinner.auth.application.input.VerifyPasswordResetTokenInput(token));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        authFacadeService.confirmPasswordReset(ResetPasswordRequest.toInput(resetPasswordRequest));
        return ResponseEntity.ok().build();
    }
}
