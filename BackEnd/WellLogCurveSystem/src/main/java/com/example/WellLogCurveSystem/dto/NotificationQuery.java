package com.example.WellLogCurveSystem.dto;

import lombok.Data;

/**
 * 通知列表查询参数
 */
@Data
public class NotificationQuery {
    /**
     * 页码，默认1
     */
    private Integer pageNum = 1;

    /**
     * 每页大小，默认10
     */
    private Integer pageSize = 10;

    /**
     * 通知类型：system/warning/task/""(全部)
     */
    private String type = "";

    /**
     * 状态：unread/read/""(全部)
     */
    private String status = "";

    /**
     * 用户ID（从JWT解析，前端不传）
     */
    private Integer userId;
}