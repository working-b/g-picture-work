package com.gs.gpicturebackend.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gs.gpicturebackend.annotation.AuthCheck;
import com.gs.gpicturebackend.common.BaseResponse;
import com.gs.gpicturebackend.common.DeleteRequest;
import com.gs.gpicturebackend.common.ResultUtils;
import com.gs.gpicturebackend.constant.UserConstant;
import com.gs.gpicturebackend.exception.BusinessException;
import com.gs.gpicturebackend.exception.ErrorCode;
import com.gs.gpicturebackend.exception.ThrowUtils;
import com.gs.gpicturebackend.model.dto.picture.PictureEditRequest;
import com.gs.gpicturebackend.model.dto.picture.PictureQueryRequest;
import com.gs.gpicturebackend.model.dto.picture.PictureUpdateRequest;
import com.gs.gpicturebackend.model.dto.picture.PictureUploadRequest;
import com.gs.gpicturebackend.model.entity.Picture;
import com.gs.gpicturebackend.model.entity.User;
import com.gs.gpicturebackend.model.vo.PictureTagCategory;
import com.gs.gpicturebackend.model.vo.PictureVO;
import com.gs.gpicturebackend.service.PictureService;
import com.gs.gpicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @Author: hzt
 * @CreateTime: 2025-09-01  17:26
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/picture")
public class PictureController {

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @RequestMapping(value = "/upload",headers = "content-type=multipart/form-data")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(@RequestPart("file")MultipartFile multipartFile,
                                      PictureUploadRequest pictureUploadRequest,
                                      HttpServletRequest request){
        // 获取用户
        User loginUser = userService.getLoginUser(request);

        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest,HttpServletRequest request){
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR, "参数不能为空");

        User loginUser = userService.getLoginUser(request);
        // 查询图片是否存在
        Picture byId = pictureService.getById(deleteRequest.getId());
        ThrowUtils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        // 判断是否为创建者或者管理员
        if (!byId.getUserId().equals(loginUser.getId()) || !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR);
        }
        boolean b = pictureService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR, "删除失败");
        return ResultUtils.success(true);
    }

    /**
     * 更新图片信息(更细范围更大，仅管理员可用)
     * @param pictureUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request){
        ThrowUtils.throwIf(pictureUpdateRequest == null || pictureUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        // 获取用户
        User loginUser = userService.getLoginUser(request);

        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureUpdateRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));

        // 校验图片
        pictureService.validPicture(picture);

        // 获取图片
        Picture byId = pictureService.getById(pictureUpdateRequest.getId());
        ThrowUtils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        // 更新
        boolean b = pictureService.updateById(picture);
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR, "更新失败");
        return ResultUtils.success(true);
    }

    /**
     * 根据id查询未脱敏图片信息(管理员权限)
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPicture(@RequestParam("id") Long id,HttpServletRequest request){
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        Picture byId = pictureService.getById(id);
        ThrowUtils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        return ResultUtils.success(byId);
    }

    /**
     * 根据id查询脱敏图片信息(用户权限)
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVO(@RequestParam("id") Long id,HttpServletRequest request){
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        Picture byId = pictureService.getById(id);
        ThrowUtils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        PictureVO pictureVO = pictureService.getPictureVO(byId, request);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 分页查询未脱敏图片信息（管理员）
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPicture(PictureQueryRequest pictureQueryRequest, HttpServletRequest request){
        int pageNum = pictureQueryRequest.getPageNum();
        int pageSize = pictureQueryRequest.getPageSize();
        Page<Picture> picturePage = pictureService.page(new Page<Picture>(pageNum, pageSize), pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页查询脱敏图片信息
     * @param pictureQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/vo")
    public BaseResponse<Page<PictureVO>> listPictureVO(PictureQueryRequest pictureQueryRequest, HttpServletRequest request){
        int pageNum = pictureQueryRequest.getPageNum();
        int pageSize = pictureQueryRequest.getPageSize();
        // 防爬虫
        ThrowUtils.throwIf(pageNum < 0 || pageSize > 20, ErrorCode.PARAMS_ERROR, "参数错误");
        Page<Picture> picturePage = pictureService.page(new Page<Picture>(pageNum, pageSize), pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }


    /**
     * 编辑图片信息(仅创建者或管理员可用)
     * @param pictureEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request){
        ThrowUtils.throwIf(pictureEditRequest == null || pictureEditRequest.getId() == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        // 获取图片
        Picture byId = pictureService.getById(pictureEditRequest.getId());
        ThrowUtils.throwIf(byId == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        // 获取用户,校验权限
        User loginUser = userService.getLoginUser(request);
        if (!byId.getUserId().equals(loginUser.getId()) || !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR);
        }
        // 转换
        Picture picture = new Picture();
        BeanUtils.copyProperties(pictureEditRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        picture.setEditTime(new Date());

        // 校验图片
        pictureService.validPicture(picture);

        // 更新
        boolean b = pictureService.updateById(picture);
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR, "更新失败");
        return ResultUtils.success(true);
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }
}
