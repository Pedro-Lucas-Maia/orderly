package bti.pds.dinner.auth.application.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TokenService {
    @Value("${app.security.jwt.expiration-seconds:300}")
    private int expirationTime;
    
    private final JwtEncoder jwtEncoder;

    public TokenService(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(String email) {
        String token;
        try{
            var now = Instant.now();
            var claims = JwtClaimsSet.builder()
                    .issuer("PedroMaia Auth Server")
                    .subject(email)
                    .issuedAt(now)
                    .expiresAt(now.plusSeconds(expirationTime))
                    .build();
            token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        } catch (
                JwtException e) {
            throw new RuntimeException("Error while generating JWT Token", e);
        }
        return token;
    }
}
