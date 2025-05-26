package com.ds.etl.collect.config.jwt;

import com.mysql.cj.util.StringUtils;
import io.jsonwebtoken.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    // Secret
    private String tokenSecret = "dreamseeking123456mayc";

    // expiration time  1 * 60 * 60 * 1000
    private Long expirationTime = 3600000L;

    private static final String TYPE = "JWT";

    private static final String ALG = "HS256";

    /**
     * 生成token(参数1，参数2)
     *
     * @param userAdmin
     * @return
     */
    public String createToken(String userAdmin, String password) {
        JwtBuilder jwtBuilder = Jwts.builder();
        return jwtBuilder
                //header
                .setHeaderParam("type", TYPE)
                .setHeaderParam("alg", ALG)
                //payload
                .claim("userAdmin", userAdmin)
                .claim("userPassword", password)
                //载荷
                .setSubject("admin-login") // 主题
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // 有效时间 系统时间加上设置的时间
                .setId(UUID.randomUUID().toString()) // UUID生成
                // 签名
                .signWith(SignatureAlgorithm.HS256, tokenSecret) // 套用上面头部指定的HS256签名
                .compact();
    }

    /**
     * 解析token
     *
     * @param token
     * @return
     */
    public Map<String, Object> praseToken(String token) {
        Map<String, Object> map = new HashMap<>();
        JwtParser jwtParser = Jwts.parser();
        //通过加密盐解析出包含token的Claims类似集合的数据
        Jws<Claims> claimsJws = null;
        try {
            claimsJws = jwtParser.setSigningKey(tokenSecret).parseClaimsJws(token);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException | IllegalArgumentException e) {
            log.error("发生异常：{}", e.getMessage());
            map.put("exception", e.getMessage());
        }
        if (Objects.nonNull(claimsJws)) {
            map.put("userAdmin", claimsJws.getBody().get("userAdmin"));
            map.put("userPassword", claimsJws.getBody().get("userPassword"));
        }
        return map;
    }

    /**
     * 验证token
     *
     * @param token
     * @return
     */
    public boolean checkToken(String token) {
        // 为空直接false
        if (StringUtils.isNullOrEmpty(token)) return false;
        // 解析有异常也直接false
        Map<String, Object> map = praseToken(token);
        return !map.containsKey("exception");
    }

}
