package com.example.WellLogCurveSystem.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

    private static final String DEFAULT_SECRET = "2026_secret_key_that_is_long_enough_for_security";

    // 生成安全的密钥，长度至少256位
    private static final SecretKey DEFAULT_SECRET_KEY = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);

    public static String generateToken(Map<String, Object> claims, Integer expireHours, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        Date expireTime = new Date(System.currentTimeMillis() + expireHours * 60 * 60 * 1000L);
        return Jwts.builder()
                .setClaims(claims)
                .signWith(key)
                .setExpiration(expireTime)
                .compact();
    }

    public static String generateToken(Map<String, Object> claims) {
        // 使用足够长的安全密钥
        return generateToken(claims, 24, DEFAULT_SECRET);
    }

    public static Claims parseToken(String token, String secret) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static Claims parseToken(String token) {
        return parseToken(token, DEFAULT_SECRET);
    }
}
