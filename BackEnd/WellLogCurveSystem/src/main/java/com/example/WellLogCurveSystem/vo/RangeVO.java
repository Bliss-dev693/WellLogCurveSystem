package com.example.WellLogCurveSystem.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 参数范围VO
 */
@Data
public class RangeVO {
    private BigDecimal min;  // 最小值
    private BigDecimal max;  // 最大值
    private BigDecimal avg;  // 平均值
}