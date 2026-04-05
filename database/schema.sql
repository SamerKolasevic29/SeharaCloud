-- Enable UUID gen
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Central Table for all files
CREATE TABLE files (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    path           TEXT NOT NULL UNIQUE,
    filename       TEXT NOT NULL,
    file_type      TEXT NOT NULL
                   CHECK (file_type IN ('video','music','image','document')),
    mime_type      TEXT,
    size_bytes     BIGINT NOT NULL DEFAULT 0,
    thumbnail_path TEXT,
    duration_sec   INT,
    metadata       JSONB NOT NULL DEFAULT '{}',
    indexed_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    file_hash      TEXT
);

CREATE INDEX idx_files_type     ON files (file_type);
CREATE INDEX idx_files_indexed  ON files (indexed_at DESC);
CREATE INDEX idx_files_hash     ON files (file_hash);
CREATE INDEX idx_files_metadata ON files USING GIN (metadata);
