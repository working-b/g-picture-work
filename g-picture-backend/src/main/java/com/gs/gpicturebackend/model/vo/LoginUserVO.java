package com.gs.gpicturebackend.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-06  22:27
 * @Description: 用户视图类
 * @Version: 1.0
 */
@Data
public class LoginUserVO {
    /**
     * 账号
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

    /**
     * 会员过期时间
     */
    private Date vipExpireTime;

    /**
     * 会员兑换码
     */
    private String vipCode;

    /**
     * 会员编号
     */
    private Long vipNumber;

    /**
     * 编辑时间
     */
    private Date editTime;
}
