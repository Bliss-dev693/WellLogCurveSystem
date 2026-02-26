package com.example.WellLogCurveSystem.service.impl;

import com.example.WellLogCurveSystem.mapper.UserMapper;
import com.example.WellLogCurveSystem.entity.User;
import com.example.WellLogCurveSystem.service.UserService;
import com.example.WellLogCurveSystem.utils.ThreadLocalUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    public void register(String username, String password) {
        // 调用mapper保存用户信息
        userMapper.register(username, password);
    }

    @Override
    public void update(User user) {

        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    public void updateAvatar(String avatarurl) {

        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        userMapper.updateAvatar(avatarurl,id);
    }

    @Override
    public void updatePwd(String md5String) {
        Map<String,Object> map = ThreadLocalUtil.get();
        Integer id = (Integer) map.get("id");
        if (id == null) {
            throw new IllegalStateException("用户未登录，无法更新密码");
        }
        userMapper.updatePwd(md5String, id);
    }
}