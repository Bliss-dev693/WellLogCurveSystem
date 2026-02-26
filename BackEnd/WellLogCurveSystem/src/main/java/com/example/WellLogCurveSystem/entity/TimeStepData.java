package com.example.WellLogCurveSystem.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 时间步数据实体类（精准匹配第三方接口的JSON结构）
 */
@Data
public class TimeStepData {
    // 强制指定JSON字段名为 "data"（避免序列化时名称变化）
    @JsonProperty("data")
    private Data data;

    /**
     * 内部Data类
     */
    @lombok.Data
    public static class Data {
        // 强制指定JSON字段名为 "parameters"
        @JsonProperty("parameters")
        private Parameters parameters;
    }

    /**
     * 参数类（AC/GR/RT/RXO 强制指定字段名）
     */
    @lombok.Data
    public static class Parameters {
        // 关键：强制指定字段名和大小写，和正确格式完全一致
        @JsonProperty("AC")
        private Double AC;
        
        @JsonProperty("GR")
        private Double GR;
        
        @JsonProperty("RT")
        private Double RT;
        
        @JsonProperty("RXO")
        private Double RXO;
    }
}