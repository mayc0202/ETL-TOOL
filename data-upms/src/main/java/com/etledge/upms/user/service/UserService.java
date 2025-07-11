package com.etledge.upms.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.etledge.upms.user.entity.User;
import com.etledge.upms.user.form.LoginInfoForm;
import com.etledge.upms.user.vo.UserVo;

/**
 * (User) Service
 *
 * @author mayc
 * @since 2025-05-23 00:24:51
 */
public interface UserService extends IService<User> {

    /**
     * 登录用户
     *
     * @param loginInfo
     * @return
     */
    String login(LoginInfoForm loginInfo);

    /**
     * 获取用户信息
     *
     * @param token
     * @return
     */
    UserVo getUserInfo(String token);

    /**
     * 注销账户
     */
    void logout();

}

