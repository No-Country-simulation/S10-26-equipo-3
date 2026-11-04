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

/**
 * Crea y valida los tokens JWT firmados con HMAC usados por el administrador.
 */
@Service
public class JwtTokenService {
    private static final long TOKEN_LIFETIME_SECONDS = 12 * 60 * 60;
    private final SecretKey signingKey;

    /**
     * Decodifica la clave secreta Base64 y prepara la llave HMAC para firmar tokens.
     */
    public JwtTokenService(@Value("${nautiflow.jwt.secret}") String encodedSecret) {
        byte[] keyBytes = Decoders.BASE64.decode(encodedSecret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Genera un token JWT de administrador con vigencia de 12 horas.
     */
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

    /**
     * Valida la firma y la vigencia de un token y devuelve sus claims.
     */
    public Claims validarToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
