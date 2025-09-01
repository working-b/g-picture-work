package com.gs.gpicturebackend.controller;

import ch.qos.logback.classic.spi.EventArgUtil;
import com.gs.gpicturebackend.annotation.AuthCheck;
import com.gs.gpicturebackend.common.BaseResponse;
import com.gs.gpicturebackend.common.ResultUtils;
import com.gs.gpicturebackend.constant.UserConstant;
import com.gs.gpicturebackend.model.dto.picture.PictureUploadRequest;
import com.gs.gpicturebackend.model.entity.User;
import com.gs.gpicturebackend.model.vo.PictureVO;
import com.gs.gpicturebackend.service.PictureService;
import com.gs.gpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author: hzt
 * @CreateTime: 2025-09-01  17:26
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @RequestMapping(value = "/upload",headers = "content-type=multipart/form-data")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file")MultipartFile multipartFile,
                                      PictureUploadRequest pictureUploadRequest,
                                      HttpServletRequest request){
        // 获取用户
        User loginUser = userService.getLoginUser(request);

        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }
}
