package com.ds.etl.database.db.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Author: yc
 * @CreateTime: 2025-05-21
 * @Description:
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DbCategoryVo {

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