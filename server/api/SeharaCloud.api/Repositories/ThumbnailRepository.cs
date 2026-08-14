namespace SeharaCloud.Repositories;

using Dapper;
using SeharaCloud.Repositories.Interfaces;
using Npgsql;
using System;
using System.Threading.Tasks;

public class ThumbnailRepository : IThumbnailRepository
{
    private readonly NpgsqlDataSource _dataSource;

    public ThumbnailRepository(NpgsqlDataSource dataSource)
    {
        _dataSource = dataSource;
    }

     public async Task<(string Path, string MimeType)?> GetThumbnailInfoAsync(Guid id)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        
        const string sql = """
            SELECT path, mime_type AS MimeType 
            FROM thumbnails 
            WHERE id = @Id
        """;

        var result = await conn.QueryFirstOrDefaultAsync<(string Path, string MimeType)>(
            sql, new { Id = id });

        return result.Path is null ? null : result;
    }
}