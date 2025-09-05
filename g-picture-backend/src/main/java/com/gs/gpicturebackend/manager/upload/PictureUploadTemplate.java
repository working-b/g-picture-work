package com.gs.gpicturebackend.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.gs.gpicturebackend.config.CosClientConfig;
import com.gs.gpicturebackend.manager.CosManager;
import com.gs.gpicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-18  23:48
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@Service
public abstract class PictureUploadTemplate<T> {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 校验文件
     * @param inputSource
     */
    protected abstract void validPicture(T inputSource);

    /**
     * 获取文件名
     * @param inputSource
     * @return
     */
    protected abstract String getOriginalFilename(T inputSource);

    /**
     * 处理文件
     * @param inputSource
     * @param tmpFile
     */
    protected abstract void processFile(T inputSource, File tmpFile) throws IOException;


    /**
     * 上传文件
     * @param inputSource
     * @param prefix
     * @return
     */
    public UploadPictureResult uploadPicture(T inputSource, String prefix) {
        // 校验文件
        validPicture(inputSource);

        // 格式化文件名称和路径
        //  1.设置唯一id
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginalFilename(inputSource);
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
            // 将源文件写入临时文件
            processFile(inputSource, tmpFile);
            // 奖本地临时上传到cos（临时文件可以本地进行校验）
            PutObjectResult putObjectResult = cosManager.putPictureObject(filePath, tmpFile);
            // 处理上传返回结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            UploadPictureResult result = new UploadPictureResult();
            result.setUrl(cosClientConfig.getHost() + "/" + filePath);
            result.setThumbnailUrl("");
            result.setPicName(FileUtil.mainName(originalFilename));
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
     * 删除临时文件
     * @param tmpFile
     */
    private static void deleteTempFile(File tmpFile) {
        if (tmpFile != null) {
            boolean delete = tmpFile.delete();
            if (!delete) {
                log.error("临时文件删除失败");
            }
        }
    }

}
