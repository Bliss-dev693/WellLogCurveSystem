package com.example.WellLogCurveSystem.service.impl;

import com.example.WellLogCurveSystem.dto.TemplateQueryDTO;
import com.example.WellLogCurveSystem.entity.PageResult;
import com.example.WellLogCurveSystem.entity.ReportTemplate;
import com.example.WellLogCurveSystem.entity.User;
import com.example.WellLogCurveSystem.mapper.ReportTemplateMapper;
import com.example.WellLogCurveSystem.mapper.UserMapper;
import com.example.WellLogCurveSystem.service.ReportTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class ReportTemplateServiceImpl implements ReportTemplateService {

    @Autowired
    private ReportTemplateMapper templateMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    public PageResult<ReportTemplate> getTemplateList(TemplateQueryDTO queryDTO) {
        // 计算偏移量
        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        
        // 查询数据列表
        List<ReportTemplate> records = templateMapper.selectTemplateList(
                queryDTO.getKeyword(),
                queryDTO.getCategory(),
                queryDTO.getUserId(),
                queryDTO.getIsPublic()
        );
        
        // 获取总记录数
        int total = templateMapper.countTemplates(
                queryDTO.getKeyword(),
                queryDTO.getCategory(),
                queryDTO.getUserId(),
                queryDTO.getIsPublic()
        );
        
        return new PageResult<>((long) total, queryDTO.getPageNum(), queryDTO.getPageSize(), records);
    }

    @Override
    public ReportTemplate getTemplateDetail(Long id) {
        return templateMapper.selectById(id);
    }

    @Override
    public boolean createTemplate(ReportTemplate template) {
        // 验证用户ID是否存在
        if (template.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        User user = userMapper.findById(template.getUserId());
        if (user == null) {
            throw new RuntimeException("用户ID不存在：" + template.getUserId());
        }
        
        template.setStatus(1);
        template.setUsageCount(0);
        template.setCreateTime(LocalDateTime.now());
        template.setUpdateTime(LocalDateTime.now());
        return templateMapper.insert(template) > 0;
    }

    @Override
    public boolean updateTemplate(ReportTemplate template) {
        ReportTemplate existing = templateMapper.selectById(template.getId());
        if (existing != null && existing.getStatus() == 1) {
            // 验证用户ID是否存在
            if (template.getUserId() != null) {
                User user = userMapper.findById(template.getUserId());
                if (user == null) {
                    throw new RuntimeException("用户ID不存在：" + template.getUserId());
                }
            }
            
            template.setUpdateTime(LocalDateTime.now());
            return templateMapper.updateById(template) > 0;
        }
        return false;
    }

    @Override

    public boolean deleteTemplate(Long id) {
        ReportTemplate template = templateMapper.selectById(id);
        if (template != null) {
            return templateMapper.deleteById(id) > 0;
        }
        return false;
    }

    @Override
    public String generateReportFromTemplate(Long templateId, Object reportData) {
        ReportTemplate template = templateMapper.selectById(templateId);
        if (template == null || template.getStatus() != 1) {
            throw new RuntimeException("模板不存在或已被删除");
        }

        // 增加使用次数
        templateMapper.incrementUsageCount(templateId);

        // 处理模板变量替换
        String content = template.getDocumentUrl();
        // 这里可以实现变量替换逻辑
        // 示例：简单的占位符替换
        if (reportData != null && reportData instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> dataMap = (java.util.Map<String, Object>) reportData;
            for (java.util.Map.Entry<String, Object> entry : dataMap.entrySet()) {
                content = content.replace("${" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }
        }

        return content;
    }

    @Override
    public List<String> getTemplateCategories() {
        return Arrays.asList("basic", "technical", "comprehensive", "specialized", "custom");
    }

    @Override
    public List<ReportTemplate> getTemplateUsageStats(Integer userId) {
        return templateMapper.getTemplateUsageStats(userId);
    }
}