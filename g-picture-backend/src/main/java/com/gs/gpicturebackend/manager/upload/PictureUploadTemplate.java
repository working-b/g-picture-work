package com.gs.gpicturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.gs.gpicturebackend.config.CosClientConfig;
import com.gs.gpicturebackend.manager.CosManager;
import com.gs.gpicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

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
            // 规则处理结果（按照cosManager中加入规则顺序处理）
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                // 获取压缩规则结果
                CIObject compressCiObject = objectList.get(0);
                // 获取缩略图规则结果
                CIObject thumbnailCiObject = compressCiObject;
                if (objectList.size() > 1) {
                    // 获取缩略图规则结果
                    thumbnailCiObject = objectList.get(1);
                }
                return buildResult(originalFilename,compressCiObject,thumbnailCiObject);
            }
            return buildResult(originalFilename,tmpFile, filePath, imageInfo);
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

    /**
     * 封装返回结果
     *
     * @param originalFilename
     * @param file
     * @param uploadPath
     * @param imageInfo        对象存储返回的图片信息
     * @return
     */
    private UploadPictureResult buildResult(String originalFilename, File file, String uploadPath, ImageInfo imageInfo) {
        // 计算宽高
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        // 封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        uploadPictureResult.setPicColor(imageInfo.getAve());
        // 返回可访问的地址
        return uploadPictureResult;
    }

    /**
     * 封装经过规则处理的返回结果
     * @param originalFilename
     * @param compressCiObject
     * @return
     */
    private UploadPictureResult buildResult(String originalFilename, CIObject compressCiObject, CIObject thumbnailCiObject) {
        // 计算宽高
        int picWidth = compressCiObject.getWidth();
        int picHeight = compressCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        // 封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressCiObject.getKey());
        // 缩略图地址
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicSize(compressCiObject.getSize().longValue());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressCiObject.getFormat());
        // 返回可访问的地址
        return uploadPictureResult;
    }
}
