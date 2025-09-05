package com.gs.gpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.gs.gpicturebackend.exception.ErrorCode;
import com.gs.gpicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: hzt
 * @CreateTime: 2025-09-05  14:04
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate<MultipartFile> {

    @Override
    protected void validPicture(MultipartFile inputSource) {
        // 空文件校验
        ThrowUtils.throwIf(inputSource == null, ErrorCode.PARAMS_ERROR, "文件不能为空");

        // 文件大小校验
        final int ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(inputSource.getSize() > ONE_MB * 2, ErrorCode.PARAMS_ERROR, "文件大小不能超过2MB");

        // 文件格式校验
        final List<String> suffixList = Arrays.asList("jpeg","png","jpg","bmp","gif");
        ThrowUtils.throwIf(!suffixList.contains(FileUtil.getSuffix(inputSource.getOriginalFilename())), ErrorCode.PARAMS_ERROR, "文件格式不正确");

    }

    @Override
    protected String getOriginalFilename(MultipartFile inputSource) {
        return inputSource.getOriginalFilename();
    }

    @Override
    protected void processFile(MultipartFile inputSource, File tmpFile) throws IOException {
        // 将文件写入临时文件
        inputSource.transferTo(tmpFile);

    }
}
