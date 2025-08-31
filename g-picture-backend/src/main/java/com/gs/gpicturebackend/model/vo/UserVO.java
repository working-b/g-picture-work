package com.gs.gpicturebackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-11  21:32
 * @Description: 用户视图（脱敏）
 * @Version: 1.0
 */
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = -5770447888230045645L;
    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

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
