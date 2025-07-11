package com.etledge.database.db.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: yc
 * @CreateTime: 2025-06-01
 * @Description:
 * @Version: 1.0
 */
@Data
public class TreeVo {

    private Integer id;

    private String name;

    private Integer orderBy;

    /**
     * 类型
     */
    private String type;

    /**
     * 图标
     */
    private String icon;

    /**
     * 父级id
     */
    private Integer parentId;

    private String treeId;

    private String treeParentId;

    /**
     * 子级
     */
    private List<TreeVo> children;
}