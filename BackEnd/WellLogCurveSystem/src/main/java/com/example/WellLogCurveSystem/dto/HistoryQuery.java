package com.example.WellLogCurveSystem.dto;

import lombok.Data;

@Data
public class HistoryQuery {
    private Integer userId;         // 用户ID（必需）
    private String datasetName;     // 数据集名（可选）
    private String wellName;        // 井名（可选）
    private String status;          // 状态（可选）
    private Integer pageNum = 1;    // 当前页码，默认1
    private Integer pageSize = 10;  // 每页条数，默认10
}