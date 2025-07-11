package com.etledge.database.db.vo;

import io.swagger.models.auth.In;
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

    // 数据源分层 1-业务库BIZ；2-贴源库ODS；3-治理库DW；4-应用库ADS；5-共享库DS
    private String label;

    private String labelName;

    //数据库大类：1-关系型数据库；2-非关系型数据库；3-消息型数据库；4-FTP类型; 5-OSS
    private String category;

    //数据库类型：MySQL、Oracle、SQLServer、PostgreSQL等
    private String type;

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
     * be地址
     */
    private String beAddress;

    /**
     * fe地址
     */
    private String feAddress;

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