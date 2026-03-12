package com.huacai.ai.service;


import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * 对话服务Service
 */
public interface ChatService {
    /**
     * 基于SSE的流式对话接口
     */
    @SystemMessage(fromResource = "prompt.txt")
    Flux<String> sseChat(@MemoryId int memoryId, @UserMessage String message);
}