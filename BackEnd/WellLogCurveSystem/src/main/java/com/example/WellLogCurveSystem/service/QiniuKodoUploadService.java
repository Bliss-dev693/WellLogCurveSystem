package com.example.WellLogCurveSystem.service;

import com.example.WellLogCurveSystem.config.QiniuKodoConfig;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@Service
public class QiniuKodoUploadService {

    @Autowired
    private Auth qiniuAuth;

    @Autowired
    private QiniuKodoConfig.QiniuKodoProperties properties;

    /**
     * 上传文件到七牛云
     * @param file 前端上传的文件（MultipartFile）
     * @return 上传后的文件访问URL
     * @throws Exception 上传异常
     */
    public String uploadFile(MultipartFile file) throws Exception {
        // 1. 校验文件
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 2. 生成唯一文件名（避免重复）
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString() + suffix;

        // 3. 配置七牛云存储区域
        Configuration cfg = new Configuration(Region.region0()); // 根据endpoint匹配：z0=region0，z1=region1，z2=region2
        UploadManager uploadManager = new UploadManager(cfg);

        // 4. 生成上传凭证（默认3600秒过期）
        StringMap putPolicy = new StringMap();
        putPolicy.put("returnBody", "{\"key\":\"$(key)\",\"url\":\"" + properties.getDomain() + "/$(key)\"}");
        String upToken = qiniuAuth.uploadToken(properties.getBucketName(), fileName, 3600, putPolicy);

        // 5. 执行上传
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(file.getBytes())) {
            Response response = uploadManager.put(inputStream, fileName, upToken, null, null);
            // 解析响应结果
            String responseBody = response.bodyString();
            if (!response.isOK()) {
                throw new QiniuException(response);
            }
            // 返回文件访问URL
            return properties.getDomain() + "/" + fileName;
        } catch (QiniuException e) {
            Response r = e.response;
            throw new Exception("七牛云上传失败：" + r.bodyString(), e);
        }
    }
}