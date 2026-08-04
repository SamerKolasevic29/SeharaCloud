namespace HomeCloud.Repositories.Interfaces;

using HomeCloud.DTOs;
using HomeCloud.Enums;

public interface IDocumentRepository
{
    // -- Basic lists ------------------------
    Task<IEnumerable<DocumentDto>> GetAllAsync();
    
    // -- Via Category ------------------------
    Task<IEnumerable<DocumentDto>> GetByCategoryAsync(DocumentCategory category);

    // -- Search ------------------------
    Task<IEnumerable<DocumentDto>> SearchAsync(string query);
    Task<IEnumerable<DocumentDto>> SearchByCategoryAsync(string query, DocumentCategory category);
}