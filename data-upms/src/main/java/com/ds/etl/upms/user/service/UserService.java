package com.ds.etl.upms.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ds.etl.upms.user.entity.User;
import com.ds.etl.upms.user.form.LoginInfoForm;
import com.ds.etl.upms.user.vo.UserVo;

/**
 * (User) Service
 *
 * @author mayc
 * @since 2025-05-23 00:24:51
 */
public interface UserService extends IService<User> {

    /**
     * Login
     *
     * @param loginInfo
     * @return
     */
    UserVo login(LoginInfoForm loginInfo);

    /**
     * Logout
     *
     * @param token
     */
    void logout(String token);
}

