package com.example.WellLogCurveSystem.controller;

import com.example.WellLogCurveSystem.dto.HistoryQuery;
import com.example.WellLogCurveSystem.entity.PageResult;
import com.example.WellLogCurveSystem.entity.PredictionHistory;
import com.example.WellLogCurveSystem.entity.Result;
import com.example.WellLogCurveSystem.entity.TimeStepData;
import com.example.WellLogCurveSystem.service.DrillingPredictionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/drilling")
public class DrillingPredictionController {
    @Autowired
    private DrillingPredictionService predictionService;

    /**
     * 钻井工程预测接口
     * @param userId        用户ID
     * @param datasetName   数据集名
     * @param wellName      井名
     * @param depthRange    深度范围
     * @param requestData   5个时间步的测井参数
     * @return 预测结果
     */
    @PostMapping("/predict")
    public Result<Double> predict(
            @RequestParam Integer userId,
            @RequestParam String datasetName,
            @RequestParam String wellName,
            @RequestParam String depthRange,
            @Valid @RequestBody List<TimeStepData> requestData) {
        
        return predictionService.predict(userId, datasetName, wellName, depthRange, requestData);
    }

    /**
     * 查询用户历史记录接口
     * @param query 查询参数
     * @return 分页的历史记录
     */
    @GetMapping("/history")
    public Result<PageResult<PredictionHistory>> getHistory(HistoryQuery query) {
        return predictionService.queryHistory(query);
    }

    /**
     * 根据ID获取单条历史记录
     * @param id 历史记录ID
     * @return 单条历史记录
     */
    @GetMapping("/history/{id}")
    public Result<PredictionHistory> getHistoryById(@PathVariable Long id) {
        // 由于Mapper中没有按ID查询的方法，这里暂时返回错误
        // 如果需要按ID查询，还需要在Mapper中添加相应方法
        return Result.error("按ID查询历史记录功能暂未实现");
    }

    /**
     * 根据井名查询历史记录
     * @param wellName 井名
     * @param datasetName 数据集名（可选）
     * @param status 状态（可选）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页的历史记录
     */
    @GetMapping("/history/by-well")
    public Result<PageResult<PredictionHistory>> getHistoryByWell(
            @RequestParam String wellName,
            @RequestParam(required = false) String datasetName,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        return predictionService.queryHistoryByWell(wellName, datasetName, status, pageNum, pageSize);
    }
}