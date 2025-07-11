package com.etledge.upms.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.etledge.api.upms.UpmsServer;
import com.etledge.api.upms.vo.ApiUserVo;
import com.etledge.common.Constants;
import com.etledge.upms.config.exception.ETLException;
import com.etledge.upms.config.jwt.JwtConfig;
import com.etledge.upms.user.dao.UserDao;
import com.etledge.upms.user.entity.User;
import com.etledge.upms.user.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Objects;

/**
 * @Author: yc
 * @CreateTime: 2025-06-02
 * @Description:
 * @Version: 1.0
 */
@DubboService(version = "1.0")
public class DubboUserServiceImpl implements UpmsServer {

    public static final Logger logger = LoggerFactory.getLogger(DubboUserServiceImpl.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private UserService userService;

    /**
     * 获取用户信息
     *
     * @param token
     * @return
     */
    @Override
    public ApiUserVo getUserInfo(String token) {
        ApiUserVo userVo = new ApiUserVo();
        try {
            BeanUtils.copyProperties(userService.getUserInfo(token), userVo);
        } catch (BeansException e) {
            e.printStackTrace();
            throw new ETLException(e.getMessage());
        }
        return userVo;
    }

    /**
     * 解析token
     *
     * @param token
     */
    @Override
    public void parseToken(String token) {

        // 校验token是否为空
        if (StringUtils.isBlank(token)) {
            throw new ETLException("请校验token是否为空!");
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

        // 校验用户信息是否为空!
        LambdaQueryWrapper<User> lqd = new LambdaQueryWrapper<>();
        lqd.eq(User::getId,userId)
                .eq(User::getAccount,account)
                .eq(User::getDeleted, Constants.DELETE_FLAG.FALSE);
        User user = userDao.selectOne(lqd);
        if (Objects.isNull(user)) {
            throw new ETLException("请验证用户是否为空!");
        }
    }
}