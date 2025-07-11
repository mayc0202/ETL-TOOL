package com.etledge.api.upms;

import com.etledge.api.upms.vo.ApiUserVo;

/**
 * User Service
 */
public interface UpmsServer {

    /**
     * 获取用户信息
     *
     * @param token
     * @return
     */
    ApiUserVo getUserInfo(String token);

    /**
     * 解析token
     *
     * @param token
     */
    void parseToken(String token);
}
