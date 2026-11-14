package com.portfolio.springai.config;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class AiConfiguration {
 @Bean ChatClient chatClient(ChatClient.Builder builder) { return builder.defaultSystem("You are a helpful, precise engineering assistant. If uncertain, say so.").build(); }
}
