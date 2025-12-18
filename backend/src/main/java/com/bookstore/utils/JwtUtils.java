package com.bookstore.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类，用于生成和验证token
 */
@Component
public class JwtUtils {

    // 密钥，用于签名JWT
    private static final String SECRET_KEY = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
    
    // token过期时间：24小时
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24;

    /**
     * 从token中获取用户名
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * 从token中获取指定的声明
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 生成token，使用默认的过期时间
     */
    public String generateToken(String username) {
        return generateToken(new HashMap<>(), username);
    }

    /**
     * 生成token，可以添加额外的声明
     */
    public String generateToken(Map<String, Object> extraClaims, String username) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignInKey()) // 只需要传入密钥，不需要指定算法，JJWT会自动选择合适的算法
                .compact();
    }

    /**
     * 验证token是否有效
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            // 捕获JWT解析异常，视为无效token
            return false;
        }
    }

    /**
     * 检查token是否过期
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * 从token中获取过期时间
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * 从token中获取所有声明
     */
    private Claims extractAllClaims(String token) {
        try {
            SecretKey signingKey = getSignInKey();
            return Jwts.parser()
                    .verifyWith(signingKey) // 使用verifyWith方法替代setSigningKey方法
                    .build()
                    .parseSignedClaims(token) // 使用parseSignedClaims方法替代parseClaimsJws方法
                    .getPayload(); // 使用getPayload方法替代getBody方法
        } catch (Exception e) {
            // 捕获JWT解析异常，视为无效token
            throw e;
        }
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
