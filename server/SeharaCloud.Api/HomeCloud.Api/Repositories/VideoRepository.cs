namespace HomeCloud.Repositories;

using Dapper;
using HomeCloud.DTOs;
using HomeCloud.Enums;
using HomeCloud.Repositories.Interfaces;
using Npgsql;

public class VideoRepository : IVideoRepository
{
    private readonly NpgsqlDataSource _dataSource;

     public VideoRepository(NpgsqlDataSource dataSource) { _dataSource = dataSource; }

    // private method - jointly SELECT for evading repetitive code 
    private static string BaseSelect => """
        SELECT
            f.id                AS Id,
            f.filename          AS Filename,
            f.thumbnail_path    AS ThumbnailUrl,
            v.title             AS Title,
            v.category          AS Category,
            v.year              AS Year,
            v.duration_sec      AS DurationSec,
            v.resolution        AS Resolution,
            v.codec             AS Codec

        FROM files f
        JOIN video_meta v ON f.id = v.file_id
        """;

    public async Task<IEnumerable<VideoDto>> GetAllAsync()
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"{BaseSelect} ORDER BY v.title";
        return await conn.QueryAsync<VideoDto>(sql);
    }

    public async Task<IEnumerable<VideoDto>> GetRecentAsync(int limit)
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"{BaseSelect} ORDER BY f.indexed_at DESC LIMIT @Limit";
        return await conn.QueryAsync<VideoDto>(sql, new { Limit = limit });
    }

    public async Task<IEnumerable<VideoDto>> GetByCategoryAsync(VideoCategory category)
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"{BaseSelect} WHERE v.category = @Category ORDER BY v.title";

        // Enum -> string beacuse DB holds TEXT
        return await conn.QueryAsync<VideoDto>(sql,
            new { Category = category.ToString().ToLower() });
    }

    public async Task<IEnumerable<VideoDto>> SearchAsync(string query)
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"{BaseSelect} WHERE v.title ILIKE @Query ORDER BY v.title";
        return await conn.QueryAsync<VideoDto>(sql, new { Query = $"%{query}%" });
    }

    public async Task<IEnumerable<VideoDto>> SearchByCategoryAsync(
        string query, VideoCategory category)
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
            {BaseSelect}
            WHERE v.category = @Category
              AND v.title ILIKE @Query
            ORDER BY v.title
            """;
        return await conn.QueryAsync<VideoDto>(sql, new
        {
            Category = category.ToString().ToLower(),
            Query = $"%{query}%"
        });
    }
}