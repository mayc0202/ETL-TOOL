package com.etledge.database.db.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.models.auth.In;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * (DbDatabase)表实体类
 *
 * @author mayc
 * @since 2025-06-03 21:22:27
 */
@Data
@ApiModel(value = "数据源信息", description = "数据源信息")
@TableName(value = "db_database")
public class DbDatabase implements Serializable {

    @TableId
    private Integer id;

    // 数据源名称
    private String name;

    private Integer groupId;

    // 数据源分层 1-业务库BIZ；2-贴源库ODS；3-治理库DW；4-应用库ADS；5-共享库DS
    private String label;

    //数据库大类：1-关系型数据库；2-非关系型数据库；3-消息型数据库；4-FTP类型; 5-OSS
    private String category;

    //数据库类型：MySQL、Oracle、SQLServer、PostgreSQL等
    private String type;

    //主机地址
    private String dbHost;

    //端口
    private String dbPort;

    //数据库名称/SID
    private String dbName;

    //模式schema，目前只支持Oracle
    private String dbSchema;

    //数据库用户名
    private String username;

    //数据库密码
    private String password;

    //备注
    private String remark;

    //是否按天做增量统计。默认值：不需要增量统计
    private Integer isStatsByDay;

    //最近同步时间
    private Date lastSyncTime;

    //数据同步周期
    private Integer dataSyncCycle;

    //是否解除：0-未解除；1-已解除，默认未删除
    private Boolean deleted;

    //创建人账号
    private String createdBy;

    //创建时间
    private Date createdTime;

    //更新人账号
    private String updatedBy;

    //更新时间
    private Date updatedTime;

    //属性
    private String properties;

    //扩展配置
    private String extConfig;

}

