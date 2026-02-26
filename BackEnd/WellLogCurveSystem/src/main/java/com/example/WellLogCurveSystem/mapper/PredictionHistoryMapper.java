package com.example.WellLogCurveSystem.mapper;

import com.example.WellLogCurveSystem.entity.PredictionHistory;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface PredictionHistoryMapper {

    /**
     * 插入预测历史记录
     */
    @Insert("INSERT INTO prediction_history (user_id, dataset_name, well_name, depth_range, input_prediction_data, " +
            "cnl_prediction, status, error_message, execution_time, create_time, update_time) " +
            "VALUES (#{userId}, #{datasetName}, #{wellName}, #{depthRange}, #{inputPredictionData}, " +
            "#{cnlPrediction}, #{status}, #{errorMessage}, #{executionTime}, NOW(), NOW())")
    void insert(PredictionHistory history);

    /**
     * 根据用户ID查询历史记录
     */
    List<PredictionHistory> findByUserId(@Param("userId") Integer userId, 
                                       @Param("datasetName") String datasetName,
                                       @Param("wellName") String wellName,
                                       @Param("status") String status,
                                       @Param("offset") int offset,
                                       @Param("limit") int limit);

    /**
     * 根据用户ID统计历史记录总数
     */
    int countByUserId(@Param("userId") Integer userId,
                     @Param("datasetName") String datasetName,
                     @Param("wellName") String wellName,
                     @Param("status") String status);

    /**
     * 根据井名查询历史记录
     */
    List<PredictionHistory> findByWellName(@Param("wellName") String wellName,
                                         @Param("datasetName") String datasetName,
                                         @Param("status") String status,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);

    /**
     * 根据井名统计历史记录总数
     */
    int countByWellName(@Param("wellName") String wellName,
                       @Param("datasetName") String datasetName,
                       @Param("status") String status);

    /**
     * 新增：按用户ID查询所有历史预测记录（无分页，用于CSV导出）
     */
    List<PredictionHistory> findAllByUserId(@Param("userId") Integer userId);

    /**
     * 用户统计：查询指定用户的预测历史记录总数
     */
    int countCurvesByUserId(@Param("userId") Integer userId);

    /**
     * 用户统计：查询指定用户的总处理时间
     */
    Long getTotalProcessingTimeByUserId(@Param("userId") Integer userId);

    /**
     * 用户统计：查询指定用户今日新增预测记录数
     */
    int countCurvesAddedTodayByUserId(@Param("userId") Integer userId);

    /**
     * 用户统计：计算指定用户的预测准确率
     */
    java.math.BigDecimal getUserPredictionAccuracy(@Param("userId") Integer userId);

    /**
     * 用户统计：计算指定用户准确率提升幅度
     */
    java.math.BigDecimal getUserAccuracyLift(@Param("userId") Integer userId);

    /**
     * 用户统计：计算指定用户处理时间节省率
     */
    java.math.BigDecimal getUserTimeSavedRate(@Param("userId") Integer userId);
}