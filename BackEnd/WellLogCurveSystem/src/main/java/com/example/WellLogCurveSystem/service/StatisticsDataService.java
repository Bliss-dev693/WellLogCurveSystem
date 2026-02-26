package com.example.WellLogCurveSystem.service;

import com.example.WellLogCurveSystem.entity.Result;
import com.example.WellLogCurveSystem.mapper.LogDataMapper;
import com.example.WellLogCurveSystem.mapper.PredictionHistoryMapper;
import com.example.WellLogCurveSystem.vo.StatisticsDataVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 用户统计数据服务
 */
@Slf4j
@Service
public class StatisticsDataService {

    @Resource
    private LogDataMapper logDataMapper;

    @Resource
    private PredictionHistoryMapper predictionHistoryMapper;

    /**
     * 获取指定用户的统计数据
     * @param userId 用户ID
     * @return 统计数据VO
     */
    public Result<StatisticsDataVO> getUserStatistics(Integer userId) {
        try {
            StatisticsDataVO vo = new StatisticsDataVO();

            // 1. 用户总井数 - log_data表中该用户不同井的数量
            int totalWells = logDataMapper.countDistinctWellsByUserId(userId);
            vo.setTotalWells(totalWells);

            // 2. 用户曲线总数 - prediction_history表中该用户记录总数
            int curvesCount = predictionHistoryMapper.countCurvesByUserId(userId);
            vo.setCurvesCount(curvesCount);

            // 3. 用户平均准确率 - 从预测历史表中计算该用户预测准确率
            BigDecimal avgAccuracy = predictionHistoryMapper.getUserPredictionAccuracy(userId);
            if (avgAccuracy == null) {
                avgAccuracy = BigDecimal.ZERO;
            }
            vo.setAvgAccuracy(avgAccuracy);

            // 4. 用户处理时间 - prediction_history表中该用户的execution_time字段求和
            Long processingTime = predictionHistoryMapper.getTotalProcessingTimeByUserId(userId);
            if (processingTime == null) {
                processingTime = 0L;
            }
            vo.setProcessingTime(processingTime);

            // 5. 用户井数周环比增长率
            BigDecimal wellsGrowthRate = logDataMapper.getWellsWeeklyGrowthRateByUserId(userId);
            if (wellsGrowthRate == null) {
                wellsGrowthRate = BigDecimal.ZERO;
            }
            wellsGrowthRate = wellsGrowthRate.setScale(2, RoundingMode.HALF_UP);
            vo.setWellsGrowthRate(wellsGrowthRate);

            // 6. 用户今日新增曲线数
            int curvesAddedToday = predictionHistoryMapper.countCurvesAddedTodayByUserId(userId);
            vo.setCurvesAddedToday(curvesAddedToday);

            // 7. 用户准确率提升幅度
            BigDecimal accuracyLift = predictionHistoryMapper.getUserAccuracyLift(userId);
            if (accuracyLift == null) {
                accuracyLift = BigDecimal.ZERO;
            }
            accuracyLift = accuracyLift.setScale(2, RoundingMode.HALF_UP);
            vo.setAccuracyLift(accuracyLift);

            // 8. 用户处理时间节省率
            BigDecimal timeSavedRate = predictionHistoryMapper.getUserTimeSavedRate(userId);
            if (timeSavedRate == null) {
                timeSavedRate = BigDecimal.ZERO;
            }
            timeSavedRate = timeSavedRate.setScale(2, RoundingMode.HALF_UP);
            vo.setTimeSavedRate(timeSavedRate);

            log.info("用户{}统计数据获取成功: totalWells={}, curvesCount={}, avgAccuracy={}, processingTime={}, " +
                    "wellsGrowthRate={}%, curvesAddedToday={}, accuracyLift={}%, timeSavedRate={}%",
                    userId, totalWells, curvesCount, avgAccuracy, processingTime,
                    wellsGrowthRate, curvesAddedToday, accuracyLift, timeSavedRate);

            return Result.success("获取用户统计数据成功", vo);
        } catch (Exception e) {
            log.error("获取用户{}统计数据失败", userId, e);
            return Result.error("获取用户统计数据失败: " + e.getMessage());
        }
    }
}