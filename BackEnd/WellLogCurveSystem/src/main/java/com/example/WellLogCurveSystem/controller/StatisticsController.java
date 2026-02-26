package com.example.WellLogCurveSystem.controller;

import com.example.WellLogCurveSystem.entity.Result;
import com.example.WellLogCurveSystem.service.StatisticsDataService;
import com.example.WellLogCurveSystem.vo.StatisticsDataVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

/**
 * 系统统计接口
 */
@RestController
@RequestMapping("/api/statistics")
@Slf4j
public class StatisticsController {

    @Resource
    private StatisticsDataService statisticsDataService;

    /**
     * 获取系统全局统计数据
     * @param userId 用户ID（必填）
     * @return 统计数据VO
     */
    @GetMapping("/system")
    public Result<StatisticsDataVO> getSystemStatistics(
            @RequestParam(required = true) Integer userId) {
        
        // 参数校验
        if (userId == null || userId <= 0) {
            return Result.error("用户ID不能为空且必须为正整数");
        }
        
        return statisticsDataService.getUserStatistics(userId);
    }
}