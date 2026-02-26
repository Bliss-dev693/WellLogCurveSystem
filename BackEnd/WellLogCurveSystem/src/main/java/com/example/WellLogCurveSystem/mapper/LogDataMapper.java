package com.example.WellLogCurveSystem.mapper;

import com.example.WellLogCurveSystem.entity.LogData;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface LogDataMapper {

    /**
     * 插入测井数据记录
     */
    @Insert("INSERT INTO log_data (user_id, dataset_name, well_name, depth, ac, cal, gr, den, rt, rxo, is_original, confidence) " +
            "VALUES (#{userId}, #{datasetName}, #{wellName}, #{depth}, #{ac}, #{cal}, #{gr}, #{den}, #{rt}, #{rxo}, #{isOriginal}, #{confidence})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LogData logData);

    /**
     * 根据ID删除测井数据记录
     */
    @Delete("DELETE FROM log_data WHERE id = #{id}")
    int deleteById(Long id);

    /**
     * 更新测井数据记录
     */
    @Update("UPDATE log_data SET user_id=#{userId}, dataset_name=#{datasetName}, well_name=#{wellName}, " +
            "depth=#{depth}, ac=#{ac}, cal=#{cal}, gr=#{gr}, den=#{den}, rt=#{rt}, rxo=#{rxo}, " +
            "is_original=#{isOriginal}, confidence=#{confidence}, update_time=NOW() WHERE id=#{id}")
    int update(LogData logData);

    /**
     * 根据ID查询测井数据记录
     */
    @Select("SELECT * FROM log_data WHERE id = #{id}")
    LogData selectById(Long id);

    /**
     * 根据用户ID查询测井数据记录
     */
    List<LogData> selectByUserId(@Param("userId") Integer userId,
                                 @Param("datasetName") String datasetName,
                                 @Param("wellName") String wellName,
                                 @Param("minDepth") BigDecimal minDepth,
                                 @Param("maxDepth") BigDecimal maxDepth,
                                 @Param("isOriginal") Integer isOriginal,
                                 @Param("offset") int offset,
                                 @Param("limit") int limit);

    /**
     * 根据用户ID统计测井数据记录总数
     */
    int countByUserId(@Param("userId") Integer userId,
                      @Param("datasetName") String datasetName,
                      @Param("wellName") String wellName,
                      @Param("minDepth") BigDecimal minDepth,
                      @Param("maxDepth") BigDecimal maxDepth,
                      @Param("isOriginal") Integer isOriginal);

    /**
     * 根据井名查询测井数据记录
     */
    List<LogData> selectByWellName(@Param("wellName") String wellName,
                                   @Param("userId") Integer userId,
                                   @Param("datasetName") String datasetName,
                                   @Param("minDepth") BigDecimal minDepth,
                                   @Param("maxDepth") BigDecimal maxDepth,
                                   @Param("isOriginal") Integer isOriginal,
                                   @Param("offset") int offset,
                                   @Param("limit") int limit);

    /**
     * 根据井名统计测井数据记录总数
     */
    int countByWellName(@Param("wellName") String wellName,
                        @Param("userId") Integer userId,
                        @Param("datasetName") String datasetName,
                        @Param("minDepth") BigDecimal minDepth,
                        @Param("maxDepth") BigDecimal maxDepth,
                        @Param("isOriginal") Integer isOriginal);

    /**
     * 根据数据集名查询测井数据记录
     */
    List<LogData> selectByDatasetName(@Param("datasetName") String datasetName,
                                      @Param("userId") Integer userId,
                                      @Param("wellName") String wellName,
                                      @Param("minDepth") BigDecimal minDepth,
                                      @Param("maxDepth") BigDecimal maxDepth,
                                      @Param("isOriginal") Integer isOriginal,
                                      @Param("offset") int offset,
                                      @Param("limit") int limit);

    /**
     * 根据数据集名统计测井数据记录总数
     */
    int countByDatasetName(@Param("datasetName") String datasetName,
                           @Param("userId") Integer userId,
                           @Param("wellName") String wellName,
                           @Param("minDepth") BigDecimal minDepth,
                           @Param("maxDepth") BigDecimal maxDepth,
                           @Param("isOriginal") Integer isOriginal);

    /**
     * 查询所有测井数据记录
     */
    List<LogData> selectAll(@Param("userId") Integer userId,
                            @Param("datasetName") String datasetName,
                            @Param("wellName") String wellName,
                            @Param("minDepth") BigDecimal minDepth,
                            @Param("maxDepth") BigDecimal maxDepth,
                            @Param("isOriginal") Integer isOriginal,
                            @Param("offset") int offset,
                            @Param("limit") int limit);

    /**
     * 统计所有测井数据记录总数
     */
    int countAll(@Param("userId") Integer userId,
                 @Param("datasetName") String datasetName,
                 @Param("wellName") String wellName,
                 @Param("minDepth") BigDecimal minDepth,
                 @Param("maxDepth") BigDecimal maxDepth,
                 @Param("isOriginal") Integer isOriginal);

    /**
     * 查询指定井的深度范围（最小/最大深度）
     */
    @Select("SELECT MIN(depth) as minDepth, MAX(depth) as maxDepth FROM log_data WHERE user_id = #{userId} AND dataset_name = #{datasetName} AND well_name = #{wellName}")
    Map<String, Object> selectDepthRangeMap(
            @Param("userId") Integer userId,
            @Param("datasetName") String datasetName,
            @Param("wellName") String wellName);

    /**
     * 查询指定井的深度范围（最小/最大深度）- 原始方法保持兼容
     */
    @Select("SELECT MIN(depth), MAX(depth) FROM log_data WHERE user_id = #{userId} AND dataset_name = #{datasetName} AND well_name = #{wellName}")
    List<Object> selectDepthRange(
            @Param("userId") Integer userId,
            @Param("datasetName") String datasetName,
            @Param("wellName") String wellName);

    /**
     * 查询单井测井数据随深度变化的记录（按深度升序）
     */
    @Select("SELECT * FROM log_data WHERE user_id = #{userId} AND dataset_name = #{datasetName} AND well_name = #{wellName} ORDER BY depth ASC")
    List<LogData> selectByDepthVariation(
            @Param("userId") Integer userId,
            @Param("datasetName") String datasetName,
            @Param("wellName") String wellName,
            @Param("minDepth") BigDecimal minDepth,
            @Param("maxDepth") BigDecimal maxDepth,
            @Param("isOriginal") Integer isOriginal);

    /**
     * 查询用户不同井的数量
     */
    int countDistinctWellsByUserId(@Param("userId") Integer userId);

    /**
     * 查询用户的曲线总数
     */
    int countCurvesByUserId(@Param("userId") Integer userId);

    /**
     * 用户统计：查询指定用户的平均准确率
     */
    java.math.BigDecimal getAverageConfidenceByUserId(@Param("userId") Integer userId);

    /**
     * 用户统计：查询指定用户的平均处理时间（毫秒）
     */
    Long getAverageProcessingTimeByUserId(@Param("userId") Integer userId);

    /**
     * 用户统计：查询指定用户的井数周环比增长率
     */
    java.math.BigDecimal getWellsWeeklyGrowthRateByUserId(@Param("userId") Integer userId);

    /**
     * 查询今日新增曲线数
     */
    int countCurvesAddedTodayByUserId(@Param("userId") Integer userId);

    /**
     * 查询准确率提升幅度（本月与上月对比）
     */
    java.math.BigDecimal getAccuracyLiftByUserId(@Param("userId") Integer userId);

    /**
     * 查询处理时间节省率（本月与上月对比）
     */
    java.math.BigDecimal getTimeSavedRateByUserId(@Param("userId") Integer userId);

    /**
     * 根据用户ID查询数据集列表（去重）
     */
    List<String> selectDatasetNamesByUserId(@Param("userId") Integer userId);

}

