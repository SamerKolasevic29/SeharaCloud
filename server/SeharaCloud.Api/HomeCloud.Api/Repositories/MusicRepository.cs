namespace HomeCloud.Repositories;

using Dapper;
using HomeCloud.DTOs;
using HomeCloud.Repositories.Interfaces;
using Microsoft.Extensions.Configuration;
using Npgsql;

public class MusicRepository : IMusicRepository
{
    private readonly NpgsqlDataSource _dataSource;
    private readonly string _baseUrl;

    public MusicRepository(NpgsqlDataSource dataSource, IConfiguration config)
    {
        _dataSource = dataSource;
        _baseUrl    = config["AppSettings:BaseUrl"]!;
    }

    private string ThumbnailUrl(string alias = "f") =>
        $"""
        CASE
            WHEN {alias}.thumbnail_path IS NOT NULL
            THEN '{_baseUrl}/api/thumbnail/' || {alias}.id::text
            ELSE NULL
        END
        """;

    public async Task<IEnumerable<MusicDto>> GetAllAsync()
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
            SELECT
                f.id                        AS Id,
                f.filename                  AS Filename,
                {ThumbnailUrl()}            AS ThumbnailUrl,
                m.title                     AS Title,
                a.name                      AS Artist,
                m.album                     AS Album,
                g.name                      AS Genre,
                m.duration_sec              AS DurationSec
            FROM files f
            JOIN music_meta m ON f.id = m.file_id
            LEFT JOIN artists a ON m.artist_id = a.id
            LEFT JOIN genres  g ON m.genre_id  = g.id
            ORDER BY m.title
            """;
        return await conn.QueryAsync<MusicDto>(sql);
    }

    public async Task<IEnumerable<MusicDto>> GetRecentAsync(int limit)
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
            SELECT
                f.id                        AS Id,
                f.filename                  AS Filename,
                {ThumbnailUrl()}            AS ThumbnailUrl,
                m.title                     AS Title,
                a.name                      AS Artist,
                m.album                     AS Album,
                g.name                      AS Genre,
                m.duration_sec              AS DurationSec
            FROM files f
            JOIN music_meta m ON f.id = m.file_id
            LEFT JOIN artists a ON m.artist_id = a.id
            LEFT JOIN genres  g ON m.genre_id  = g.id
            ORDER BY f.indexed_at DESC
            LIMIT @Limit
            """;
        return await conn.QueryAsync<MusicDto>(sql, new { Limit = limit });
    }

    public async Task<IEnumerable<ArtistDto>> GetArtistsAsync()
{
    using var conn = await _dataSource.OpenConnectionAsync();
    var sql = $"""
        SELECT
            a.id                AS Id,
            a.name              AS Name,
            CASE
                WHEN a.thumbnail_path IS NOT NULL
                THEN '{_baseUrl}/api/thumbnail/' || a.id::text
                ELSE NULL
            END                 AS ThumbnailUrl,
            COUNT(m.file_id)    AS SongCount
        FROM artists a
        LEFT JOIN music_meta m ON a.id = m.artist_id
        GROUP BY a.id, a.name, a.thumbnail_path
        ORDER BY a.name
        """;
    return await conn.QueryAsync<ArtistDto>(sql);
}

    public async Task<IEnumerable<MusicDto>> GetByArtistIdAsync(Guid artistId)
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
            SELECT
                f.id                        AS Id,
                f.filename                  AS Filename,
                {ThumbnailUrl()}            AS ThumbnailUrl,
                m.title                     AS Title,
                a.name                      AS Artist,
                m.album                     AS Album,
                g.name                      AS Genre,
                m.duration_sec              AS DurationSec
            FROM files f
            JOIN music_meta m ON f.id = m.file_id
            LEFT JOIN artists a ON m.artist_id = a.id
            LEFT JOIN genres  g ON m.genre_id  = g.id
            WHERE m.artist_id = @ArtistId
            ORDER BY m.album, m.track_no
            """;
        return await conn.QueryAsync<MusicDto>(sql, new { ArtistId = artistId });
    }

    public async Task<IEnumerable<GenreDto>> GetGenresAsync()
{
    using var conn = await _dataSource.OpenConnectionAsync();
    var sql = $"""
        SELECT
            g.id                AS Id,
            g.name              AS Name,
            CASE
                WHEN g.thumbnail_path IS NOT NULL
                THEN '{_baseUrl}/api/thumbnail/' || g.id::text
                ELSE NULL
            END                 AS ThumbnailUrl,
            COUNT(m.file_id)    AS SongCount
        FROM genres g
        LEFT JOIN music_meta m ON g.id = m.genre_id
        GROUP BY g.id, g.name, g.thumbnail_path
        ORDER BY g.name
        """;
    return await conn.QueryAsync<GenreDto>(sql);
}

    public async Task<IEnumerable<MusicDto>> GetByGenreIdAsync(Guid genreId)
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
            SELECT
                f.id                        AS Id,
                f.filename                  AS Filename,
                {ThumbnailUrl()}            AS ThumbnailUrl,
                m.title                     AS Title,
                a.name                      AS Artist,
                m.album                     AS Album,
                m.duration_sec              AS DurationSec
            FROM files f
            JOIN music_meta m ON f.id = m.file_id
            LEFT JOIN artists a ON m.artist_id = a.id
            WHERE m.genre_id = @GenreId
            ORDER BY a.name, m.title
            """;
        return await conn.QueryAsync<MusicDto>(sql, new { GenreId = genreId });
    }

    public async Task<IEnumerable<MusicDto>> SearchAsync(string query)
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
            SELECT
                f.id                        AS Id,
                f.filename                  AS Filename,
                {ThumbnailUrl()}            AS ThumbnailUrl,
                m.title                     AS Title,
                a.name                      AS Artist,
                m.album                     AS Album,
                g.name                      AS Genre,
                m.duration_sec              AS DurationSec
            FROM files f
            JOIN music_meta m ON f.id = m.file_id
            LEFT JOIN artists a ON m.artist_id = a.id
            LEFT JOIN genres  g ON m.genre_id  = g.id
            WHERE m.title  ILIKE @Query
               OR a.name   ILIKE @Query
               OR m.album  ILIKE @Query
            ORDER BY m.title
            """;
        return await conn.QueryAsync<MusicDto>(sql, new { Query = $"%{query}%" });
    }

    public async Task<IEnumerable<ArtistDto>> SearchArtistsAsync(string query)
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
            SELECT
                a.id             AS Id,
                a.name           AS Name,
                a.thumbnail_url  AS ThumbnailUrl,
                COUNT(m.file_id) AS SongCount
            FROM artists a
            LEFT JOIN music_meta m ON a.id = m.artist_id
            WHERE a.name ILIKE @Query
            GROUP BY a.id, a.name, a.thumbnail_url
            ORDER BY a.name
            """;
        return await conn.QueryAsync<ArtistDto>(sql, new { Query = $"%{query}%" });
    }
}