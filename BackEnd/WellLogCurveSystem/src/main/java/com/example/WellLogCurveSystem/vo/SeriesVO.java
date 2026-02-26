package com.example.WellLogCurveSystem.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 测井参数系列VO（ECharts系列数据）
 */
@Data
public class SeriesVO {
    private String name;            // 参数名称（如：自然伽马(GR)）
    private String param;           // 参数标识（如：gr）
    private String unit;            // 单位（如：API）
    private List<BigDecimal> data;  // 参数值列表
    private RangeVO range;          // 最值/平均值
}