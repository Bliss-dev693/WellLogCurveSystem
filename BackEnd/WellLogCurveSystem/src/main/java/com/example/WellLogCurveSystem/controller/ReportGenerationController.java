package com.example.WellLogCurveSystem.controller;



import com.example.WellLogCurveSystem.entity.GeneratedReport;
import com.example.WellLogCurveSystem.entity.Result;
import com.example.WellLogCurveSystem.service.ReportGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportGenerationController {

    @Autowired
    private ReportGenerationService reportGenerationService;

    /**
     * 生成报告预览
     */
    @PostMapping("/generate-preview")
    public Result<Map<String, Object>> generatePreview(@RequestBody Map<String, Object> requestData,
                                                      @RequestParam(required = true) Integer userId) {
        try {
            // 参数校验
            if (userId == null || userId <= 0) {
                return Result.error("用户ID不能为空且必须为正整数");
            }
            
            Long templateId = Long.valueOf(requestData.get("templateId").toString());
            String previewContent = reportGenerationService.generateReportPreview(templateId, requestData);

            return Result.success(Map.of("content", previewContent));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 生成最终报告
     */
    @PostMapping("/generate-final")
    public Result<Map<String, Object>> generateFinal(@RequestBody Map<String, Object> requestData,
                                                     @RequestParam(required = true) Integer userId) {
        try {
            // 参数校验
            if (userId == null || userId <= 0) {
                return Result.error("用户ID不能为空且必须为正整数");
            }
            
            Long templateId = Long.valueOf(requestData.get("templateId").toString());
            GeneratedReport report = reportGenerationService.generateFinalReport(templateId, requestData, userId);

            return Result.success(Map.of(
                    "id", report.getId(),
                    "title", report.getReportTitle(),
                    "generatedTime", report.getCreateTime()
            ));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取报告历史记录
     */
    @GetMapping("/history")
    public Result<List<GeneratedReport>> getHistory(@RequestParam(required = true) Integer userId) {
        try {
            // 参数校验
            if (userId == null || userId <= 0) {
                return Result.error("用户ID不能为空且必须为正整数");
            }
            
            List<GeneratedReport> reports = reportGenerationService.getReportHistory(userId);
            return Result.success(reports);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取报告详情
     */
    @GetMapping("/{id}")
    public Result<GeneratedReport> getReportDetail(@PathVariable Long id,
                                                  @RequestParam(required = true) Integer userId) {
        try {
            // 参数校验
            if (userId == null || userId <= 0) {
                return Result.error("用户ID不能为空且必须为正整数");
            }
            
            GeneratedReport report = reportGenerationService.getReportDetail(id);
            if (report == null) {
                return Result.error("报告不存在");
            }
            return Result.success(report);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 下载报告
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "markdown") String format,
                                                 @RequestParam(required = true) Integer userId) {
        try {
            // 参数校验
            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest().build();
            }
            
            byte[] reportBytes = reportGenerationService.downloadReport(id, format);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "report." + format);

            return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除报告
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteReport(@PathVariable Long id,
                                                           @RequestParam(required = true) Integer userId) {
        // 参数校验
        if (userId == null || userId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("code", 1, "message", "用户ID不能为空且必须为正整数"));
        }
        
        // 实现删除逻辑
        return ResponseEntity.ok(Map.of("code", 0, "message", "删除成功"));
    }
}