package com.example.WellLogCurveSystem.service.impl;


import com.example.WellLogCurveSystem.entity.GeneratedReport;
import com.example.WellLogCurveSystem.entity.ReportTemplate;
import com.example.WellLogCurveSystem.mapper.GeneratedReportMapper;
import com.example.WellLogCurveSystem.mapper.ReportTemplateMapper;
import com.example.WellLogCurveSystem.service.ReportGenerationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.List;

@Service
public class ReportGenerationServiceImpl implements ReportGenerationService {

    @Autowired
    private ReportTemplateMapper templateMapper;

    @Autowired
    private GeneratedReportMapper reportMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String generateReportPreview(Long templateId, Map<String, Object> reportData) {
        ReportTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }

        // 处理模板变量替换（支持多种占位符格式）
        String content = template.getDocumentUrl();
        for (Map.Entry<String, Object> entry : reportData.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            
            // 支持 {{variable}} 格式
            content = content.replace("{{" + key + "}}", value);
            // 支持 ${variable} 格式
            content = content.replace("${" + key + "}", value);
            // 支持 {variable} 格式
            content = content.replace("{" + key + "}", value);
        }

        return content;
    }

    @Override
    @Transactional
    public GeneratedReport generateFinalReport(Long templateId, Map<String, Object> reportData, Integer userId) {
        ReportTemplate template = templateMapper.selectById(templateId);
        if (template == null) {
            throw new RuntimeException("模板不存在");
        }

        try {
            // 生成报告内容
            String content = generateReportPreview(templateId, reportData);

            // 创建报告记录
            GeneratedReport report = new GeneratedReport();
            report.setTemplateId(templateId);
            report.setUserId(userId);
            report.setReportTitle((String) reportData.getOrDefault("reportTitle", "分析报告"));
            report.setContent(content);
            report.setStatus(1); // 1表示完成

            // 序列化参数
            String parameters = objectMapper.writeValueAsString(reportData);
            report.setParameters(parameters);

            // 保存到数据库
            reportMapper.insert(report);

            // 增加模板使用次数
            templateMapper.incrementUsageCount(templateId);

            return report;
        } catch (Exception e) {
            throw new RuntimeException("报告生成失败: " + e.getMessage());
        }
    }

    @Override
    public List<GeneratedReport> getReportHistory(Integer userId) {
        return reportMapper.selectByUserId(userId);
    }

    @Override
    public GeneratedReport getReportDetail(Long id) {
        return reportMapper.selectById(id);
    }

    @Override
    public byte[] downloadReport(Long id, String format) {
        GeneratedReport report = reportMapper.selectById(id);
        if (report == null) {
            throw new RuntimeException("报告不存在");
        }

        // 增加下载次数
        reportMapper.incrementDownloadCount(id);

        // 根据格式返回不同的字节数据
        switch (format.toLowerCase()) {
            case "pdf":
                return generatePdfReport(report);
            case "word":
                return generateWordReport(report);
            case "markdown":
            default:
                return report.getContent().getBytes();
        }
    }

    private byte[] generatePdfReport(GeneratedReport report) {
        // PDF生成逻辑（可以使用iText等库）
        return report.getContent().getBytes();
    }

    private byte[] generateWordReport(GeneratedReport report) {
        // Word生成逻辑（可以使用Apache POI等库）
        return report.getDocumentUrl().getBytes();
    }
}