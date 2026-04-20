-- Enable UUID gen
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- central table for all files that includes everything except metadata 
CREATE TABLE files (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    path           TEXT NOT NULL UNIQUE,
    filename       TEXT NOT NULL,
    file_type      TEXT NOT NULL
                   CHECK (file_type IN ('video','music','image','document')),
    mime_type      TEXT,
    size_bytes     BIGINT NOT NULL DEFAULT 0,
    thumbnail_path TEXT,
    indexed_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    file_hash      TEXT
);

-- Music metadata table 
CREATE TABLE music_meta (
    file_id    UUID PRIMARY KEY REFERENCES files(id) ON DELETE CASCADE,
    title      TEXT,
    artist     TEXT,
    album      TEXT,
    genre      TEXT,
    year       INT,
    duration_sec INT,
    bitrate    INT,
    track_no   INT
);

-- Video metadata table
CREATE TABLE video_meta (
    file_id      UUID PRIMARY KEY REFERENCES files(id) ON DELETE CASCADE,
    title        TEXT,
    category     TEXT CHECK (category IN ('movie','documentary','other')),
    year         INT,
    duration_sec INT,
    resolution   TEXT,
    codec        TEXT
);

-- documents metadata table
CREATE TABLE document_meta (
    file_id    UUID PRIMARY KEY REFERENCES files(id) ON DELETE CASCADE,
    title      TEXT,
    category   TEXT CHECK (category IN ('book','document','other')),
    author     TEXT,
    page_count INT
);

-- images metadata table
CREATE TABLE image_meta (
    file_id    UUID PRIMARY KEY REFERENCES files(id) ON DELETE CASCADE,
    width      INT,
    height     INT,
    date_taken TIMESTAMPTZ,
    camera     TEXT
);


-- indexes on columns who are most common in queries/APIs

-- files
CREATE INDEX idx_files_type    ON files (file_type);
CREATE INDEX idx_files_indexed ON files (indexed_at DESC);

-- music_meta
CREATE INDEX idx_music_artist ON music_meta (artist);
CREATE INDEX idx_music_genre  ON music_meta (genre);
CREATE INDEX idx_music_title  ON music_meta (title);

-- video_meta
CREATE INDEX idx_video_category ON video_meta (category);
CREATE INDEX idx_video_title    ON video_meta (title);

-- document_meta
CREATE INDEX idx_doc_category ON document_meta (category);
CREATE INDEX idx_doc_title    ON document_meta (title);

-- image_meta
CREATE INDEX idx_image_date ON image_meta (date_taken DESC);
