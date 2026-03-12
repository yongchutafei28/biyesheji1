package com.huacai.ai.controller;

import com.huacai.ai.service.ChatService;
import com.huacai.system.general.core.controller.BaseController;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai/chat")
public class ChatController extends BaseController {
    @Resource
    private ChatService chatService;

    /**
     * 对话接口
     */
    @GetMapping("/sse")
    public Flux<ServerSentEvent<String>> sseChat(int memoryId, String message) {
        return chatService.sseChat(memoryId, message)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }
}