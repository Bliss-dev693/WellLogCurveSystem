package com.example.WellLogCurveSystem.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 统计数据VO
 */
@Data
public class StatisticsDataVO {
    
    /**
     * 总井数 - log_data表中记录总数
     */
    private Integer totalWells;         // 总井数
    
    /**
     * 曲线总数 - prediction_history表中记录总数
     */
    private Integer curvesCount;        // 曲线总数
    
    /**
     * 平均准确率 - 从预测历史表中计算预测准确率
     */
    private BigDecimal avgAccuracy;     // 平均准确率
    
    /**
     * 处理时间 - prediction_history表中的execution_time字段求和
     */
    private Long processingTime;        // 处理时间
    
    /**
     * 井数周环比增长率(%)
     */
    private BigDecimal wellsGrowthRate; // 井数周环比增长率
    
    /**
     * 今日新增曲线数
     */
    private Integer curvesAddedToday;   // 今日新增曲线数
    
    /**
     * 准确率提升幅度(%)
     */
    private BigDecimal accuracyLift;    // 准确率提升幅度
    
    /**
     * 处理时间节省率(%)
     */
    private BigDecimal timeSavedRate;   // 处理时间节省率
}