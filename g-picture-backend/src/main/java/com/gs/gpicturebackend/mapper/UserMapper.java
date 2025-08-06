package com.gs.gpicturebackend.mapper;

import com.gs.gpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author hanzhongtao
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2025-08-06 19:16:57
* @Entity com.gs.gpicturebackend.model.entity.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




