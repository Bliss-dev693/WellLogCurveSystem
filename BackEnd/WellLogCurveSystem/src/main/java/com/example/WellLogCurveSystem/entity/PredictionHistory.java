package com.example.WellLogCurveSystem.entity;


import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class PredictionHistory {
    private Long id;                // 主键ID
    private Integer userId;         // 关联用户表ID
    private String datasetName;     // 数据集名（如：dataset-2026）
    private String wellName;        // 井名
    private String depthRange;      // 深度范围（如：1-1000m）
    private String inputPredictionData; // 输入的预测数据（JSON格式，存储AC/CAL/GR等输入参数）
    private BigDecimal cnlPrediction; // CNL（中子孔隙度）预测结果
    private String status;          // 预测状态：success/processing/error
    private String errorMessage;    // 错误信息（如有）
    private Long executionTime;     // 执行耗时(毫秒)
    private Date createTime;        // 创建时间
    private Date updateTime;        // 修改时间
}