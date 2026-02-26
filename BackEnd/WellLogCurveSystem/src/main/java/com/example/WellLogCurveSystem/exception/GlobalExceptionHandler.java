package com.example.WellLogCurveSystem.exception;

import com.example.WellLogCurveSystem.entity.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 捕获所有未处理的异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleGeneralException(Exception ex) {
        logger.error("系统异常", ex);
        return Result.error(Result.SERVER_ERROR_CODE, "系统内部错误：" + ex.getMessage());
    }
    
    /**
     * 捕获参数验证异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.warn("参数验证异常：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }
    
    /**
     * 捕获文件不存在异常
     */
    @ExceptionHandler(java.io.FileNotFoundException.class)
    public Result<Void> handleFileNotFoundException(java.io.FileNotFoundException ex) {
        logger.error("文件不存在：{}", ex.getMessage());
        return Result.error(Result.SERVER_ERROR_CODE, "文件不存在：" + ex.getMessage());
    }
    
    /**
     * 捕获超时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException ex) {
        if (ex.getMessage().contains("超时")) {
            logger.error("执行超时：{}", ex.getMessage());
            return Result.error(Result.SERVER_ERROR_CODE, "执行超时：" + ex.getMessage());
        }
        return handleGeneralException(ex);
    }
}
