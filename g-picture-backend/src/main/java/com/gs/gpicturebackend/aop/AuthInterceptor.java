package com.gs.gpicturebackend.aop;

import com.gs.gpicturebackend.annotation.AuthCheck;
import com.gs.gpicturebackend.exception.BusinessException;
import com.gs.gpicturebackend.exception.ErrorCode;
import com.gs.gpicturebackend.exception.ThrowUtils;
import com.gs.gpicturebackend.model.entity.User;
import com.gs.gpicturebackend.model.enums.UserRoleEnum;
import com.gs.gpicturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-11  20:48
 * @Description: TODO
 * @Version: 1.0
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(authCheck)")
    public Object authInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 如果注解用户不是枚举中角色的，则通过
        String mustRole = authCheck.mustRole();
        UserRoleEnum roleEnum = UserRoleEnum.getEnumByValue(mustRole);
        if(roleEnum == null){
            return joinPoint.proceed();
        }

        // 存在角色，获取当前用户
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        ServletRequestAttributes attributes = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = attributes.getRequest();
        User loginUser = userService.getLoginUser(request);
        // 用户没登录或者用户角色不存在
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        UserRoleEnum userRole = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        ThrowUtils.throwIf(userRole == null, ErrorCode.NO_AUTH_ERROR);

        // 用户不是admin，但是要求是admin
        if (UserRoleEnum.ADMIN.getValue().equals(mustRole) && !UserRoleEnum.ADMIN.getValue().equals(userRole.getValue())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "用户权限不足");
        }

        return joinPoint.proceed();

    }
}
