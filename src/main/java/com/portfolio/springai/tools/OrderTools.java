package com.portfolio.springai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderTools {
    private static final Map<String, String> ORDERS = Map.of(
            "ORD-1001", "DELAYED: Installation appointment is pending; the field team has not confirmed a slot.",
            "ORD-1002", "IN_PROGRESS: Circuit provisioning is underway.",
            "ORD-1003", "COMPLETED: Service activation was successful."
    );

    @Tool(description = "Look up the current status and reason for an enterprise order using its order ID, such as ORD-1001")
    public String getOrderStatus(@ToolParam(description = "Enterprise order identifier, for example ORD-1001") String orderId) {
        String normalized = orderId.trim().toUpperCase(java.util.Locale.ROOT);
        return ORDERS.containsKey(normalized)
                ? "Order " + normalized + ": " + ORDERS.get(normalized)
                : "No order found for ID " + normalized + ".";
    }
}
