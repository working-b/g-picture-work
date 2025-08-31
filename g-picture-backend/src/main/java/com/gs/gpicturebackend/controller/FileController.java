package com.gs.gpicturebackend.controller;

import com.gs.gpicturebackend.annotation.AuthCheck;
import com.gs.gpicturebackend.common.BaseResponse;
import com.gs.gpicturebackend.common.ResultUtils;
import com.gs.gpicturebackend.constant.UserConstant;
import com.gs.gpicturebackend.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-19  00:12
 * @Description: 测试文件上传
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {
    @Autowired
    private CosManager cosManager;

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/upload")
    public BaseResponse<String> testUpload(@RequestPart("file") MultipartFile multipartFile){
        String originalFilename = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/%s", originalFilename);
        File tmpFile = null;
        try {
            // 创建临时文件
            tmpFile = File.createTempFile("test", null);
            System.out.println("tmpFile.getName() = " + tmpFile.getName());
            // 将文件写入临时文件
            multipartFile.transferTo(tmpFile);
            System.out.println("multipartFile.getOriginalFilename() = " + multipartFile.getOriginalFilename());
            System.out.println("tmpFile.getName() = " + tmpFile.getName());
            // 上传到cos
            cosManager.putObject(filePath, tmpFile);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException(e);
        } finally {
            // 删除临时文件
            if (tmpFile != null) {
                boolean delete = tmpFile.delete();
                if (!delete) {
                    log.error("临时文件删除失败");
                }
            }
        }
        return ResultUtils.success(filePath);
    }

    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    @GetMapping("/download")
    public void testDownload(String filePath, HttpServletResponse response){

        COSObjectInputStream cosObjectInputStream = null;
        try {
            // 获取云端的输入流

            COSObject cosObject = cosManager.getObject(filePath);
            cosObjectInputStream = cosObject.getObjectContent();

            // 将流封装为bytes[]
            byte[] byteArray = IOUtils.toByteArray(cosObjectInputStream);

            // 设置响应头
            response.setContentType("application/octet-stream;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + filePath);
            // 写入响应
            response.getOutputStream().write(byteArray);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("文件下载失败", e);
            throw new RuntimeException(e);
        } finally {
            if (cosObjectInputStream != null) {
                try {
                    cosObjectInputStream.close();
                } catch (IOException e) {
                    log.error("文件流关闭失败", e);
                    throw new RuntimeException(e);
                }
            }
        }

    }

}
