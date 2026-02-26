package com.example.WellLogCurveSystem.service;

import com.example.WellLogCurveSystem.config.SiliconFlowConfig;

import com.example.WellLogCurveSystem.dto.ChatRequest;
import com.example.WellLogCurveSystem.dto.Message;
import com.example.WellLogCurveSystem.dto.SiliconFlowStreamResponse;
import com.example.WellLogCurveSystem.dto.SseStandardResponse;
import com.example.WellLogCurveSystem.utils.OkHttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * SiliconFlow核心聊天服务（已修正所有编译错误）
 * 处理流式请求、解析SiliconFlow响应、转发标准SSE格式给前端
 */
@Slf4j
@Service
public class SiliconFlowChatService {
    // SiliconFlow SSE常量
    private static final String SSE_DATA_PREFIX = "data: ";
    private static final String SSE_DONE_FLAG = "data: [DONE]";
    // SSE内容类型常量
    public static final String TYPE_THINKING = "thinking";
    public static final String TYPE_GENERATION = "generation";
    public static final String TYPE_ERROR = "error";
    public static final String TYPE_DONE = "done";
    // 缓冲区大小（1MB，Long类型）
    private static final Long EMITTER_BUFFER_SIZE = 1024 * 1024L;

    @Resource
    private OkHttpClientUtil okHttpClientUtil;
    @Resource
    private SiliconFlowConfig siliconFlowConfig;
    /** Jackson JSON解析器（SpringBoot3默认注入） */
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 流式对话核心方法（已修正）
     * @param chatRequest 前端请求参数
     * @param emitter SpringBoot SSE发射器，用于向前端推送数据
     */
    public void streamChat(ChatRequest chatRequest, ResponseBodyEmitter emitter) {
        // 1. 补全请求参数（前端未传则使用配置类默认值）
        completeRequestParams(chatRequest);
        // 2. 确保系统提示存在
        ensureSystemPromptExists(chatRequest);

        // 3. 补全请求参数（前端未传则使用配置类默认值）
        completeRequestParams(chatRequest);
        // 4. 构建SiliconFlow请求体（修正：OkHttp3过时方法，使用字节数组重载）
        RequestBody requestBody;
        try {
            // 将请求参数转为JSON，强制添加stream=true
            String requestJson = objectMapper.writeValueAsString(chatRequest);
            requestJson = requestJson.substring(0, requestJson.length() - 1)
                    + ",\"stream\":" + siliconFlowConfig.isDefaultStream() + "}";
            // 修正1：替换过时的RequestBody.create(MediaType, String)
            requestBody = RequestBody.create(
                    requestJson.getBytes(StandardCharsets.UTF_8),
                    MediaType.parse("application/json; charset=utf-8")
            );
        } catch (Exception e) {
            sendSseMessage(emitter, new SseStandardResponse(TYPE_ERROR, "请求参数解析失败：" + e.getMessage(), false));
            completeEmitter(emitter);
            return;
        }

        // 3. 构造OkHttp请求（设置鉴权头、接口地址）
        Request request = new Request.Builder()
                .url(siliconFlowConfig.getApiUrl())
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + siliconFlowConfig.getApiKey())
                .addHeader("Content-Type", "application/json; charset=utf-8")
                .build();

        // 4. 异步执行流式请求（修正：持有Call引用，用于后续取消请求）
        OkHttpClient client = okHttpClientUtil.getClient();
        Call call = client.newCall(request); // 关键：保存Call实例
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, java.io.IOException e) {
                log.error("SiliconFlow请求失败", e);
                sendSseMessage(emitter, new SseStandardResponse(TYPE_ERROR, "服务端请求失败：" + e.getMessage(), false));
                completeEmitter(emitter);
            }

            @Override
            public void onResponse(Call call, Response response) throws java.io.IOException {
                if (!response.isSuccessful()) {
                    String errorMsg = String.format("SiliconFlow响应异常，状态码：%d，信息：%s",
                            response.code(), response.message());
                    log.error(errorMsg);
                    sendSseMessage(emitter, new SseStandardResponse(TYPE_ERROR, errorMsg, false));
                    response.close();
                    completeEmitter(emitter);
                    return;
                }

                // 5. 流式读取SiliconFlow响应并转发给前端
                try (ResponseBody responseBody = response.body();
                     InputStreamReader isr = new InputStreamReader(responseBody.byteStream(), StandardCharsets.UTF_8);
                     BufferedReader br = new BufferedReader(isr)) {

                    String line;
                    while ((line = br.readLine()) != null) {
                        // 忽略空行（SSE协议换行分隔）
                        if (line.isBlank()) continue;
                        // 处理结束标识：推送结束信号
                        if (SSE_DONE_FLAG.equals(line)) {
                            sendSseMessage(emitter, new SseStandardResponse(TYPE_DONE, "对话结束", true));
                            break;
                        }
                        // 解析SSE数据（截取data:前缀，解析JSON）
                        if (line.startsWith(SSE_DATA_PREFIX)) {
                            parseAndSendSseData(line.substring(SSE_DATA_PREFIX.length()), emitter);
                        }
                    }
                } catch (Exception e) {
                    log.error("流式读取响应失败", e);
                    sendSseMessage(emitter, new SseStandardResponse(TYPE_ERROR, "流式响应处理失败：" + e.getMessage(), false));
                } finally {
                    completeEmitter(emitter);
                    response.close();
                }
            }
        });

        // 修正2：移除过时的emitter.setTimeout，SpringBoot3通过构造器设置超时，此处无需重复设置
        // 前端断开连接时，取消OkHttp的Call请求（修正3：调用Call的cancel方法，而非Request）
        emitter.onCompletion(() -> {
            log.info("前端断开连接，取消SiliconFlow请求");
            if (!call.isCanceled() && !call.isExecuted()) {
                call.cancel(); // 正确：Call实例的cancel方法
            }
        });
    }

    /**
     * 补全请求参数：前端未传则使用配置类中的默认值
     */
    private void completeRequestParams(ChatRequest chatRequest) {
        SiliconFlowConfig.DefaultParams defaultParams = siliconFlowConfig.getDefaultParams();
        // 模型名
        if (chatRequest.getModel() == null || chatRequest.getModel().isBlank()) {
            chatRequest.setModel(siliconFlowConfig.getDefaultModel());
        }
        // 最大Token
        if (chatRequest.getMaxTokens() == null) {
            chatRequest.setMaxTokens(defaultParams.getMaxTokens());
        }
        // 温度系数
        if (chatRequest.getTemperature() == null) {
            chatRequest.setTemperature(defaultParams.getTemperature());
        }
        // 核采样
        if (chatRequest.getTopP() == null) {
            chatRequest.setTopP(defaultParams.getTopP());
        }
        // 采样数
        if (chatRequest.getTopK() == null) {
            chatRequest.setTopK(defaultParams.getTopK());
        }
        // 思考链开关（默认true）
        if (chatRequest.getEnableThinking() == null) {
            chatRequest.setEnableThinking(true);
        }
    }
    /**
 * 为聊天请求添加默认系统提示（如果没有提供的话）
 */
    private void ensureSystemPromptExists(ChatRequest chatRequest) {
        // 检查是否已有 system 角色的消息
        boolean hasSystemMessage = chatRequest.getMessages().stream()
            .anyMatch(message -> "system".equals(message.getRole()));

        // 如果没有系统消息，则添加默认的系统提示
        if (!hasSystemMessage) {
            Message systemMessage = new Message();
            systemMessage.setRole("system");
            systemMessage.setContent(siliconFlowConfig.getDefaultSystemPrompt());

            // 将系统消息插入到消息列表的开头
            chatRequest.getMessages().add(0, systemMessage);
        }
    }


    /**
     * 解析SiliconFlow的SSE数据，转发为标准格式给前端
     */
    private void parseAndSendSseData(String jsonStr, org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter emitter) {
        try {
            SiliconFlowStreamResponse streamResponse = objectMapper.readValue(jsonStr, SiliconFlowStreamResponse.class);
            if (streamResponse.getChoices() == null || streamResponse.getChoices().isEmpty()) {
                return;
            }
            SiliconFlowStreamResponse.Choice choice = streamResponse.getChoices().get(0);
            if (choice.getDelta() == null) {
                return;
            }

            // 提取思考链内容，非空则推送
            String reasoningContent = choice.getDelta().getReasoningContent();
            if (reasoningContent != null && !reasoningContent.isBlank()) {
                log.info("【思考链】{}", reasoningContent);
                sendSseMessage(emitter, new SseStandardResponse(TYPE_THINKING, reasoningContent, false));
            }

            // 提取生成内容，非空则推送
            String generationContent = choice.getDelta().getContent();
            if (generationContent != null && !generationContent.isBlank()) {
                log.info("【生成内容】{}", generationContent);
                sendSseMessage(emitter, new SseStandardResponse(TYPE_GENERATION, generationContent, false));
            }

            // 检测结束原因，若存在则推送结束信号
            if (choice.getFinishReason() != null) {
                log.info("【结束原因】{}", choice.getFinishReason());
                sendSseMessage(emitter, new SseStandardResponse(TYPE_DONE, "因[" + choice.getFinishReason() + "]结束", true));
            }
        } catch (Exception e) {
            log.warn("SSE数据解析失败，原始数据：{}", jsonStr, e);
        }
    }

    /**
     * 向前端推送SSE消息
     */
    private void sendSseMessage(org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter emitter, SseStandardResponse response) {
        try {
            String responseJson = objectMapper.writeValueAsString(response);
            // 在发送到前端之前打印到控制台
            log.info("【SSE响应】{}", responseJson);
            // 转为JSON字符串，SSE标准格式
            emitter.send(responseJson);
            // SSE分隔符，确保前端正常解析
            emitter.send("\n");
        } catch (Exception e) {
            log.warn("向前端推送SSE消息失败", e);
        }
    }

    /**
     * 关闭发射器，释放资源
     */
    private void completeEmitter(org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception e) {
            log.warn("关闭SSE发射器失败", e);
        }
    }
}