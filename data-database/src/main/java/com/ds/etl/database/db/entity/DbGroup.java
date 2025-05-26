package com.ds.etl.database.db.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * (DbGroup)表实体类
 *
 * @author mayc
 * @since 2025-05-22 00:00:40
 */
@Data
@ApiModel(value = "数据源分组", description = "数据库基础信息")
@TableName(value = "db_group")
public class DbGroup implements Serializable {

    @TableId
    private Integer id;

    /**
     * 数据库名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer orderBy;

    /**
     * 删除标记
     */
    private Boolean deleted;

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

