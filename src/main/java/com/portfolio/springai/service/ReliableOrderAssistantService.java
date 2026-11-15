package com.portfolio.springai.service;

import com.portfolio.springai.tools.OrderTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReliableOrderAssistantService {
    private static final Logger log = LoggerFactory.getLogger(ReliableOrderAssistantService.class);
    private static final Pattern ORDER_ID = Pattern.compile("(?i)\\bORD-[0-9]+\\b");

    private final ChatClient chatClient;
    private final OrderTools orderTools;

    public ReliableOrderAssistantService(ChatClient chatClient, OrderTools orderTools) {
        this.chatClient = chatClient;
        this.orderTools = orderTools;
    }

    public Mono<String> ask(String question) {
        Matcher matcher = ORDER_ID.matcher(question);
        if (!matcher.find()) {
            return Mono.just("Please include an order ID such as ORD-1001.");
        }

        String orderId = matcher.group().toUpperCase(java.util.Locale.ROOT);
        // Retrieve business data in Java, regardless of the model's tool-selection behavior.
        String facts = orderTools.getOrderStatus(orderId);
        if (facts.startsWith("No order found")) {
            return Mono.just(facts);
        }

        return Mono.fromCallable(() -> {
                    try {
                        String explanation = chatClient.prompt()
                                .system("Explain the supplied order record clearly and briefly. Use only the supplied facts. Do not invent additional details. Do not ask for an order ID.")
                                .user("Question: " + question + "\nVerified order record: " + facts)
                                .call().content();
                        if (explanation == null || explanation.isBlank()
                                || !explanation.contains(orderId)) {
                            log.warn("Model returned an incomplete order explanation for {}; using verified facts", orderId);
                            return facts;
                        }
                        return explanation;
                    } catch (Exception ex) {
                        log.warn("Model explanation failed for {}; using verified facts: {}", orderId, ex.toString());
                        return facts;
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}
