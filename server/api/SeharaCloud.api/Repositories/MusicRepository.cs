namespace SeharaCloud.Repositories; 


using Dapper;
using SeharaCloud.DTOs;
using SeharaCloud.Repositories.Interfaces;
using Microsoft.Extensions.Configuration;
using Npgsql;

// Repositories/Helpers 
using static SeharaCloud.Repositories.Helpers.SqlThumbnailHelper; 
public class MusicRepository : IMusicRepository
{
    private readonly NpgsqlDataSource _dataSource;
    private readonly string _baseUrl;

    public MusicRepository(NpgsqlDataSource dataSource, IConfiguration config)
    {
        _dataSource = dataSource;
        // TrimEnd ensures no double slashes (e.g. http://localhost:5000//api/...)
        _baseUrl = config["AppSettings:BaseUrl"]!.TrimEnd('/');    
    }

    

    public async Task<IEnumerable<MusicDto>> GetAllAsync()
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
                SELECT
                    m.id                        AS Id,
                    m.filename                  AS Filename,
                    m.mime_type                 AS MimeType,
                    {Single(_baseUrl)}       AS ThumbnailUrl,
                    m.title                     AS Title,
                    a.name                      AS Artist,
                    g.name                      AS Genre,
                    m.duration_sec              AS DurationSec
                FROM music m
                LEFT JOIN artists a ON m.artist_id = a.id
                LEFT JOIN genres g ON a.genre_id = g.id
                ORDER BY m.title
             """;
             return await conn.QueryAsync<MusicDto>(sql);
    }

    public async Task<IEnumerable<MusicDto>> GetRecentAsync(int limit)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
                SELECT
                    m.id                        AS Id,
                    m.filename                  AS Filename,
                    m.mime_type                 AS MimeType,
                    {Single(_baseUrl)}       AS ThumbnailUrl,
                    m.title                     AS Title,
                    a.name                      AS Artist,
                    g.name                      AS Genre,
                    m.duration_sec              AS DurationSec
                FROM music m
                LEFT JOIN artists a ON m.artist_id = a.id
                LEFT JOIN genres g ON a.genre_id = g.id
                ORDER BY m.indexed_at DESC
                LIMIT @Limit
             """;
             return await conn.QueryAsync<MusicDto>(sql, new {Limit = limit});
    }

    public async Task<IEnumerable<ArtistDto>> GetArtistsAsync()
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
               SELECT 
                    a.id                        AS Id,
                    a.name                      AS Name,
                    g.name                      AS GenreName,
                    {Triple(_baseUrl)}      AS ThumbnailUrl,
                    (SELECT COUNT(1) FROM music m WHERE m.artist_id = a.id) AS SongCount
                FROM artists a
                LEFT JOIN genres g ON g.id = a.genre_id
                ORDER BY a.name
        """;
        return await conn.QueryAsync<ArtistDto>(sql); 
    }

    public async Task<IEnumerable<MusicDto>> GetByArtistIdAsync(Guid artistId)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
                SELECT
                    m.id                        AS Id,
                    m.filename                  AS Filename,
                    m.mime_type                 AS MimeType,
                    {Single(_baseUrl)}       AS ThumbnailUrl,
                    m.title                     AS Title,
                    a.name                      AS Artist,
                    g.name                      AS Genre,
                    m.duration_sec              AS DurationSec
                FROM music m
                LEFT JOIN artists a ON m.artist_id = a.id
                LEFT JOIN genres g ON a.genre_id = g.id
                WHERE a.id = @ArtistId
                ORDER BY m.title
        """;
         return await conn.QueryAsync<MusicDto>(sql, new {ArtistId = artistId});

    }

    public async Task<IEnumerable<GenreDto>> GetGenresAsync()
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
            SELECT 
                g.name                      AS Name,
                {Double(_baseUrl)}       AS ThumbnailUrl,
             (
                SELECT COUNT(m.id) 
                FROM music m 
                JOIN artists a ON a.id = m.artist_id 
                WHERE a.genre_id = g.id
            )                           AS SongCount
            FROM genres g
            ORDER BY g.name
        """;
        return await conn.QueryAsync<GenreDto>(sql);
    }


    public async Task<IEnumerable<MusicDto>> GetByGenreIdAsync(Guid genreId)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
                SELECT
                    m.id                        AS Id,
                    m.filename                  AS Filename,
                    m.mime_type                 AS MimeType,
                    {Single(_baseUrl)}       AS ThumbnailUrl,
                    m.title                     AS Title,
                    a.name                      AS Artist,
                    g.name                      AS Genre,
                    m.duration_sec              AS DurationSec
                FROM music m
                LEFT JOIN artists a ON m.artist_id = a.id
                LEFT JOIN genres g ON a.genre_id = g.id
                WHERE g.id = @GenreId
                ORDER BY m.title
        
        """;

        return await conn.QueryAsync<MusicDto>(sql, new {GenreId = genreId});

    }

    public async Task<IEnumerable<MusicDto>> SearchMusicAsync(string query)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
            SELECT
                    m.id                        AS Id,
                    m.filename                  AS Filename,
                    m.mime_type                 AS MimeType,
                    {Single(_baseUrl)}       AS ThumbnailUrl,
                    m.title                     AS Title,
                    a.name                      AS Artist,
                    g.name                      AS Genre,
                    m.duration_sec              AS DurationSec
                FROM music m
                LEFT JOIN artists a ON m.artist_id = a.id
                LEFT JOIN genres g ON a.genre_id = g.id
                WHERE m.title ILIKE @Query
                ORDER BY m.title
        """;
        return await conn.QueryAsync<MusicDto>(sql, new {Query = $"%{query}%"});

    }

        public async Task<IEnumerable<ArtistDto>> SearchArtistAsync(string query)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
                SELECT 
                    a.id                        AS Id,
                    a.name                      AS Name,
                    g.name                      AS GenreName,
                    {Triple(_baseUrl)}      AS ThumbnailUrl,
                    (SELECT COUNT(1) FROM music m WHERE m.artist_id = a.id) AS SongCount
                FROM artists a
                LEFT JOIN genres g ON g.id = a.genre_id
                WHERE a.name ILIKE @Query
                ORDER BY a.name
        """;

        return await conn.QueryAsync<ArtistDto>(sql, new {Query = $"%{query}%"});
    }

    public async Task<(string Path, string MimeType)?> GetStreamInfoAsync(Guid id)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        
        const string sql = """
            SELECT path, mime_type AS MimeType 
            FROM music 
            WHERE id = @Id
        """;

        var result = await conn.QueryFirstOrDefaultAsync<(string Path, string MimeType)>(
            sql, new { Id = id });

        return result.Path is null ? null : result;
    }
}