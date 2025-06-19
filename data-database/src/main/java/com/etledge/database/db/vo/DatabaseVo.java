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

    // 数据源名称
    private String name;

    private Integer groupId;

    private String groupName;

    private String label;

    private String labelName;

    private String dbType;

    private Integer dbId;

    // 数据库名称
    private String dbName;

    private Date createdTime;

    private String createdTimeTxt;
}