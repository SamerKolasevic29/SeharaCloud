-- Enable UUID gen
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS pg_trgm;

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
    file_id       UUID PRIMARY KEY REFERENCES files(id) ON DELETE CASCADE,
    title         TEXT NOT NULL,
    artist_id     UUID REFERENCES artists(id) ON DELETE SET NULL,
    genre_id      UUID REFERENCES genres(id) ON DELETE SET NULL,
    album         TEXT,
    year          INT,
    duration_sec  INT,
    bitrate       INT,
    track_no      INT
);

-- LOOKUP tables for Music metadata table
CREATE TABLE artists (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          TEXT NOT NULL UNIQUE,
    thumbnail_url TEXT
);

CREATE TABLE genres (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          TEXT NOT NULL UNIQUE,
    thumbnail_url TEXT
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

-- music_meta ("fuzzy" search by title, FK via 'artists' and 'genres' tables)
CREATE INDEX idx_music_title_trgm ON music_meta USING gin (title gin_trgm_ops);
CREATE INDEX idx_music_artist_id ON music_meta (artist_id);
CREATE INDEX idx_music_genre_id  ON music_meta (genre_id);

-- video_meta
CREATE INDEX idx_video_category ON video_meta (category);
CREATE INDEX idx_video_title_trgm ON video_meta USING gin (title gin_trgm_ops);

-- document_meta
CREATE INDEX idx_doc_category ON document_meta (category);
CREATE INDEX idx_doc_title_trgm ON document_meta USING gin (title gin_trgm_ops);

-- image_meta
CREATE INDEX idx_image_date ON image_meta (date_taken DESC);
