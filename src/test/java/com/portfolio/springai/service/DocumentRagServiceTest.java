package com.portfolio.springai.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DocumentRagServiceTest {
    @Test
    void splitsAtSentenceBoundaries() {
        String sentence = "The engineer has experience with Spring Boot and React. ";
        var chunks = DocumentRagService.split(sentence.repeat(40));
        assertTrue(chunks.size() > 1);
        assertTrue(chunks.stream().allMatch(s -> s.length() <= 1200));
        assertTrue(chunks.stream().allMatch(s -> s.endsWith(".")));
        assertTrue(chunks.stream().allMatch(s -> !s.startsWith("ngineer")));
    }

    @Test
    void splitsLongUnpunctuatedTextOnWords() {
        var chunks = DocumentRagService.split("enterprise ".repeat(300));
        assertTrue(chunks.size() > 1);
        assertTrue(chunks.stream().allMatch(s -> s.length() <= 1200));
        assertTrue(chunks.stream().allMatch(s -> s.startsWith("enterprise")));
    }

    @Test
    void validatesSourceNumbers() {
        assertEquals("Experience [Source 1] and education.",
                DocumentRagService.validateCitations("Experience [Source 1] and education [Source 9].", 2));
        assertEquals("I cannot find that information in the uploaded documents.",
                DocumentRagService.validateCitations("", 2));
    }

    @Test
    void serializesEmbeddingForPgvector() {
        assertEquals("[0.1,-0.2]", DocumentRagService.vectorLiteral(new float[]{0.1f, -0.2f}));
        assertThrows(IllegalArgumentException.class,
                () -> DocumentRagService.vectorLiteral(new float[]{Float.NaN}));
    }
}
