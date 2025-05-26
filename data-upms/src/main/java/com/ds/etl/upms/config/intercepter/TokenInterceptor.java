package com.ds.etl.upms.config.intercepter;

import com.ds.etl.common.Constants;
import com.ds.etl.upms.config.exception.ETLException;
import com.ds.etl.upms.config.jwt.JwtConfig;
import com.ds.etl.upms.user.dao.UserDao;
import com.ds.etl.upms.user.entity.User;
import com.ds.etl.upms.user.util.UserContext;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * token拦截器
 */
@Slf4j
@Component("tokenInterceptor")
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private UserDao userDao;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 1. 放行预检请求和资源请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) ||
                handler instanceof ResourceHttpRequestHandler) {
            return true;
        }

        // 2. 获取并校验Token
        String token = request.getHeader("Authorization");
        if (StringUtils.isEmpty(token)) {
            throw new ETLException("请求头未携带Authorization信息");
        }

        // 去除Bearer前缀
        token = token.replace("Bearer ", "");

        // 3. 基础Token校验
        if (!jwtConfig.checkToken(token)) {
            throw new ETLException("令牌无效或已过期");
        }

        // 4. 解析Token载荷
        Claims claims;
        try {
            claims = jwtConfig.parseToken(token).getBody();
        } catch (Exception e) {
            throw new ETLException("令牌解析失败");
        }

        // 5. 单点登录核心校验
        Integer userId = claims.get("userId", Integer.class);
        String account = claims.get("account", String.class);

        // 5.1 检查令牌是否在黑名单中（已注销）
        if (Boolean.TRUE.equals(redisTemplate.hasKey("jwt:blacklist:" + token))) {
            throw new ETLException("令牌已失效，请重新登录");
        }

        // 5.2 获取Redis中存储的最新令牌
        String redisKey = "sso:user:" + userId;
        String currentToken = redisTemplate.opsForValue().get(redisKey);

        // 5.3 比较当前令牌与系统记录的最新令牌
        if (!token.equals(currentToken)) {
            throw new ETLException("账号已在其他地方登录");
        }

        // 5.4 检查用户状态
        User user = userDao.selectById(userId);
        if (user == null || user.getDeleted() == Constants.DELETE_FLAG.TRUE) {
            throw new ETLException("用户不存在或已被删除");
        }

        // 6. 设置用户上下文
        UserContext.setCurrentUserId(userId);
        UserContext.setCurrentAccount(account);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 请求完成后清除用户上下文
        UserContext.clear();
    }
}
