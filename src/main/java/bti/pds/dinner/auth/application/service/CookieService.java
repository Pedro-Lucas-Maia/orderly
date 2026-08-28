package bti.pds.dinner.auth.application.service;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;


@Service
public class CookieService {
    private static final String COOKIE_NAME = "token";
    
    @Value("${app.security.jwt.expiration-seconds:300}")
    private int expirationTime;
    
    private final TokenService tokenService;

    @Value("${app.security.cookie.secure:false}")
    private boolean secure;

    @Value("${app.security.cookie.domain:localhost}")
    private String cookieDomain;

    public CookieService(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public String generateTokenCookie(String email) {
        String token = tokenService.generateToken(email);

        return buildCookie(token, expirationTime).toString();
    }

    public String getCleanCookie() {
        return buildCookie("", 0).toString();
    }

    private @NonNull ResponseCookie buildCookie(String value, int expirationTime) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(expirationTime)
                .sameSite("lax");
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }
        return builder.build();
    }
}
