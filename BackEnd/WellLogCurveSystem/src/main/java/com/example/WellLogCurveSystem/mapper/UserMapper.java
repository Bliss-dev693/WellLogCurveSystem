package com.example.WellLogCurveSystem.mapper;

import com.example.WellLogCurveSystem.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {
    // 注册
    @Insert("insert into user(username,password,create_time,update_time) values(#{username},#{password},now(),now())")
    public void register(String username, String password) ;

    // 查询用户（按用户名）
    @Select("select * from user where username=#{username}")
    public User findByUsername(String username)  ;

    // 添加按ID查询用户的方法
    @Select("select * from user where id=#{id}")
    public User findById(Integer id);

    @Update("update user set nickname=#{nickname},email=#{email},update_time=#{updateTime} where id=#{id}")
    void update(User user);

    @Update("update user set user_pic= #{avatarurl},update_time=now() where id= #{id}")
    void updateAvatar(String avatarurl, Integer id);

    @Update("update user set password= #{md5String},update_time=now() where id= #{id}")
    void updatePwd(String md5String, Integer id);

}