namespace SeharaCloud.Repositories;

using Dapper;
using SeharaCloud.DTOs;
using SeharaCloud.Repositories.Interfaces;
using Microsoft.Extensions.Configuration;
using Npgsql;

using static SeharaCloud.Repositories.Helpers.SqlThumbnailHelper;

public class ImageRepository : IImageRepository
{
    private readonly NpgsqlDataSource _dataSource;
    private readonly string _baseUrl;


    public ImageRepository(NpgsqlDataSource dataSource, IConfiguration config)
    {
        _dataSource = dataSource;
        _baseUrl = config["AppSettings:BaseUrl"]!.TrimEnd('/');
    }

    public async Task<IEnumerable<ImageDto>> GetAllAsync()
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
                SELECT 
                    i.id                       AS Id,
                    i.filename                 AS Filename,
                    {Single(_baseUrl, "i")}    AS ThumbnailUrl,
                    i.size_bytes               AS SizeBytes,
                    i.width                    AS Width,
                    i.height                   AS height,
                    i.date_taken               AS DateTaken,
                    i.camera                   AS Camera
                FROM images i
                ORDER BY i.date_taken DESC NULLS LAST
        """;

        return await conn.QueryAsync<ImageDto>(sql);
    }

    public async Task<ImageDto?> GetByIdAsync(Guid id)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
                SELECT
                    i.id                       AS Id,
                    i.filename                 AS Filename,
                    {Single(_baseUrl, "i")}    AS ThumbnailUrl,
                    i.size_bytes               AS SizeBytes,
                    i.width                    AS Width,
                    i.height                   AS Height,
                    i.date_taken               AS DateTaken,
                    i.camera                   AS Camera
                FROM images i 
                WHERE i.id = @Id
        """;

        return await conn.QueryFirstOrDefaultAsync<ImageDto>(sql, new { Id = id });
    }
}