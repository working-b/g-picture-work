package com.gs.gpicturebackend.manager;

import cn.hutool.core.io.FileUtil;
import com.gs.gpicturebackend.config.CosClientConfig;
import com.gs.gpicturebackend.model.entity.Picture;
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
import java.util.ArrayList;
import java.util.List;

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

        // 设置图片处理规则
        List<PicOperations.Rule> ruleList = new ArrayList<>();
        String webpKey = FileUtil.mainName(key) + ".webp";
        // 上传压缩（不同key时保存原文件）
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setRule("imageMogr2/format/webp");
        //https://cloud.tencent.com/document/product/436/115609#.E4.B8.8A.E4.BC.A0.E6.97.B6.E5.A4.84.E7.90.86
        // 阿里云对象处理入参：file不以/开头，为key的相对路径
        compressRule.setFileId(webpKey);
        ruleList.add(compressRule);

        // 大于5kb进行压缩
        if (file.length() > 5 * 1024) {
            // 缩略图处理
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail,jpeg";
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>",128,128));
            //https://cloud.tencent.com/document/product/436/115609#.E4.B8.8A.E4.BC.A0.E6.97.B6.E5.A4.84.E7.90.86
            // 阿里云对象处理入参：file不以/开头，为key的相对路径
            thumbnailRule.setFileId(thumbnailKey);
            ruleList.add(thumbnailRule);
        }

        picOperations.setRules(ruleList);
        putObjectRequest.setPicOperations(picOperations);
        // 调用客户端上传
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 删除腾讯云对象存储中的文件
     *
     * @param key
     * @return
     */
    public void deleteObject(String key){
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }
}
