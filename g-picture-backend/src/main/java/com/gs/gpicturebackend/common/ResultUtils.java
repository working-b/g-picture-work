package com.gs.gpicturebackend.common;

import com.gs.gpicturebackend.exception.ErrorCode;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-05  10:33
 * @Description: 返回结果工具类
 * @Version: 1.0
 */
public class ResultUtils {

    /**
     * 成功返回
     * @param data
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0,data,"ok");
    }

    /**
     * 返回错误
     * @param errorCode
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode);
    }

    /**
     * 返回错误
     * @param code
     * @param msg
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> error(int code, String msg){
        return new BaseResponse<>(code,null,msg);
    }

    /**
     * 返回错误
     * @param errorCode
     * @param msg
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String msg){
        return new BaseResponse<>(errorCode.getCode(),null,msg);
    }
}
