package com.gs.gpicturebackend.exception;

import lombok.Getter;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-05  09:43
 * @Description: 自定义业务异常
 * @Version: 1.0
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code,String message) {
        super(message);
        this.code = code;
    }
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }
    public BusinessException(ErrorCode errorCode,String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}
