package com.example.WellLogCurveSystem.controller;

import com.example.WellLogCurveSystem.entity.Result;
import com.example.WellLogCurveSystem.service.QiniuKodoUploadService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileUploadController {

    @Autowired
    private QiniuKodoUploadService uploadService;

    @PostMapping("/upload")
    public Result<String> uploadFile(
            @RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = uploadService.uploadFile(file);
            return Result.success("上传成功", fileUrl);
        } catch (Exception e) {
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * 处理文件大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return Result.error("文件大小超过限制，请上传小于10MB的文件");
    }

}