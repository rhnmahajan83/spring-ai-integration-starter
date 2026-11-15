package com.portfolio.springai.service;

import com.portfolio.springai.tools.OrderTools;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class OrderAssistantService {
    private static final Logger log = LoggerFactory.getLogger(OrderAssistantService.class);
    private final ChatClient chatClient;
    private final OrderTools orderTools;
    private final OrderToolTrace trace;
    private final MeterRegistry registry;

    public OrderAssistantService(ChatClient chatClient, OrderTools orderTools,
                                 OrderToolTrace trace, MeterRegistry registry) {
        this.chatClient = chatClient;
        this.orderTools = orderTools;
        this.trace = trace;
        this.registry = registry;
    }

    public Mono<String> ask(String question) {
        return Mono.fromCallable(() -> {
                    long start = System.nanoTime();
                    trace.begin();
                    boolean success = false;
                    try {
                        String response = chatClient.prompt()
                                .system("""
                                        You are an enterprise order management assistant.
                                        For any question about an order's status, delay or installation,
                                        call getOrderStatus before answering. The order ID may already
                                        appear in the user's question. Never invent order details.
                                        Base the final answer on the tool result.
                                        """)
                                .user(question)
                                .tools(orderTools)
                                .call()
                                .content();
                        success = true;
                        return response;
                    } finally {
                        OrderToolTrace.Trace result = trace.end();
                        long durationMs = (System.nanoTime() - start) / 1_000_000;
                        registry.counter("ai.order.requests", "tool_invoked",
                                Boolean.toString(result.invoked), "success", Boolean.toString(success)).increment();
                        log.info("ORDER ASK completed: toolInvoked={}, tool={}, toolDurationMs={}, requestDurationMs={}, success={}",
                                result.invoked, result.toolName, result.durationMs, durationMs, success);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}
