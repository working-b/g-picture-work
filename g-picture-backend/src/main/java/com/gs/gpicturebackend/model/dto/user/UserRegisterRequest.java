package com.gs.gpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-06  20:09
 * @Description: 用户注册请求
 * @Version: 1.0
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = -8344716601510036435L;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;

    /**
     * 用户确认密码
     */
    private String checkPassword;
}
