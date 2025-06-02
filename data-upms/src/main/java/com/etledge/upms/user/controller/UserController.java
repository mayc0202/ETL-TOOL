package com.etledge.upms.user.controller;

import com.etledge.common.result.RtnData;
import com.etledge.upms.user.form.LoginInfoForm;
import com.etledge.upms.user.service.UserService;
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
    @PostMapping("/login.do")
    public RtnData login(@RequestBody @Valid LoginInfoForm loginInfo) {
        return RtnData.ok(userService.login(loginInfo));
    }

    /**
     * Logout
     *
     * @return
     */
    @ApiOperation(value = "注销用户")
    @PostMapping("/logout.do")
    public RtnData logout() {
        userService.logout();
        return RtnData.ok("User logout successful!");
    }

    /**
     * Get user information
     *
     * @return
     */
    @ApiOperation(value = "获取用户信息")
    @GetMapping("/getUserInfo.do")
    public RtnData getInfo() {
        return RtnData.ok(userService.getUserInfo(null));
    }

}

