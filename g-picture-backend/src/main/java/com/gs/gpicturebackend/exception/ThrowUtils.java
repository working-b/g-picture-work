package com.gs.gpicturebackend.exception;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-05  09:48
 * @Description: 异常处理工具类
 * @Version: 1.0
 */
public class ThrowUtils {
    /**
     * 条件成立，抛出异常
     * @param condition
     * @param exception
     */
    public static void throwIf(boolean condition, RuntimeException exception) {
        if (condition) {
            throw exception;
        }
    }

    /**
     * 条件成立，抛出异常
     * @param condition
     * @param errorCode
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }
    /**
     * 条件成立，抛出异常
     * @param condition
     * @param errorCode
     * @param message
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}
