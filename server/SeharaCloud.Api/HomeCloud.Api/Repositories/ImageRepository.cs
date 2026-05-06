namespace HomeCloud.Repositories;

using Dapper;
using HomeCloud.DTOs;
using HomeCloud.Repositories.Interfaces;
using Npgsql;

public class ImageRepository : IImageRepository
{
    private readonly NpgsqlDataSource _dataSource;
    public ImageRepository(NpgsqlDataSource dataSource) { _dataSource = dataSource; }

    // private method - jointly SELECT for evading repetitive code 
    private static string BaseSelect => """
        SELECT
                f.id             AS Id,
                f.filename       AS Filename,
                f.thumbnail_path AS ThumbnailUrl,
                f.size_bytes     AS SizeBytes,
                i.width          AS Width,
                i.height         AS Height,
                i.date_taken     AS DateTaken,
                i.camera         AS Camera

            FROM files f
            JOIN image_meta i ON f.id = i.file_id
        """;

    public async Task<IEnumerable<ImageDto>> GetAllAsync()
    {
        using var conn = await _dataSource.OpenConnectionAsync();

        var sql = $"{BaseSelect} ORDER BY i.date_taken DESC NULLS LAST";
        return await conn.QueryAsync<ImageDto>(sql);
    }

    public async Task<ImageDto?> GetByIdAsync(Guid id)
    {
        using var conn = await _dataSource.OpenConnectionAsync();

        var sql = $"{BaseSelect} WHERE f.id = @Id";
        return await conn.QueryFirstOrDefaultAsync<ImageDto>(sql, new { Id = id });
    }
}