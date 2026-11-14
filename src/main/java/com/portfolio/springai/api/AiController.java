package com.portfolio.springai.api;
import com.portfolio.springai.service.AiService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
@RestController @RequestMapping("/api/v1/ai")
public class AiController {
 private final AiService service;
 public AiController(AiService service) { this.service=service; }
 public record ChatRequest(@NotBlank @Size(max=8000) String message) {}
 public record ChatResponse(String response) {}
 @PostMapping("/chat") public ChatResponse chat(@Valid @RequestBody ChatRequest request) { return new ChatResponse(service.chat(request.message())); }
 @PostMapping("/summarize") public ChatResponse summarize(@Valid @RequestBody ChatRequest request) { return new ChatResponse(service.summarize(request.message())); }
 @PostMapping(value="/stream",produces=MediaType.TEXT_EVENT_STREAM_VALUE) public Flux<String> stream(@Valid @RequestBody ChatRequest request) { return service.stream(request.message()); }
}
