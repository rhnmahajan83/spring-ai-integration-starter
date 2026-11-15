# Spring AI Integration Starter

Java 21 / Spring Boot / Spring AI starter using local Ollama for chat, summarization, streaming, and a demo enterprise order assistant with tool calling.

## Stack
Java 21, Spring Boot 4.0.1, Spring AI 2.0.2, Gradle, WebFlux, Validation, Actuator, Ollama.

## Prerequisites
- Java 21 and Gradle 8.14+ (or a locally generated Gradle wrapper)
- Ollama running locally on port 11434
- Install the model once: `ollama pull qwen2.5:1.5b`

No OpenAI API key is required.

## Run
```bash
ollama list
gradle bootRun
```

## API
```bash
curl -X POST http://localhost:8080/api/v1/ai/chat -H 'Content-Type: application/json' -d '{"message":"Explain RAG in two sentences"}'
curl -X POST http://localhost:8080/api/v1/ai/summarize -H 'Content-Type: application/json' -d '{"message":"Spring AI provides a portable API for integrating language models with enterprise services."}'
curl -N -X POST http://localhost:8080/api/v1/ai/stream -H 'Content-Type: application/json' -d '{"message":"Explain vector embeddings"}'
curl -X POST http://localhost:8080/api/v1/orders/ask -H 'Content-Type: application/json' -d '{"message":"Why is order ORD-1001 delayed?"}'
```

Health: `GET /actuator/health`.

## Tool calling demo
`OrderTools.getOrderStatus` is a Spring AI `@Tool` that returns mock order data. Available IDs: `ORD-1001` (delayed), `ORD-1002` (in progress), `ORD-1003` (completed). Unknown IDs return a not-found message. `OrderAssistantService` registers the tool with `.tools(orderTools)` and offloads the synchronous model call to Reactor's bounded elastic scheduler.

Small models such as `qwen2.5:1.5b` may not consistently invoke tools. Validate the result against known mock data and inspect logs when debugging.

## Gradle commands
```bash
gradle clean build
gradle test
gradle bootRun
```

A Gradle wrapper is not included. Generate one with `gradle wrapper --gradle-version 8.14.3` if desired.

## Roadmap
- Persistent conversation memory
- RAG with pgvector
- Observability and request metrics
- Authentication, rate limits, and integration tests

## Notes
This is a local demo, not production-ready. The Docker image (if used) needs network access to a running Ollama server; `localhost` inside a container refers to the container, not the host.
