namespace HomeCloud.Repositories;

using Dapper;
using HomeCloud.DTOs;
using HomeCloud.Enums;
using HomeCloud.Repositories.Interfaces;
using Npgsql;

public class DocumentRepository : IDocumentRepository
{
    private readonly NpgsqlDataSource _dataSource;

    public DocumentRepository(NpgsqlDataSource dataSource){ _dataSource = dataSource; }

    // private method - jointly SELECT for evading repetitive code 
    private static string BaseSelect => """
        SELECT
            f.id             AS Id,
            f.filename       AS Filename,
            f.thumbnail_path AS ThumbnailPath,
            f.size_bytes     AS SizeBytes,
            d.title          AS Title,
            d.category       AS Category,
            d.author         AS Author,
            d.page_count     AS PageCount

        FROM files f
        JOIN document_meta d ON f.id = d.file_id
        """;

    public async Task<IEnumerable<DocumentDto>> GetAllAsync()
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"{BaseSelect} ORDER BY d.title";
        return await conn.QueryAsync<DocumentDto>(sql);
    }

    public async Task<IEnumerable<DocumentDto>> GetByCategoryAsync(DocumentCategory category)
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"{BaseSelect} WHERE d.category = @Category ORDER BY d.title";
        return await conn.QueryAsync<DocumentDto>(sql,
            new { Category = category.ToString().ToLower() });
    }

    public async Task<IEnumerable<DocumentDto>> SearchAsync(string query)
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
            {BaseSelect}
            WHERE d.title  ILIKE @Query
               OR d.author ILIKE @Query
            ORDER BY d.title
            """;
        return await conn.QueryAsync<DocumentDto>(sql, new { Query = $"%{query}%" });
    }

    public async Task<IEnumerable<DocumentDto>> SearchByCategoryAsync(
        string query, DocumentCategory category)
    {
        using var conn = await _dataSource.OpenConnectionAsync();
        var sql = $"""
            {BaseSelect}
            WHERE d.category = @Category
              AND (d.title ILIKE @Query OR d.author ILIKE @Query)
            ORDER BY d.title
            """;
        return await conn.QueryAsync<DocumentDto>(sql, new
        {
            Category = category.ToString().ToLower(),
            Query = $"%{query}%"
        });
    }
}