package com.example.WellLogCurveSystem.dto;


import lombok.Data;

/**
 * 待办事项查询参数
 */
@Data
public class TodoQuery {
    /**
     * 用户ID（必填，从Token解析）
     */
    private Integer userId;

    /**
     * 状态：0-待完成，1-已完成，2-已取消
     */
    private Integer status;

    /**
     * 优先级：0-低，1-中，2-高
     */
    private Integer priority;

    /**
     * 分类标签（模糊查询）
     */
    private String category;

    /**
     * 页码，默认1
     */
    private Integer pageNum = 1;

    /**
     * 每页条数，默认10
     */
    private Integer pageSize = 10;

    /**
     * 计算分页起始位置
     */
    public Integer getStartRow() {
        return (pageNum - 1) * pageSize;
    }
}