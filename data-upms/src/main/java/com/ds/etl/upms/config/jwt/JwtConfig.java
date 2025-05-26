package com.ds.etl.upms.config.jwt;

import io.jsonwebtoken.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    @Value("${jwt.token-secret}")
    private String secret;

    @Value("${jwt.expiration-time}")
    private Long expiration;

    @Value("${jwt.issuer}")
    private String issuer;

    private static final String TYPE = "JWT";

    private static final String ALG = "HS256";

    /**
     * 生成token
     *
     * @param userId
     * @param account
     * @return
     */
    public String createToken(Integer userId, String account) {
        JwtBuilder jwtBuilder = Jwts.builder();
        return jwtBuilder
                .setHeaderParam("type", TYPE)
                .setHeaderParam("alg", ALG)
                .setIssuer(issuer)    // 签发者
                .setAudience("web")   // 接收方
                .setSubject(account)  // 主题
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 过期时间 系统时间加上设置的时间
                .setIssuedAt(new Date())
                .claim("userId", userId)
                .claim("account", account)
                .signWith(SignatureAlgorithm.HS256, secret) // 套用上面头部指定的HS256签名
                .compact();
    }

    /**
     * 解析token
     *
     * @param token
     * @return
     */
    public Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token);
    }

    /**
     * 基础Token校验
     * @param token JWT令牌
     * @return 是否有效
     */
    public boolean checkToken(String token) {
        if (StringUtils.isBlank(token)) {
            return false;
        }

        try {
            // 1. 检查是否符合JWT格式（三段式）
            String[] segments = token.split("\\.");
            if (segments.length != 3) {
                return false;
            }

            // 2. 尝试解析验证（会自动验证签名和过期时间）
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Token格式错误: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("签名验证失败: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("Token验证异常: {}", e.getMessage());
            return false;
        }
    }
}
