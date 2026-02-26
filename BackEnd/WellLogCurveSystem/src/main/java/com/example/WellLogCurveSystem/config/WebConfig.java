package com.example.WellLogCurveSystem.config;

import com.example.WellLogCurveSystem.interceptors.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 添加JWT认证拦截器，排除登录和注册接口
        registry.addInterceptor(authInterceptor)
                .excludePathPatterns("/user/login")
                .excludePathPatterns("/user/register")
                .excludePathPatterns("/error")
                .excludePathPatterns("/api/export/history/csv");



    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 允许跨域请求
        registry.addMapping("/**")  // 所有路径都允许跨域
                .allowedOriginPatterns("*")  // 允许所有域名来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")  // 允许的请求方法
                .allowedHeaders("*")  // 允许所有请求头
                .allowCredentials(true)  // 允许携带凭据(cookie/token等)
                .maxAge(3600);  // 预检请求缓存时间(秒)
    }
}
