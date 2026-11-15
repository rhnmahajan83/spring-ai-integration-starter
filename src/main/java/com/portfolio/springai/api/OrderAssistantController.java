package com.portfolio.springai.api;

import com.portfolio.springai.service.OrderAssistantService;
import com.portfolio.springai.service.ReliableOrderAssistantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderAssistantController {
    private final OrderAssistantService service;
    private final ReliableOrderAssistantService reliableService;

    public OrderAssistantController(OrderAssistantService service,
                                    ReliableOrderAssistantService reliableService) {
        this.service = service;
        this.reliableService = reliableService;
    }

    public record AskRequest(@NotBlank @Size(max = 8000) String message) {}
    public record AskResponse(String response) {}

    @PostMapping("/ask")
    public Mono<AskResponse> ask(@Valid @RequestBody AskRequest request) {
        return service.ask(request.message()).map(AskResponse::new);
    }

    @PostMapping("/ask-reliable")
    public Mono<AskResponse> askReliable(@Valid @RequestBody AskRequest request) {
        return reliableService.ask(request.message()).map(AskResponse::new);
    }
}
