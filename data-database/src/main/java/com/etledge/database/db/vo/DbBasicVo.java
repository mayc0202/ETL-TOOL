package com.etledge.database.db.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: yc
 * @CreateTime: 2025-05-21
 * @Description:
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DbBasicVo {

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
     * 数据库类型名称
     */
    private String categoryName;

    /**
     * 排序
     */
    private Integer orderBy;

}