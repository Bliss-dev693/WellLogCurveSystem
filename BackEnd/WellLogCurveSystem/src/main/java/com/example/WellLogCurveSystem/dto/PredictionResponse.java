package com.example.WellLogCurveSystem.dto;

import lombok.Data;

/**
 * 第三方预测接口响应DTO
 */
@Data
public class PredictionResponse {
    private Double prediction; // CNL（中子孔隙度）预测结果
    private String status;     // 预测状态：success/error
    
    // 可选的额外字段，用于更详细的响应信息
    private String message;    // 响应消息
    private Long executionTime; // 执行时间（毫秒）
}