package com.portfolio.springai.service;

import com.portfolio.springai.tools.OrderTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class OrderAssistantService {
    private final ChatClient chatClient;
    private final OrderTools orderTools;

    public OrderAssistantService(ChatClient chatClient, OrderTools orderTools) {
        this.chatClient = chatClient;
        this.orderTools = orderTools;
    }

    public Mono<String> ask(String question) {
        return Mono.fromCallable(() -> chatClient.prompt()
                .system("You are an enterprise order assistant. For order status questions, use the getOrderStatus tool. Never invent order details. If an order is unknown, say so.")
                .user(question)
                .tools(orderTools)
                .call()
                .content())
                .subscribeOn(Schedulers.boundedElastic());
    }
}
