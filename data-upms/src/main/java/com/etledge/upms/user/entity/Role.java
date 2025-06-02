package com.etledge.upms.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

/**
 * (Role)表实体类
 *
 * @Author: yc
 * @CreateTime: 2025-05-27
 * @Description:
 * @Version: 1.0
 */
@Data
@ApiModel(value = "角色信息", description = "角色信息")
@TableName(value = "role")
public class Role {

    @TableId
    private Integer id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 角色描述
     */
    private String dec;

    /**
     * 是否删除
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