package com.portfolio.springai.service;

import com.portfolio.springai.tools.OrderTools;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

class ReliableOrderAssistantServiceTest {
    private final ChatClient chatClient = mock(ChatClient.class);
    private final OrderTools orderTools = mock(OrderTools.class);
    private final ReliableOrderAssistantService service = new ReliableOrderAssistantService(chatClient, orderTools);

    @Test
    void retrievesEveryDistinctOrderInOriginalOrderWithoutModel() {
        when(orderTools.getOrderStatus("ORD-1003")).thenReturn("Order ORD-1003: COMPLETED");
        when(orderTools.getOrderStatus("ORD-1002")).thenReturn("Order ORD-1002: IN_PROGRESS");

        StepVerifier.create(service.ask("Status of ord-1003, ORD-1002 and ORD-1003?"))
                .expectNext("Order ORD-1003: COMPLETED\nOrder ORD-1002: IN_PROGRESS")
                .verifyComplete();

        verify(orderTools).getOrderStatus("ORD-1003");
        verify(orderTools).getOrderStatus("ORD-1002");
        verifyNoInteractions(chatClient);
    }

    @Test
    void handlesUnknownOrderAlongsideKnownOrder() {
        when(orderTools.getOrderStatus("ORD-9999")).thenReturn("No order found for ID ORD-9999.");
        when(orderTools.getOrderStatus("ORD-1002")).thenReturn("Order ORD-1002: IN_PROGRESS");

        StepVerifier.create(service.ask("ORD-9999 and ORD-1002"))
                .expectNext("No order found for ID ORD-9999.\nOrder ORD-1002: IN_PROGRESS")
                .verifyComplete();
        verifyNoInteractions(chatClient);
    }

    @Test
    void asksForIdWhenMissing() {
        StepVerifier.create(service.ask("What is the order status?"))
                .expectNext("Please include an order ID such as ORD-1001.")
                .verifyComplete();
        verifyNoInteractions(orderTools, chatClient);
    }
}
