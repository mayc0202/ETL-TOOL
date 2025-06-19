package com.etledge.database.dict.vo;

import lombok.Data;

import java.util.List;

/**
 * @Author: yc
 * @CreateTime: 2025-02-27
 * @Description:
 * @Version: 1.0
 */
@Data
public class Dict {

    // 字典编码
    private String dictCode;

    // 字典名称
    private String name;

    // 字典项
    private List<DictItem> list;
}