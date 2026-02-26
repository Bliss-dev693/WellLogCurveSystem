package com.example.WellLogCurveSystem.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    /**
     * 通知ID
     */
    private Long id;

    /**
     * 关联用户ID
     */
    private Integer userId;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知类型：system/warning/task
     */
    private String type;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 状态：unread/read
     */
    private String status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}