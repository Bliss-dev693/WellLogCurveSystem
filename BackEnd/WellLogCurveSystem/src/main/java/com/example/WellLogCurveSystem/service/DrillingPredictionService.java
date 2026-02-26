package com.example.WellLogCurveSystem.service;

import com.example.WellLogCurveSystem.dto.HistoryQuery;
import com.example.WellLogCurveSystem.dto.PredictionResponse;
import com.example.WellLogCurveSystem.entity.*;
import com.example.WellLogCurveSystem.mapper.PredictionHistoryMapper;
import com.example.WellLogCurveSystem.mapper.UserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * 钻井预测服务（最终版：保证JSON序列化和正确格式100%一致）
 */
@Slf4j
@Service
public class DrillingPredictionService {

    private final PredictionHistoryMapper predictionHistoryMapper;
    private final UserMapper userMapper; // 添加用户mapper用于验证用户ID
    private final ObjectMapper objectMapper;
    
    @Autowired
    public DrillingPredictionService(PredictionHistoryMapper predictionHistoryMapper, 
                                   UserMapper userMapper,
                                   ObjectMapper objectMapper) {
        this.predictionHistoryMapper = predictionHistoryMapper;
        this.userMapper = userMapper; // 注入用户mapper
        // 配置ObjectMapper：禁用多余格式化，保证JSON和正确格式完全一致
        this.objectMapper = objectMapper
                .disable(SerializationFeature.INDENT_OUTPUT) // 禁用自动换行缩进
                .setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL) // 不输出null字段
                .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
    }

    // 第三方接口地址
    private static final String PREDICTION_API_URL = "http://mengtty.cn/drilling/predict";

    /**
     * 查询用户的历史记录
     */
    public Result<PageResult<PredictionHistory>> queryHistory(HistoryQuery query) {
        try {
            if (query.getUserId() == null) {
                return Result.error("用户ID不能为空");
            }

            // 验证用户ID是否存在
            User user = findUserById(query.getUserId());
            if (user == null) {
                return Result.error("用户ID不存在：" + query.getUserId());
            }

            // 验证分页参数，设置默认值
            Integer pageNum = query.getPageNum();
            if (pageNum == null || pageNum < 1) {
                pageNum = 1; // 默认第一页
            }

            Integer pageSize = query.getPageSize();
            if (pageSize == null || pageSize < 1) {
                pageSize = 10; // 默认每页10条
            } else if (pageSize > 100) {
                pageSize = 100; // 限制最大页面大小
            }

            // 计算分页偏移量
            int offset = (pageNum - 1) * pageSize;

            // 查询历史记录
            List<PredictionHistory> histories = predictionHistoryMapper.findByUserId(
                query.getUserId(),
                query.getDatasetName(),
                query.getWellName(),
                query.getStatus(),
                offset,
                pageSize
            );

            // 统计总记录数
            int totalCount = predictionHistoryMapper.countByUserId(
                query.getUserId(),
                query.getDatasetName(),
                query.getWellName(),
                query.getStatus()
            );

            // 构造分页结果
            PageResult<PredictionHistory> pageResult = new PageResult<>(
                (long) totalCount,
                pageNum,
                pageSize,
                histories
            );

            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("查询历史记录失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据井名查询历史记录
     */
    public Result<PageResult<PredictionHistory>> queryHistoryByWell(String wellName, String datasetName, 
                                                                  String status, Integer pageNum, Integer pageSize) {
        try {
            if (wellName == null || wellName.trim().isEmpty()) {
                return Result.error("井名不能为空");
            }

            // 验证分页参数
            if (pageNum == null || pageNum < 1) pageNum = 1;
            if (pageSize == null || pageSize < 1) pageSize = 10;
            if (pageSize > 100) pageSize = 100; // 限制最大页面大小

            // 计算分页偏移量
            int offset = (pageNum - 1) * pageSize;
            
            // 查询历史记录
            List<PredictionHistory> histories = predictionHistoryMapper.findByWellName(
                wellName,
                datasetName,
                status,
                offset,
                pageSize
            );
            
            // 统计总记录数
            int totalCount = predictionHistoryMapper.countByWellName(
                wellName,
                datasetName,
                status
            );

            // 构造分页结果
            PageResult<PredictionHistory> pageResult = new PageResult<>(
                (long) totalCount,
                pageNum,
                pageSize,
                histories
            );

            return Result.success(pageResult);
        } catch (Exception e) {
            log.error("根据井名查询历史记录失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 调用第三方预测接口并保存记录
     */
    public Result<Double> predict(Integer userId, String datasetName, String wellName, 
                                  String depthRange, List<TimeStepData> requestData) {
        // 1. 参数校验：必须是5个时间步
        if (requestData == null || requestData.size() != 5) {
            String errorMsg = "请求参数错误：必须包含且仅包含5个时间步的测井参数，当前数量：" + (requestData == null ? 0 : requestData.size());
            log.error(errorMsg);
            return Result.error(errorMsg);
        }

        // 2. 验证用户ID是否存在
        if (userId == null) {
            String errorMsg = "用户ID不能为空";
            log.error(errorMsg);
            return Result.error(errorMsg);
        }
        
        User user = findUserById(userId);
        if (user == null) {
            String errorMsg = "用户ID不存在：" + userId;
            log.error(errorMsg);
            return Result.error(errorMsg);
        }

        // 3. 校验每个时间步的参数是否完整（避免null导致序列化缺失）
        for (int i = 0; i < requestData.size(); i++) {
            TimeStepData step = requestData.get(i);
            if (step == null || step.getData() == null || step.getData().getParameters() == null) {
                String errorMsg = "第" + (i+1) + "个时间步数据缺失：data.parameters 不能为空";
                log.error(errorMsg);
                return Result.error(errorMsg);
            }
            TimeStepData.Parameters params = step.getData().getParameters();
            if (params.getAC() == null || params.getGR() == null || params.getRT() == null || params.getRXO() == null) {
                String errorMsg = "第" + (i+1) + "个时间步参数缺失：AC/GR/RT/RXO 不能为空";
                log.error(errorMsg);
                return Result.error(errorMsg);
            }
        }

        long startTime = System.currentTimeMillis();
        String inputDataJson = null;
        try {
            // 4. 序列化请求数据（保证和正确格式100%一致）
            inputDataJson = objectMapper.writeValueAsString(requestData);
            // 打印对比：格式化后便于查看（和你提供的正确格式对比）
            String formattedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestData);
            log.info("【第三方接口请求】序列化后的JSON（和正确格式对比）：\n{}", formattedJson);

            // 5. 调用第三方接口（直接传递序列化后的JSON字符串）
            WebClient webClient = WebClient.create();
            String thirdPartyRawResponse = webClient.post()
                    .uri(PREDICTION_API_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(inputDataJson) // 传递精准序列化的JSON
                    .retrieve()
                    // 修复：使用Lambda表达式替代静态方法引用
                    .onStatus(statusCode -> statusCode.isError(), clientResponse -> {
                        return clientResponse.bodyToMono(String.class)
                                .map(errorBody -> {
                                    String errorMsg = String.format("第三方接口返回错误，状态码：%s，响应体：%s",
                                            clientResponse.statusCode(), errorBody);
                                    log.error(errorMsg);
                                    return new RuntimeException(errorMsg);
                                });
                    })
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            log.info("【第三方接口响应】原始响应体：\n{}", thirdPartyRawResponse);

            // 6. 反序列化响应结果
            PredictionResponse response = objectMapper.readValue(thirdPartyRawResponse, PredictionResponse.class);

            // 7. 处理响应并保存记录
            long executionTime = System.currentTimeMillis() - startTime;
            if (response == null || !"success".equals(response.getStatus())) {
                String errorMessage = response == null ? "第三方接口返回空响应" : "第三方接口返回失败，状态：" + response.getStatus();
                saveHistory(userId, datasetName, wellName, depthRange, inputDataJson, 
                            null, "error", errorMessage, executionTime);
                log.error(errorMessage);
                return Result.error("预测失败：" + errorMessage);
            }

            // 8. 保存成功记录并返回结果
            BigDecimal predictionValue = BigDecimal.valueOf(response.getPrediction());
            saveHistory(userId, datasetName, wellName, depthRange, inputDataJson, 
                        predictionValue, "success", "", executionTime);

            log.info("【预测成功】用户ID：{}，预测结果：{}，耗时：{}ms", userId, response.getPrediction(), executionTime);
            return Result.success(response.getPrediction());

        } catch (JsonProcessingException e) {
            String errorMsg = "请求数据JSON序列化/反序列化失败：" + e.getMessage();
            log.error(errorMsg, e);
            saveHistory(userId, datasetName, wellName, depthRange, inputDataJson, 
                        null, "error", errorMsg, System.currentTimeMillis() - startTime);
            return Result.error(errorMsg);
        } catch (Exception e) {
            String errorMsg = "系统异常：" + e.getMessage();
            log.error(errorMsg, e);
            saveHistory(userId, datasetName, wellName, depthRange, inputDataJson, 
                        null, "error", errorMsg, System.currentTimeMillis() - startTime);
            return Result.error(Result.SERVER_ERROR_CODE, errorMsg);
        }
    }

    /**
     * 保存预测历史记录到数据库
     */
    private void saveHistory(Integer userId, String datasetName, String wellName,
                             String depthRange, String inputDataJson, BigDecimal predictionValue,
                             String status, String errorMessage, long executionTime) {
        try {
            // 验证用户ID是否存在（双重保险）
            User user = findUserById(userId);
            if (user == null) {
                log.error("警告：用户ID {} 不存在，无法保存历史记录", userId);
                return;
            }
            
            PredictionHistory history = new PredictionHistory();
            history.setUserId(userId);
            history.setDatasetName(datasetName);
            history.setWellName(wellName);
            history.setDepthRange(depthRange);
            history.setInputPredictionData(inputDataJson);
            history.setCnlPrediction(predictionValue);
            history.setStatus(status);
            history.setErrorMessage(errorMessage);
            history.setExecutionTime(executionTime);
            
            predictionHistoryMapper.insert(history);
            log.info("【记录保存成功】用户ID：{}，状态：{}", userId, status);
        } catch (Exception e) {
            log.error("保存预测历史记录失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 通过ID查找用户（辅助方法）
     */
    private User findUserById(Integer userId) {
        try {
            return userMapper.findById(userId);
        } catch (Exception e) {
            log.warn("按ID查找用户失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 新增：按用户ID查询所有历史预测记录（无分页，用于CSV导出）
     * @param userId 用户ID
     * @return 该用户所有历史记录，无数据返回空集合
     */
    public List<PredictionHistory> findAllHistoryForExport(Integer userId) {
        // 1. 参数非空校验
        if (userId == null) {
            log.error("导出CSV失败：用户ID不能为空");
            return Collections.emptyList();
        }
        // 2. 验证用户是否存在
        User user = findUserById(userId);
        if (user == null) {
            log.error("导出CSV失败：用户ID不存在，userId={}", userId);
            return Collections.emptyList();
        }
        // 3. 查询所有记录
        try {
            List<PredictionHistory> historyList = predictionHistoryMapper.findAllByUserId(userId);
            log.info("用户{}的历史预测记录查询成功，共{}条数据（用于CSV导出）", userId, historyList.size());
            return historyList;
        } catch (Exception e) {
            log.error("查询用户{}所有历史记录失败（CSV导出）：{}", userId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}