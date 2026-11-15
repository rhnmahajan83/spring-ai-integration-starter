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
curl -X POST http://localhost:8080/api/v1/orders/ask-reliable -H 'Content-Type: application/json' -d '{"message":"Why is order ORD-1001 delayed?"}'
```

Health: `GET /actuator/health`.

## Tool calling demo
`OrderTools.getOrderStatus` is a Spring AI `@Tool` that returns mock order data. Available IDs: `ORD-1001` (delayed), `ORD-1002` (in progress), `ORD-1003` (completed). Unknown IDs return a not-found message. `OrderAssistantService` registers the tool with `.tools(orderTools)` and offloads the synchronous model call to Reactor's bounded elastic scheduler.

Small models such as `qwen2.5:1.5b` may not consistently invoke tools. Validate the result against known mock data and inspect logs when debugging.

## Tool tracing and reliable lookup
- `/api/v1/orders/ask` demonstrates autonomous LLM tool selection. Console logs show `ORDER ASK completed: toolInvoked=...`, tool name, tool duration, request duration, and success.
- `/api/v1/orders/ask-reliable` extracts all distinct `ORD-1234` identifiers and retrieves each record on the bounded-elastic scheduler. For multiple orders it returns verified records directly, preventing the LLM from dropping an order. For a single known order it asks the LLM to explain the record, falling back to verified facts if the LLM errors, returns an empty answer, or omits the order ID. It does not guarantee detection of every possible model hallucination.
- The model temperature is set to `0.0`; tool selection may still vary.
- Micrometer metrics: `GET /actuator/metrics/ai.order.tool.invocations`, `GET /actuator/metrics/ai.order.tool.duration`, and `GET /actuator/metrics/ai.order.requests`.
- This demo logs order IDs. Avoid logging sensitive business identifiers in production.

## Conversation memory (local demo)
Start a conversation, then reuse its ID for follow-ups:
```bash
curl -X POST http://localhost:8080/api/v1/orders/conversations
# Copy conversationId from the JSON response
curl -X POST http://localhost:8080/api/v1/orders/conversations/YOUR_ID/messages -H 'Content-Type: application/json' -d '{"message":"Status of ORD-1001 and ORD-1002?"}'
curl -X POST http://localhost:8080/api/v1/orders/conversations/YOUR_ID/messages -H 'Content-Type: application/json' -d '{"message":"Which one is delayed?"}'
curl http://localhost:8080/api/v1/orders/conversations/YOUR_ID
curl -X DELETE http://localhost:8080/api/v1/orders/conversations/YOUR_ID
```
The new conversation endpoint uses a bounded in-memory Java session store (last 10 turns), isolated by unguessable conversation IDs. It resolves explicit order IDs and a small set of follow-up references, then deterministically retrieves the records. **This is application-level conversation memory, not Spring AI ChatMemory or a general-purpose LLM conversation agent.** The existing `/ask` and `/ask-reliable` endpoints are unchanged. Memory disappears on restart, is not shared across app instances, and has no authentication, expiry, or total-session limit; do not expose it publicly without those protections.

## Document RAG (PDF/TXT + Ollama + pgvector)
Requires Docker, PostgreSQL with pgvector, and Ollama's embedding model. Run before starting Spring Boot:
```bash
ollama pull qwen2.5:1.5b
ollama pull nomic-embed-text
docker compose up -d
gradle bootRun
```
`nomic-embed-text` produces 768-dimensional vectors; the demo schema is fixed at 768. If changing embedding models, migrate the vector column and re-index documents. PostgreSQL settings default to `localhost:5432/springai` with demo credentials `springai/springai` and can be overridden using `DB_URL`, `DB_USER`, and `DB_PASSWORD`.

Upload a searchable PDF or UTF-8 text file (up to 10 MB):
```bash
curl -X POST http://localhost:8080/api/v1/documents/upload -F 'file=@sample.txt'
curl -X POST http://localhost:8080/api/v1/documents/ask -H 'Content-Type: application/json' -d '{"question":"What does the installation SOP say about delayed appointments?"}'
```
The response includes `response` and `sources` (document ID, filename, chunk index, excerpt). The pipeline extracts text, splits into overlapping chunks, embeds locally, persists in pgvector, retrieves the four nearest chunks by cosine distance, and prompts Qwen to answer with source labels. Scanned PDFs without selectable text need OCR and are not supported. Retrieval uses a configurable minimum cosine similarity (`app.rag.min-similarity`, default `0.55`), sentence/word-aware chunking, and removes references to nonexistent source numbers. Citation validation checks only source-number validity, **not whether the cited excerpt actually supports the claim**. The similarity threshold is a starting value and should be tuned using representative questions. Re-upload existing documents to re-index them with the new chunker; previously indexed chunks remain unchanged. Do not treat answers as authoritative without checking the returned source excerpts.

This demo initializes its tables at startup and requires a working database even when only using older endpoints. It has no authentication, per-user document isolation, migration management, or vector index optimization. Secure these before production use.

## Gradle commands
```bash
gradle clean build
gradle test
gradle bootRun
```

A Gradle wrapper is not included. Generate one with `gradle wrapper --gradle-version 8.14.3` if desired.

## Roadmap
- Persistent conversation memory
- Improve RAG retrieval quality, relevance thresholds and integration tests
- Observability and request metrics
- Authentication, rate limits, and integration tests

## Notes
This is a local demo, not production-ready. The Docker image (if used) needs network access to a running Ollama server; `localhost` inside a container refers to the container, not the host.
