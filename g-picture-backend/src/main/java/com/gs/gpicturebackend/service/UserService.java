package com.gs.gpicturebackend.service;

import com.gs.gpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gs.gpicturebackend.model.vo.LoginUserVo;

import javax.servlet.http.HttpServletRequest;

/**
* @author hanzhongtao
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-08-06 19:16:57
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    LoginUserVo login(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);
    /**
     * 获取加密后的密码
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户登出
     * @param request
     * @return
     */
    void logout(HttpServletRequest request);
}
