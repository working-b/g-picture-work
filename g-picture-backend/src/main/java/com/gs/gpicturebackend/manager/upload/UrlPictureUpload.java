package com.gs.gpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.gs.gpicturebackend.exception.BusinessException;
import com.gs.gpicturebackend.exception.ErrorCode;
import com.gs.gpicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: hzt
 * @CreateTime: 2025-09-05  14:10
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate<String> {
    @Override
    protected void validPicture(String inputSource) {
        // 空文件校验
        ThrowUtils.throwIf(StrUtil.isBlank(inputSource), ErrorCode.PARAMS_ERROR, "文件地址为空");

        // url格式校验
        try {
            new URL(inputSource);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }
        // url协议校验
        ThrowUtils.throwIf(!inputSource.startsWith("http://") && !inputSource.startsWith("https://"), ErrorCode.PARAMS_ERROR, "文件地址协议不正确，仅支持http或https协议");
        // 发送Head请求，如果有结果则校验
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, inputSource).execute();
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                // 有些不支持head请求，不代表没有这个资源，所以不能提示错误
                return;
            }
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                // 允许的图片类型
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            // 6. 文件存在，文件大小校验
            String contentLengthStr = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long ONE_M = 1024 * 1024;
                    ThrowUtils.throwIf(contentLength > 2 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 2MB");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式异常");
                }
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    protected String getOriginalFilename(String inputSource) {
        return FileUtil.getName(inputSource);
    }

    @Override
    protected void processFile(String inputSource, File tmpFile) {
        HttpUtil.downloadFile(inputSource, tmpFile);
    }
}
