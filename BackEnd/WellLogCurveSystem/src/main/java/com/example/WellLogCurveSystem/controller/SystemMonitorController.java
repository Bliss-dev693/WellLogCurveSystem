package com.example.WellLogCurveSystem.controller;

import com.example.WellLogCurveSystem.entity.Result;
import com.example.WellLogCurveSystem.service.SystemMonitorService;
import com.example.WellLogCurveSystem.vo.SystemMonitorVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统监控接口（前端可视化专用）
 */
@RestController
@RequestMapping("/api/monitor")
@Slf4j
public class SystemMonitorController {

    @Autowired
    private SystemMonitorService systemMonitorService;

    /**
     * 获取全量系统监控数据
     * 前端可定时（如每5秒）调用此接口更新可视化数据
     */
    @GetMapping("/system")
    public Result<SystemMonitorVO> getSystemMonitorData() {
        return systemMonitorService.getSystemMonitorData();
    }
}