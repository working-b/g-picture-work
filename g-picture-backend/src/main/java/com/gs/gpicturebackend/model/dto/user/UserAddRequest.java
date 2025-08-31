package com.gs.gpicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-06  20:09
 * @Description: 用户创建请求
 * @Version: 1.0
 */
@Data
public class UserAddRequest implements Serializable {

    private static final long serialVersionUID = -563146139188385887L;
    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

}
