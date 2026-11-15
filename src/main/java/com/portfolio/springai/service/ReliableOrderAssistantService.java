package com.portfolio.springai.service;

import com.portfolio.springai.tools.OrderTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
        return Mono.fromCallable(() -> {
            List<String> orderIds = ORDER_ID.matcher(question)
                    .results()
                    .map(match -> match.group().toUpperCase(Locale.ROOT))
                    .distinct()
                    .toList();

            if (orderIds.isEmpty()) {
                return "Please include an order ID such as ORD-1001.";
            }

            // Always retrieve every distinct order in the user's original order.
            String verifiedFacts = orderIds.stream()
                    .map(orderTools::getOrderStatus)
                    .collect(Collectors.joining("\n"));

            // Multiple orders are returned directly: no LLM can accidentally omit one.
            if (orderIds.size() > 1) {
                log.info("Reliable order lookup returned {} distinct records", orderIds.size());
                return verifiedFacts;
            }

            String orderId = orderIds.get(0);
            if (verifiedFacts.startsWith("No order found")) {
                return verifiedFacts;
            }

            try {
                String explanation = chatClient.prompt()
                        .system("Explain the supplied order record clearly and briefly. Use only the supplied facts. Never invent details or ask for an order ID. Include the order ID in your response.")
                        .user("Question: " + question + "\nVerified order record: " + verifiedFacts)
                        .call().content();
                if (explanation == null || explanation.isBlank()
                        || !explanation.toUpperCase(Locale.ROOT).contains(orderId)) {
                    log.warn("Model explanation missing required order ID {}; falling back to verified facts (empty={})",
                            orderId, explanation == null || explanation.isBlank());
                    return verifiedFacts;
                }
                return explanation;
            } catch (Exception ex) {
                log.warn("Model explanation failed for {}; using verified facts: {}", orderId, ex.toString());
                return verifiedFacts;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
