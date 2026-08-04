namespace HomeCloud.Repositories;

using Dapper;
using HomeCloud.DTOs;
using HomeCloud.Repositories.Interfaces;
using Microsoft.Extensions.Configuration;
using Npgsql;

public class ImageRepository : IImageRepository
{
    private readonly NpgsqlDataSource _dataSource;
    private readonly string _baseUrl;

    public ImageRepository(NpgsqlDataSource dataSource, IConfiguration config)
    {
        _dataSource = dataSource;
        _baseUrl    = config["AppSettings:BaseUrl"]!;
    }

    public async Task<IEnumerable<ImageDto>> GetAllAsync()
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
            SELECT
                f.id                AS Id,
                f.filename          AS Filename,
                CASE
                    WHEN f.thumbnail_path IS NOT NULL
                    THEN '{_baseUrl}/api/thumbnail/' || f.id::text
                    ELSE NULL
                END                 AS ThumbnailUrl,
                f.size_bytes        AS SizeBytes,
                i.width             AS Width,
                i.height            AS Height,
                i.date_taken        AS DateTaken,
                i.camera            AS Camera
            FROM files f
            JOIN image_meta i ON f.id = i.file_id
            ORDER BY i.date_taken DESC NULLS LAST
            """;
        return await conn.QueryAsync<ImageDto>(sql);
    }

    public async Task<ImageDto?> GetByIdAsync(Guid id)
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
            SELECT
                f.id                AS Id,
                f.filename          AS Filename,
                CASE
                    WHEN f.thumbnail_path IS NOT NULL
                    THEN '{_baseUrl}/api/thumbnail/' || f.id::text
                    ELSE NULL
                END                 AS ThumbnailUrl,
                f.size_bytes        AS SizeBytes,
                i.width             AS Width,
                i.height            AS Height,
                i.date_taken        AS DateTaken,
                i.camera            AS Camera
            FROM files f
            JOIN image_meta i ON f.id = i.file_id
            WHERE f.id = @Id
            """;
        return await conn.QueryFirstOrDefaultAsync<ImageDto>(sql, new { Id = id });
    }
}