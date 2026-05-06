namespace HomeCloud.Repositories;

using Dapper;
using HomeCloud.Repositories.Interfaces;
using Npgsql;

public class StreamRepository : IStreamRepository
{
    private readonly NpgsqlDataSource _dataSource;

    public StreamRepository(NpgsqlDataSource dataSource) { _dataSource = dataSource; }

    public async Task<(string Path, string MimeType)?> GetFileInfoAsync(Guid fileId)
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        const string sql = """
            SELECT path, mime_type AS MimeType
            FROM files
            WHERE id = @FileId
            """;

        var result = await conn.QueryFirstOrDefaultAsync<(string Path, string MimeType)>(
            sql, new { FileId = fileId });

        // if path is an empty string, file does not exist
        return result.Path is null ? null : result;
    }
}