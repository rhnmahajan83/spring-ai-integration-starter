package com.portfolio.springai.tools;

import com.portfolio.springai.service.OrderToolTrace;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class OrderTools {
    private static final Logger log = LoggerFactory.getLogger(OrderTools.class);
    private static final Map<String, String> ORDERS = Map.of(
            "ORD-1001", "DELAYED: Installation appointment is pending; the field team has not confirmed a slot.",
            "ORD-1002", "IN_PROGRESS: Circuit provisioning is underway.",
            "ORD-1003", "COMPLETED: Service activation was successful."
    );

    private final OrderToolTrace trace;
    private final MeterRegistry registry;

    public OrderTools(OrderToolTrace trace, MeterRegistry registry) {
        this.trace = trace;
        this.registry = registry;
    }

    @Tool(description = "Look up the current status and reason for an enterprise order using its order ID, such as ORD-1001")
    public String getOrderStatus(@ToolParam(description = "Enterprise order identifier, for example ORD-1001") String orderId) {
        long start = System.nanoTime();
        String normalized = orderId.trim().toUpperCase(Locale.ROOT);
        try {
            return ORDERS.containsKey(normalized)
                    ? "Order " + normalized + ": " + ORDERS.get(normalized)
                    : "No order found for ID " + normalized + ".";
        } finally {
            long elapsed = System.nanoTime() - start;
            long durationMs = TimeUnit.NANOSECONDS.toMillis(elapsed);
            trace.record("getOrderStatus", durationMs);
            registry.counter("ai.order.tool.invocations", "tool", "getOrderStatus").increment();
            Timer.builder("ai.order.tool.duration")
                    .tag("tool", "getOrderStatus")
                    .register(registry)
                    .record(elapsed, TimeUnit.NANOSECONDS);
            log.info("TOOL EXECUTED: getOrderStatus({}), durationMs={}", normalized, durationMs);
        }
    }
}
