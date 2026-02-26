package com.example.WellLogCurveSystem.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 统计信息VO
 */
@Data
public class StatisticsVO {
    private Integer paramCount;        // 参数数量
    private BigDecimal dataCompleteness; // 数据完整率（%）
}