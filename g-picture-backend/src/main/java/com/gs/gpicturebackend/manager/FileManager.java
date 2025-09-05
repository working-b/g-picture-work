package com.gs.gpicturebackend.manager;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.gs.gpicturebackend.config.CosClientConfig;
import com.gs.gpicturebackend.exception.BusinessException;
import com.gs.gpicturebackend.exception.ErrorCode;
import com.gs.gpicturebackend.exception.ThrowUtils;
import com.gs.gpicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-18  23:48
 * @Description: 已经通过模版设计模式整合到upload包中
 * @Version: 1.0
 */
@Slf4j
@Service
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传文件
     * @param file
     * @param prefix
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile file, String prefix) {
        // 校验文件
        validPicture(file);

        // 格式化文件名称和路径
        //  1.设置唯一id
        String uuid = RandomUtil.randomString(16);
        String originalFilename = file.getOriginalFilename();
        //  2.设置文件名(日期_uuid.后缀)  这里格式化文件名是为了统一格式和防止文件名乱起和云存储路径、解析有冲突
        String fileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        //      防止没有后缀名，去掉最后的“.”
        if (fileName.endsWith(".")){
            fileName = fileName.substring(0,fileName.length()-1);
        }
        //  3.设置文件路径，为每个用户创建路径
        String filePath = String.format("/%s/%s", prefix, fileName);

        // 上传文件
        File tmpFile = null;
        try {
            // 创建临时文件
            tmpFile = File.createTempFile(filePath, null);
            // 将文件写入临时文件
            file.transferTo(tmpFile);
            // 上传到cos
            PutObjectResult putObjectResult = cosManager.putPictureObject(filePath, tmpFile);
            // 处理上传返回结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            UploadPictureResult result = new UploadPictureResult();
            result.setUrl(cosClientConfig.getHost() + "/" + filePath);
            result.setThumbnailUrl("");
            result.setPicName(FileUtil.mainName(file.getOriginalFilename()));
            result.setPicSize(FileUtil.size(tmpFile));
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            double picScale = NumberUtil.round(width * 1.0 /height,2).doubleValue();
            result.setPicWidth(width);
            result.setPicHeight(height);
            result.setPicScale(picScale);
            result.setPicFormat(imageInfo.getFormat());
            return result;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException(e);
        } finally {
            // 删除临时文件
            deleteTempFile(tmpFile);
        }

    }

    /**
     * 通过url上传文件
     * @param fileUrl
     * @param prefix
     * @return
     */
    public UploadPictureResult uploadPictureByUrl(String fileUrl, String prefix) {
        // 校验文件
        validPicture(fileUrl);

        // 格式化文件名称和路径
        //  1.设置唯一id
        String uuid = RandomUtil.randomString(16);
        String originalFilename = FileUtil.getName(fileUrl);
        //  2.设置文件名(日期_uuid.后缀)  这里格式化文件名是为了统一格式和防止文件名乱起和云存储路径、解析有冲突
        String fileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        //      防止没有后缀名，去掉最后的“.”
        if (fileName.endsWith(".")){
            fileName = fileName.substring(0,fileName.length()-1);
        }
        //  3.设置文件路径，为每个用户创建路径
        String filePath = String.format("/%s/%s", prefix, fileName);

        // 上传文件
        File tmpFile = null;
        try {
            // 创建临时文件
            tmpFile = File.createTempFile(filePath, null);
            // 访问url文件，并写入临时文件
            HttpUtil.downloadFile(fileUrl, tmpFile);
            // 上传到cos
            PutObjectResult putObjectResult = cosManager.putPictureObject(filePath, tmpFile);
            // 处理上传返回结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            UploadPictureResult result = new UploadPictureResult();
            result.setUrl(cosClientConfig.getHost() + "/" + filePath);
            result.setThumbnailUrl("");
//            result.setPicName(FileUtil.mainName(file.getOriginalFilename()));
            result.setPicSize(FileUtil.size(tmpFile));
            int width = imageInfo.getWidth();
            int height = imageInfo.getHeight();
            double picScale = NumberUtil.round(width * 1.0 /height,2).doubleValue();
            result.setPicWidth(width);
            result.setPicHeight(height);
            result.setPicScale(picScale);
            result.setPicFormat(imageInfo.getFormat());
            return result;

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException(e);
        } finally {
            // 删除临时文件
            deleteTempFile(tmpFile);
        }

    }

    private static void deleteTempFile(File tmpFile) {
        if (tmpFile != null) {
            boolean delete = tmpFile.delete();
            if (!delete) {
                log.error("临时文件删除失败");
            }
        }
    }

    /**
     * 校验图片
     * @param file
     */
    private void validPicture(MultipartFile file) {
        // 空文件校验
        ThrowUtils.throwIf(file == null, ErrorCode.PARAMS_ERROR, "文件不能为空");

        // 文件大小校验
        final int ONE_MB = 1024 * 1024;
        ThrowUtils.throwIf(file.getSize() > ONE_MB * 2, ErrorCode.PARAMS_ERROR, "文件大小不能超过2MB");

        // 文件格式校验
        final List<String> suffixList = Arrays.asList("jpeg","png","jpg","bmp","gif");
        ThrowUtils.throwIf(!suffixList.contains(FileUtil.getSuffix(file.getOriginalFilename())), ErrorCode.PARAMS_ERROR, "文件格式不正确");

    }

    /**
     * 校验url图片
     * @param fileUrl
     */
    private void validPicture(String fileUrl) {
        // 空文件校验
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址为空");

        // url格式校验
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }
        // url协议校验
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"), ErrorCode.PARAMS_ERROR, "文件地址协议不正确，仅支持http或https协议");
        // 发送Head请求，如果有结果则校验
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
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
}
