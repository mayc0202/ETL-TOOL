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
 * (DbBasic)表实体类
 *
 * @author mayc
 * @since 2025-05-20 23:34:42
 */
@Data
@ApiModel(value = "数据源基础信息", description = "数据源基础信息")
@TableName(value = "db_basic")
public class DbBasic implements Serializable {

    @TableId
    private Integer id;

    /**
     * 数据库名称
     */
    private String name;

    /**
     * logo图片
     */
    private String img;

    /**
     * 数据库类型id
     */
    private Integer categoryId;

    /**
     * 排序
     */
    private Integer orderBy;

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

