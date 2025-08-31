package com.gs.gpicturebackend.manager;

import com.gs.gpicturebackend.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-18  23:48
 * @Description: TODO
 * @Version: 1.0
 */
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;


    /**
     * 上传文件到腾讯云对象存储
     *
     * @param key  文件唯一标识
     * @param file 文件
     * @return
     * @throws CosClientException
     * @throws CosServiceException
     */
    public PutObjectResult putObject(String key, File file) throws CosClientException, CosServiceException {
        // cosClientConfig.getBucket(),桶名
        return cosClient.putObject(cosClientConfig.getBucket(), key, file);
    }

    /**
     * 从腾讯云对象存储下载文件
     *
     * @param key
     * @return
     * @throws CosClientException
     * @throws CosServiceException
     */
    public COSObject getObject(String key) throws CosClientException, CosServiceException {
        return cosClient.getObject(cosClientConfig.getBucket(), key);
    }

    /**
     * 上传图片到腾讯云对象存储
     *
     * @param key
     * @param file
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);

        // 对图片进行处理，获取图片信息也是对图片的一种处理
        PicOperations picOperations = new PicOperations();

        // 获取图片信息
        picOperations.setIsPicInfo(1);

        putObjectRequest.setPicOperations(picOperations);
        // 调用客户端上传
        return cosClient.putObject(putObjectRequest);
    }
}
