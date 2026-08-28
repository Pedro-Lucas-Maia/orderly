package bti.pds.dinner.auth.application.output;

import java.util.UUID;

public record UserOutput (
        UUID id,
        String name,
        String email,
        String role
){
}
