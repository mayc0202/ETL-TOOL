package com.etledge.upms.config.intercepter;

import com.etledge.common.Constants;
import com.etledge.upms.config.exception.ETLException;
import com.etledge.upms.config.jwt.JwtConfig;
import com.etledge.upms.user.dao.UserDao;
import com.etledge.upms.user.entity.User;
import com.etledge.upms.user.util.UserContext;
import io.jsonwebtoken.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * token拦截器
 */
@Component("tokenInterceptor")
public class TokenInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TokenInterceptor.class);

    private static final String DELETED = "DELETED";
    private static final String ACTIVE = "ACTIVE";

    @Value("${security.interceptor.enabled:true}")
    private boolean interceptorEnabled;

    @Autowired
    private UserDao userDao;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 拦截器被禁用时直接放行
        if (!interceptorEnabled) {
            return true;
        }

        final String requestURI = request.getRequestURI();

        try {
            // 1. 放行预检请求和资源请求
            if (shouldSkipInterceptor(request, handler)) {
                return true;
            }

            // 2. 获取并校验Token
            String token = extractToken(request);
            if (StringUtils.isBlank(token)) {
                throw new ETLException("The request header does not carry Authorization information!");
            }

            // 3. 基础Token校验
            if (!jwtConfig.checkToken(token)) {
                throw new ETLException("The token is invalid or has expired");
            }

            // 4. 解析Token载荷
            Claims claims = parseTokenClaims(token);

            // 5. 单点登录核心校验
            Integer userId = claims.get("userId", Integer.class);
            String account = claims.get("account", String.class);

            validateTokenStatus(token, userId, account);

            // 6. 设置用户上下文
            setUserContext(userId, account);

            logger.debug("Token验证通过 - 用户: {}[{}] 访问: {}", account, userId, requestURI);
            return true;
        } catch (ETLException e) {
            logger.warn("Token验证失败 - {}: {}", requestURI, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Token拦截器异常 - {}: {}", requestURI, e.getMessage(), e);
            throw new ETLException("Internal server error, please try again later!");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 请求完成后清除用户上下文
        UserContext.clear();
    }

    /**
     * 判断是否应该跳过拦截器处理
     */
    private boolean shouldSkipInterceptor(HttpServletRequest request, Object handler) {
        // 放行OPTIONS预检请求
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        // 放行静态资源请求
        if (handler instanceof ResourceHttpRequestHandler) {
            return true;
        }

        // 可以在此添加其他需要放行的路径
        // if (request.getRequestURI().startsWith("/public/")) {
        //     return true;
        // }

        return false;
    }

    /**
     * 从请求头提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String token = request.getHeader(Constants.ETL_EDGE_TOKEN_CONFIG.ETL_EDGE_TOKEN);

        if (StringUtils.isNotBlank(token) && token.startsWith(Constants.ETL_EDGE_TOKEN_CONFIG.BEARER_PREFIX)) {
            token = token.substring(Constants.ETL_EDGE_TOKEN_CONFIG.BEARER_PREFIX.length());
        }

        return token;
    }

    /**
     * 解析Token载荷
     */
    private Claims parseTokenClaims(String token) {
        try {
            return jwtConfig.parseToken(token).getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("Token已过期: {}", token);
            throw new ETLException("The token has expired, please log in again!");
        } catch (UnsupportedJwtException | MalformedJwtException e) {
            logger.warn("无效的Token格式: {}", token);
            throw new ETLException("Invalid token format!");
        } catch (SignatureException e) {
            logger.warn("Token签名验证失败: {}", token);
            throw new ETLException("Token signature verification failed!");
        } catch (IllegalArgumentException e) {
            logger.warn("Token参数错误: {}", token);
            throw new ETLException("Token parameter error!");
        } catch (Exception e) {
            logger.error("解析Token异常: {}", token, e);
            throw new ETLException("The token is invalid or has expired!");
        }
    }

    /**
     * 验证Token状态
     */
    private void validateTokenStatus(String token, Integer userId, String account) {
        if (userId == null || StringUtils.isBlank(account)) {
            throw new ETLException("Incomplete token information!");
        }

        // 1. 检查令牌是否在黑名单中（已注销）
        String blacklistKey = Constants.ETL_EDGE_TOKEN_CONFIG.BLACKLIST_KEY_PREFIX + token;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            throw new ETLException("The token has expired, please log in again!");
        }

        // 2. 获取Redis中存储的最新令牌
        String redisKey = Constants.ETL_EDGE_TOKEN_CONFIG.SSO_KEY_PREFIX + userId;
        String currentToken = redisTemplate.opsForValue().get(redisKey);

        // 3. 比较当前令牌与系统记录的最新令牌
        if (!token.equals(currentToken)) {
            // 添加短暂延迟，防止暴力枚举
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException ignored) {
            }

            throw new ETLException("The account has been logged in elsewhere!");
        }

        // 4. 检查用户状态
        checkUserStatus(userId);
    }

    /**
     * 检查用户状态
     */
    private void checkUserStatus(Integer userId) {

        // 尝试从缓存获取
        String cacheKey = Constants.ETL_EDGE_TOKEN_CONFIG.USER_STATUS_PREFIX + userId;
        String status = redisTemplate.opsForValue().get(cacheKey);

        if (status != null) {
            if (DELETED.equals(status)) {
                throw new ETLException("用户已被删除");
            }
            return;
        }

        // 可以添加缓存机制减少数据库查询
        User user = userDao.selectById(userId);

        if (user == null) {
            throw new ETLException("user does not exist!");
        }

        if (Constants.DELETE_FLAG.TRUE.equals(user.getDeleted())) {
            throw new ETLException("The user has been deleted!");
        }

        // 可以添加其他状态检查，如账户锁定等
        // if (user.getStatus() == UserStatus.LOCKED) {
        //     throw new ETLException("用户账户已被锁定");
        // }

        // 更新缓存
        String newStatus = user.getDeleted() ? DELETED : ACTIVE;
        redisTemplate.opsForValue().set(cacheKey, newStatus, 1, TimeUnit.HOURS);
    }

    /**
     * 设置用户上下文
     */
    private void setUserContext(Integer userId, String account) {
        UserContext.setCurrentUserId(userId);
        UserContext.setCurrentAccount(account);
    }
}
