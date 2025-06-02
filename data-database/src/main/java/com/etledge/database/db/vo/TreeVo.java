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

    private String type;

    private List<TreeVo> children;
}