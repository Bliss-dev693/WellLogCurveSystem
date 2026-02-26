package com.example.WellLogCurveSystem.dto;

import lombok.Data;

/**
 * 对话消息体（OpenAI标准格式）
 * role: system/user/assistant/tool
 */
@Data
public class Message {
    /** 消息角色 */
    private String role;
    /** 消息内容 */
    private String content;
}