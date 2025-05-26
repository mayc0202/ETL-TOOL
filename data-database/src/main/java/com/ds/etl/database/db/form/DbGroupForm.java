package com.ds.etl.database.db.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @Author: yc
 * @CreateTime: 2025-05-22
 * @Description:
 * @Version: 1.0
 */
@Data
public class DbGroupForm {

    private Integer id;

    /**
     * 分组名称
     */
    @NotBlank(message = "分组名称不能为空!")
    @Size(max = 50,message = "分组名称长度不能超过50字符!")
    @ApiModelProperty(value = "分组名称", name = "name", example = "分组名称")
    private String name;

    /**
     * 排序
     */
    @NotNull(message = "排序不能为空!")
    @ApiModelProperty(value = "排序", name = "orderBy", example = "1")
    private Integer orderBy;

}