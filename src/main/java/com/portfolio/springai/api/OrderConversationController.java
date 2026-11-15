package com.portfolio.springai.api;

import com.portfolio.springai.service.OrderConversationMemory;
import com.portfolio.springai.service.OrderConversationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders/conversations")
public class OrderConversationController {
    private final OrderConversationService service;

    public OrderConversationController(OrderConversationService service) {
        this.service = service;
    }

    public record ConversationCreated(String conversationId) {}
    public record MessageRequest(@NotBlank @Size(max = 8000) String message) {}
    public record MessageResponse(String conversationId, String response) {}

    @PostMapping
    public ConversationCreated start() {
        return new ConversationCreated(service.start());
    }

    @PostMapping("/{conversationId}/messages")
    public Mono<MessageResponse> ask(@PathVariable String conversationId,
                                     @Valid @RequestBody MessageRequest request) {
        return service.ask(conversationId, request.message())
                .map(response -> new MessageResponse(conversationId, response))
                .onErrorMap(IllegalArgumentException.class,
                        ex -> new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @GetMapping("/{conversationId}")
    public List<OrderConversationMemory.Turn> history(@PathVariable String conversationId) {
        try {
            return service.history(conversationId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }

    @DeleteMapping("/{conversationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String conversationId) {
        try {
            service.delete(conversationId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage());
        }
    }
}
