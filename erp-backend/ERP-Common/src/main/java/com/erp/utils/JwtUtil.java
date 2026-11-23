package com.erp.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    /**
     * generate jwt
     * @param secretKey signing secret (at least 32 bytes, checked at startup)
     * @param ttlMillis jwt expiry time (ms)
     * @param claims    message
     * @return signed token
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        Date exp = new Date(System.currentTimeMillis() + ttlMillis);

        return Jwts.builder()
                .claims(claims)
                // HS256 explicitly: signWith(key) alone would pick the algorithm from the key length
                .signWith(toKey(secretKey), Jwts.SIG.HS256)
                .expiration(exp)
                .compact();
    }

    /**
     * Token decoding: throws if the signature is wrong or the token has expired
     *
     * @param secretKey signing secret
     * @param token     token from the request header
     * @return the token's claims
     */
    public static Claims parseJWT(String secretKey, String token) {
        return Jwts.parser()
                .verifyWith(toKey(secretKey))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Same bytes as before the jjwt upgrade, so tokens issued earlier stay valid
    private static SecretKey toKey(String secretKey) {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
