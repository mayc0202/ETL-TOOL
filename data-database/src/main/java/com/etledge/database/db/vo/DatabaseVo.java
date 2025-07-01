package com.etledge.database.db.vo;

import lombok.Data;

import java.util.Date;

/**
 * @Author: yc
 * @CreateTime: 2025-06-03
 * @Description:
 * @Version: 1.0
 */
@Data
public class DatabaseVo {

    private Integer id;

    /**
     * 数据源名称
     */
    private String name;

    private Integer groupId;

    private String groupName;

    private String label;

    private String labelName;

    private String dbType;

    /**
     * 主机地址
     */
    private String dbHost;

    /**
     * 端口
     */
    private String dbPort;

    /**
     * 数据库名称/SID
     */
    private String dbName;

    /**
     * 模式schema
     */
    private String dbSchema;

    /**
     * 数据库用户名
     */
    private String username;

    /**
     * 数据库密码
     */
    private String password;

    /**
     * 备注
     */
    private String remark;

    /**
     * 配置
     */
    private String properties;

    /**
     * 扩展配置
     */
    private String extConfig;

    private Date createdTime;

    private String createdTimeTxt;
}