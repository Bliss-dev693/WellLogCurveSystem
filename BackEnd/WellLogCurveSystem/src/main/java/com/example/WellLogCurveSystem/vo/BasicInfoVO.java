package com.example.WellLogCurveSystem.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 基础信息VO
 */
@Data
public class BasicInfoVO {
    private String wellName;        // 井名
    private String datasetName;     // 数据集名
    private BigDecimal minDepth;    // 最小深度
    private BigDecimal maxDepth;    // 最大深度
    private Integer totalPoints;    // 总数据点数
    private Integer returnedPoints; // 返回数据点数
}