package com.gs.gpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gs.gpicturebackend.common.DeleteRequest;
import com.gs.gpicturebackend.model.dto.picture.*;
import com.gs.gpicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gs.gpicturebackend.model.entity.User;
import com.gs.gpicturebackend.model.vo.PictureVO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
* @author hanzhongtao
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-08-19 23:59:08
*/
public interface PictureService extends IService<Picture> {

    /**
     * 校验图片
     *
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 上传图片
     * @param inputSource
     * @param user
     * @return
     */
    PictureVO uploadPicture(Object inputSource, PictureUploadRequest request, User user);

    /**
     * 删除图片
     * @param
     * @param
     * @return
     */
    void deletePicture(long pictureId, User loginUser) ;

    /**
     * 编辑图片
     * @param pictureEditRequest
     * @param loginUser
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    void clearPictureFile(Picture oldPicture);

    /**
     * 获取查询对象
     *
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片包装类（单条）
     *
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片包装类（分页）
     *
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 审核图片
     * @param pictureReviewRequest
     * @param user
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User user);

    /**
     * 填充审核参数
     * @param picture
     * @param operator
     */
    void fillReviewParam(Picture picture, User operator);

    /**
     * 批量上传图片
     * @param request
     * @param user
     * @return
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest request, User user);

    /**
     * 校验空间图片权限
     * @param loginUser
     * @param picture
     */
    void checkPictureAuth(User loginUser, Picture picture);
}
