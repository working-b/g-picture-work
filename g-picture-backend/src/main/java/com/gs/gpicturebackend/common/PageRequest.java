package com.gs.gpicturebackend.common;

import lombok.Data;

/**
 * @Author: hzt
 * @CreateTime: 2025-08-05  11:17
 * @Description: 分页请求类
 * @Version: 1.0
 */
@Data
public class PageRequest {
    /**
     * 页码
     */
    private int pageNum = 1;
    /**
     * 每页大小
     */
    private int pageSize = 10;
    /**
     * 排序字段
     */
    private String sortField;
    /**
     * 排序顺序
     */
    private String sortOrder = "descend";
}
