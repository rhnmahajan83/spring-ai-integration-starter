package com.portfolio.springai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class RagDatabaseConfig {
    public RagDatabaseConfig(JdbcTemplate jdbc) {
        jdbc.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS rag_documents (
                    id uuid PRIMARY KEY,
                    filename text NOT NULL,
                    created_at timestamptz NOT NULL DEFAULT now()
                )
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS rag_chunks (
                    id bigserial PRIMARY KEY,
                    document_id uuid NOT NULL REFERENCES rag_documents(id) ON DELETE CASCADE,
                    chunk_index integer NOT NULL,
                    content text NOT NULL,
                    embedding vector(768) NOT NULL,
                    UNIQUE (document_id, chunk_index)
                )
                """);
    }
}
