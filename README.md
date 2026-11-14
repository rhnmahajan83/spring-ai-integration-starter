# Spring AI Integration Starter

Portfolio-ready Spring Boot API for OpenAI-backed chat, summarization, and SSE streaming.

## Stack
Java 21, Spring Boot 4.0.1, Spring AI 2.0.2, Gradle, WebFlux, Validation, Actuator.

## Prerequisites
Java 21, Gradle 8.14+, an OpenAI API key with API billing enabled.

## Run
```bash
export OPENAI_API_KEY='your-api-key'
gradle bootRun
```

## API
```bash
curl -X POST http://localhost:8080/api/v1/ai/chat -H 'Content-Type: application/json' -d '{"message":"Explain RAG in two sentences"}'
curl -X POST http://localhost:8080/api/v1/ai/summarize -H 'Content-Type: application/json' -d '{"message":"Spring AI provides a portable API for integrating language models with enterprise services."}'
curl -N -X POST http://localhost:8080/api/v1/ai/stream -H 'Content-Type: application/json' -d '{"message":"Explain vector embeddings"}'
```
Health: `GET /actuator/health`.

## Docker
```bash
docker build -t spring-ai-starter .
docker run --rm -p 8080:8080 -e OPENAI_API_KEY="$OPENAI_API_KEY" spring-ai-starter
```

## Roadmap
- Provider selection (OpenAI / Ollama) via profiles
- Tool calling with a safe demo business function
- Persistent conversation memory
- RAG with pgvector
- OpenTelemetry tracing, request metrics, and cost monitoring
- Authentication, rate limits, circuit breakers, and robust integration tests

## Notes
This is a functional starter scaffold, not a hardened production deployment. API calls incur provider charges. Do not commit API keys. Verify dependency resolution and tests in your environment.

## Gradle commands
```bash
gradle clean build
gradle test
gradle bootRun
```

A Gradle wrapper is not included; install Gradle 8.14+ or generate a wrapper using `gradle wrapper --gradle-version 8.14.3`.
# spring-ai-integration-starter
