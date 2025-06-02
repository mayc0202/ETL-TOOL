package com.etledge.upms.user.service.impl;

import com.etledge.api.upms.UpmsServer;
import com.etledge.api.upms.vo.UserVo;
import com.etledge.upms.config.exception.ETLException;
import com.etledge.upms.user.service.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author: yc
 * @CreateTime: 2025-06-02
 * @Description:
 * @Version: 1.0
 */
@DubboService(version = "1.0")
public class DubboUserServiceImpl implements UpmsServer {

    @Autowired
    private UserService userService;

    /**
     * Get user information
     *
     * @param token
     * @return
     */
    @Override
    public UserVo getUserInfo(String token) {
        UserVo userVo = new UserVo();
        try {
            BeanUtils.copyProperties(userService.getUserInfo(token),userVo);
        } catch (BeansException e) {
            e.printStackTrace();
            throw new ETLException(e.getMessage());
        }
        return userVo;
    }
}