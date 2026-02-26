package com.example.WellLogCurveSystem.controller;


import com.example.WellLogCurveSystem.entity.*;
import com.example.WellLogCurveSystem.service.LogDataService;
import com.example.WellLogCurveSystem.vo.DepthVariationVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;



@Slf4j
@RestController
@RequestMapping("/api/log-data")
public class LogDataController {

    @Autowired
    private LogDataService logDataService;

    // 时间格式化器：统一日期显示格式
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 添加测井数据记录
     */
    @PostMapping
    public Result<String> addLogData(@RequestBody LogData logData) {
        return logDataService.addLogData(logData);
    }

    /**
     * 删除测井数据记录
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteLogData(@PathVariable Long id) {
        return logDataService.deleteLogData(id);
    }

    /**
     * 更新测井数据记录
     */
    @PutMapping
    public Result<String> updateLogData(@RequestBody LogData logData) {
        return logDataService.updateLogData(logData);
    }

    /**
     * 根据ID查询测井数据记录
     */
    @GetMapping("/{id}")
    public Result<LogData> getLogDataById(@PathVariable Long id) {
        return logDataService.getLogDataById(id);
    }

    /**
     * 根据用户ID查询测井数据记录（支持分页和筛选）
     */
    @GetMapping("/by-user")
    public Result<PageResult<LogData>> getLogDataByUserId(
            @RequestParam Integer userId,
            @RequestParam(required = false) String datasetName,
            @RequestParam(required = false) String wellName,
            @RequestParam(required = false) BigDecimal minDepth,
            @RequestParam(required = false) BigDecimal maxDepth,
            @RequestParam(required = false) Integer isOriginal,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return logDataService.getLogDataByUserId(userId, datasetName, wellName, 
                                               minDepth, maxDepth, isOriginal, pageNum, pageSize);
    }

    /**
     * 根据井名查询测井数据记录（支持分页和筛选）
     */
    @GetMapping("/by-well")
    public Result<PageResult<LogData>> getLogDataByWellName(
            @RequestParam String wellName,
            @RequestParam Integer userId,  // 修改为必填
            @RequestParam(required = false) String datasetName,
            @RequestParam(required = false) BigDecimal minDepth,
            @RequestParam(required = false) BigDecimal maxDepth,
            @RequestParam(required = false) Integer isOriginal,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return logDataService.getLogDataByWellName(wellName, userId, datasetName,
                                                 minDepth, maxDepth, isOriginal, pageNum, pageSize);
    }

    /**
     * 根据数据集名查询测井数据记录（支持分页和筛选）
     */
    @GetMapping("/by-dataset")
    public Result<PageResult<LogData>> getLogDataByDatasetName(
            @RequestParam String datasetName,
            @RequestParam Integer userId,  // 修改为必填
            @RequestParam(required = false) String wellName,
            @RequestParam(required = false) BigDecimal minDepth,
            @RequestParam(required = false) BigDecimal maxDepth,
            @RequestParam(required = false) Integer isOriginal,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return logDataService.getLogDataByDatasetName(datasetName, userId, wellName,
                                                   minDepth, maxDepth, isOriginal, pageNum, pageSize);
    }

    /**
     * 查询所有测井数据记录（支持分页和筛选）
     */
    @GetMapping("/all")
    public Result<PageResult<LogData>> getAllLogData(
            @RequestParam Integer userId,
            @RequestParam(required = false) String datasetName,
            @RequestParam(required = false) String wellName,
            @RequestParam(required = false) BigDecimal minDepth,
            @RequestParam(required = false) BigDecimal maxDepth,
            @RequestParam(required = false) Integer isOriginal,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return logDataService.getAllLogData(userId, datasetName, wellName,
                                          minDepth, maxDepth, isOriginal, pageNum, pageSize);
    }
    /**
     * 核心功能：导出指定用户的所有测井数据为CSV（UTF-8编码+BOM头，解决中文乱码）
     * 接口地址：/api/logData/export/csv
     * 请求方式：GET
     * 请求参数：userId（必传，用户ID）
     */
    @GetMapping("/export/csv")
    public void exportLogDataCsv(@RequestParam Integer userId, HttpServletResponse response) throws Exception {
        // 1. 设置响应头：RFC标准格式，兼容所有浏览器，中文文件名无乱码
        response.setContentType("text/csv;charset=utf-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        String fileName = "测井数据全量导出.csv";
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
        response.setHeader("Content-Disposition",
                String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s", encodedFileName, encodedFileName));

        try (
                // 2. 显式强制UTF-8编码，保证写入内容编码统一
                OutputStreamWriter osw = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
                PrintWriter writer = new PrintWriter(osw)
        ) {
            // 写入UTF-8 BOM头：让Excel/WPS自动识别UTF-8编码，彻底解决中文乱码
            writer.write('\ufeff');

            // 3. 写入CSV表头：与LogData实体字段一一对应，中文清晰易读
            writer.write("数据ID,用户ID,数据集名,井名,深度(m),声波时差(AC),井径(CAL),自然伽马(GR),密度(DEN),深电阻率(RT),浅电阻率(RXO),是否原始数据,预测置信度,创建时间,修改时间\n");

            // 4. 调用Service查询该用户的所有测井数据（不分页，全量导出）
            // 分页参数：pageNum=1，pageSize=Integer.MAX_VALUE 实现全量查询
            Result<PageResult<LogData>> result = logDataService.getLogDataByUserId(
                    userId, null, null, null, null, null, 1, Integer.MAX_VALUE
            );

            // 5. 处理查询结果，写入CSV数据
            if (result.getCode() != 0 || result.getData() == null || result.getData().getRecords().isEmpty()) {
                // 无数据时写入提示，保证CSV格式合法
                writer.write(",,该用户暂无测井数据记录,,,,,,,,,,,,,\n");
                writer.flush();
                log.info("用户{}无测井数据，导出空CSV文件", userId);
                return;
            }

            List<LogData> logDataList = result.getData().getRecords();
            // 遍历数据，格式化写入（处理null值、数值、日期类型）
            for (LogData logData : logDataList) {
                String csvLine = String.format(
                        "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        getValue(logData.getId()),          // 数据ID
                        getValue(logData.getUserId()),      // 用户ID
                        getValue(logData.getDatasetName()),// 数据集名
                        getValue(logData.getWellName()),   // 井名
                        getValue(logData.getDepth()),      // 深度
                        getValue(logData.getAc()),         // 声波时差
                        getValue(logData.getCal()),        // 井径
                        getValue(logData.getGr()),         // 自然伽马
                        getValue(logData.getDen()),        // 密度
                        getValue(logData.getRt()),         // 深电阻率
                        getValue(logData.getRxo()),        // 浅电阻率
                        // 转换是否原始数据为中文：1-是，0-否，null-未知
                        convertIsOriginal(logData.getIsOriginal()),
                        getValue(logData.getConfidence()), // 预测置信度
                        formatDate(logData.getCreateTime()),// 创建时间
                        formatDate(logData.getUpdateTime()) // 修改时间
                );
                writer.write(csvLine);
            }
            writer.flush();
            log.info("用户{}测井数据CSV导出成功，共{}条记录", userId, logDataList.size());

        } catch (Exception e) {
            // 6. 异常处理：重置响应，返回JSON格式错误信息，让前端正常捕获
            log.error("用户{}测井数据CSV导出失败：{}", userId, e.getMessage(), e);
            response.reset();
            response.setContentType("application/json;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"code\":500,\"message\":\"测井数据导出失败：" + e.getMessage().replace("\"", "\\\"") + "\"}");
            response.getWriter().flush();
        }
    }
    /**
     * 查询单井测井参数随深度变化的数据（适配ECharts可视化）
     */
    @GetMapping("/depth-variation")
    public Result<DepthVariationVO> getDepthVariationData(
            @RequestParam Integer userId,
            @RequestParam String datasetName,
            @RequestParam String wellName,
            @RequestParam(required = false) BigDecimal minDepth,
            @RequestParam(required = false) BigDecimal maxDepth,
            @RequestParam(required = false, defaultValue = "ac,cal,gr,den,rt,rxo") String params,
            @RequestParam(required = false, defaultValue = "1") Integer sampleRate,
            @RequestParam(required = false) Integer isOriginal) {
        return logDataService.getDepthVariationData(
                userId, datasetName, wellName, minDepth, maxDepth, params, sampleRate, isOriginal
        );
    }

    /**
     * 获取测井数据统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getLogDataStatistics(@RequestParam Integer userId) {
        return logDataService.getLogDataStatistics(userId);
    }

    /**
     * 获取用户的数据集列表
     */
    @GetMapping("/datasets")
    public Result<List<String>> getDatasetList(@RequestParam Integer userId) {
        // 校验用户ID
        if (userId == null || userId <= 0) {
            return Result.error("用户ID不能为空且必须为正整数");
        }
        
        return logDataService.getDatasetList(userId);
    }

    // 工具方法：处理null值，避免CSV写入空指针，null返回空字符串
    private String getValue(Object obj) {
        return Objects.isNull(obj) ? "" : obj.toString();
    }

    // 工具方法：格式化日期，null返回空字符串
    private String formatDate(java.util.Date date) {
        return Objects.isNull(date) ? "" : DATE_FORMAT.format(date);
    }

    // 工具方法：转换是否原始数据为中文，提升CSV可读性
    private String convertIsOriginal(Integer isOriginal) {
        if (isOriginal == 1) {
            return "是";
        } else if (isOriginal == 0) {
            return "否";
        } else {
            return "";
        }
    }
}