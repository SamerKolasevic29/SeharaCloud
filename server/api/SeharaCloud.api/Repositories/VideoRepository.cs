namespace SeharaCloud.Repositories;

using Dapper;
using SeharaCloud.DTOs;
using SeharaCloud.Repositories.Interfaces;
using Microsoft.Extensions.Configuration;
using Npgsql;

// Repositories/Helpers 
using static SeharaCloud.Repositories.Helpers.SqlThumbnailHelper; 


public class VideoRepository : IVideoRepository
{
    private readonly NpgsqlDataSource _dataSource;
    private readonly string _baseUrl;

    public VideoRepository(NpgsqlDataSource dataSource, IConfiguration config)
    {
        _dataSource = dataSource;
        _baseUrl = config["AppSettings:BaseUrl"]!.TrimEnd('/');
    }


    public async Task<IEnumerable<VideoDto>> GetAllAsync()
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
                SELECT
                   v.id                       AS Id,
                   v.filename                 AS Filename,
                   v.mime_type                AS MimeType,
                   {Single(_baseUrl, "v")}    AS ThumbnailUrl,
                   v.title                    AS Title,
                   v.duration_sec             AS DurationSec,
                   v.resolution               AS Resolution,
                   v.codec                    AS Codec
                FROM videos v
                ORDER BY v.title
        """;

        return await conn.QueryAsync<VideoDto>(sql);
    }

    public async Task<IEnumerable<VideoDto>> GetRecentAsync(int limit)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
                SELECT
                   v.id                       AS Id,
                   v.filename                 AS Filename,
                   v.mime_type                AS MimeType,
                   {Single(_baseUrl, "v")}    AS ThumbnailUrl,
                   v.title                    AS Title,
                   v.duration_sec             AS DurationSec,
                   v.resolution               AS Resolution,
                   v.codec                    AS Codec
                FROM videos v
                ORDER BY v.indexed_at DESC
                LIMIT @limit
        """;

        return await conn.QueryAsync<VideoDto>(sql, new {Limit = limit});
    }


    public async Task<IEnumerable<VideoDto>> SearchVideoAsync(string query)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
                SELECT
                   v.id                       AS Id,
                   v.filename                 AS Filename,
                   v.mime_type                AS MimeType,
                   {Single(_baseUrl, "v")}    AS ThumbnailUrl,
                   v.title                    AS Title,
                   v.duration_sec             AS DurationSec,
                   v.resolution               AS Resolution,
                   v.codec                    AS Codec
                FROM videos v
                WHERE v.title ILIKE @Query
                ORDER BY v.title
        """;

        return await conn.QueryAsync<VideoDto>(sql, new {Query = $"%{query}%"});
    }

}