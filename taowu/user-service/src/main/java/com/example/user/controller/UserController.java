package com.example.user.controller;


import com.example.common.result.Result;
import com.example.common.utils.UserHolder;
import com.example.user.domain.dto.UserLoginDTO;
import com.example.user.domain.po.User;
import com.example.user.domain.vo.UserLoginVo;
import com.example.user.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author author
 * @since 2024-03-31
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "用户相关接口")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    @ApiOperation("发送验证码")
    @PostMapping("/code")
    public Result<String> sendCode(@RequestParam("phone") String phone){
        return userService.sendCode(phone);
    }

    @ApiOperation("登录")
    @PostMapping("/login")
    public Result<UserLoginVo> login(@RequestBody UserLoginDTO loginDTO){
        return userService.login(loginDTO);
    }

    @ApiOperation("个人信息")
    @GetMapping("/me")
    public Result<User> me(){
        return userService.getMeById(UserHolder.getUserId());
    }

    @ApiOperation("登出")
    @PostMapping("/out")
    public Result<String> loginOut(HttpServletRequest request){
        return userService.loginOut(request);
    }

    @PostMapping("/deduct")
    @ApiOperation("扣减用户余额")
        public Result<String> deductMoney(@RequestParam("amount") Integer amount){
        return userService.deductMoney(amount);
    }
}
