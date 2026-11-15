package com.portfolio.springai.service;

import com.portfolio.springai.tools.OrderTools;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderConversationServiceTest {
    private final OrderConversationMemory memory = new OrderConversationMemory();
    private final OrderTools tools = mock(OrderTools.class);
    private final OrderConversationService service = new OrderConversationService(memory, tools);

    @Test
    void remembersOrdersAcrossTurnsAndFiltersDelayed() {
        when(tools.getOrderStatus("ORD-1001")).thenReturn("Order ORD-1001: DELAYED");
        when(tools.getOrderStatus("ORD-1002")).thenReturn("Order ORD-1002: IN_PROGRESS");
        String id = service.start();

        StepVerifier.create(service.ask(id, "Status of ORD-1001 and ORD-1002?"))
                .expectNext("Order ORD-1001: DELAYED\nOrder ORD-1002: IN_PROGRESS")
                .verifyComplete();
        StepVerifier.create(service.ask(id, "Which one is delayed?"))
                .expectNext("Order ORD-1001: DELAYED")
                .verifyComplete();
        assertEquals(2, service.history(id).size());
    }

    @Test
    void keepsSessionsIsolatedAndAllowsDeletion() {
        String first = service.start();
        String second = service.start();
        when(tools.getOrderStatus("ORD-1003")).thenReturn("Order ORD-1003: COMPLETED");

        StepVerifier.create(service.ask(first, "Status ORD-1003"))
                .expectNext("Order ORD-1003: COMPLETED").verifyComplete();
        StepVerifier.create(service.ask(second, "Which one is delayed?"))
                .expectNext("Please provide an order ID, such as ORD-1001, so I can look it up.")
                .verifyComplete();
        assertEquals(0, service.history(second).get(0).response().indexOf("Please"));
        service.delete(first);
        assertThrows(IllegalArgumentException.class, () -> service.history(first));
    }

    @Test
    void deduplicatesIds() {
        String id = service.start();
        when(tools.getOrderStatus("ORD-1002")).thenReturn("Order ORD-1002: IN_PROGRESS");
        StepVerifier.create(service.ask(id, "ORD-1002 and ord-1002"))
                .expectNext("Order ORD-1002: IN_PROGRESS").verifyComplete();
        verify(tools, times(1)).getOrderStatus("ORD-1002");
    }
}
