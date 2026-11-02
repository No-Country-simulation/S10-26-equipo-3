package com.nautiflow.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtTokenService {
    private static final long TOKEN_LIFETIME_SECONDS = 12 * 60 * 60;
    private final SecretKey signingKey;

    public JwtTokenService(@Value("${nautiflow.jwt.secret}") String encodedSecret) {
        byte[] keyBytes = Decoders.BASE64.decode(encodedSecret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String crearTokenAdministrador(String email) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(email)
                .claim("role", "ADMIN")
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusSeconds(TOKEN_LIFETIME_SECONDS)))
                .signWith(signingKey)
                .compact();
    }

    public Claims validarToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
