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

    public async Task<string?> GetThumbnailPathAsync(Guid id)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        
        // Sada radimo direktan upit u tabelu 'thumbnails'
        var sql = """
            SELECT path 
            FROM thumbnails 
            WHERE id = @Id
        """;

        return await conn.QueryFirstOrDefaultAsync<string>(sql, new { Id = id });
    }
}