package com.gs.gpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.gs.gpicturebackend.common.BaseResponse;
import com.gs.gpicturebackend.common.ResultUtils;
import com.gs.gpicturebackend.exception.ErrorCode;
import com.gs.gpicturebackend.exception.ThrowUtils;
import com.gs.gpicturebackend.model.dto.UserLoginRequest;
import com.gs.gpicturebackend.model.dto.UserRegisterRequest;
import com.gs.gpicturebackend.model.vo.LoginUserVo;
import com.gs.gpicturebackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-06  19:13
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @param registerRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest registerRequest){
        ThrowUtils.throwIf(registerRequest == null, ErrorCode.PARAMS_ERROR);
        long l = userService.userRegister(registerRequest.getUserAccount(), registerRequest.getUserPassword(), registerRequest.getCheckPassword());
        return ResultUtils.success(l);
    }

    /**
     * 用户登录
     * @param loginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVo> login(@RequestBody UserLoginRequest loginRequest, HttpServletRequest request){
        ThrowUtils.throwIf(loginRequest == null, ErrorCode.PARAMS_ERROR);
        LoginUserVo loginUserVo = userService.login(loginRequest.getUserAccount(), loginRequest.getUserPassword(), request);
        return ResultUtils.success(loginUserVo);

    }

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVo> getLoginUser(HttpServletRequest request){
        LoginUserVo loginUserVo = new LoginUserVo();
        BeanUtil.copyProperties(userService.getLoginUser(request), loginUserVo);
        return ResultUtils.success(loginUserVo);
    }

    /**
     * 用户登出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<String> logout(HttpServletRequest request) {
        userService.logout(request);
        return ResultUtils.success("退出成功");
    }
}
