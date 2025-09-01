package com.gs.gpicturebackend.manager;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.gs.gpicturebackend.config.CosClientConfig;
import com.gs.gpicturebackend.exception.ErrorCode;
import com.gs.gpicturebackend.exception.ThrowUtils;
import com.gs.gpicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-18  23:48
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@Service
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    public UploadPictureResult uploadPicture(MultipartFile file, String prefix) {
        // 校验文件
        validPicture(file);

        // 格式化文件名称和路径
        //  1.设置唯一id
        String uuid = RandomUtil.randomString(16);
        //  2.设置文件名(日期_uuid.后缀)  这里格式化文件名是为了统一格式和防止文件名乱起和云存储路径、解析有冲突
        String fileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(file.getOriginalFilename()));
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

    private static void deleteTempFile(File tmpFile) {
        if (tmpFile != null) {
            boolean delete = tmpFile.delete();
            if (!delete) {
                log.error("临时文件删除失败");
            }
        }
    }

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
}
