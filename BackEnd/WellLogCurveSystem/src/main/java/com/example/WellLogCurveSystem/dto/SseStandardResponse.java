package com.example.WellLogCurveSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标准SSE响应格式（前端统一解析）
 * 区分思考链/生成内容/错误，便于前端差异化展示
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SseStandardResponse {
    /** 内容类型：thinking-思考链 generation-生成内容 error-错误 done-结束 */
    private String type;
    /** 内容体 */
    private String content;
    /** 是否结束（仅done类型为true） */
    private Boolean isDone = false;
}