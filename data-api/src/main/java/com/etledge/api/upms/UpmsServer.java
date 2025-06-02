package com.etledge.api.upms;

import com.etledge.api.upms.vo.UserVo;

/**
 * User Service
 */
public interface UpmsServer {

    /**
     * Get user information
     *
     * @param token
     * @return
     */
    UserVo getUserInfo(String token);
}
