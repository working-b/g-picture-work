package com.gs.gpicturebackend.common;

import com.gs.gpicturebackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-05  10:29
 * @Description: 全局响应封装类
 * @Version: 1.0
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;
    private T data;
    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }
    public BaseResponse(int code, T data) {
        this(code, data, null);
    }
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
