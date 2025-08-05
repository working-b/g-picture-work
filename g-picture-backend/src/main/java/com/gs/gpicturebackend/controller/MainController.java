package com.gs.gpicturebackend.controller;

import com.gs.gpicturebackend.common.BaseResponse;
import com.gs.gpicturebackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-05  13:32
 * @Description: 健康检查
 * @Version: 1.0
 */
@RestController
@RequestMapping("/")
public class MainController {
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }
}
