package com.example.WellLogCurveSystem.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 待办事项实体类
 */
@Data
public class TodoTask {
    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 待办标题
     */
    private String title;

    /**
     * 详细内容
     */
    private String content;

    /**
     * 状态：0-待完成，1-已完成，2-已取消
     */
    private Integer status;

    /**
     * 状态文本（非数据库字段，前端展示用）
     */
    private String statusText;

    /**
     * 优先级：0-低，1-中，2-高
     */
    private Integer priority;

    /**
     * 优先级文本（非数据库字段，前端展示用）
     */
    private String priorityText;

    /**
     * 截止日期
     */
    private LocalDateTime dueDate;

    /**
     * 分类标签
     */
    private String category;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 初始化状态/优先级文本
     */
    public void initText() {
        // 状态文本
        switch (status) {
            case 0:
                this.statusText = "待完成";
                break;
            case 1:
                this.statusText = "已完成";
                break;
            case 2:
                this.statusText = "已取消";
                break;
            default:
                this.statusText = "未知";
        }

        // 优先级文本
        switch (priority) {
            case 0:
                this.priorityText = "低";
                break;
            case 1:
                this.priorityText = "中";
                break;
            case 2:
                this.priorityText = "高";
                break;
            default:
                this.priorityText = "未知";
        }
    }
}