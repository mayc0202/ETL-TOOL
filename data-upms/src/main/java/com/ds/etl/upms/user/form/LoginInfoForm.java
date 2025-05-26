package com.ds.etl.upms.user.form;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @Author: yc
 * @CreateTime: 2025-05-23
 * @Description: Login Information
 * @Version: 1.0
 */
@Data
public class LoginInfoForm {

    @NotBlank(message = "用户名不能为空!")
    @Size(max = 32,message = "用户名长度不能超过32字符!")
    @ApiModelProperty(value = "用户名", name = "username", example = "mayc")
    private String username;

    @NotBlank(message = "密码不能为空!")
    @ApiModelProperty(value = "密码", name = "password", example = "123456")
    private String password;

}