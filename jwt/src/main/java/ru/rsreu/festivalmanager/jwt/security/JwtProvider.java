package ru.rsreu.festivalmanager.jwt.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {

    private final SecretKey jwtAccessSecret;

    public JwtProvider(@Value("${jwt.secret.access}") String jwtAccessSecret) {
        this.jwtAccessSecret = Keys.hmacShaKeyFor(jwtAccessSecret.getBytes());
    }

    public String generateAccessToken(Long id, String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("id", id)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1 час
                .signWith(jwtAccessSecret)
                .compact();
    }

    public boolean validateAccessToken(String accessToken) {
        try {
            Jwts.parser().verifyWith(jwtAccessSecret).build().parseSignedClaims(accessToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Claims getAccessClaims(String token) {
        return Jwts.parser().verifyWith(jwtAccessSecret).build().parseSignedClaims(token).getPayload();
    }
}
