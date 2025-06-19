package com.etledge.database.dict.vo;

import lombok.Data;

/**
 * @Author: yc
 * @CreateTime: 2025-02-27
 * @Description:
 * @Version: 1.0
 */
@Data
public class DictItem {

    // 字典项编码
    private String code;

    // 字典项值
    private String value;

    // 排序
    private Integer order;

    // 描述
    private String description;

}