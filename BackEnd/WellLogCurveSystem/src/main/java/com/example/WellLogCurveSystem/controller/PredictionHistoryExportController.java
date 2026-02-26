package com.example.WellLogCurveSystem.controller;

import com.example.WellLogCurveSystem.entity.PredictionHistory;
import com.example.WellLogCurveSystem.service.DrillingPredictionService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;

/**
 * 预测历史记录导出Controller
 * 修复UTF-8编码+添加BOM头，解决中文乱码
 */
@RestController
@RequestMapping("/api/export/history")
public class PredictionHistoryExportController {

    private static final Logger log = LoggerFactory.getLogger(PredictionHistoryExportController.class);
    @Resource
    private DrillingPredictionService drillingPredictionService;

    // 时间格式化器：统一Date类型显示
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/csv")
    public void exportHistoryToCsv(@RequestParam("userId") Integer userId,
                                   HttpServletResponse response) throws Exception {
        // 1. 响应头设置：RFC标准+UTF-8编码，兼容所有浏览器
        response.setContentType("text/csv;charset=utf-8");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        String fileName = "钻井预测历史记录.csv";
        String encodedFileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20");
        response.setHeader("Content-Disposition",
                String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s",
                        encodedFileName, encodedFileName));

        // 2. 查询用户所有历史记录
        List<PredictionHistory> historyList = drillingPredictionService.findAllHistoryForExport(userId);

        // 3. 核心修复：显式使用OutputStreamWriter强制UTF-8编码，添加BOM头
        try (OutputStreamWriter osw = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osw)) {

            // 写入UTF-8 BOM头：让Excel/浏览器识别UTF-8编码，解决中文乱码核心
            writer.write('\ufeff');

            // 写入CSV表头（中文表头，与实体字段对应）
            writer.write("ID,用户ID,数据集名,井名,深度范围,CNL预测结果,预测状态,错误信息,执行耗时(毫秒),创建时间,修改时间\n");

            // 无数据处理
            if (historyList.isEmpty()) {
                writer.write(",,该用户暂无历史预测记录,,,,,,,,\n");
                writer.flush();
                log.info("用户{}无历史预测记录，导出空CSV", userId);
                return;
            }

            // 遍历写入数据，处理null值和特殊类型
            for (PredictionHistory history : historyList) {
                String csvLine = String.format(
                        "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        getValue(history.getId()),
                        getValue(history.getUserId()),
                        getValue(history.getDatasetName()),
                        getValue(history.getWellName()),
                        getValue(history.getDepthRange()),
                        getValue(history.getCnlPrediction()),
                        getValue(history.getStatus()),
                        getValue(history.getErrorMessage()),
                        getValue(history.getExecutionTime()),
                        formatDate(history.getCreateTime()),
                        formatDate(history.getUpdateTime())
                );
                writer.write(csvLine);
            }
            writer.flush();
            log.info("用户{}的历史预测记录CSV导出成功，共{}条数据，编码：UTF-8（含BOM头）", userId, historyList.size());
        } catch (Exception e) {
            log.error("用户{}的历史预测记录CSV导出失败：{}", userId, e.getMessage(), e);
            // 异常时返回JSON错误信息
            response.reset();
            response.setContentType("application/json;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"code\":500,\"message\":\"CSV导出失败：" + e.getMessage().replace("\"", "\\\"") + "\"}");
            response.getWriter().flush();
        }
    }

    // 工具方法：处理null值，返回空字符串
    private String getValue(Object obj) {
        return Objects.isNull(obj) ? "" : obj.toString();
    }

    // 工具方法：格式化Date，null返回空字符串
    private String formatDate(java.util.Date date) {
        return Objects.isNull(date) ? "" : DATE_FORMAT.format(date);
    }
}