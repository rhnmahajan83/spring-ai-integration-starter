package com.portfolio.springai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class AiService {
    private final ChatClient client;

    public AiService(ChatClient client) {
        this.client = client;
    }

    public Mono<String> chat(String message) {
        return Mono.fromCallable(() -> client.prompt().user(message).call().content())
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Flux<String> stream(String message) {
        return client.prompt().user(message).stream().content();
    }

    public Mono<String> summarize(String text) {
        return Mono.fromCallable(() -> client.prompt()
                .user(u -> u.text("Summarize the following text in three concise bullet points:\n{text}")
                        .param("text", text))
                .call().content())
                .subscribeOn(Schedulers.boundedElastic());
    }
}
