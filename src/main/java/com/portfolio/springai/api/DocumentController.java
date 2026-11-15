package com.portfolio.springai.api;

import com.portfolio.springai.service.DocumentRagService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {
    private final DocumentRagService service;

    public DocumentController(DocumentRagService service) {
        this.service = service;
    }

    public record AskRequest(@NotBlank @Size(max = 8000) String question) {}

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<DocumentRagService.UploadResult> upload(@RequestPart("file") FilePart file) {
        return DataBufferUtils.join(file.content(), 10 * 1024 * 1024)
                .flatMap(buffer -> {
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    try { buffer.read(bytes); }
                    finally { DataBufferUtils.release(buffer); }
                    return Mono.fromCallable(() -> service.ingest(file.filename(), bytes))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .onErrorMap(IllegalArgumentException.class,
                        e -> new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    @PostMapping("/ask")
    public Mono<DocumentRagService.Answer> ask(@Valid @RequestBody AskRequest request) {
        return Mono.fromCallable(() -> service.ask(request.question()))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
