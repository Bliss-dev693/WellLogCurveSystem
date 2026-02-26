package com.example.WellLogCurveSystem.service;


import com.example.WellLogCurveSystem.entity.GeneratedReport;

import java.util.List;
import java.util.Map;

public interface ReportGenerationService {

    /**
     * 生成报告预览
     */
    String generateReportPreview(Long templateId, Map<String, Object> reportData);

    /**
     * 生成最终报告
     */
    GeneratedReport generateFinalReport(Long templateId, Map<String, Object> reportData, Integer userId);

    /**
     * 获取报告历史记录
     */
    List<GeneratedReport> getReportHistory(Integer userId);

    /**
     * 获取报告详情
     */
    GeneratedReport getReportDetail(Long id);

    /**
     * 下载报告
     */
    byte[] downloadReport(Long id, String format);
}