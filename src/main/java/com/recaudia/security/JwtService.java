package com.recaudia.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    public Long extractEmpresaId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("empresaId", Long.class);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(String email, Long userId, Long empresaId, String rol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("empresaId", empresaId);
        claims.put("rol", rol);
        claims.put("tipo", "TENANT");
        return buildToken(claims, email, jwtExpiration);
    }

    public String generateRefreshToken(String email, Long userId, Long empresaId, String rol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("empresaId", empresaId);
        claims.put("rol", rol);
        claims.put("tipo", "REFRESH");
        return buildToken(claims, email, refreshExpiration);
    }

    public boolean isRefreshTokenValid(String token, String email) {
        try {
            return "REFRESH".equals(extractTipo(token))
                    && email.equals(extractUsername(token))
                    && !isTokenExpired(token);
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractTipo(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("tipo", String.class);
    }

    public String extractRol(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("rol", String.class);
    }

    public String generateMasterToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tipo", "MASTER");
        claims.put("rol", "MASTER");
        return buildToken(claims, email, jwtExpiration);
    }

    public boolean isTokenValid(String token, String email) {
        final String username = extractUsername(token);
        return (username.equals(email)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
