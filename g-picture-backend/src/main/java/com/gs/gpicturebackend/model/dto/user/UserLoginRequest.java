package com.gs.gpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-06  20:09
 * @Description: 用户登录请
 * @Version: 1.0
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -8344716601510036435L;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户密码
     */
    private String userPassword;
}
