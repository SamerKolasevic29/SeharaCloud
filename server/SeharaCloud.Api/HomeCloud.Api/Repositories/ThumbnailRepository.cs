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

    public async Task<string?> GetThumbnailPathAsync(Guid fileId)
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        return await conn.QueryFirstOrDefaultAsync<string>(
            "SELECT thumbnail_path FROM files WHERE id = @Id",
            new { Id = fileId }
        );
    }
}