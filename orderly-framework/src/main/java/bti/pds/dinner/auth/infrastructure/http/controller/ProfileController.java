package bti.pds.dinner.auth.infrastructure.http.controller;


import bti.pds.dinner.auth.application.service.AuthFacadeService;
import bti.pds.dinner.auth.application.service.ProfileService;
import bti.pds.dinner.auth.infrastructure.http.request.DeleteProfileRequest;
import bti.pds.dinner.auth.infrastructure.http.request.UpdateNameRequest;
import bti.pds.dinner.auth.infrastructure.http.request.UpdatePasswordRequest;
import bti.pds.dinner.auth.infrastructure.http.response.AuthResponse;
import bti.pds.dinner.auth.infrastructure.http.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profileService;
    private final AuthFacadeService authFacadeService;

    public ProfileController(ProfileService profileService, AuthFacadeService authFacadeService) {
        this.profileService = profileService;
        this.authFacadeService = authFacadeService;
    }

    @GetMapping()
    public ResponseEntity<UserResponse> getProfile(Authentication authentication) {
        AuthResponse authResponse = authFacadeService.getProfile(authentication.getName());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authResponse.cookie())
                .body(new UserResponse(authResponse.id(), authResponse.name(), authResponse.email(), authResponse.role()));
    }

    @PatchMapping("/name")
    public ResponseEntity<UserResponse> updateProfileName(@RequestBody @Valid UpdateNameRequest updateNameRequest, Authentication authentication) {
        UserResponse userResponse = UserResponse.from(profileService.updateProfileName(UpdateNameRequest.toInput(updateNameRequest, authentication.getName())));

        return ResponseEntity.ok()
                .body(userResponse);
    }

    @PatchMapping("/password")
    public ResponseEntity<UserResponse> updateProfilePassword(@RequestBody @Valid UpdatePasswordRequest updatePasswordRequest, Authentication authentication) {
         UserResponse userResponse = UserResponse.from(profileService.updateProfilePassword(UpdatePasswordRequest.toInput(updatePasswordRequest, authentication.getName())));

         return ResponseEntity.ok()
                 .body(userResponse);
    }

    @DeleteMapping()
    public ResponseEntity<Void> deleteProfile(@RequestBody @Valid DeleteProfileRequest deleteProfileRequest, Authentication authentication) {
        profileService.deleteProfile(DeleteProfileRequest.toInput(deleteProfileRequest, authentication.getName()));

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
