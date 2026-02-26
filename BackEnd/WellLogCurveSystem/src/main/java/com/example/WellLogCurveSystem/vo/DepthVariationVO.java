package com.example.WellLogCurveSystem.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 深度变化可视化数据VO（适配ECharts）
 */
@Data
public class DepthVariationVO {
    /** 基础信息 */
    private BasicInfoVO basicInfo;
    /** X轴：深度数据列表 */
    private List<BigDecimal> xAxis;
    /** 系列数据（各测井参数） */
    private List<SeriesVO> series;
    /** 统计信息 */
    private StatisticsVO statistics;
}