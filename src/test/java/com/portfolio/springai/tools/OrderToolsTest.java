package com.portfolio.springai.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderToolsTest {
    private final OrderTools tools = new OrderTools();

    @Test
    void returnsKnownOrderStatus() {
        assertTrue(tools.getOrderStatus("ord-1001").contains("DELAYED"));
    }

    @Test
    void reportsUnknownOrders() {
        assertTrue(tools.getOrderStatus("ORD-9999").contains("No order found"));
    }
}
