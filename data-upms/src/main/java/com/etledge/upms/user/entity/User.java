package com.etledge.upms.user.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * (User)表实体类
 *
 * @author mayc
 * @since 2025-05-23 00:24:46
 */
@Data
@ApiModel(value = "用户基础信息", description = "用户基础信息")
@TableName(value = "user")
public class User implements Serializable {

    @TableId
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
     * 密码
     */
    private String password;

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
     * 是否删除
     */
    private Boolean deleted;

    /**
     * 部门id
     */
    private Integer deptId;

    /**
     * 过期日期
     */
    private Date expireDate;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private Date updatedTime;
}

