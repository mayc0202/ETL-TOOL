package com.etledge.upms.user.vo;

import lombok.Data;

import java.util.Set;

/**
 * @Author: yc
 * @CreateTime: 2025-05-23
 * @Description: User Basic Information
 * @Version: 1.0
 */
@Data
public class UserVo {

    private Integer id;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 账户
     */
    private String account;

    /**
     * 号码
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 姓名
     */
    private String name;

    /**
     * 微信
     */
    private String wechat;

    /**
     * 部门id
     */
    private Integer deptId;

    /**
     * 令牌
     */
    private String token;

    /**
     * 角色集合
     */
    private Set<String> roles;
}