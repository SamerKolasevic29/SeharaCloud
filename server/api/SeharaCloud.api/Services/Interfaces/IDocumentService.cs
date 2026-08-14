namespace SeharaCloud.Services.Interfaces;

using SeharaCloud.DTOs;

public interface IDocumentService
{
    Task<IEnumerable<DocumentDto>> GetAllAsync();
    Task<IEnumerable<DocumentDto>> GetRecentAsync();
    Task<IEnumerable<DocumentDto>> SearchDocumentAsync(string query);
    Task<(string Path, string MimeType)> GetStreamInfoAsync(Guid id);
}