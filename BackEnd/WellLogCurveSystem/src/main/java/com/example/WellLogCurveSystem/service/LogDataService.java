package com.example.WellLogCurveSystem.service;

import com.example.WellLogCurveSystem.entity.LogData;
import com.example.WellLogCurveSystem.entity.PageResult;
import com.example.WellLogCurveSystem.entity.Result;
import com.example.WellLogCurveSystem.mapper.LogDataMapper;
import com.example.WellLogCurveSystem.mapper.UserMapper;
import com.example.WellLogCurveSystem.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
public class LogDataService {

    @Autowired
    private LogDataMapper logDataMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * 添加测井数据记录
     */
    @Transactional
    public Result<String> addLogData(LogData logData) {
        try {
            // 验证必要字段
            if (logData.getUserId() == null) {
                return Result.error("用户ID不能为空");
            }
            if (logData.getDatasetName() == null || logData.getDatasetName().trim().isEmpty()) {
                return Result.error("数据集名不能为空");
            }
            if (logData.getWellName() == null || logData.getWellName().trim().isEmpty()) {
                return Result.error("井名不能为空");
            }
            if (logData.getDepth() == null) {
                return Result.error("深度不能为空");
            }

            // 检查用户是否存在，以防止外键约束错误
            com.example.WellLogCurveSystem.entity.User user = userMapper.findById(logData.getUserId());
            if (user == null) {
                return Result.error("用户不存在，无法添加测井数据");
            }

            logDataMapper.insert(logData);
            return Result.success("添加成功", "ID: " + logData.getId());
        } catch (Exception e) {
            return Result.error("添加失败：" + e.getMessage());
        }
    }

    /**
     * 删除测井数据记录
     */
    @Transactional
    public Result<String> deleteLogData(Long id) {
        try {
            if (id == null) {
                return Result.error("ID不能为空");
            }

            int affectedRows = logDataMapper.deleteById(id);
            if (affectedRows > 0) {
                return Result.success("删除成功", "影响行数：" + affectedRows);
            } else {
                return Result.error("删除失败，记录不存在");
            }
        } catch (Exception e) {
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 更新测井数据记录
     */
    @Transactional
    public Result<String> updateLogData(LogData logData) {
        try {
            if (logData.getId() == null) {
                return Result.error("ID不能为空");
            }

            // 检查记录是否存在
            LogData existing = logDataMapper.selectById(logData.getId());
            if (existing == null) {
                return Result.error("记录不存在，无法更新");
            }

            // 检查用户是否存在，以防止外键约束错误
            if (logData.getUserId() != null) {
                com.example.WellLogCurveSystem.entity.User user = userMapper.findById(logData.getUserId());
                if (user == null) {
                    return Result.error("用户不存在，无法更新测井数据");
                }
            }

            int affectedRows = logDataMapper.update(logData);
            if (affectedRows > 0) {
                return Result.success("更新成功", "影响行数：" + affectedRows);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询测井数据记录
     */
    public Result<LogData> getLogDataById(Long id) {
        try {
            if (id == null) {
                return Result.error("ID不能为空");
            }

            LogData logData = logDataMapper.selectById(id);
            if (logData != null) {
                return Result.success(logData);
            } else {
                return Result.error("记录不存在");
            }
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID查询测井数据记录（支持分页和筛选）
     */
    public Result<PageResult<LogData>> getLogDataByUserId(Integer userId, String datasetName, String wellName,
                                                          BigDecimal minDepth, BigDecimal maxDepth,
                                                          Integer isOriginal, Integer pageNum, Integer pageSize) {
        try {
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }

            // 验证分页参数，设置默认值
            if (pageNum == null || pageNum < 1) pageNum = 1;
            if (pageSize == null || pageSize < 1) pageSize = 10;
            if (pageSize > 100) pageSize = 100; // 限制最大页面大小

            // 计算分页偏移量
            int offset = (pageNum - 1) * pageSize;

            // 查询数据
            List<LogData> records = logDataMapper.selectByUserId(
                    userId, datasetName, wellName, minDepth, maxDepth, isOriginal, offset, pageSize
            );

            // 统计总数
            int total = logDataMapper.countByUserId(userId, datasetName, wellName, minDepth, maxDepth, isOriginal);

            // 构造分页结果
            PageResult<LogData> pageResult = new PageResult<>((long) total, pageNum, pageSize, records);

            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据井名查询测井数据记录（支持分页和筛选）
     */
    public Result<PageResult<LogData>> getLogDataByWellName(String wellName, Integer userId, String datasetName,
                                                            BigDecimal minDepth, BigDecimal maxDepth,
                                                            Integer isOriginal, Integer pageNum, Integer pageSize) {
        try {
            if (wellName == null || wellName.trim().isEmpty()) {
                return Result.error("井名不能为空");
            }

            if (userId == null) {
                return Result.error("用户ID不能为空");
            }

            // 验证分页参数，设置默认值
            if (pageNum == null || pageNum < 1) pageNum = 1;
            if (pageSize == null || pageSize < 1) pageSize = 10;
            if (pageSize > 100) pageSize = 100; // 限制最大页面大小

            // 计算分页偏移量
            int offset = (pageNum - 1) * pageSize;

            // 查询数据
            List<LogData> records = logDataMapper.selectByWellName(
                    wellName, userId, datasetName, minDepth, maxDepth, isOriginal, offset, pageSize
            );

            // 统计总数
            int total = logDataMapper.countByWellName(wellName, userId, datasetName, minDepth, maxDepth, isOriginal);

            // 构造分页结果
            PageResult<LogData> pageResult = new PageResult<>((long) total, pageNum, pageSize, records);

            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据数据集名查询测井数据记录（支持分页和筛选）
     */
    public Result<PageResult<LogData>> getLogDataByDatasetName(String datasetName, Integer userId, String wellName,
                                                               BigDecimal minDepth, BigDecimal maxDepth,
                                                               Integer isOriginal, Integer pageNum, Integer pageSize) {
        try {
            if (datasetName == null || datasetName.trim().isEmpty()) {
                return Result.error("数据集名不能为空");
            }

            if (userId == null) {
                return Result.error("用户ID不能为空");
            }

            // 验证分页参数，设置默认值
            if (pageNum == null || pageNum < 1) pageNum = 1;
            if (pageSize == null || pageSize < 1) pageSize = 10;
            if (pageSize > 100) pageSize = 100; // 限制最大页面大小

            // 计算分页偏移量
            int offset = (pageNum - 1) * pageSize;

            // 查询数据
            List<LogData> records = logDataMapper.selectByDatasetName(
                    datasetName, userId, wellName, minDepth, maxDepth, isOriginal, offset, pageSize
            );

            // 统计总数
            int total = logDataMapper.countByDatasetName(datasetName, userId, wellName, minDepth, maxDepth, isOriginal);

            // 构造分页结果
            PageResult<LogData> pageResult = new PageResult<>((long) total, pageNum, pageSize, records);

            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有测井数据记录（支持分页和筛选）
     */
    public Result<PageResult<LogData>> getAllLogData(Integer userId, String datasetName, String wellName,
                                                     BigDecimal minDepth, BigDecimal maxDepth,
                                                     Integer isOriginal, Integer pageNum, Integer pageSize) {
        try {
            // 验证分页参数，设置默认值
            if (pageNum == null || pageNum < 1) pageNum = 1;
            if (pageSize == null || pageSize < 1) pageSize = 10;
            if (pageSize > 100) pageSize = 100; // 限制最大页面大小

            // 计算分页偏移量
            int offset = (pageNum - 1) * pageSize;

            // 查询数据
            List<LogData> records = logDataMapper.selectAll(
                    userId, datasetName, wellName, minDepth, maxDepth, isOriginal, offset, pageSize
            );

            // 统计总数
            int total = logDataMapper.countAll(userId, datasetName, wellName, minDepth, maxDepth, isOriginal);

            // 构造分页结果
            PageResult<LogData> pageResult = new PageResult<>((long) total, pageNum, pageSize, records);

            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    public Result<DepthVariationVO> getDepthVariationData(
            Integer userId,
            String datasetName,
            String wellName,
            BigDecimal minDepth,
            BigDecimal maxDepth,
            String params,
            Integer sampleRate,
            Integer isOriginal) {
        try {
            // 1. 参数校验
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }
            if (datasetName == null || datasetName.trim().isEmpty()) {
                return Result.error("数据集名不能为空");
            }
            if (wellName == null || wellName.trim().isEmpty()) {
                return Result.error("井名不能为空");
            }

            // 2. 获取该井的深度范围（使用Map方式避免MyBatis聚合查询问题）
            Map<String, Object> depthRangeMap = logDataMapper.selectDepthRangeMap(userId, datasetName, wellName);
            log.info("深度范围查询结果(Map): {}", depthRangeMap);
            
            BigDecimal dbMinDepth = null;
            BigDecimal dbMaxDepth = null;
            
            if (depthRangeMap != null && !depthRangeMap.isEmpty()) {
                // 从Map中提取minDepth和maxDepth
                Object minObj = depthRangeMap.get("minDepth");
                Object maxObj = depthRangeMap.get("maxDepth");
                
                if (minObj instanceof BigDecimal) {
                    dbMinDepth = (BigDecimal) minObj;
                } else if (minObj instanceof Number) {
                    dbMinDepth = new BigDecimal(minObj.toString());
                }
                
                if (maxObj instanceof BigDecimal) {
                    dbMaxDepth = (BigDecimal) maxObj;
                } else if (maxObj instanceof Number) {
                    dbMaxDepth = new BigDecimal(maxObj.toString());
                }
            }
            
            if (dbMinDepth == null || dbMaxDepth == null) {
                log.error("无法解析深度范围数据: min={}, max={}", dbMinDepth, dbMaxDepth);
                return Result.error("未找到该井的深度数据");
            }
            
            log.info("解析后的深度范围: min={}, max={}", dbMinDepth, dbMaxDepth);

            // 3. 修正传入的深度范围（确保在数据库实际范围内）
            BigDecimal actualMinDepth = (minDepth != null && minDepth.compareTo(dbMinDepth) >= 0)
                    ? minDepth
                    : dbMinDepth;
            BigDecimal actualMaxDepth = (maxDepth != null && maxDepth.compareTo(dbMaxDepth) <= 0)
                    ? maxDepth
                    : dbMaxDepth;

            // 防止最小深度大于最大深度
            if (actualMinDepth.compareTo(actualMaxDepth) > 0) {
                BigDecimal temp = actualMinDepth;
                actualMinDepth = actualMaxDepth;
                actualMaxDepth = temp;
            }

            // 4. 设置采样参数（默认1，最大100）
            int actualSampleRate = sampleRate == null || sampleRate < 1 ? 1 : sampleRate;
            actualSampleRate = Math.min(actualSampleRate, 100);


               log.info("查询条件：userId={}, datasetName={}, wellName={}, minDepth={}, maxDepth={}, isOriginal={}",
           userId, datasetName, wellName, actualMinDepth, actualMaxDepth, isOriginal);

            // 5. 查询原始数据
            log.info("开始查询原始数据: userId={}, datasetName={}, wellName={}", userId, datasetName, wellName);
            List<LogData> rawDataList = logDataMapper.selectByDepthVariation(
                    userId, datasetName, wellName, actualMinDepth, actualMaxDepth, isOriginal
            );
            log.info("原始数据查询完成，结果数量: {}", rawDataList != null ? rawDataList.size() : 0);

            log.info("深度变化查询结果：用户{} 数据集{} 井{} 深度[{},{}] 数据量={}",
                    userId, datasetName, wellName, actualMinDepth, actualMaxDepth,
                    rawDataList == null ? 0 : rawDataList.size());

            if (rawDataList == null || rawDataList.isEmpty()) {
                return Result.error(String.format(
                        "在深度范围[%s, %s]内未找到该井的测井数据（isOriginal=%s）",
                        actualMinDepth, actualMaxDepth, isOriginal
                ));
            }

            // 6. 数据采样（降低数据量，优化前端渲染）
            List<LogData> sampledData = sampleLogData(rawDataList, actualSampleRate);

            // 7. 解析参数列表（默认返回所有核心参数）
            List<String> paramList = params != null ?
                    Arrays.stream(params.split(","))
                            .map(String::trim)
                            .filter(p -> !p.isEmpty())
                            .collect(Collectors.toList()) :
                    Arrays.asList("ac", "cal", "gr", "den", "rt", "rxo");

            // 8. 构建返回VO
            DepthVariationVO vo = new DepthVariationVO();

            // 8.1 基础信息
            BasicInfoVO basicInfo = new BasicInfoVO();
            basicInfo.setWellName(wellName);
            basicInfo.setDatasetName(datasetName);
            basicInfo.setMinDepth(actualMinDepth);
            basicInfo.setMaxDepth(actualMaxDepth);
            basicInfo.setTotalPoints(rawDataList.size());
            basicInfo.setReturnedPoints(sampledData.size());
            vo.setBasicInfo(basicInfo);

            // 8.2 X轴：深度数据
            List<BigDecimal> xAxis = sampledData.stream()
                    .map(LogData::getDepth)
                    .collect(Collectors.toList());
            vo.setXAxis(xAxis);

            // 8.3 系列数据（各测井参数）
            List<SeriesVO> seriesList = new ArrayList<>();
            for (String param : paramList) {
                SeriesVO series = buildSeriesVO(sampledData, param);
                if (series != null) {
                    seriesList.add(series);
                }
            }
            vo.setSeries(seriesList);

            // 8.4 统计信息
            StatisticsVO statistics = new StatisticsVO();
            statistics.setParamCount(seriesList.size());
            // 计算数据完整率
            BigDecimal totalValues = BigDecimal.valueOf(seriesList.size() * sampledData.size());
            BigDecimal nullValues = BigDecimal.ZERO;
            for (SeriesVO series : seriesList) {
                long nullCount = series.getData().stream().filter(Objects::isNull).count();
                nullValues = nullValues.add(BigDecimal.valueOf(nullCount));
            }
            BigDecimal completeness = totalValues.compareTo(BigDecimal.ZERO) == 0 ?
                    BigDecimal.ZERO :
                    BigDecimal.ONE.subtract(nullValues.divide(totalValues, 4, RoundingMode.HALF_UP))
                            .multiply(BigDecimal.valueOf(100));
            statistics.setDataCompleteness(completeness);
            vo.setStatistics(statistics);

            return Result.success(vo);
        } catch (Exception e) {
            log.error("获取深度变化数据失败", e);
            return Result.error(Result.SERVER_ERROR_CODE, "获取深度变化数据失败：" + e.getMessage());
        }
    }

    /**
     * 数据采样（按采样率抽取数据）
     */
    private List<LogData> sampleLogData(List<LogData> dataList, int sampleRate) {
        if (sampleRate <= 1 || dataList.size() <= sampleRate) {
            return new ArrayList<>(dataList);
        }
        List<LogData> sampledList = new ArrayList<>();
        for (int i = 0; i < dataList.size(); i += sampleRate) {
            sampledList.add(dataList.get(i));
        }
        // 确保最后一条数据被包含
        if (!sampledList.contains(dataList.get(dataList.size() - 1))) {
            sampledList.add(dataList.get(dataList.size() - 1));
        }
        return sampledList;
    }

    /**
     * 构建参数系列VO
     */
    private SeriesVO buildSeriesVO(List<LogData> dataList, String param) {
        SeriesVO series = new SeriesVO();
        List<BigDecimal> data = new ArrayList<>();
        BigDecimal min = null;
        BigDecimal max = null;
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;

        switch (param.toLowerCase()) {
            case "ac":
                series.setName("声波时差(AC)");
                series.setParam("ac");
                series.setUnit("μs/m");
                for (LogData dataItem : dataList) {
                    BigDecimal value = dataItem.getAc();
                    data.add(value);
                    if (value != null) {
                        min = (min == null) ? value : (value.compareTo(min) < 0 ? value : min);
                        max = (max == null) ? value : (value.compareTo(max) > 0 ? value : max);
                        sum = sum.add(value);
                        count++;
                    }
                }
                break;
            case "cal":
                series.setName("井径(CAL)");
                series.setParam("cal");
                series.setUnit("in");
                for (LogData dataItem : dataList) {
                    BigDecimal value = dataItem.getCal();
                    data.add(value);
                    if (value != null) {
                        min = (min == null) ? value : (value.compareTo(min) < 0 ? value : min);
                        max = (max == null) ? value : (value.compareTo(max) > 0 ? value : max);
                        sum = sum.add(value);
                        count++;
                    }
                }
                break;
            case "gr":
                series.setName("自然伽马(GR)");
                series.setParam("gr");
                series.setUnit("API");
                for (LogData dataItem : dataList) {
                    BigDecimal value = dataItem.getGr();
                    data.add(value);
                    if (value != null) {
                        min = (min == null) ? value : (value.compareTo(min) < 0 ? value : min);
                        max = (max == null) ? value : (value.compareTo(max) > 0 ? value : max);
                        sum = sum.add(value);
                        count++;
                    }
                }
                break;
            case "den":
                series.setName("密度(DEN)");
                series.setParam("den");
                series.setUnit("g/cm³");
                for (LogData dataItem : dataList) {
                    BigDecimal value = dataItem.getDen();
                    data.add(value);
                    if (value != null) {
                        min = (min == null) ? value : (value.compareTo(min) < 0 ? value : min);
                        max = (max == null) ? value : (value.compareTo(max) > 0 ? value : max);
                        sum = sum.add(value);
                        count++;
                    }
                }
                break;
            case "rt":
                series.setName("深电阻率(RT)");
                series.setParam("rt");
                series.setUnit("Ω·m");
                for (LogData dataItem : dataList) {
                    BigDecimal value = dataItem.getRt();
                    data.add(value);
                    if (value != null) {
                        min = (min == null) ? value : (value.compareTo(min) < 0 ? value : min);
                        max = (max == null) ? value : (value.compareTo(max) > 0 ? value : max);
                        sum = sum.add(value);
                        count++;
                    }
                }
                break;
            case "rxo":
                series.setName("浅电阻率(RXO)");
                series.setParam("rxo");
                series.setUnit("Ω·m");
                for (LogData dataItem : dataList) {
                    BigDecimal value = dataItem.getRxo();
                    data.add(value);
                    if (value != null) {
                        min = (min == null) ? value : (value.compareTo(min) < 0 ? value : min);
                        max = (max == null) ? value : (value.compareTo(max) > 0 ? value : max);
                        sum = sum.add(value);
                        count++;
                    }
                }
                break;
            default:
                return null; // 未知参数，跳过
        }

        series.setData(data);

        // 计算参数范围（最值/平均值）
        RangeVO range = new RangeVO();
        range.setMin(min);
        range.setMax(max);
        if (count > 0) {
            range.setAvg(sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
        } else {
            range.setAvg(null);
        }
        series.setRange(range);

        return series;
    }

    /**
     * 获取测井数据统计信息
     */
    public Result<Map<String, Object>> getLogDataStatistics(Integer userId) {
        try {
            // 1. 参数校验
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }

            // 2. 查询总井数
            int totalWells = logDataMapper.countDistinctWellsByUserId(userId);

            // 3. 查询曲线总数
            int curvesCount = logDataMapper.countCurvesByUserId(userId);

            // 4. 查询平均准确率（假设confidence字段表示准确率）
            BigDecimal avgAccuracy = logDataMapper.getAverageConfidenceByUserId(userId);
            if (avgAccuracy == null) {
                avgAccuracy = BigDecimal.ZERO;
            }
            avgAccuracy = avgAccuracy.setScale(2, RoundingMode.HALF_UP);

            // 5. 查询平均处理时间（毫秒）
            Long processingTime = logDataMapper.getAverageProcessingTimeByUserId(userId);
            if (processingTime == null) {
                processingTime = 0L;
            }

            // 6. 查询井数月环比增长率
            BigDecimal wellsGrowthRate = logDataMapper.getWellsWeeklyGrowthRateByUserId(userId);
            if (wellsGrowthRate == null) {
                wellsGrowthRate = BigDecimal.ZERO;
            }
            wellsGrowthRate = wellsGrowthRate.setScale(2, RoundingMode.HALF_UP);

            // 7. 查询今日新增曲线数
            int curvesAddedToday = logDataMapper.countCurvesAddedTodayByUserId(userId);

            // 8. 查询准确率提升幅度
            BigDecimal accuracyLift = logDataMapper.getAccuracyLiftByUserId(userId);
            if (accuracyLift == null) {
                accuracyLift = BigDecimal.ZERO;
            }
            accuracyLift = accuracyLift.setScale(2, RoundingMode.HALF_UP);

            // 9. 查询处理时间节省率
            BigDecimal timeSavedRate = logDataMapper.getTimeSavedRateByUserId(userId);
            if (timeSavedRate == null) {
                timeSavedRate = BigDecimal.ZERO;
            }
            timeSavedRate = timeSavedRate.setScale(2, RoundingMode.HALF_UP);

            // 10. 构造返回结果
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalWells", totalWells);
            statistics.put("curvesCount", curvesCount);
            statistics.put("avgAccuracy", avgAccuracy);
            statistics.put("processingTime", processingTime);
            statistics.put("wellsGrowthRate", wellsGrowthRate);
            statistics.put("curvesAddedToday", curvesAddedToday);
            statistics.put("accuracyLift", accuracyLift);
            statistics.put("timeSavedRate", timeSavedRate);

            return Result.success(statistics);
        } catch (Exception e) {
            log.error("查询统计信息失败", e);
            return Result.error("查询统计信息失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户的数据集列表
     */
    public Result<List<String>> getDatasetList(Integer userId) {
        try {
            // 参数校验
            if (userId == null) {
                return Result.error("用户ID不能为空");
            }

            // 查询数据集列表
            List<String> datasetList = logDataMapper.selectDatasetNamesByUserId(userId);
            
            return Result.success(datasetList);
        } catch (Exception e) {
            log.error("查询数据集列表失败", e);
            return Result.error("查询数据集列表失败：" + e.getMessage());
        }
    }
}