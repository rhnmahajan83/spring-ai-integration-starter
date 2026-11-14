package com.portfolio.springai.api;

import com.portfolio.springai.service.AiService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/ai")
public class AiController {
    private final AiService service;

    public AiController(AiService service) {
        this.service = service;
    }

    public record ChatRequest(@NotBlank @Size(max = 8000) String message) {}
    public record ChatResponse(String response) {}

    @PostMapping("/chat")
    public Mono<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return service.chat(request.message()).map(ChatResponse::new);
    }

    @PostMapping("/summarize")
    public Mono<ChatResponse> summarize(@Valid @RequestBody ChatRequest request) {
        return service.summarize(request.message()).map(ChatResponse::new);
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@Valid @RequestBody ChatRequest request) {
        return service.stream(request.message());
    }
}
