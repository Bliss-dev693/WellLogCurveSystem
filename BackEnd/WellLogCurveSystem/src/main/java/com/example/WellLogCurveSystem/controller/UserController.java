package com.example.WellLogCurveSystem.controller;

import com.example.WellLogCurveSystem.entity.Result;
import com.example.WellLogCurveSystem.entity.User;
import com.example.WellLogCurveSystem.service.UserService;
import com.example.WellLogCurveSystem.utils.JwtUtil;
import com.example.WellLogCurveSystem.utils.Md5Util;
import com.example.WellLogCurveSystem.utils.ThreadLocalUtil;

import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    
    

    @PostMapping("/register")
    public Result<?> register(
            @Pattern(regexp = "^[a-zA-Z0-9_-]{4,16}$", message = "用户名格式错误") 
            String username,
            @Pattern(regexp = "^[a-zA-Z0-9_-]{6,16}$", message = "密码格式错误") 
            String password) {
        // 添加空值检查
        if (username == null || password == null) {
            return Result.error("用户名或密码不能为空");
        }
        User user = userService.findByUsername(username);
        if (user != null) {
            return Result.error("用户已存在");
        } else {
           // 使用MD5加密密码
           String encryptedPassword = Md5Util.getMD5String(password);
           userService.register(username, encryptedPassword);
           return Result.success("注册成功");
        }
    }
    @RequestMapping("/login")
    public Result<?> login(@Pattern(regexp = "^[a-zA-Z0-9_-]{4,16}$", message = "用户名格式错误")String username,
                          @Pattern(regexp = "^[a-zA-Z0-9_-]{6,16}$", message = "密码格式错误") String password) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        } else {
            // 使用MD5加密密码
            String encryptedPassword = Md5Util.getMD5String(password);

            if ( user.getPassword().equals(encryptedPassword)) {
               Map<String, Object> claims = new HashMap<>();
                claims.put("id", user.getId());
                claims.put("username", user.getUsername());

                String token = JwtUtil.generateToken(claims);
                return Result.success( "登录成功", token);
            } else {
                return Result.error("密码错误");
            }
        }
    }
    @RequestMapping("/userInfo")
    public Result<User> userinfo(/*@RequestHeader(name="Authorization") String token*/) {
        // 获取当前用户信息
//        Map<String, Object> claims = JwtUtil.parseToken(token);
        Map<String, Object> claims = ThreadLocalUtil.get();
        String username = (String) claims.get("username");
        User user = userService.findByUsername(username);

        return Result.success("获取成功", user);
    }

    @PutMapping ("/update")
    public Result<?> update(@RequestBody @Validated User user) {
        userService.update(user);
        return Result.success("更新成功");
    }

    

    @PatchMapping ("/updateAvatar")
    public Result<?> updateAvatar(
            @RequestParam  @URL String avatarurl) {
        userService.updateAvatar(avatarurl);
        return Result.success("更新成功");

    }

    

    @PatchMapping ("/updatePwd")
    public Result<?> updatePwd(@RequestBody Map<String, String> params ){
        String oldPassword = params.get("old_pwd");
        String newPassword = params.get("new_pwd");
        String confirmPassword = params.get("re_pwd");
        if(!StringUtils.hasLength(oldPassword)|| !StringUtils.hasLength(newPassword)|| !StringUtils.hasLength(confirmPassword)){
            return Result.error("请填写必要的参数");
        }
        //  检查原密码是否正确
        Map<String, Object> claims = ThreadLocalUtil.get();
        String username = (String) claims.get("username");
        User user = userService.findByUsername(username);
        if (!user.getPassword().equals(Md5Util.getMD5String(oldPassword))) {
            return Result.error("原密码错误");
        }
        if (!newPassword.equals(confirmPassword)) {
            return Result.error("两次输入的密码不一致");
        }
        userService.updatePwd(Md5Util.getMD5String(newPassword));
        return Result.success("更新成功", "密码修改成功");
    }

}
