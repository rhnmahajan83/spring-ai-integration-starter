package com.portfolio.springai.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentRagServiceTest {
    @Test
    void splitsLongDocumentsWithOverlap() {
        String input = "a".repeat(2500);
        var chunks = DocumentRagService.split(input);
        assertEquals(3, chunks.size());
        assertEquals(1200, chunks.get(0).length());
        assertEquals(chunks.get(0).substring(1050), chunks.get(1).substring(0, 150));
    }

    @Test
    void serializesEmbeddingForPgvector() {
        assertEquals("[0.1,-0.2]", DocumentRagService.vectorLiteral(new float[]{0.1f, -0.2f}));
        assertThrows(IllegalArgumentException.class,
                () -> DocumentRagService.vectorLiteral(new float[]{Float.NaN}));
    }
}
