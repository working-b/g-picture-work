package com.gs.gpicturebackend.exception;

import com.gs.gpicturebackend.common.BaseResponse;
import com.gs.gpicturebackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-05  10:43
 * @Description: 全局异常处理
 * @Version: 1.0
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    public BaseResponse<String> handlerException(BusinessException e){
        log.error("BusinessException, msg:{}", e.getMessage());
        return ResultUtils.error(e.getCode(),e.getMessage());
    }

    @ExceptionHandler(value = RuntimeException.class)
    public BaseResponse<String> handlerException(RuntimeException e) {
        log.error("RuntimeException, msg:{}", e.getMessage());
        e.printStackTrace();
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
    }
}
