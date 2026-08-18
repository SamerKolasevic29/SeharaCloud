-- ============================================================
-- SEHARACLOUD - Nova shema (v3) sa 'thumbnails' tabelom
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ============================================================
-- SENTINEL UUIDs (Fixed IDs for system fallback data) -- ============================================================
-- Unknown Genre             : 00000000-0000-0000-0000-000000000000
-- Unknown Artist            : 00000000-0000-0000-0000-000000000001
-- Default Genre Thumbnail   : 00000000-0000-0000-0000-000000000010
-- Default Artist Thumb 1    : 00000000-0000-0000-0000-000000000011
-- Default Song Thumbnail    : 00000000-0000-0000-0000-000000000014
-- Default Video Thumbnail   : 00000000-0000-0000-0000-000000000015
-- Default Document Thumbnail: 00000000-0000-0000-0000-000000000016
-- Default Image Thumbnail   : 00000000-0000-0000-0000-000000000017

-- ============================================================
-- THUMBNAILS
-- ============================================================
CREATE TABLE thumbnails (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    path       TEXT NOT NULL UNIQUE,
    mime_type  TEXT,
    width      INT,
    height     INT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- GENRES
-- ============================================================
CREATE TABLE genres (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          TEXT NOT NULL UNIQUE,
    thumbnail_id1 UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000010'
                  REFERENCES thumbnails(id) ON DELETE SET DEFAULT,
    thumbnail_id2 UUID REFERENCES thumbnails(id) ON DELETE SET NULL
);

-- ============================================================
-- ARTISTS
-- ============================================================
CREATE TABLE artists (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          TEXT NOT NULL UNIQUE,
    genre_id      UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000'
                  REFERENCES genres(id) ON DELETE SET DEFAULT,
    thumbnail_id1 UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000011'
                  REFERENCES thumbnails(id) ON DELETE SET DEFAULT,
    thumbnail_id2 UUID DEFAULT NULL REFERENCES thumbnails(id) ON DELETE SET NULL,
    thumbnail_id3 UUID DEFAULT NULL REFERENCES thumbnails(id) ON DELETE SET NULL
);

-- ============================================================
-- MUSIC
-- ============================================================
CREATE TABLE music (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    path         TEXT NOT NULL UNIQUE,
    filename     TEXT NOT NULL,
    mime_type    TEXT,
    file_hash    TEXT,
    size_bytes   BIGINT NOT NULL DEFAULT 0,
    indexed_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    title        TEXT NOT NULL DEFAULT 'Unknown song',
    artist_id    UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000001'
                 REFERENCES artists(id) ON DELETE SET DEFAULT,
    duration_sec INT,
    bitrate      INT,
    thumbnail_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000014'
                 REFERENCES thumbnails(id) ON DELETE SET DEFAULT
);

-- ============================================================
-- VIDEOS
-- ============================================================
CREATE TABLE videos (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    path         TEXT NOT NULL UNIQUE,
    filename     TEXT NOT NULL,
    mime_type    TEXT,
    file_hash    TEXT,
    size_bytes   BIGINT NOT NULL DEFAULT 0,
    indexed_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    title        TEXT NOT NULL DEFAULT 'Unknown video',
    duration_sec INT,
    resolution   TEXT,
    codec        TEXT,
    thumbnail_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000015'
                 REFERENCES thumbnails(id) ON DELETE SET DEFAULT
);

-- ============================================================
-- DOCUMENTS
-- ============================================================
CREATE TABLE documents (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    path         TEXT NOT NULL UNIQUE,
    filename     TEXT NOT NULL,
    mime_type    TEXT,
    file_hash    TEXT,
    size_bytes   BIGINT NOT NULL DEFAULT 0,
    indexed_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    title        TEXT NOT NULL DEFAULT 'Unknown document',
    page_count   INT,
    thumbnail_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000016'
                 REFERENCES thumbnails(id) ON DELETE SET DEFAULT
);

-- ============================================================
-- IMAGES
-- ============================================================
CREATE TABLE images (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    path         TEXT NOT NULL UNIQUE,
    filename     TEXT NOT NULL,
    mime_type    TEXT,
    file_hash    TEXT,
    size_bytes   BIGINT NOT NULL DEFAULT 0,
    indexed_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    title        TEXT NOT NULL DEFAULT 'Unknown image',
    width        INT,
    height       INT,
    date_taken   TIMESTAMPTZ,
    camera       TEXT,
    thumbnail_id UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000017'
                 REFERENCES thumbnails(id) ON DELETE SET DEFAULT
);

-- ============================================================
-- INDEXES
-- ============================================================

-- genres & artists
CREATE INDEX idx_genres_name_trgm      ON genres USING gin (name gin_trgm_ops);
CREATE INDEX idx_artists_genre_id      ON artists (genre_id);
CREATE INDEX idx_artists_name_trgm     ON artists USING gin (name gin_trgm_ops);

-- music
CREATE INDEX idx_music_artist_id       ON music (artist_id);
CREATE INDEX idx_music_title_trgm      ON music USING gin (title gin_trgm_ops);
CREATE INDEX idx_music_indexed         ON music (indexed_at DESC);
CREATE INDEX idx_music_hash            ON music (file_hash);
CREATE INDEX idx_music_thumbnail_id    ON music (thumbnail_id);

-- videos
CREATE INDEX idx_videos_title_trgm     ON videos USING gin (title gin_trgm_ops);
CREATE INDEX idx_videos_indexed        ON videos (indexed_at DESC);
CREATE INDEX idx_videos_hash           ON videos (file_hash);
CREATE INDEX idx_videos_thumbnail_id   ON videos (thumbnail_id);

-- documents
CREATE INDEX idx_documents_title_trgm  ON documents USING gin (title gin_trgm_ops);
CREATE INDEX idx_documents_indexed     ON documents (indexed_at DESC);
CREATE INDEX idx_documents_hash        ON documents (file_hash);
CREATE INDEX idx_documents_thumbnail_id ON documents (thumbnail_id);

-- images
CREATE INDEX idx_images_date_taken     ON images (date_taken DESC);
CREATE INDEX idx_images_indexed        ON images (indexed_at DESC);
CREATE INDEX idx_images_hash           ON images (file_hash);
CREATE INDEX idx_images_thumbnail_id   ON images (thumbnail_id);

-- ============================================================
-- SEED DATA
-- ============================================================

-- 1. Insert 6 system default thumbnails
INSERT INTO thumbnails (id, path, width, height) VALUES
('00000000-0000-0000-0000-000000000010', '/mnt/cloud/thumbnails/GenreThumbnail.png', 512, 512),
('00000000-0000-0000-0000-000000000011', '/mnt/cloud/thumbnails/ArtistThumbnail.png', 512, 512),
('00000000-0000-0000-0000-000000000014', '/mnt/cloud/thumbnails/SongThumbnail.png', 512, 512),
('00000000-0000-0000-0000-000000000015', '/mnt/cloud/thumbnails/VideoThumbnail.png', 1280, 720),
('00000000-0000-0000-0000-000000000016', '/mnt/cloud/thumbnails/DocumentThumbnail.png', 256, 256),
('00000000-0000-0000-0000-000000000017', '/mnt/cloud/thumbnails/ImageThumbnail.png', 512, 512)
ON CONFLICT (id) DO NOTHING;

-- 2. Insert Unknown Genre and Unknown Artist
INSERT INTO genres (id, name, thumbnail_id1)
VALUES (
    '00000000-0000-0000-0000-000000000000',
    'Unknown Genre',
    '00000000-0000-0000-0000-000000000010'
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO artists (id, name, genre_id, thumbnail_id1)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Unknown Artist',
    '00000000-0000-0000-0000-000000000000',
    '00000000-0000-0000-0000-000000000011'
)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- SENTINEL ROW PROTECTION
-- Prevent accidental deletion of Sentinel / System default rows.
-- ============================================================
CREATE OR REPLACE FUNCTION fn_protect_unknown_rows()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF OLD.id IN (
        '00000000-0000-0000-0000-000000000000',
        '00000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000010',
        '00000000-0000-0000-0000-000000000011',
        '00000000-0000-0000-0000-000000000014',
        '00000000-0000-0000-0000-000000000015',
        '00000000-0000-0000-0000-000000000016',
        '00000000-0000-0000-0000-000000000017'
    ) THEN
        RAISE EXCEPTION 'Sentinel red (Default System Data) se ne može obrisati, id: %', OLD.id;
    END IF;
    RETURN OLD;
END;
$$;

CREATE TRIGGER trg_protect_unknown_genre
BEFORE DELETE ON genres
FOR EACH ROW EXECUTE FUNCTION fn_protect_unknown_rows();

CREATE TRIGGER trg_protect_unknown_artist
BEFORE DELETE ON artists
FOR EACH ROW EXECUTE FUNCTION fn_protect_unknown_rows();

CREATE TRIGGER trg_protect_default_thumbnails
BEFORE DELETE ON thumbnails
FOR EACH ROW EXECUTE FUNCTION fn_protect_unknown_rows();

-- ============================================================
-- FUNCTION: fn_resolve_artist(p_name, p_similarity_threshold)
-- ============================================================
CREATE OR REPLACE FUNCTION fn_resolve_artist(
    p_name TEXT,
    p_similarity_threshold REAL DEFAULT 0.4
)
RETURNS artists
LANGUAGE plpgsql
AS $$
DECLARE
    v_artist artists%ROWTYPE;
BEGIN
    -- 1) Exact match, case-insensitive
    SELECT * INTO v_artist
    FROM artists
    WHERE lower(name) = lower(trim(p_name))
    LIMIT 1;

    IF FOUND THEN
        RETURN v_artist;
    END IF;

    -- 2) Fuzzy match (pg_trgm)
    SELECT * INTO v_artist
    FROM artists
    WHERE similarity(name, p_name) > p_similarity_threshold
    ORDER BY similarity(name, p_name) DESC
    LIMIT 1;

    IF FOUND THEN
        RETURN v_artist;
    END IF;

    -- 3) Fallback to Unknown Artist
    SELECT * INTO v_artist
    FROM artists
    WHERE id = '00000000-0000-0000-0000-000000000001';

    RETURN v_artist;
END;
$$;

-- ============================================================
-- VIEW: v_artists_full
-- ============================================================
CREATE OR REPLACE VIEW v_artists_full AS
SELECT
    a.id            AS artist_id,
    a.name          AS artist_name,
    a.thumbnail_id1 AS artist_thumbnail_id1,
    t1.path         AS artist_thumbnail_path1,
    a.thumbnail_id2 AS artist_thumbnail_id2,
    t2.path         AS artist_thumbnail_path2,
    a.thumbnail_id3 AS artist_thumbnail_id3,
    t3.path         AS artist_thumbnail_path3,
    g.id            AS genre_id,
    g.name          AS genre_name
FROM artists a
JOIN genres g ON g.id = a.genre_id
LEFT JOIN thumbnails t1 ON t1.id = a.thumbnail_id1
LEFT JOIN thumbnails t2 ON t2.id = a.thumbnail_id2
LEFT JOIN thumbnails t3 ON t3.id = a.thumbnail_id3;

-- ============================================================
-- FUNCTION: fn_find_duplicate(p_hash)
-- ============================================================
CREATE OR REPLACE FUNCTION fn_find_duplicate(p_hash TEXT)
RETURNS TABLE(source_table TEXT, id UUID, path TEXT)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
    SELECT 'music'::TEXT, m.id, m.path FROM music m WHERE m.file_hash = p_hash
    UNION ALL
    SELECT 'videos'::TEXT, v.id, v.path FROM videos v WHERE v.file_hash = p_hash
    UNION ALL
    SELECT 'documents'::TEXT, d.id, d.path FROM documents d WHERE d.file_hash = p_hash
    UNION ALL
    SELECT 'images'::TEXT, i.id, i.path FROM images i WHERE i.file_hash = p_hash;
END;
$$;
