package com.nautiflow.service;

import org.springframework.scheduling.annotation.Scheduled;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class JwtTokenRevocationService {
    private final ConcurrentMap<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    public boolean estaRevocado(String token) {
        String tokenHash = hash(token);
        Instant expiresAt = revokedTokens.get(tokenHash);
        if (expiresAt == null) {
            return false;
        }
        if (!expiresAt.isAfter(Instant.now())) {
            revokedTokens.remove(tokenHash, expiresAt);
            return false;
        }
        return true;
    }

    public boolean revocar(String token, Instant expiresAt) {
        if (token == null || token.isBlank() || expiresAt == null || !expiresAt.isAfter(Instant.now())) {
            return false;
        }
        return revokedTokens.putIfAbsent(hash(token), expiresAt) == null;
    }

    @Scheduled(fixedDelay = 60_000)
    void limpiarExpirados() {
        limpiarExpirados(Instant.now());
    }

    void limpiarExpirados(Instant ahora) {
        revokedTokens.entrySet().removeIf(entry -> !entry.getValue().isAfter(ahora));
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 no está disponible.", exception);
        }
    }
}
