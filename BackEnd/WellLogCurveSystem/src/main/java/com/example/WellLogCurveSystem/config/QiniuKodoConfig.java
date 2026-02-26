package com.example.WellLogCurveSystem.config;

import com.qiniu.util.Auth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QiniuKodoConfig {

    @Value("${qiniu.kodo.access-key}")
    private String accessKey;

    @Value("${qiniu.kodo.secret-key}")
    private String secretKey;

    @Value("${qiniu.kodo.bucket-name}")
    private String bucketName;

    @Value("${qiniu.kodo.endpoint}")
    private String endpoint;

    @Value("${qiniu.kodo.domain}")
    private String domain;

    // 初始化七牛云认证对象
    @Bean
    public Auth qiniuAuth() {
        return Auth.create(accessKey, secretKey);
    }

    // 暴露配置参数（供业务层使用）
    @Bean
    public QiniuKodoProperties qiniuKodoProperties() {
        QiniuKodoProperties properties = new QiniuKodoProperties();
        properties.setAccessKey(accessKey);
        properties.setSecretKey(secretKey);
        properties.setBucketName(bucketName);
        properties.setEndpoint(endpoint);
        properties.setDomain(domain);
        return properties;
    }

    // 配置参数封装类
    public static class QiniuKodoProperties {
        private String accessKey;
        private String secretKey;
        private String bucketName;
        private String endpoint;
        private String domain;


        public String getAccessKey() { return accessKey; }
        public void setAccessKey(String accessKey) { this.accessKey = accessKey; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
        public String getBucketName() { return bucketName; }
        public void setBucketName(String bucketName) { this.bucketName = bucketName; }
        public String getEndpoint() { return endpoint; }
        public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
    }
}