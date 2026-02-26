package com.example.WellLogCurveSystem.controller;

import com.example.WellLogCurveSystem.dto.ChatRequest;
import com.example.WellLogCurveSystem.service.SiliconFlowChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * 流式对话接口控制器（前端调用入口，已修正编译错误）
 * 标准SSE响应，MediaType为text/event-stream
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatStreamController {

    private final SiliconFlowChatService siliconFlowChatService;
    // 修正：缓冲区大小改为Long类型（1MB），避免int转Long错误
    private static final Long EMITTER_BUFFER_SIZE = 1024 * 1024L;

    /**
     * 标准SSE流式对话接口（已修正）
     * @param chatRequest 前端请求参数（JSON格式）
     * @return ResponseBodyEmitter SpringBoot SSE发射器
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=utf-8")
    public ResponseBodyEmitter streamChat(@RequestBody ChatRequest chatRequest) {
        // 修正：传入Long类型的缓冲区大小，解决int转Long不兼容问题
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(EMITTER_BUFFER_SIZE);
        // 调用核心服务处理流式请求
        siliconFlowChatService.streamChat(chatRequest, emitter);
        return emitter;
    }
}