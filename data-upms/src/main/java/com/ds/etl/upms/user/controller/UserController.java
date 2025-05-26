package com.ds.etl.upms.user.controller;

import com.ds.etl.common.result.RtnData;
import com.ds.etl.upms.user.form.LoginInfoForm;
import com.ds.etl.upms.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * (User) Controller
 *
 * @author mayc
 * @since 2025-05-23 00:24:44
 */
@Api(tags = "用户信息管理")
@Validated
@RestController
@RequestMapping(value = "/user", produces = {"application/json;charset=utf-8"})
public class UserController {

    @Resource
    private UserService userService;

    /**
     * Login
     *
     * @param loginInfo
     * @return
     */
    @ApiOperation(value = "用户登录")
    @PostMapping("login.do")
    public RtnData login(@RequestBody @Valid LoginInfoForm loginInfo) {
        return RtnData.ok(userService.login(loginInfo));
    }
}

