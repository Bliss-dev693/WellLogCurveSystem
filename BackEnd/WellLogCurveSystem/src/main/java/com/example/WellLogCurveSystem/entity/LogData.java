package com.example.WellLogCurveSystem.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 测井数据实体类
 */
@Data
public class LogData {
    private Long id;              // 数据ID（主键）
    private Integer userId;       // 关联用户表ID
    private String datasetName;   // 数据集名（如：dataset-2026）
    private String wellName;      // 井名
    private BigDecimal depth;     // 深度（单位：m）
    private BigDecimal ac;        // 声波时差（AC）
    private BigDecimal cal;       // 井径（CAL）
    private BigDecimal gr;        // 自然伽马（GR）
    private BigDecimal den;       // 密度（DEN）
    private BigDecimal rt;        // 深电阻率（RT）
    private BigDecimal rxo;       // 浅电阻率（RXO）
    private Integer isOriginal;   // 是否原始数据：1-是，0-预测数据
    private BigDecimal confidence; // 预测置信度（仅预测数据有效）
    private Date createTime;      // 创建时间
    private Date updateTime;      // 修改时间
}