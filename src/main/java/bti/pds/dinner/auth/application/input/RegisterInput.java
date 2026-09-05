package bti.pds.dinner.auth.application.input;

public record RegisterInput(
    String name,
    String cpf,
    String email,
    String password
) {
}
