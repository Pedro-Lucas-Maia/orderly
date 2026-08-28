package bti.pds.dinner.auth.application.service;


import bti.pds.dinner.auth.application.input.LoginInput;
import bti.pds.dinner.auth.application.input.RegisterInput;
import bti.pds.dinner.auth.infrastructure.http.response.AuthResponse;
import bti.pds.dinner.auth.infrastructure.http.response.UserResponse;
import org.springframework.stereotype.Service;

@Service
public class AuthFacadeService {
    private final ProfileService profileService;
    private final LoginService loginService;
    private final CookieService cookieService;
    private final RegisterService registerService;
    private final PasswordResetService passwordResetService;

    public AuthFacadeService(ProfileService profileService, LoginService loginService, CookieService cookieService, RegisterService registerService, PasswordResetService passwordResetService) {
        this.profileService = profileService;
        this.loginService = loginService;
        this.cookieService = cookieService;
        this.registerService = registerService;
        this.passwordResetService = passwordResetService;
    }

    public AuthResponse login(LoginInput loginInput) {
        UserResponse user = UserResponse.from(loginService.login(loginInput));
        return new AuthResponse(user.id(), user.name(), user.email(), user.role(), cookieService.generateTokenCookie(user.email()));
    }

    public String getCleanCookie() {
        return cookieService.getCleanCookie();
    }

    public UserResponse register(RegisterInput registerInput) {
       return UserResponse.from(registerService.register(registerInput));
    }

    public AuthResponse getProfile(String email) {
        UserResponse user = UserResponse.from(profileService.getProfile(email));
        return new AuthResponse(user.id(), user.name(), user.email(), user.role(), cookieService.generateTokenCookie(user.email()));
    }

    public AuthResponse verifyToken(String token) {
        UserResponse userResponse = UserResponse.from(registerService.verifyToken(token));
        return new AuthResponse(userResponse.id(), userResponse.name(), userResponse.email(), userResponse.role(), cookieService.generateTokenCookie(userResponse.email()));
    }

    public void initiatePasswordReset(bti.pds.dinner.auth.application.input.InitiatePasswordResetInput input) {
        passwordResetService.initiatePasswordReset(input);
    }

    public void validatePasswordResetToken(bti.pds.dinner.auth.application.input.VerifyPasswordResetTokenInput input) {
        passwordResetService.verifyToken(input);
    }

    public void confirmPasswordReset(bti.pds.dinner.auth.application.input.ConfirmPasswordResetInput input) {
        passwordResetService.confirmPasswordReset(input);
    }
}
