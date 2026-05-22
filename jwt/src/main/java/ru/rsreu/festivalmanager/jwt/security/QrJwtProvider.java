package ru.rsreu.festivalmanager.jwt.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class QrJwtProvider {

    private final SecretKey qrSecret;
    private static final long EXPIRATION_MS = 15 * 60 * 1000;

    public QrJwtProvider(@Value("${jwt.secret.access}") String secret) {
        this.qrSecret = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateQrToken(Map<String, Object> claims, Long subjectId) {
        return Jwts.builder()
                .claims(claims)
                .subject(subjectId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(qrSecret)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser().verifyWith(qrSecret).build().parseSignedClaims(token).getPayload();
    }
}