package com.etledge.upms.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.etledge.common.Constants;
import com.etledge.common.utils.AESUtil;
import com.etledge.common.utils.RSAUtil;
import com.etledge.upms.config.exception.ETLException;
import com.etledge.upms.config.jwt.JwtConfig;
import com.etledge.upms.user.dao.RoleDao;
import com.etledge.upms.user.dao.UserDao;
import com.etledge.upms.user.entity.User;
import com.etledge.upms.user.form.LoginInfoForm;
import com.etledge.upms.user.service.UserService;
import com.etledge.upms.user.vo.RoleVo;
import com.etledge.upms.user.vo.UserVo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Autowired
    private RoleDao roleDao;

    /**
     * 登录
     *
     * @param loginInfo
     * @return
     */
    @Override
    public String login(LoginInfoForm loginInfo) {

        // 1.校验用户是否存在
        LambdaQueryWrapper<User> ldq = new LambdaQueryWrapper<>();
        ldq.eq(User::getAccount, loginInfo.getUsername())
                .eq(User::getDeleted, Constants.DELETE_FLAG.FALSE);
        User user = userDao.selectOne(ldq);
        if (Objects.isNull(user)) {
            throw new ETLException(String.format("Please verify if the current [%s] user exists!", loginInfo.getUsername()));
        }

        // 2.密码校验
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

        // 3.创建token
        String token = jwtConfig.createToken(user.getId(), user.getAccount());

        // 4.记录登录状态
        afterLoginSuccess(user.getId(), token);

        // 5.返回token
        token = Constants.ETL_EDGE_TOKEN_CONFIG.BEARER_PREFIX + token;
        return token;
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    @Override
    public UserVo getUserInfo(String token) {

        // 校验token
        if (StringUtils.isBlank(token)) {
            token = getToken();
        }

        // 解析token
        Claims body = null;
        try {
            Jws<Claims> claims = jwtConfig.parseToken(token);
            body = claims.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("解析Token异常: {}", token, e);
            throw new ETLException("令牌无效或已过期!");
        }

        if (Objects.isNull(body.get("userId"))) {
            throw new ETLException("请验证令牌中的userId是否为空!");
        }
        Integer userId = Integer.valueOf(body.get("userId").toString());

        if (Objects.isNull(body.get("account"))) {
            throw new ETLException("请验证令牌中的帐户是否为空!");
        }
        String account = String.valueOf(body.get("account"));

        // 校验用户信息是否为空
        LambdaQueryWrapper<User> lqd = new LambdaQueryWrapper<>();
        lqd.eq(User::getId,userId)
                .eq(User::getAccount,account)
                .eq(User::getDeleted,Constants.DELETE_FLAG.FALSE);
        User user = userDao.selectOne(lqd);
        if (Objects.isNull(user)) {
            throw new ETLException("请验证用户是否为空!");
        }

        // 查询用户角色
        List<RoleVo> roleVos = roleDao.getRoleListByUserId(userId);
        if (roleVos.isEmpty()) {
            throw new ETLException("请验证用户角色是否为空!");
        }
        Set<String> roleList = roleVos.stream().map(RoleVo::getRoleName).collect(Collectors.toSet());

        // 复制
        UserVo userVo = new UserVo();
        BeanUtils.copyProperties(user,userVo);
        userVo.setRoles(roleList);

        return userVo;
    }

    /**
     * 登录成功后置操作
     *
     * @param userId
     * @param newToken
     */
    public void afterLoginSuccess(Integer userId, String newToken) {
        String redisKey = Constants.ETL_EDGE_TOKEN_CONFIG.SSO_KEY_PREFIX + userId;

        String oldToken = null;
        try {
            // 1. 获取旧令牌并加入黑名单
            oldToken = redisTemplate.opsForValue().get(redisKey);
            if (StringUtils.isNotBlank(oldToken)) {
                Claims oldClaims = jwtConfig.parseToken(oldToken).getBody();
                long expirationTime = oldClaims.getExpiration().getTime();
                long currentTime = System.currentTimeMillis();
                long ttl = expirationTime - currentTime;

                if (ttl > 0) {
                    String blacklistKey = Constants.ETL_EDGE_TOKEN_CONFIG.BLACKLIST_KEY_PREFIX + oldToken;
                    redisTemplate.opsForValue().set(
                            blacklistKey,
                            "1",
                            ttl,
                            TimeUnit.MILLISECONDS
                    );
                }
            }
        } catch (RedisConnectionFailureException e) {
            logger.error("Redis连接异常: {}", e.getMessage(), e);
            throw new ETLException("Redis连接异常:" + e.getMessage());
        } catch (Exception e) {
            logger.error("旧Token解析失败: {}", oldToken, e);
            throw new ETLException("旧Token解析失败:" + e.getMessage());
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
     * 刷新token
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

        // 创建新的token
        Integer userId = claims.get("uid", Integer.class);
        String account = claims.getSubject();
        String newToken = jwtConfig.createToken(userId, account);

        // 更新 Redis 并失效旧 Token
        afterLoginSuccess(userId, newToken);

        return newToken;
    }

    /**
     * 注销用户
     *
     */
    @Override
    public void logout() {

        String token = getToken();

        Claims claims = jwtConfig.parseToken(token).getBody();
        Integer userId = claims.get("uid", Integer.class);

        // 加入黑名单
        String blacklistKey = Constants.ETL_EDGE_TOKEN_CONFIG.BLACKLIST_KEY_PREFIX + token;
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
        redisTemplate.delete(Constants.ETL_EDGE_TOKEN_CONFIG.SSO_KEY_PREFIX + userId);
    }

    /**
     * 获取token
     *
     * @return
     */
    private String getToken() {

        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        Cookie[] cookies = request.getCookies();
        String token = "";
        for (Cookie cookie : cookies) {
            if (Constants.ETL_EDGE_TOKEN_CONFIG.ETL_EDGE_TOKEN.equals(cookie.getName())) {
                token = cookie.getValue().replaceAll(Constants.ETL_EDGE_TOKEN_CONFIG.BEARER_PREFIX,"");
                break;
            }
        }

        if (StringUtils.isBlank(token)) {
            throw new ETLException("请校验token是否为空!");
        }

        return token;
    }
}

