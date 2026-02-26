package com.example.WellLogCurveSystem.service;

import com.example.WellLogCurveSystem.dto.TemplateQueryDTO;
import com.example.WellLogCurveSystem.entity.PageResult;
import com.example.WellLogCurveSystem.entity.ReportTemplate;

import java.util.List;

public interface ReportTemplateService {

    /**
     * 分页查询模板列表
     */
    PageResult<ReportTemplate> getTemplateList(TemplateQueryDTO queryDTO);

    /**
     * 获取模板详情
     */
    ReportTemplate getTemplateDetail(Long id);

    /**
     * 创建模板
     */
    boolean createTemplate(ReportTemplate template);

    /**
     * 更新模板
     */
    boolean updateTemplate(ReportTemplate template);

    /**
     * 删除模板
     */
    boolean deleteTemplate(Long id);

    /**
     * 使用模板生成报告
     */
    String generateReportFromTemplate(Long templateId, Object reportData);

    /**
     * 获取模板分类列表
     */
    List<String> getTemplateCategories();

    /**
     * 模板使用统计
     */
    List<ReportTemplate> getTemplateUsageStats(Integer userId);
}