package com.doan.WEB_TMDT.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret; // chuỗi dài (>=32 bytes)

    @Value("${app.jwt.expiration-ms:86400000}")
    private long expirationMs; // 24h mặc định

    private Key getSigningKey() {
        // secret có thể là plain text → encode base64 để đảm bảo đủ bytes cho HMAC-SHA
        byte[] keyBytes = Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(secret.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // ====== Generate ======
    public String generateToken(String subjectEmail, Map<String, Object> claims) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subjectEmail)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ====== Extract ======
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Claims extractAllClaims(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    // ====== Validate ======
    public boolean isTokenStructurallyValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // overload KHÁC với hàm trên: vừa đúng user vừa không hết hạn
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String email = extractEmail(token);
            Date exp = extractClaim(token, Claims::getExpiration);
            return email.equalsIgnoreCase(userDetails.getUsername()) && exp.after(new Date());
        } catch (JwtException e) {
            return false;
        }
    }
}
