package com.ds.etl.upms.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ds.etl.common.Constants;
import com.ds.etl.common.utils.AESUtil;
import com.ds.etl.common.utils.RSAUtil;
import com.ds.etl.upms.config.exception.ETLException;
import com.ds.etl.upms.config.jwt.JwtConfig;
import com.ds.etl.upms.user.dao.UserDao;
import com.ds.etl.upms.user.entity.User;
import com.ds.etl.upms.user.form.LoginInfoForm;
import com.ds.etl.upms.user.service.UserService;
import com.ds.etl.upms.user.vo.UserVo;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * (User) ServiceImpl
 *
 * @author mayc
 * @since 2025-05-23 00:24:52
 */
@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService {

    public static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserDao userDao;

    /**
     * Login
     *
     * @param loginInfo
     * @return
     */
    @Override
    public UserVo login(LoginInfoForm loginInfo) {

        // 1.verify if the user exists
        LambdaQueryWrapper<User> ldq = new LambdaQueryWrapper<>();
        ldq.eq(User::getAccount, loginInfo.getUsername())
                .eq(User::getDeleted, Constants.DELETE_FLAG.FALSE);
        User user = userDao.selectOne(ldq);
        if (Objects.isNull(user)) {
            throw new ETLException(String.format("Please verify if the current [%s] user exists!", loginInfo.getUsername()));
        }

        // 2.password verifiers
        try {
            String aesPwd = AESUtil.decrypt(user.getPassword(), AESUtil.getAesKey());
            String rsaPwd = RSAUtil.decryptByFixedPrivateKeytoString(loginInfo.getPassword());
            if (!aesPwd.equals(rsaPwd)) {
                logger.warn("User {} Password error", loginInfo.getUsername());
                throw new ETLException("Password error, please log in again!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Password error: {}", e.getMessage());
            throw new ETLException("Password error, please log in again!");
        }

        // 3.create token
        String token = jwtConfig.createToken(user.getId(), user.getAccount());

        // 4.record login status
        afterLoginSuccess(user.getId(), token);

        // 5.copy
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user, userVo);
        userVo.setToken(token);
        return userVo;
    }

    /**
     * Save Redis when logging in
     *
     * @param userId
     * @param newToken
     */
    public void afterLoginSuccess(Integer userId, String newToken) {
        String redisKey = "sso:user:" + userId;

        // 1. 获取旧令牌并加入黑名单
        String oldToken = redisTemplate.opsForValue().get(redisKey);
        if (StringUtils.isNotBlank(oldToken)) {
            try {
                Claims oldClaims = jwtConfig.parseToken(oldToken).getBody();
                long expirationTime = oldClaims.getExpiration().getTime();
                long currentTime = System.currentTimeMillis();
                long ttl = expirationTime - currentTime;

                if (ttl > 0) {
                    String blacklistKey = "jwt:blacklist:" + oldToken;
                    redisTemplate.opsForValue().set(
                            blacklistKey,
                            "1",
                            ttl,
                            TimeUnit.MILLISECONDS
                    );
                }
            } catch (Exception e) {
                logger.error("旧Token解析失败: {}", oldToken, e);
            }
        }

        // 2. 存储新令牌
        redisTemplate.opsForValue().set(
                redisKey,
                newToken,
                jwtConfig.getExpiration(),
                TimeUnit.MILLISECONDS
        );

    }

    /**
     * Refresh token
     *
     * @param oldToken
     * @return
     */
    public String refreshToken(String oldToken) {
        Claims claims = jwtConfig.parseToken(oldToken).getBody();

        // 剩余时间小于30分钟才刷新
        if (claims.getExpiration().getTime() - System.currentTimeMillis() > 30 * 60 * 1000) {
            return oldToken;
        }

        // create new token
        Integer userId = claims.get("uid", Integer.class);
        String account = claims.getSubject();
        String newToken = jwtConfig.createToken(userId, account);

        // 更新 Redis 并失效旧 Token
        afterLoginSuccess(userId, newToken);

        return newToken;
    }

    /**
     * Logout
     *
     * @param token
     */
    @Override
    public void logout(String token) {
        Claims claims = jwtConfig.parseToken(token).getBody();
        Integer userId = claims.get("uid", Integer.class);

        // 加入黑名单
        String blacklistKey = "jwt:blacklist:" + token;
        long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            redisTemplate.opsForValue().set(
                    blacklistKey,
                    "1",
                    ttl,
                    TimeUnit.MILLISECONDS
            );
        }

        // 删除 Redis 中的 Token 记录
        redisTemplate.delete("sso:user:" + userId);
    }
}

