package com.example.WellLogCurveSystem.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SiliconFlowStreamResponse {
    /** 响应结果列表 */
    private List<Choice> choices;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        /** 流式增量内容（核心） */
        private Delta delta;
        /** 结束原因（stop/length/tool_calls等，最后一条数据才有） */
        private String finishReason;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Delta {
        /** 生成内容增量 */
        private String content;
        /** 思考链内容增量（enableThinking=true时返回） */
        @JsonProperty("reasoning_content")
        private String reasoningContent;
    }
}