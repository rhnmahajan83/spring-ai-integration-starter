package com.portfolio.springai.service;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
@Service
public class AiService {
 private final ChatClient client;
 public AiService(ChatClient client) { this.client=client; }
 public String chat(String message) { return client.prompt().user(message).call().content(); }
 public Flux<String> stream(String message) { return client.prompt().user(message).stream().content(); }
 public String summarize(String text) { return client.prompt().user(u -> u.text("Summarize the following text in three concise bullet points:\n{text}").param("text",text)).call().content(); }
}
