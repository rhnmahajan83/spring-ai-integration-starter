package com.portfolio.springai.tools;

import com.portfolio.springai.service.OrderToolTrace;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderToolsTest {
    private final OrderToolTrace trace = new OrderToolTrace();
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final OrderTools tools = new OrderTools(trace, registry);

    @Test
    void returnsKnownOrderStatusAndRecordsToolInvocation() {
        trace.begin();
        assertTrue(tools.getOrderStatus("ord-1001").contains("DELAYED"));
        OrderToolTrace.Trace result = trace.end();
        assertTrue(result.invoked);
        assertEquals("getOrderStatus", result.toolName);
        assertEquals(1.0, registry.counter("ai.order.tool.invocations", "tool", "getOrderStatus").count());
    }

    @Test
    void reportsUnknownOrders() {
        assertTrue(tools.getOrderStatus("ORD-9999").contains("No order found"));
    }
}
