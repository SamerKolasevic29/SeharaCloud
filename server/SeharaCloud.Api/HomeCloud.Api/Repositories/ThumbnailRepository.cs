namespace HomeCloud.Repositories;

using Dapper;
using HomeCloud.Repositories.Interfaces;
using Npgsql;

public class ThumbnailRepository : IThumbnailRepository
{
    private readonly NpgsqlDataSource _dataSource;

    public ThumbnailRepository(NpgsqlDataSource dataSource)
    {
        _dataSource = dataSource;
    }

    public async Task<string?> GetThumbnailPathAsync(Guid id)
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        return await conn.QueryFirstOrDefaultAsync<string>(
           @"SELECT thumbnail_path FROM files WHERE id = @Id
             UNION ALL
             SELECT thumbnail_path FROM artists WHERE id = @Id
             UNION ALL
             SELECT thumbnail_path FROM genres WHERE id = @Id
             LIMIT 1",
            new { Id = id }
        );
    }

}