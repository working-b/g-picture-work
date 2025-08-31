package com.gs.gpicturebackend.model.dto.user;

import com.gs.gpicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-06  20:09
 * @Description: 用户查询请求
 * @Version: 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 6855341853104465400L;

    private Long id;
    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户简介
     */
    private String userProfile;

}
