namespace SeharaCloud.Repositories.Interfaces;

using SeharaCloud.DTOs;

public interface IDocumentRepository
{
    Task<IEnumerable<DocumentDto>> GetAllAsync();
    Task<IEnumerable<DocumentDto>> GetRecentAsync(int limit);
    Task<IEnumerable<DocumentDto>> SearchDocumentAsync(string query);
    Task<(string Path, string MimeType)?> GetStreamInfoAsync(Guid id);

}