package com.portfolio.springai.service;

import com.portfolio.springai.tools.OrderTools;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class OrderConversationService {
    private final OrderConversationMemory memory;
    private final OrderTools orderTools;

    public OrderConversationService(OrderConversationMemory memory, OrderTools orderTools) {
        this.memory = memory;
        this.orderTools = orderTools;
    }

    public String start() {
        return memory.newConversation();
    }

    public Mono<String> ask(String conversationId, String question) {
        return Mono.fromCallable(() -> {
            List<String> ids = memory.resolveOrderIds(conversationId, question);
            String response;
            if (ids.isEmpty()) {
                response = "Please provide an order ID, such as ORD-1001, so I can look it up.";
            } else {
                List<String> facts = ids.stream().map(orderTools::getOrderStatus).toList();
                String lower = question.toLowerCase(Locale.ROOT);
                if (lower.contains("which") && lower.contains("delay")) {
                    response = facts.stream().filter(f -> f.contains("DELAYED"))
                            .collect(Collectors.joining("\n"));
                    if (response.isEmpty()) response = "None of the referenced orders are delayed.";
                } else {
                    response = String.join("\n", facts);
                }
            }
            memory.addTurn(conversationId, question, response, ids);
            return response;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public List<OrderConversationMemory.Turn> history(String conversationId) {
        return memory.history(conversationId);
    }

    public void delete(String conversationId) {
        memory.delete(conversationId);
    }
}
