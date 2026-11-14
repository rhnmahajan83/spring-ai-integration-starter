package com.portfolio.springai.service;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
class AiServiceTest {
 @Test void constructsService() { assertNotNull(new AiService(mock(ChatClient.class))); }
}
