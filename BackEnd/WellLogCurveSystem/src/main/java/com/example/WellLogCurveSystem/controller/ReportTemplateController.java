package com.example.WellLogCurveSystem.controller;

import com.example.WellLogCurveSystem.dto.TemplateQueryDTO;
import com.example.WellLogCurveSystem.entity.PageResult;
import com.example.WellLogCurveSystem.entity.ReportTemplate;
import com.example.WellLogCurveSystem.entity.Result;
import com.example.WellLogCurveSystem.service.ReportTemplateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report/templates")
public class ReportTemplateController {

    @Autowired
    private ReportTemplateService templateService;

    /**
     * 获取模板列表
     */
    @GetMapping
    public Result<PageResult<ReportTemplate>> getTemplateList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = true) Integer userId,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        // 参数校验
        if (userId == null || userId <= 0) {
            return Result.error("用户ID不能为空且必须为正整数");
        }
        
        TemplateQueryDTO queryDTO = new TemplateQueryDTO();
        queryDTO.setKeyword(keyword);
        queryDTO.setCategory(category);
        queryDTO.setUserId(userId);
        queryDTO.setIsPublic(isPublic);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);

        PageResult<ReportTemplate> result = templateService.getTemplateList(queryDTO);
        return Result.success(result);
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/{id}")
    public Result<ReportTemplate> getTemplateDetail(@PathVariable Long id,
                                                   @RequestParam(required = true) Integer userId) {
        // 参数校验
        if (userId == null || userId <= 0) {
            return Result.error("用户ID不能为空且必须为正整数");
        }
        
        ReportTemplate template = templateService.getTemplateDetail(id);
        if (template != null) {
            return Result.success(template);
        }
        return Result.error("模板不存在");
    }

    /**
     * 创建模板
     */
    @PostMapping
    public Result<String> createTemplate(@RequestBody @Valid ReportTemplate template,
                                       @RequestParam(required = true) Integer userId) {
        // 参数校验
        if (userId == null || userId <= 0) {
            return Result.error("用户ID不能为空且必须为正整数");
        }
        
        // 验证必要字段
        if (template.getName() == null || template.getName().trim().isEmpty()) {
            return Result.error("模板名称不能为空");
        }
        
        if (template.getCategory() == null || template.getCategory().trim().isEmpty()) {
            return Result.error("模板分类不能为空");
        }
        
        if (template.getDocumentUrl() == null || template.getDocumentUrl().trim().isEmpty()) {
            return Result.error("模板文档URL不能为空");
        }
        
        // 设置用户ID
        template.setUserId(userId);
        boolean success = templateService.createTemplate(template);
        if (success) {
            return Result.success("模板创建成功");
        }
        return Result.error("模板创建失败");
    }

    /**
     * 更新模板
     */
    @PutMapping("/{id}")
    public Result<String> updateTemplate(@PathVariable Long id,
                                         @RequestBody @Valid ReportTemplate template,
                                         @RequestParam(required = true) Integer userId) {
        // 参数校验
        if (userId == null || userId <= 0) {
            return Result.error("用户ID不能为空且必须为正整数");
        }
        
        // 验证必要字段
        if (template.getName() == null || template.getName().trim().isEmpty()) {
            return Result.error("模板名称不能为空");
        }
        
        if (template.getCategory() == null || template.getCategory().trim().isEmpty()) {
            return Result.error("模板分类不能为空");
        }
        
        if (template.getDocumentUrl() == null || template.getDocumentUrl().trim().isEmpty()) {
            return Result.error("模板文档URL不能为空");
        }
        
        template.setId(id);
        template.setUserId(userId);
        boolean success = templateService.updateTemplate(template);
        if (success) {
            return Result.success("模板更新成功");
        }
        return Result.error("模板更新失败");
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteTemplate(@PathVariable Long id,
                                       @RequestParam(required = true) Integer userId) {
        // 参数校验
        if (userId == null || userId <= 0) {
            return Result.error("用户ID不能为空且必须为正整数");
        }
        
        boolean success = templateService.deleteTemplate(id);
        if (success) {
            return Result.success("模板删除成功");
        }
        return Result.error("模板删除失败");
    }

    /**
     * 使用模板生成报告
     */
    @PostMapping("/{id}/generate")
    public Result<String> generateReportFromTemplate(@PathVariable Long id,
                                                     @RequestBody Map<String, Object> reportData,
                                                     @RequestParam(required = true) Integer userId) {
        try {
            // 参数校验
            if (userId == null || userId <= 0) {
                return Result.error("用户ID不能为空且必须为正整数");
            }
            
            String reportContent = templateService.generateReportFromTemplate(id, reportData);
            return Result.success(reportContent);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取模板分类列表
     */
    @GetMapping("/categories")
    public Result<List<String>> getTemplateCategories() {
        List<String> categories = templateService.getTemplateCategories();
        return Result.success(categories);
    }

    /**
     * 模板使用统计
     */
    @GetMapping("/stats")
    public Result<List<ReportTemplate>> getTemplateUsageStats(
            @RequestParam(required = true) Integer userId) {
        // 参数校验
        if (userId == null || userId <= 0) {
            return Result.error("用户ID不能为空且必须为正整数");
        }
        
        List<ReportTemplate> stats = templateService.getTemplateUsageStats(userId);
        return Result.success(stats);
    }
}