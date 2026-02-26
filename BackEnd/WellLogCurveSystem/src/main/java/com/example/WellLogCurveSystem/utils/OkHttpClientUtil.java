package com.example.WellLogCurveSystem.utils;

import com.example.WellLogCurveSystem.config.SiliconFlowConfig;
import jakarta.annotation.Resource;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Component
public class OkHttpClientUtil {
    @Resource
    private SiliconFlowConfig siliconFlowConfig;

    private OkHttpClient okHttpClient;

    /**
     * 初始化OkHttp客户端（容器加载后执行，单例）
     */
    @PostConstruct
    public void initClient() {
        this.okHttpClient = new OkHttpClient.Builder()
                // 连接超时（配置类中的普通超时）
                .connectTimeout(siliconFlowConfig.getTimeout(), TimeUnit.SECONDS)
                // 读取超时（流式请求用专用超时，设大值）
                .readTimeout(siliconFlowConfig.getStreamTimeout(), TimeUnit.SECONDS)
                // 写入超时
                .writeTimeout(10, TimeUnit.SECONDS)
                // 连接失败重试
                .retryOnConnectionFailure(true)
                .build();
    }

    /**
     * 获取OkHttp客户端实例
     */
    public OkHttpClient getClient() {
        return okHttpClient;
    }
}