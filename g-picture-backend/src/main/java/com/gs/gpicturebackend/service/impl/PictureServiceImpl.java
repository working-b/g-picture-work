package com.gs.gpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gs.gpicturebackend.exception.BusinessException;
import com.gs.gpicturebackend.exception.ErrorCode;
import com.gs.gpicturebackend.exception.ThrowUtils;
import com.gs.gpicturebackend.manager.CosManager;
import com.gs.gpicturebackend.manager.FileManager;
import com.gs.gpicturebackend.manager.upload.FilePictureUpload;
import com.gs.gpicturebackend.manager.upload.PictureUploadTemplate;
import com.gs.gpicturebackend.manager.upload.UrlPictureUpload;
import com.gs.gpicturebackend.model.dto.file.UploadPictureResult;
import com.gs.gpicturebackend.model.dto.picture.PictureQueryRequest;
import com.gs.gpicturebackend.model.dto.picture.PictureReviewRequest;
import com.gs.gpicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.gs.gpicturebackend.model.dto.picture.PictureUploadRequest;
import com.gs.gpicturebackend.model.entity.Picture;
import com.gs.gpicturebackend.model.entity.User;
import com.gs.gpicturebackend.model.enums.PictureReviewStatusEnum;
import com.gs.gpicturebackend.model.vo.PictureVO;
import com.gs.gpicturebackend.model.vo.UserVO;
import com.gs.gpicturebackend.service.PictureService;
import com.gs.gpicturebackend.mapper.PictureMapper;
import com.gs.gpicturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author hanzhongtao
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-08-19 23:59:08
*/
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService{

//    @Resource
//    private FileManager fileManager;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private UserService userService;
    @Autowired
    private CosManager cosManager;

    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest request, User user) {
        // 校验参数
        ThrowUtils.throwIf(inputSource == null , ErrorCode.PARAMS_ERROR, "参数不能为空");

        // 判断是新增还是更新
        Picture picture = new Picture();
        //  更新:设置id和编辑时间
        if (request != null && request.getId() != null) {
            Long id = request.getId();
            Picture oldPic = this.getById(id);
            ThrowUtils.throwIf(oldPic == null, ErrorCode.PARAMS_ERROR, "图片不存在");

            // 仅本人和管理员可编辑图片
            Long oldPicUserId = oldPic.getUserId();
            ThrowUtils.throwIf(!oldPicUserId.equals(user.getId()) && !userService.isAdmin(user), ErrorCode.NOT_AUTH_ERROR, "无权限编辑图片");
            picture.setId(id);
            picture.setEditTime(new Date());
        }

        // 获取用户前缀空间，按照用户空间划分目录
        String prefix = String.format("public/%s", user.getId());
        // 根据inputSource选择上传方式，上传文件到云端
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, prefix);
        // 数据库保存图片信息
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicScale(uploadPictureResult.getPicScale());
        String picName = uploadPictureResult.getPicName();
        if (request != null && StrUtil.isNotBlank(request.getPicName())){
            picName = request.getPicName();
        }
        picture.setName(picName);
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setUserId(user.getId());
        // 补充审核参数
        fillReviewParam(picture,user);
        boolean b = this.saveOrUpdate(picture);
        ThrowUtils.throwIf(!b, ErrorCode.PARAMS_ERROR, "数据库保存失败");
        return PictureVO.objToVo(picture);
    }

    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        if (oldPicture == null){
            return;
        }
        String url = oldPicture.getUrl();
        // 判断当前图片文件是否被多个用户引用（此项目中不涉及，但是一些快传等公用软件中设计）
        // 如果被多个用户引用，则不删除文件
        Long count = this.lambdaQuery().eq(Picture::getUrl, url).count();
        if (count > 1){
            return;
        }
        // 删除原图
        cosManager.deleteObject(url);

        // 删除缩略图
        String thumbnailUrl = oldPicture.getThumbnailUrl();
        if (StrUtil.isNotBlank(thumbnailUrl)) {
            cosManager.deleteObject(thumbnailUrl);
        }

    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        QueryWrapper<Picture> queryWrapper = new QueryWrapper<>();
        if (pictureQueryRequest == null) {
            return queryWrapper;
        }
        // 从对象中取值
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        Long picSize = pictureQueryRequest.getPicSize();
        Integer picWidth = pictureQueryRequest.getPicWidth();
        Integer picHeight = pictureQueryRequest.getPicHeight();
        Double picScale = pictureQueryRequest.getPicScale();
        String picFormat = pictureQueryRequest.getPicFormat();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        Integer reviewStatus = pictureQueryRequest.getReviewStatus();
        String reviewMessage = pictureQueryRequest.getReviewMessage();
        Long reviewerId = pictureQueryRequest.getReviewerId();
        Date startEditTime = pictureQueryRequest.getStartEditTime();
        Date endEditTime = pictureQueryRequest.getEndEditTime();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();
        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            // 需要拼接查询条件
            // and (name like "%xxx%" or introduction like "%xxx%")
            queryWrapper.and(
                    qw -> qw.like("name", searchText)
                            .or()
                            .like("introduction", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        queryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        queryWrapper.like(StrUtil.isNotBlank(picFormat), "picFormat", picFormat);
        queryWrapper.like(StrUtil.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        queryWrapper.eq(ObjUtil.isNotEmpty(picWidth), "picWidth", picWidth);
        queryWrapper.eq(ObjUtil.isNotEmpty(picHeight), "picHeight", picHeight);
        queryWrapper.eq(ObjUtil.isNotEmpty(picSize), "picSize", picSize);
        queryWrapper.eq(ObjUtil.isNotEmpty(picScale), "picScale", picScale);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewerId), "reviewerId", reviewerId);
        // >= startEditTime
        queryWrapper.ge(ObjUtil.isNotEmpty(startEditTime), "editTime", startEditTime);
        // < endEditTime
        queryWrapper.lt(ObjUtil.isNotEmpty(endEditTime), "editTime", endEditTime);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tags)) {
            /* and (tag like "%\"Java\"%" and like "%\"Python\"%") */
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        // 对象转封装类
        PictureVO pictureVO = PictureVO.objToVo(picture);
        // 关联查询用户信息
        Long userId = picture.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
        // 1. 关联查询用户信息
        // 1,2,3,4
        Set<Long> userIdSet = pictureList.stream().map(Picture::getUserId).collect(Collectors.toSet());
        // 1 => user1, 2 => user2
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        pictureVOList.forEach(pictureVO -> {
            Long userId = pictureVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            pictureVO.setUser(userService.getUserVO(user));
        });
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User user) {
        // 1. 参数校验
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR, "参数不能为空");
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum enumByValue = PictureReviewStatusEnum.getEnumByValue(reviewStatus);
        String reviewMessage = pictureReviewRequest.getReviewMessage();
        ThrowUtils.throwIf(id == null || reviewStatus == null || enumByValue == null || PictureReviewStatusEnum.REVIEWING.equals(enumByValue),
                ErrorCode.PARAMS_ERROR, "参数不能为空");

        // 2. 权限校验
        ThrowUtils.throwIf(!userService.isAdmin(user), ErrorCode.NOT_AUTH_ERROR, "没有审核权限");
        // 3. 查询是否存在图片、是否状态重复
        Picture oldPic = this.getById(id);
        ThrowUtils.throwIf(oldPic == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        ThrowUtils.throwIf(reviewStatus.equals(oldPic.getReviewStatus()), ErrorCode.PARAMS_ERROR, "请勿重复审核");

        // 4. 更新图片状态
        Picture picture = new Picture();
        picture.setId(id);
        picture.setReviewStatus(reviewStatus);
        picture.setReviewMessage(reviewMessage);
        picture.setReviewerId(user.getId());
        picture.setReviewTime(new Date());
        boolean b = this.updateById(picture);
        ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR, "审核失败");
    }


    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);
        // 从对象中取值
        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();
        // 修改数据时，id 不能为空，有参数则校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "id 不能为空");
        // 如果传递了 url，才校验
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public void fillReviewParam(Picture picture, User operator) {
        if (picture == null || operator == null) {
            return;
        }
        if (userService.isAdmin(operator)) {
            // 管理员自动过审
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewTime(new Date());
            picture.setReviewerId(operator.getId());
            picture.setReviewMessage("管理员自动过审");
        }else {
            // 非管理员
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User user) {
        // 校验参数，权限校验
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null
                || StrUtil.isBlank(pictureUploadByBatchRequest.getSearchText()), ErrorCode.PARAMS_ERROR, "参数不能为空");
        ThrowUtils.throwIf(pictureUploadByBatchRequest.getCount() > 30 ,ErrorCode.PARAMS_ERROR, "一次最多上传30张图片");
        ThrowUtils.throwIf(!userService.isAdmin(user), ErrorCode.NOT_AUTH_ERROR, "没有上传权限");
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = pictureUploadByBatchRequest.getSearchText();
        }
        // 解析地址
        String searchText = pictureUploadByBatchRequest.getSearchText();
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Integer count = pictureUploadByBatchRequest.getCount();
        Document document = null;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取图片信息失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取图片信息失败");
        }
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imgElementList = div.select("img.mimg");
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过: {}", fileUrl);
                continue;
            }
            // 处理图片上传地址，防止出现转义问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            // 上传图片
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, user);
                log.info("图片上传成功, id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }
}




