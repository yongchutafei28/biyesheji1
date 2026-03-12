package com.huacai.ai;

import com.huacai.ai.service.ChatService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 对话服务工厂类
 */
@Configuration
public class ChatServiceFactory {
    /**
     * 聊天模型
     */
    @Resource
    private ChatModel chatModel;

    /**
     * 流式输出模型
     */
    @Resource
    private StreamingChatModel streamingChatModel;

    /**
     * 创建对话服务Bean
     */
    @Bean
    public ChatService chatService() {
        ChatService codeService = AiServices.builder(ChatService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                .build();

        //返回对话实例
        return codeService;
    }

}
