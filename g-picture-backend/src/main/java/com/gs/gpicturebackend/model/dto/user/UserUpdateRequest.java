package com.gs.gpicturebackend.model.dto.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-06  20:09
 * @Description: 用户更新请求
 * @Version: 1.0
 */
@Data
public class UserUpdateRequest implements Serializable {


    private static final long serialVersionUID = 2618948052106781209L;
    /**
     * id
     */
    private Long id;

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
