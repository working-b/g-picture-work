package com.gs.gpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-30  16:12
 * @Description: TODO
 * @Version: 1.0
 */
@Data
public class PictureUploadRequest implements Serializable {
    private static final long serialVersionUID = 7906519464995179282L;

    /**
     * 图片id,用于同一个页面下更改图片
     */
    private Long id;
}
