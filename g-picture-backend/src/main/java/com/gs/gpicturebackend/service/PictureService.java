package com.gs.gpicturebackend.service;

import com.gs.gpicturebackend.model.dto.picture.PictureUploadRequest;
import com.gs.gpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gs.gpicturebackend.model.entity.User;
import com.gs.gpicturebackend.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

/**
* @author hanzhongtao
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-08-19 23:59:08
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     * @param multipartFile
     * @param user
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest request, User user);
}
