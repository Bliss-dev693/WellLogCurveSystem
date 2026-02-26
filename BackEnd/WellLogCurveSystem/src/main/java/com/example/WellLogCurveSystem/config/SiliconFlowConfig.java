package com.example.WellLogCurveSystem.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

/**
 * 硅基流动配置映射类
 */
@Data
@Component
@ConfigurationProperties(prefix = "silicon-flow")
public class SiliconFlowConfig {
    // API 地址
    private String apiUrl;
    // API Key
    private String apiKey;
    // 默认模型
    private String defaultModel;
    // 默认流式开关
    private boolean defaultStream;
    // 请求超时时间（秒）
    private Integer timeout;
    // 流式响应超时时间（秒）
    private Integer streamTimeout;
    // 默认参数配置
    private DefaultParams defaultParams;

    private String defaultSystemPrompt ;


    /**
     * 默认参数内部类
     */
    @Data
    public static class DefaultParams {
        private Double temperature;
        private Integer maxTokens;
        private Double topP;
        private Integer topK;
    }
}