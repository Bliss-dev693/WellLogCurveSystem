package com.example.WellLogCurveSystem.service;

import com.example.WellLogCurveSystem.entity.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    User findByUsername(String username);
    void register(String username, String password);

    void update(User user);

    void updateAvatar(String avatarurl);

    void updatePwd(String md5String);
}