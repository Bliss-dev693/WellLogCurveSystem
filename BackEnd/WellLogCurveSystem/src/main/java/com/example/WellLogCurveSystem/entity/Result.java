package com.example.WellLogCurveSystem.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果
 */
@Data
@AllArgsConstructor // 保留lombok的全参构造器
@NoArgsConstructor  // 保留无参构造器
public class Result<T> {

    private Integer code; // 业务状态码  0-成功  1-失败

    private String message; // 提示信息

    private T data; // 响应数据

    // 定义常用的状态码常量
    public static final Integer SUCCESS_CODE = 0; // 成功状态码
    public static final Integer ERROR_CODE = 1;   // 失败状态码
    public static final Integer SERVER_ERROR_CODE = 500; // 服务器错误状态码
    public static final Integer NOT_FOUND_CODE = 404;    // 未找到状态码

    /**
     * 快速返回操作成功响应结果(带响应数据)
     */
    public static <E> Result<E> success(E data) {
        // 明确指定泛型类型E，解决类型推断问题
        return new Result<E>(SUCCESS_CODE, "操作成功", data);
    }

    /**
     * 快速返回操作成功响应结果(无数据)
     */
    public static <E> Result<E> success() {
        // 明确指定泛型类型E
        return new Result<E>(SUCCESS_CODE, "操作成功", null);
    }

    /**
     * 自定义成功消息的响应结果
     */
    public static <E> Result<E> success(String message, E data) {
        // 明确指定泛型类型E
        return new Result<E>(SUCCESS_CODE, message, data);
    }

    /**
     * 快速返回操作失败响应结果
     */
    public static <E> Result<E> error(String message) {
        // 明确指定泛型类型E
        return new Result<E>(ERROR_CODE, message, null);
    }

    /**
     * 自定义错误码和错误信息的响应结果
     */
    public static <E> Result<E> error(Integer code, String message) {
        // 明确指定泛型类型E
        return new Result<E>(code, message, null);
    }

    /**
     * 自定义错误信息和数据的响应结果
     */
    public static <E> Result<E> error(String message, E data) {
        // 明确指定泛型类型E
        return new Result<E>(ERROR_CODE, message, data);
    }
}