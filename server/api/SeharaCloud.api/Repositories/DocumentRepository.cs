namespace SeharaCloud.Repositories;

using Dapper;
using SeharaCloud.DTOs;
using SeharaCloud.Repositories.Interfaces;
using Microsoft.Extensions.Configuration;
using Npgsql;

// Repositories/Helpers 
using static SeharaCloud.Repositories.Helpers.SqlThumbnailHelper; 

public class DocumentRepository : IDocumentRepository
{
    private readonly NpgsqlDataSource _dataSource;
    private readonly string _baseUrl;
    public DocumentRepository(NpgsqlDataSource dataSource, IConfiguration config){
        _dataSource = dataSource;
        _baseUrl = config["AppSettings:BaseUrl"]!.TrimEnd('/');
    }

    public async Task<IEnumerable<DocumentDto>> GetAllAsync()
    {
       await using var conn = await _dataSource.OpenConnectionAsync();
       
        var sql = $"""
                SELECT 
                    d.id                       AS Id,
                    d.filename                 AS Filename,
                    {Single(_baseUrl, "d")}    AS ThumbnailUrl,
                    d.title                    AS Title,
                    d.page_count               AS PageCount
                FROM documents d
                ORDER BY d.title
        """;
       return await conn.QueryAsync<DocumentDto>(sql);

    }

    public async Task<IEnumerable<DocumentDto>> GetRecentAsync(int limit)
    {
       await using var conn = await _dataSource.OpenConnectionAsync();
       
        var sql = $"""
                SELECT 
                    d.id                       AS Id,
                    d.filename                 AS Filename,
                    {Single(_baseUrl, "d")}    AS ThumbnailUrl,
                    d.title                    AS Title,
                    d.page_count               AS PageCount
                FROM documents d
                ORDER BY d.indexed_at DESC
                LIMIT @Limit
        """;
       return await conn.QueryAsync<DocumentDto>(sql, new {Limit = limit}); 
    }

    public async Task<IEnumerable<DocumentDto>> SearchDocumentAsync(string query)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
       
        var sql = $"""
                SELECT 
                    d.id                       AS Id,
                    d.filename                 AS Filename,
                    {Single(_baseUrl, "d")}    AS ThumbnailUrl,
                    d.title                    AS Title,
                    d.page_count               AS PageCount
                FROM documents d
                WHERE d.title ILIKE @Query
                ORDER BY d.title
                
        """;
       return await conn.QueryAsync<DocumentDto>(sql, new {Query = $"%{query}%"});
    }

    public async Task<(string Path, string MimeType)?> GetStreamInfoAsync(Guid id)
    {
        await using var conn = await _dataSource.OpenConnectionAsync();
        
        const string sql = """
            SELECT path, mime_type AS MimeType 
            FROM documents 
            WHERE id = @Id
        """;

        var result = await conn.QueryFirstOrDefaultAsync<(string Path, string MimeType)>(
            sql, new { Id = id });

        return result.Path is null ? null : result;
    }
}