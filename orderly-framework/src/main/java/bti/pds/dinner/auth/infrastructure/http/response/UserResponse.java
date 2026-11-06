package bti.pds.dinner.auth.infrastructure.http.response;

import bti.pds.dinner.auth.application.output.UserOutput;
import org.jspecify.annotations.NonNull;


public record UserResponse(String id, String name, String email, String role) {
    public static UserResponse from(@NonNull UserOutput userOutput) {
        return new UserResponse(
                userOutput.id().toString(),
                userOutput.name(),
                userOutput.email(),
                userOutput.role()
        );
    }
}
