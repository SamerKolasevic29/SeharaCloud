namespace HomeCloud.Services.Interfaces;

using HomeCloud.DTOs;
using HomeCloud.Enums;

public interface IDocumentService
{
    Task<IEnumerable<DocumentDto>> GetAllAsync();
    Task<IEnumerable<DocumentDto>> GetBooksAsync();
    Task<IEnumerable<DocumentDto>> GetDocumentsAsync();
    Task<IEnumerable<DocumentDto>> GetOtherAsync();
    Task<IEnumerable<DocumentDto>> SearchAsync(string query);
    Task<IEnumerable<DocumentDto>> SearchByCategoryAsync(string query, DocumentCategory category);
}