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

    private Integer groupId;

    private String groupName;

    private String dbType;

    private Integer dbId;

    private String dbName;

    private Date createTime;

}