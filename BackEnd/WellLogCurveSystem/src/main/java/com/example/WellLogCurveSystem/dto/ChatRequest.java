package com.example.WellLogCurveSystem.dto;


import lombok.Data;
import java.util.List;

// 前端请求参数
@Data
public class ChatRequest {
    /** 模型名称（可选，默认使用配置类的defaultModel） */
    private String model;
    /** 对话消息列表（必填，OpenAI格式） */
    private List<Message> messages;
    /** 最大输出Token数（可选） */
    private Integer maxTokens;
    /** 温度系数（0-2，可选） */
    private Double temperature;
    /** 核采样（0-1，可选） */
    private Double topP;
    /** 采样数（可选） */
    private Integer topK;
    /** 是否启用思考链（可选，默认true） */
    private Boolean enableThinking = true;
}