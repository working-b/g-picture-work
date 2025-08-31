package com.gs.gpicturebackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gs.gpicturebackend.exception.ErrorCode;
import com.gs.gpicturebackend.exception.ThrowUtils;
import com.gs.gpicturebackend.manager.FileManager;
import com.gs.gpicturebackend.model.dto.file.UploadPictureResult;
import com.gs.gpicturebackend.model.dto.picture.PictureUploadRequest;
import com.gs.gpicturebackend.model.entity.Picture;
import com.gs.gpicturebackend.model.entity.User;
import com.gs.gpicturebackend.model.vo.PictureVO;
import com.gs.gpicturebackend.service.PictureService;
import com.gs.gpicturebackend.mapper.PictureMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;

/**
* @author hanzhongtao
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-08-19 23:59:08
*/
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

    @Resource
    private FileManager fileManager;

    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest request, User user) {
        // 校验参数
        ThrowUtils.throwIf(multipartFile == null , ErrorCode.PARAMS_ERROR, "参数不能为空");

        // 判断是新增还是更新
        Picture picture = new Picture();
        //  更新:设置id和编辑时间
        if (request != null && request.getId() != null) {
            Long id = request.getId();
            boolean exists = this.lambdaQuery().eq(Picture::getId, id).exists();
            ThrowUtils.throwIf(!exists, ErrorCode.PARAMS_ERROR, "图片不存在");
            picture.setId(id);
            picture.setEditTime(new Date());
        }

        // 获取用户前缀空间，按照用户空间划分目录
        String prefix = String.format("public/%s", user.getId());
        // 上传文件到云端
        UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, prefix);
        // 数据库保存图片信息
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setName(uploadPictureResult.getPicName());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setUserId(user.getId());
        boolean b = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!b, ErrorCode.PARAMS_ERROR, "数据库保存失败");
        return PictureVO.objToVo(picture);
    }
}




